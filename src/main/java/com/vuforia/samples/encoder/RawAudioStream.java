package com.vuforia.samples.encoder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class RawAudioStream extends Thread {
    public interface OnReadListener {
        void onRead(byte[] data, boolean isSpeaking);
        void onVoiceStart();
        void onVoiceEnd();
    }

    private int sampleRate;	// 44.1[KHz] is only setting guaranteed to be available on all devices. //for STT, 16000hz is needed
    private int samplesPerFrame;	// AAC, bytes/frame/channel
    private int framesPerBuffer; 	// AAC, frame/buffer/sec
    private AudioRecord audioRecord;
    private boolean isStopped = false;
    private OnReadListener listener;
    private File file = null;
    private OutputStream fileOut = null;
    private int maxAmplitude = 0;
    private boolean isVoiceStart = false;

    private static final int[] AUDIO_SOURCES = new int[] {
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.DEFAULT,
            MediaRecorder.AudioSource.CAMCORDER,
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
    };

    public RawAudioStream(int sampleRate, int samplesPerFrame, int framesPerBuffer, OnReadListener listener) {
       this.sampleRate = sampleRate;
       this.samplesPerFrame = samplesPerFrame;
       this.framesPerBuffer = framesPerBuffer;
       this.listener = listener;
    }

    public boolean setOutputFile(String absolutePath) {
        file = new File(absolutePath);
        try {
            fileOut = new FileOutputStream(file);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getMaxAmplitude() {
        return maxAmplitude;
    }

    public boolean initialize() {
        final int min_buffer_size = AudioRecord.getMinBufferSize(
                sampleRate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        int buffer_size = samplesPerFrame * framesPerBuffer;
        if (buffer_size < min_buffer_size)
            buffer_size = ((min_buffer_size / samplesPerFrame) + 1) * samplesPerFrame * 2;

        audioRecord = null;
        for (final int source : AUDIO_SOURCES) {
            try {
                audioRecord = new AudioRecord(
                        source, sampleRate,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer_size);
                if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED)
                    audioRecord = null;
            } catch (final Exception e) {
                audioRecord = null;
            }
            if (audioRecord != null) break;
        }

        if(audioRecord == null)
            return false;

        return true;
    }

    @Override
    public void run() {
        audioRecord.startRecording();
        ByteBuffer buf = ByteBuffer.allocateDirect(samplesPerFrame);
        while(!isStopped) {
            buf.clear();
            int readBytes = audioRecord.read(buf, samplesPerFrame);

            if (readBytes > 0) {
                // set audio data to encoder
                buf.position(readBytes);
                buf.flip();

                int bufSize = buf.remaining();
                byte[] output = new byte[bufSize];
                buf.get(output, 0, bufSize);

                float a1=0, a2=0, a3=0, a4=0, a5=0, a6=0, a7=0;
                for(int i=0; i<bufSize/2; i++) {
                    int data = (((output[i*2+1] & 0xFF) << 8) | (output[i*2] & 0xFF));
                    if(data < 10000)
                        a1 += 1;
                    else if(data < 20000)
                        a2 += 1;
                    else if(data < 30000)
                        a3 += 1;
                    else if(data < 40000)
                        a4 += 1;
                    else if(data < 50000)
                        a5 += 1;
                    else if(data < 60000)
                        a6 += 1;
                    else if(data < 70000)
                        a7 += 1;
                }
                a1 /= bufSize/2;
                a2 /= bufSize/2;
                a3 /= bufSize/2;
                a4 /= bufSize/2;
                a5 /= bufSize/2;
                a6 /= bufSize/2;
                a7 /= bufSize/2;
                Log.d("raw", a1 + " " + a2 + " " + a3 + " " + a4 + " " + a5 + " " + a6 + " " + a7);

                if(a1 + a7 < 0.95) {
                    if(!isVoiceStart && listener != null) {
                        isVoiceStart = true;
                        listener.onVoiceStart();
                    }
                }
                else {
                    if(isVoiceStart && listener != null) {
                        isVoiceStart = false;
                        listener.onVoiceEnd();
                    }
                }

                if(fileOut != null) {
                    try {
                        fileOut.write(output);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                int ab = (output[0] & 0xff) << 8 | output[1];
                maxAmplitude = Math.abs(ab);

                listener.onRead(output, isVoiceStart);
            }
        }

        if(fileOut != null) {
            try {
                fileOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        audioRecord.stop();
    }

    public void stopStream() {
        isStopped = true;
    }
}
