package com.vuforia.samples.ARVR;

import android.util.Log;

import com.vuforia.samples.network.SocketClient;

import java.io.IOException;
import java.nio.ByteBuffer;

public class STTManager {
    private static String TAG = "STTManager";

    public enum STT_Err {
        INIT_SUCCESS,
        SOCK_INIT_ERR,
        AUDIO_INIT_ERR,
        ALREADY_INIT,
        START_SUCCESS,
        ALREADY_START,
        STOP_SUCCESS,
        ALREADY_STOP,
    }

    //STTManager는 오직 한개만 존재하여야 한다.
    private static STTManager sttManager = null;
    private boolean isInit = false;
    private boolean isStart = false;

    //for networking
    private SocketClient sockClient;
    private SocketClient.SockListener sockListener = null;

    //for audio
    private RawAudioStream rawAudioStream;
    private RawAudioStream.OnReadListener onReadListener = null;

    private STTManager() {}

    public static synchronized STTManager getInstance() {
        if(sttManager == null)
            sttManager = new STTManager();

        return sttManager;
    }

    public void setSockListener(SocketClient.SockListener sockListener) {
        this.sockListener = sockListener;
    }

    public void setOnReadListener(RawAudioStream.OnReadListener onReadListener) {
        this.onReadListener = onReadListener;
    }

    public STT_Err initialize() {
        if(isInit)
            return STT_Err.ALREADY_INIT;

        //initialize socket
        sockClient = new SocketClient(
                STTServerOpts.getIp(),
                STTServerOpts.getPort(),
                STTServerOpts.getMaxReadSize(),
                sockListener);

        if(!sockClient.connect())
            return STT_Err.SOCK_INIT_ERR;

        //initialize audio
        rawAudioStream = new RawAudioStream(
                RawAudioOpts.getSampleRate(),
                RawAudioOpts.getSamplesPerFrame(),
                RawAudioOpts.getFramesPerBuffer(),
                onReadListener
        );
        if(!rawAudioStream.initialize())
            return STT_Err.AUDIO_INIT_ERR;

        isInit = true;
        return STT_Err.INIT_SUCCESS;
    }

    public void write(byte[] buf) {
        if(isStart)
            sockClient.write(buf);
    }

    public synchronized boolean isStart() {
        return isStart;
    }

    public synchronized STT_Err start() {
        if(isStart)
            return STT_Err.ALREADY_START;
        isStart = true;

        //socket이 무조껀 먼저 시작되어야 함.
        sockClient.start();
        rawAudioStream.start();

        return STT_Err.START_SUCCESS;
    }

    public synchronized STT_Err stop() {
        if(!isStart)
            return STT_Err.ALREADY_STOP;

        sockClient.close();
        rawAudioStream.stopStream();

        isStart = false;
        return STT_Err.STOP_SUCCESS;
    }
}
