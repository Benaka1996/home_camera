package com.home.camera.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.home.camera.R;
import com.home.camera.constants.AppConstant;
import com.home.camera.databinding.AdapterCameraFileLayoutBinding;
import com.home.camera.manager.InjectManager;
import com.home.camera.model.CameraFile;

import java.util.Objects;

public class CameraFileAdapter extends ListAdapter<CameraFile, CameraFileAdapter.CameraFileViewHolder> {
    private static final String TAG = "CameraFileAdapter";
    private static final DiffUtil.ItemCallback<CameraFile> DIFF_ITEM_CALLBACK = new DiffUtil.ItemCallback<CameraFile>() {
        @Override
        public boolean areItemsTheSame(@NonNull CameraFile oldItem, @NonNull CameraFile newItem) {
            return oldItem.getFileName().equalsIgnoreCase(newItem.getFileName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull CameraFile oldItem, @NonNull CameraFile newItem) {
            return Objects.equals(oldItem, newItem);
        }
    };
    private final LayoutInflater layoutInflater;
    private final Context context;

    public CameraFileAdapter(Context context) {
        super(DIFF_ITEM_CALLBACK);
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public CameraFileAdapter.CameraFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AdapterCameraFileLayoutBinding binding = AdapterCameraFileLayoutBinding.inflate(layoutInflater, parent, false);
        return new CameraFileViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CameraFileAdapter.CameraFileViewHolder holder, int position) {
        CameraFile cameraFile = getItem(position);
        holder.getFileNameText().setText(cameraFile.getFileName());
        int fileType = cameraFile.getFileType();
        if (fileType == AppConstant.TYPE_IMAGE) {
            holder.getFileIcon().setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_jpg));
        } else {
            holder.getFileIcon().setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_regular_file));
        }
        holder.getFileSize().setText(cameraFile.getSize());
    }

    public class CameraFileViewHolder extends RecyclerView.ViewHolder {

        private final ImageView fileIcon;
        private final TextView fileNameText;
        private final TextView fileSize;

        public CameraFileViewHolder(@NonNull AdapterCameraFileLayoutBinding binding) {
            super(binding.getRoot());
            fileNameText = itemView.findViewById(R.id.file_name);
            fileIcon = itemView.findViewById(R.id.image_icon);
            fileSize = itemView.findViewById(R.id.file_size);

            itemView.setOnClickListener(view -> {
                Log.d(TAG, "CameraFileViewHolder: ");
                CameraFile cameraFile = getItem(getAdapterPosition());
                InjectManager.getInstance().inject(AppConstant.INJECT_OPEN_IMAGE, cameraFile);
            });
        }

        public TextView getFileNameText() {
            return fileNameText;
        }

        public ImageView getFileIcon() {
            return fileIcon;
        }

        public TextView getFileSize() {
            return fileSize;
        }
    }
}
