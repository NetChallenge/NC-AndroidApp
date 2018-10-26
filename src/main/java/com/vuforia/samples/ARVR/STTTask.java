package com.vuforia.samples.ARVR;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.unity3d.player.UnityPlayer;
import com.vuforia.samples.network.SocketClient;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class STTTask extends AsyncTask<Void, Void, STTManager.STT_Err>{
    private static String TAG = "STTTask";
    private Activity activity;
    private UnityPlayer unityPlayer;
    private STTManager sttManager;
    private STTServerOpts sttOpts;

    private SocketClient.SockListener sockListener = new SocketClient.SockListener() {
        @Override
        public void onConnect() {
            Log.d(TAG, "socket onConnect");
        }

        @Override
        public void onRecv(byte[] recv) {
            /*
            Log.d(TAG, "socket onRecv. data size is " + recv.length);
            try {
                String result = new String(recv, "UTF-8");
                Log.d(TAG, "socket onRecv. data size is " + recv.length + " test: " + result);
                unityPlayer.UnitySendMessage("ChattingManager", "CreateChat", result);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            */
        }

        @Override
        public void onStopped() {
            Log.d(TAG, "socket onStopped");
            /*
            if(sttManager.isStart()) {
                Toast.makeText(context, "socket server unexpectedly stopped. please restart", Toast.LENGTH_SHORT).show();
                sttManager.stop();
            }
            */
        }
    };

    private RawAudioStream.OnReadListener onReadListener = new RawAudioStream.OnReadListener() {
        @Override
        public void onRead(ByteBuffer data) {
            int len = data.remaining();
            //Log.d(TAG, "onRead. length is " + len);

            byte[] output = new byte[len];
            data.get(output, 0, len);
            sttManager.write(output);
        }
    };

    public STTTask(Activity activity, UnityPlayer unityPlayer, STTManager sttManager, STTServerOpts sttOpts) {
        this.activity = activity;
        this.unityPlayer = unityPlayer;
        this.sttManager = sttManager;
        this.sttOpts = sttOpts;
    }

    @Override
    protected STTManager.STT_Err doInBackground(Void... voids) {
        sttManager.setSockListener(sockListener);
        sttManager.setOnReadListener(onReadListener);
        return sttManager.initialize(sttOpts);
    }

    @Override
    public void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    public void onPostExecute(STTManager.STT_Err err) {
        super.onPostExecute(err);

        switch (err) {
            case ALREADY_INIT:
                break;
            case SOCK_INIT_ERR:
                Log.d(TAG, "socket init error");
                Toast.makeText(activity, "네트워크 연결에 실패하였습니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                activity.finish();
                break;
            case AUDIO_INIT_ERR:
                Log.d(TAG, "audio init error");
                Toast.makeText(activity, "오디오가 동작하지 않습니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                activity.finish();
                break;
            case AUTH_ERR:
                Log.d(TAG, "auth error");
                Toast.makeText(activity, "권한이 없습니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                activity.finish();
                break;
            case INIT_SUCCESS:
                sttManager.start();
                Log.d(TAG, "STT Start");
                //Toast.makeText(activity, "STT Start", Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
