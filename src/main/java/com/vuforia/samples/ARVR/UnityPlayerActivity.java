package com.vuforia.samples.ARVR;

import com.unity3d.player.*;
import com.vuforia.samples.encoder.AsyncThread;
import com.vuforia.samples.encoder.AvcEncoder;
import com.vuforia.samples.encoder.RawAudioOpts;
import com.vuforia.samples.encoder.RawAudioStream;
import com.vuforia.samples.model.Room;
import com.vuforia.samples.model.User;
import com.vuforia.samples.mqtt.MQTTManager;
import com.vuforia.samples.network.LocalSocketServer;
import com.vuforia.samples.network.NCARApiRequest;
import com.vuforia.samples.network.SocketClient;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class UnityPlayerActivity extends Activity
{
    private static String TAG = "UnityPlayerActivity";

    //send message type
    public enum SEND_MSG {
        VIDEO(0x00),AUDIO(0x01);

        private final byte value;
        SEND_MSG(int value) {
            this.value = (byte)value;
        }

        public byte getValue() {
            return value;
        }
    }

    protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code
    private Room room = User.getCurrentUser().getCurrentRoom();

    //for mqtt message
    private MQTTManager mqttManager = MQTTManager.getInstance();

    //for video
    private AvcEncoder avcEncoder;

    //for networking
    private SocketClient sockClient;

    //for audio
    private RawAudioStream rawAudioStream;

    //for unity communication
    private LocalSocketServer localSocketServer = new LocalSocketServer();

    //dialog
    private NCARProgressDialog dialog = new NCARProgressDialog(this);

    //options
    private boolean isAudioDisabled = false;
    private boolean isARDisabled = false;

    //voice latency check
    private long voiceStartTime = 0;
    private long voiceEndTime = 0;
    private long voiceTextArriveTime = 0;

    // Setup activity layout
    @Override protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        isAudioDisabled = getIntent().getBooleanExtra("audio", false);
        isARDisabled = getIntent().getBooleanExtra("ar", false);

        mUnityPlayer = new UnityPlayer(this);
        setContentView(mUnityPlayer);
        mUnityPlayer.requestFocus();
    }

    private void initialize() {
        //socket initialize
        new Thread(new Runnable() {
            @Override
            public void run() {
                int retry = 0;
                boolean isConnected = false;

                while(retry <= 10) {
                    sockClient = new SocketClient(
                            room.getSttOpts().getIp(),
                            room.getSttOpts().getPort(),
                            room.getSttOpts().getMaxReadSize(),
                            new SocketClient.SockListener() {
                                @Override
                                public void onConnect() {

                                }

                                @Override
                                public void onRecv(byte[] recv) {
                                    //we need to recv face rectangle
                                    try {
                                        String json = new String(recv, "UTF-8");
                                        //Log.d(TAG, "json : " + json);
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onStopped() {

                                }
                            });

                    if (!sockClient.connect()) {
                        retry++;
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        isConnected = true;
                        break;
                    }
                }

                if(!isConnected) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UnityPlayerActivity.this, R.string.ncar_network_err, Toast.LENGTH_LONG).show();
                        }
                    });
                    finish();
                    return;
                }

                if(!isARDisabled) {
                    avcEncoder = new AvcEncoder(new AvcEncoder.OnReadListener() {
                        @Override
                        public void onRead(byte[] data) {
                            //Log.d(TAG, "avcEncoder Read. size is " + data.length);
                            byte[] output = new byte[data.length + 1];
                            output[0] = SEND_MSG.VIDEO.getValue();
                            System.arraycopy(data, 0, output, 1, data.length);
                            sockClient.putData(output);
                        }
                    });

                    //video initialize
                    if (!avcEncoder.initialize()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(UnityPlayerActivity.this, R.string.ncar_video_init_err, Toast.LENGTH_LONG).show();
                            }
                        });
                        finish();
                        return;
                    }
                }

                //audio initialize
                if(!isAudioDisabled) {
                    rawAudioStream = new RawAudioStream(
                            RawAudioOpts.getSampleRate(),
                            RawAudioOpts.getSamplesPerFrame(),
                            RawAudioOpts.getFramesPerBuffer(),
                            new RawAudioStream.OnReadListener() {
                                @Override
                                public void onRead(byte[] data, boolean isSpeaking) {
                                    byte[] output = new byte[data.length + 2];
                                    output[0] = SEND_MSG.AUDIO.getValue();
                                    output[1] = (byte)(isSpeaking ? 1 : 0);
                                    //Log.d(TAG, "isSpeaking: " + output[1]);
                                    System.arraycopy(data, 0, output, 2, data.length);
                                    sockClient.putData(output);
                                }

                                @Override
                                public void onVoiceStart() {
                                    voiceStartTime = System.currentTimeMillis();
                                }

                                @Override
                                public void onVoiceEnd() {
                                    voiceEndTime = System.currentTimeMillis();
                                }
                            }
                    );
                    if (!rawAudioStream.initialize()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(UnityPlayerActivity.this, R.string.ncar_audio_init_err, Toast.LENGTH_LONG).show();
                            }
                        });
                        finish();
                        return;
                    }
                }

                //mqtt initialize
                MQTTManager.MQTT_Err mqtt_err = mqttManager.initialize(room.getMqttOpts());
                switch (mqtt_err) {
                    case INIT_FAIL:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(UnityPlayerActivity.this, R.string.ncar_mqtt_init_err, Toast.LENGTH_LONG).show();
                            }
                        });
                        finish();
                        return;

                    default:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(UnityPlayerActivity.this, R.string.ncar_unknown_err, Toast.LENGTH_LONG).show();
                            }
                        });
                        finish();
                        return;

                    case INIT_SUCCESS:
                        mqttManager.setMqttListener(new MQTTManager.MQTTListener() {
                            @Override
                            public void onConnectionLost() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(UnityPlayerActivity.this, R.string.ncar_network_err, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            @Override
                            public void onArrive(MqttMessage message) {
                                //we need to pass image
                                Log.d(TAG, "message arrived. " + message.toString());
                                voiceTextArriveTime = System.currentTimeMillis();

                                Log.d(TAG, "voice start to arrival:" + String.valueOf(voiceTextArriveTime - voiceStartTime));
                                Log.d(TAG, "voice end to arrival:" + String.valueOf(voiceTextArriveTime - voiceEndTime));
                                mUnityPlayer.UnitySendMessage("ChattingManager", "CreateChat", message.toString());

                                /*
                                if(voiceTextArriveTime - voiceStartTime <= 2000) {
                                    String[] nameAndContents = message.toString().split(":");
                                    if(nameAndContents[0] != User.getCurrentUser().getUserName())
                                        return;

                                    mUnityPlayer.UnitySendMessage("ChattingManager", "CreateChat", message.toString());
                                    Log.d(TAG, "voice start to arrival:" + String.valueOf(voiceTextArriveTime - voiceStartTime));
                                    Log.d(TAG, "voice end to arrival:" + String.valueOf(voiceTextArriveTime - voiceEndTime));
                                }
                                else {
                                    String[] nameAndContents = message.toString().split(":");
                                    if(nameAndContents[0] == User.getCurrentUser().getUserName())
                                        return;

                                    mUnityPlayer.UnitySendMessage("ChattingManager", "CreateChat", message.toString());
                                    Log.d(TAG, "voice start to arrival:" + String.valueOf(voiceTextArriveTime - voiceStartTime));
                                    Log.d(TAG, "voice end to arrival:" + String.valueOf(voiceTextArriveTime - voiceEndTime));
                                }
                                */
                            }
                        });
                        break;
                }

                //가로 x 세로 x YUV(1.5)를 곱하여야함
                localSocketServer.setBufferSize(1280 * 720 * 3 / 2);
                localSocketServer.setOnRecvListener(new LocalSocketServer.OnRecvListener() {
                    boolean isSaved = false;
                    @Override
                    public void onRecv(byte[] recv) {
                        if(!isARDisabled)
                            avcEncoder.putData(recv);

                        /*
                        if(isSaved == false) {
                            isSaved = true;
                            try {
                                ByteArrayOutputStream out = new ByteArrayOutputStream();
                                YuvImage yuvImage = new YuvImage(recv, ImageFormat.NV21, 1280, 720, null);
                                yuvImage.compressToJpeg(new Rect(0, 0, 1280, 720), 50, out);
                                File file = new File(Environment.getExternalStorageDirectory() + "/nv21test.jpeg");
                                FileOutputStream out2 = new FileOutputStream(file);
                                out2.write(out.toByteArray());
                                out2.close();
                                out.close();

                                out = new ByteArrayOutputStream();
                                YuvImage yuvImage2 = new YuvImage(recv, ImageFormat.YUY2, 1280, 720, null);
                                yuvImage2.compressToJpeg(new Rect(0, 0, 1280, 720), 50, out);
                                file = new File(Environment.getExternalStorageDirectory() + "/yuv2test.jpeg");
                                out2 = new FileOutputStream(file);
                                out2.write(out.toByteArray());
                                out2.close();
                                out.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        */
                    }
                });
                //start all
                localSocketServer.start();
                sockClient.start();
                if(!isARDisabled)
                    avcEncoder.start();
                if(!isAudioDisabled)
                    rawAudioStream.start();
            }
        }).start();
    }

    private void release() {
        localSocketServer.setStopThread(true);
        sockClient.close();
        if(!isARDisabled)
            avcEncoder.close();
        if(!isAudioDisabled)
            rawAudioStream.stopStream();
    }

    @Override protected void onNewIntent(Intent intent)
    {
        // To support deep linking, we need to make sure that the client can get access to
        // the last sent intent. The clients access this through a JNI api that allows them
        // to get the intent set on launch. To update that after launch we have to manually
        // replace the intent with the one caught here.
        setIntent(intent);
    }

    // Quit Unity
    @Override protected void onDestroy ()
    {
        mUnityPlayer.quit();
        super.onDestroy();
        //release();
        //int id = android.os.Process.myPid();
        //android.os.Process.killProcess(id);
    }

    // Pause Unity
    @Override protected void onPause()
    {
        super.onPause();
        mUnityPlayer.pause();
    }

    // Resume Unity
    @Override protected void onResume()
    {
        super.onResume();
        mUnityPlayer.resume();
    }

    @Override protected void onStart()
    {
        super.onStart();
        mUnityPlayer.start();
        initialize();
    }

    @Override protected void onStop()
    {
        super.onStop();
        mUnityPlayer.stop();
    }

    // Low Memory Unity
    @Override public void onLowMemory()
    {
        super.onLowMemory();
        mUnityPlayer.lowMemory();
    }

    // Trim Memory Unity
    @Override public void onTrimMemory(int level)
    {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_RUNNING_CRITICAL)
        {
            mUnityPlayer.lowMemory();
        }
    }

    // This ensures the layout will be correct.
    @Override public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mUnityPlayer.configurationChanged(newConfig);
    }

    // Notify Unity of the focus change.
    @Override public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        mUnityPlayer.windowFocusChanged(hasFocus);
    }

    // For some reason the multiple keyevent type is not supported by the ndk.
    // Force event injection by overriding dispatchKeyEvent().
    @Override public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
            return mUnityPlayer.injectEvent(event);
        return super.dispatchKeyEvent(event);
    }

    // Pass any events not handled by (unfocused) views straight to UnityPlayer
    @Override public boolean onKeyUp(int keyCode, KeyEvent event)     {
        //System.out.println("onKeyUp!!" + keyCode);
        if(keyCode == 4) {
            dialog.showProgressDialog();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NCARApiRequest.NCARApi_Err err = NCARApiRequest.leaveRoom(User.getCurrentUser().getUserEmail(), room.getRoomId());
                    switch (err) {
                        case SUCCESS:
                            release();
                            finish();
                            break;
                        case NETWORK_ERR:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(UnityPlayerActivity.this, R.string.ncar_network_err, Toast.LENGTH_LONG).show();
                                }
                            });
                            break;
                        case UNKNOWN_ERR:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(UnityPlayerActivity.this, R.string.ncar_unknown_err, Toast.LENGTH_LONG).show();
                                }
                            });
                            break;
                    }
                    dialog.hideProgressDialog();
                }
            }).start();
        }
        return mUnityPlayer.injectEvent(event);
    }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event)   { return mUnityPlayer.injectEvent(event); }
    @Override public boolean onTouchEvent(MotionEvent event)          { return mUnityPlayer.injectEvent(event); }
    /*API12*/ public boolean onGenericMotionEvent(MotionEvent event)  { return mUnityPlayer.injectEvent(event); }
}
