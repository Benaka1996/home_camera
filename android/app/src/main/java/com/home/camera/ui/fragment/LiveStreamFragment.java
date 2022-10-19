package com.home.camera.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.home.camera.constants.AppConstant;
import com.home.camera.databinding.FragmentLiveStreamBinding;
import com.home.camera.model.SocketData;
import com.home.camera.viewmodel.WebSocketViewModel;

import java.nio.ByteBuffer;

public class LiveStreamFragment extends Fragment {
    static final String TAG = "LiveStreamFragment";

    private FragmentLiveStreamBinding binding;
    private WebSocketViewModel webSocketViewModel;

    private final Observer<SocketData> webSocketObserver = socketData -> {
        if (webSocketViewModel.isSocketConnected()) {
            int status = socketData.getStatus();
            if (status == AppConstant.SUCCESS) {
                binding.progressBar.setVisibility(View.GONE);
                binding.connectedIcons.setVisibility(View.VISIBLE);
                if (socketData.getData() instanceof ByteBuffer) {
                    ByteBuffer byteBuffer = (ByteBuffer) socketData.getData();
                    byte[] imageArray = byteBuffer.array();
                    binding.cameraView.renderFrame(imageArray);
                }
            } else {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.connectedIcons.setVisibility(View.GONE);
            }
        }
    };

    private final Observer<Boolean> connectionStateObserver = isConnected -> {
        if (isConnected) {
            webSocketViewModel.sentText(AppConstant.LIVE_STREAM);
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        webSocketViewModel = new ViewModelProvider(requireParentFragment()).get(WebSocketViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLiveStreamBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webSocketViewModel.getSocketLiveData().observe(getViewLifecycleOwner(), webSocketObserver);
        webSocketViewModel.getConnectionStatusLiveData().observe(getViewLifecycleOwner(), connectionStateObserver);

        binding.cameraRound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webSocketViewModel.sentText(AppConstant.CAPTURE_IMAGE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        webSocketViewModel.getSocketLiveData().removeObserver(webSocketObserver);
        webSocketViewModel.getConnectionStatusLiveData().removeObserver(connectionStateObserver);
    }
}