package com.home.camera.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.home.camera.constants.AppConstant;
import com.home.camera.model.SocketData;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

public class WebSocketViewModel extends AndroidViewModel {
    private static final String TAG = "WebSocketViewModel";

    private final MutableLiveData<SocketData> socketDataMutableLiveData = new MutableLiveData<>();

    private WebSocketClient webSocketClient;

    public WebSocketViewModel(@NonNull Application application) {
        super(application);
        init();
    }

    public LiveData<SocketData> getSocketLiveData() {
        return socketDataMutableLiveData;
    }

    private void init() {
        try {
            URI uri = new URI("ws://192.168.0.102:81");
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakeData) {
                    Log.d(TAG, "onOpen: ");
                    socketDataMutableLiveData.postValue(new SocketData(AppConstant.SOCKET_STATE, true));
                }

                @Override
                public void onMessage(String message) {
                    socketDataMutableLiveData.postValue(new SocketData(AppConstant.SUCCESS, message));
                }

                @Override
                public void onMessage(ByteBuffer bytes) {
                    socketDataMutableLiveData.postValue(new SocketData(AppConstant.SUCCESS, bytes));
                    super.onMessage(bytes);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "onClose: " + reason);
                    socketDataMutableLiveData.postValue(new SocketData(AppConstant.SOCKET_STATE, false));
                }

                @Override
                public void onError(Exception ex) {
                    socketDataMutableLiveData.postValue(new SocketData(AppConstant.ERROR, null));
                    Log.d(TAG, "onError: " + ex);
                }
            };
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            socketDataMutableLiveData.postValue(new SocketData(AppConstant.ERROR, null));
            e.printStackTrace();
        }
    }

    public void updateConnectionState() {
        socketDataMutableLiveData.setValue(new SocketData(AppConstant.SOCKET_STATE, webSocketClient.isOpen()));
    }

    public void sendState(String state) {
        webSocketClient.send(state);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        webSocketClient.close();
    }
}
