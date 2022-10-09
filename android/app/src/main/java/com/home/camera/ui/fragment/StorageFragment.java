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

import com.home.camera.R;
import com.home.camera.constants.AppConstant;
import com.home.camera.model.SocketData;
import com.home.camera.viewmodel.WebSocketViewModel;

public class StorageFragment extends Fragment {
    static final String TAG = "StorageFragment";
    private WebSocketViewModel webSocketViewModel;

    private final Observer<SocketData> webSocketObserver = socketData -> {
        int status = socketData.getStatus();
        if (status == AppConstant.SOCKET_STATE) {
            boolean state = (boolean) socketData.getData();
            if (state)
                webSocketViewModel.sendState(AppConstant.SD_STORAGE);
        } else if (status == AppConstant.SUCCESS) {

        } else {

        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        webSocketViewModel = new ViewModelProvider(requireParentFragment()).get(WebSocketViewModel.class);
        webSocketViewModel.updateConnectionState();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_storage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webSocketViewModel.updateConnectionState();
        webSocketViewModel.getSocketLiveData().observe(getViewLifecycleOwner(), webSocketObserver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        webSocketViewModel.getSocketLiveData().removeObserver(webSocketObserver);
    }
}