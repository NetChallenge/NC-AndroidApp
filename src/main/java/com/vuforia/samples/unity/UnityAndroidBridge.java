package com.vuforia.samples.unity;

public class UnityAndroidBridge {
    public interface OnEventListener {
        void onByteArrayRecved(byte[] data);
    }

    //for android
    private OnEventListener onEventListener = null;
    public void setEventListener(OnEventListener onEventListener) {
        this.onEventListener = onEventListener;
    }

    //for unity
    public void sendByteArray(byte[] data) {
        if(onEventListener != null)
            onEventListener.onByteArrayRecved(data);
    }
}