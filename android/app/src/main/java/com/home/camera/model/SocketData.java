package com.home.camera.model;

public class SocketData {
    private final int status;
    private final Object data;

    public SocketData(int status, Object data) {
        this.status = status;
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return "SocketData{" +
                "status=" + status +
                ", data=" + data +
                '}';
    }
}
