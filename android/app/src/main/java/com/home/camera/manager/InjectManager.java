package com.home.camera.manager;

import java.util.HashMap;

public final class InjectManager {

    private static InjectManager injectManager;

    private final HashMap<Integer, AppEventListener> hashMap = new HashMap<>();

    public static InjectManager getInstance() {
        if (injectManager == null) {
            injectManager = new InjectManager();
        }
        return injectManager;
    }

    public void addListener(int type, AppEventListener appEventListener) {
        hashMap.put(type, appEventListener);
    }

    public void removeListener(int type) {
        hashMap.remove(type);
    }

    public void inject(int type, Object data) {
        AppEventListener appEventListener = hashMap.get(type);
        if (appEventListener != null) {
            appEventListener.inject(data, type);
        }
    }


}
