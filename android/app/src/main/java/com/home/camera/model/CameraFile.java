package com.home.camera.model;

public class CameraFile {
    private final String path;
    private final String fileName;
    private final String size;
    private final int fileType;

    public CameraFile(String path, String fileName, String size, int fileType) {
        this.path = path;
        this.fileName = fileName;
        this.size = size;
        this.fileType = fileType;
    }

    public String getPath() {
        return path;
    }

    public String getFileName() {
        return fileName;
    }

    public String getSize() {
        return size;
    }

    public int getFileType() {
        return fileType;
    }
}
