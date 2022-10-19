package com.home.camera.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.home.camera.adapter.CameraFileAdapter;
import com.home.camera.constants.AppConstant;
import com.home.camera.databinding.FragmentStorageBinding;
import com.home.camera.manager.AppEventListener;
import com.home.camera.manager.InjectManager;
import com.home.camera.model.CameraFile;
import com.home.camera.model.SocketData;
import com.home.camera.viewmodel.WebSocketViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StorageFragment extends Fragment implements AppEventListener {
    static final String TAG = "StorageFragment";
    private WebSocketViewModel webSocketViewModel;
    private FragmentStorageBinding binding;
    private final List<CameraFile> cameraFileList = new ArrayList<>();
    private String actionState = "0";

    private final Observer<SocketData> webSocketObserver = socketData -> {
        int status = socketData.getStatus();
        if (status == AppConstant.SUCCESS) {
            Object data = socketData.getData();
            if (data instanceof String) {
                String textMessage = (String) data;
                if (textMessage.equals(AppConstant.OPEN_IMAGE)) {
                    actionState = textMessage;
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.storageUi.setVisibility(View.VISIBLE);
                    String storageInfo = (String) data;
                    filterStorageInfo(storageInfo);
                }
            } else if (data instanceof ByteBuffer) {
                if (actionState.equals(AppConstant.OPEN_IMAGE)) {
                    ByteBuffer byteBuffer = (ByteBuffer) data;
                    byte[] array = byteBuffer.array();
                    binding.imageGroup.setVisibility(View.VISIBLE);
                    binding.cameraView.renderFrame(array);
                }
            }
        } else {

        }
    };

    private final Observer<Boolean> connectionStateObserver = (Observer<Boolean>) isConnected -> {
        if (isConnected) {
            webSocketViewModel.sentText(AppConstant.SD_STORAGE);
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
        // Inflate the layout for this fragment
        binding = FragmentStorageBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webSocketViewModel.getSocketLiveData().observe(getViewLifecycleOwner(), webSocketObserver);
        webSocketViewModel.getConnectionStatusLiveData().observe(getViewLifecycleOwner(), connectionStateObserver);
        binding.closeIcon.setOnClickListener(view1 -> {
            if (binding.imageGroup.getVisibility() == View.VISIBLE) {
                binding.imageGroup.setVisibility(View.GONE);
            }
        });
        InjectManager.getInstance().addListener(AppConstant.INJECT_OPEN_IMAGE, this);
    }

    private void filterStorageInfo(String storageInfo) {
        String[] splitInfo = storageInfo.split("\\|");
        cameraFileList.clear();
        for (String info : splitInfo) {
            try {
                if (info.contains("file")) {
                    JSONObject jsonObject = new JSONObject(info);
                    String file = jsonObject.getString("file");
                    String size = jsonObject.getString("size");
                    String[] filePathSplit = file.split("/");
                    String fileName = filePathSplit[filePathSplit.length - 1];
                    int fileType = AppConstant.TYPE_NORMAL;
                    if (fileName.contains(".jpg")) {
                        fileType = AppConstant.TYPE_IMAGE;
                    }
                    CameraFile cameraFile = new CameraFile(file, fileName.split("\\.")[0], getMemoryForUi(size), fileType);
                    cameraFileList.add(cameraFile);
                } else if (info.contains("space")) {
                    JSONObject jsonObject = new JSONObject(info);
                    String totalSpace = jsonObject.getString("total_space");
                    String usedSpace = jsonObject.getString("used_space");
                    DecimalFormat decimalFormat = new DecimalFormat("#.#");

                    double totalSpaceInBytes = Double.parseDouble(totalSpace);
                    double usedSpaceInBytes = Double.parseDouble(usedSpace);
                    double storagePercentage = (usedSpaceInBytes / totalSpaceInBytes) * 100;
                    binding.storageProgressBar.setProgress((int) storagePercentage);
                    double totalSpaceInGB = Double.parseDouble(decimalFormat.format(totalSpaceInBytes / (1024 * 1024 * 1024)));
                    binding.usedSpace.setText(getMemoryForUi(usedSpace));
                    binding.totalSpace.setText(String.format(Locale.getDefault(), "Free of %.1f GB", totalSpaceInGB));
                }
                setFileListAdapter();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setFileListAdapter() {
        CameraFileAdapter cameraFileAdapter = new CameraFileAdapter(requireContext());
        cameraFileAdapter.submitList(cameraFileList);
        binding.fileRecyclerView.setAdapter(cameraFileAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        webSocketViewModel.getSocketLiveData().removeObserver(webSocketObserver);
        webSocketViewModel.getConnectionStatusLiveData().removeObserver(connectionStateObserver);
        InjectManager.getInstance().removeListener(AppConstant.INJECT_OPEN_IMAGE);
    }

    private String getMemoryForUi(String bytes) {
        double memoryInBytes = Double.parseDouble(bytes);
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        double memoryInGb = Double.parseDouble(decimalFormat.format(memoryInBytes / (1024 * 1024 * 1024)));
        if (memoryInGb == 0.0f) {
            double usedSpaceInMb = Double.parseDouble(decimalFormat.format(memoryInBytes / (1024 * 1024)));
            if (usedSpaceInMb == 0.0f) {
                double usedSpaceInKb = Double.parseDouble(decimalFormat.format(memoryInBytes / (1024)));
                return String.format(Locale.getDefault(), "%.1f KB", usedSpaceInKb);
            } else {
                return String.format(Locale.getDefault(), "%.1f MB", usedSpaceInMb);
            }
        }
        return String.format(Locale.getDefault(), "%.1f GB", memoryInGb);
    }

    @Override
    public void inject(Object object, int type) {
        Log.d(TAG, "inject: ");
        if (object instanceof CameraFile) {
            CameraFile cameraFile = (CameraFile) object;
            String filePath = cameraFile.getPath();
            if (filePath.contains(".jpg")) {
                String message = AppConstant.OPEN_IMAGE + "," + filePath;
                webSocketViewModel.sentText(message);
            }
        }
    }
}