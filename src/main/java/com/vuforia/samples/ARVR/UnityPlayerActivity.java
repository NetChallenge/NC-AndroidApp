package com.vuforia.samples.ARVR;

import com.unity3d.player.*;
import com.vuforia.samples.encoder.AvcEncoder;
import com.vuforia.samples.encoder.RawAudioOpts;
import com.vuforia.samples.encoder.RawAudioStream;
import com.vuforia.samples.model.Room;
import com.vuforia.samples.model.User;
import com.vuforia.samples.mqtt.MQTTManager;
import com.vuforia.samples.network.SocketClient;
import com.vuforia.samples.unity.UnityAndroidBridge;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.ByteBuffer;

public class UnityPlayerActivity extends Activity
{
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
    private UnityAndroidBridge unityAndroidBridge = new UnityAndroidBridge();
    private Room room = User.getCurrentUser().getCurrentRoom();

    //for mqtt message
    private MQTTManager mqttManager = MQTTManager.getInstance();

    //for video
    private AvcEncoder avcEncoder;

    //for networking
    private SocketClient sockClient;

    //for audio
    private RawAudioStream rawAudioStream;

    // Setup activity layout
    @Override protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        mUnityPlayer = new UnityPlayer(this);
        setContentView(mUnityPlayer);
        mUnityPlayer.requestFocus();
    }

    private void initialize() {
        //socket initialize
        new Thread(new Runnable() {
            @Override
            public void run() {
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

                            }

                            @Override
                            public void onStopped() {

                            }
                        });

                if(!sockClient.connect()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UnityPlayerActivity.this, R.string.ncar_network_err, Toast.LENGTH_LONG).show();
                        }
                    });
                    finish();
                    return;
                }

                avcEncoder = new AvcEncoder(new AvcEncoder.OnReadListener() {
                    @Override
                    public void onRead(byte[] data) {
                        byte[] output = new byte[data.length + 1];
                        output[0] = SEND_MSG.VIDEO.getValue();
                        System.arraycopy(data, 0, output, 1, data.length);
                        sockClient.write(output);
                    }
                });

                //video initialize
                if(!avcEncoder.initialize()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UnityPlayerActivity.this, R.string.ncar_video_init_err, Toast.LENGTH_LONG).show();
                        }
                    });
                    finish();
                    return;
                }

                //audio initialize
                rawAudioStream = new RawAudioStream(
                        RawAudioOpts.getSampleRate(),
                        RawAudioOpts.getSamplesPerFrame(),
                        RawAudioOpts.getFramesPerBuffer(),
                        new RawAudioStream.OnReadListener() {
                            @Override
                            public void onRead(ByteBuffer data) {
                                int len = data.remaining();
                                byte[] output = new byte[len+1];
                                output[0] = SEND_MSG.AUDIO.getValue();
                                data.get(output, 1, len);
                                sockClient.write(output);
                            }
                        }
                );
                if(!rawAudioStream.initialize()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UnityPlayerActivity.this, R.string.ncar_audio_init_err, Toast.LENGTH_LONG).show();
                        }
                    });
                    finish();
                    return;
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
                                mUnityPlayer.UnitySendMessage("ChattingManager", "CreateChat", message.toString());
                            }
                        });
                        break;
                }

                //start sttmanager using asynctask
                unityAndroidBridge.setEventListener(new UnityAndroidBridge.OnEventListener() {
                    @Override
                    public void onByteArrayRecved(byte[] data) {
                        avcEncoder.putData(data);
                    }
                });

                //start all
                sockClient.start();
                avcEncoder.start();
                rawAudioStream.start();
                mUnityPlayer.start();
            }
        }).start();
    }

    private void release() {
        sockClient.close();
        avcEncoder.close();
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
        release();
        int id = android.os.Process.myPid();
        android.os.Process.killProcess(id);
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
        System.out.println("onKeyUp!!" + keyCode);
        if(keyCode == 4)
            finish();
        return mUnityPlayer.injectEvent(event);
    }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event)   { return mUnityPlayer.injectEvent(event); }
    @Override public boolean onTouchEvent(MotionEvent event)          { return mUnityPlayer.injectEvent(event); }
    /*API12*/ public boolean onGenericMotionEvent(MotionEvent event)  { return mUnityPlayer.injectEvent(event); }
}
