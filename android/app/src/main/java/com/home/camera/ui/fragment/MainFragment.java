package com.home.camera.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.home.camera.R;
import com.home.camera.viewmodel.WebSocketViewModel;
import com.home.camera.databinding.FragmentMainBinding;

public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";
    private FragmentMainBinding binding;
    private int selectedId = R.id.live_stream_id;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        new ViewModelProvider(this).get(WebSocketViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setFragment(new LiveStreamFragment(), LiveStreamFragment.TAG);
        binding.bottomActionRecycler.setOnItemSelectedListener(item -> {
            if (selectedId == item.getItemId()) {
                return false;
            }
            if (item.getItemId() == R.id.live_stream_id) {
                setFragment(new LiveStreamFragment(), LiveStreamFragment.TAG);
            } else if (item.getItemId() == R.id.sd_storage_id) {
                setFragment(new StorageFragment(), StorageFragment.TAG);
            } else if (item.getItemId() == R.id.settings_id) {
                setFragment(new SettingsFragment(), SettingsFragment.TAG);
            }
            selectedId = item.getItemId();
            return true;
        });

    }

    public void setFragment(Fragment fragment, String tag) {
        getChildFragmentManager().beginTransaction().replace(R.id.child_fragment_container, fragment, tag).commit();
    }
}