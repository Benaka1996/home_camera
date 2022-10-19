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
    private final MutableLiveData<Boolean> connectionStatusLiveData = new MutableLiveData<>();

    private WebSocketClient webSocketClient;

    public WebSocketViewModel(@NonNull Application application) {
        super(application);
        connect();
    }

    public LiveData<SocketData> getSocketLiveData() {
        return socketDataMutableLiveData;
    }

    public LiveData<Boolean> getConnectionStatusLiveData() {
        return connectionStatusLiveData;
    }

    public void connect() {
        try {
            URI uri = new URI("ws://192.168.0.180:81");
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakeData) {
                    Log.d(TAG, "onOpen: ");
                    connectionStatusLiveData.postValue(true);
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "onMessage: " + message);
                    socketDataMutableLiveData.postValue(new SocketData(AppConstant.SUCCESS, message));
                }

                @Override
                public void onMessage(ByteBuffer bytes) {
                    Log.d(TAG, "onMessage: bytes");
                    socketDataMutableLiveData.postValue(new SocketData(AppConstant.SUCCESS, bytes));
                    super.onMessage(bytes);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "onClose: " + reason);
                    connectionStatusLiveData.postValue(false);
                }

                @Override
                public void onError(Exception ex) {
                    connectionStatusLiveData.postValue(false);
                    Log.d(TAG, "onError: " + ex);
                }
            };
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            socketDataMutableLiveData.postValue(new SocketData(AppConstant.ERROR, null));
            e.printStackTrace();
        }
    }

    public void sentText(String state) {
        webSocketClient.send(state);
    }

    public boolean isSocketConnected() {
        return webSocketClient.isOpen();
    }

    public void disconnect() {
        webSocketClient.close();
    }

    public void disconnectWithBlock() throws InterruptedException {
        webSocketClient.closeBlocking();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disconnect();
    }
}
