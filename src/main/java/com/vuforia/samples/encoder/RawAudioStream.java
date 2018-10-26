package com.vuforia.samples.encoder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.nio.ByteBuffer;

public class RawAudioStream extends Thread {
    public interface OnReadListener {
        void onRead(ByteBuffer data);
    }

    private int sampleRate;	// 44.1[KHz] is only setting guaranteed to be available on all devices. //for STT, 16000hz is needed
    private int samplesPerFrame;	// AAC, bytes/frame/channel
    private int framesPerBuffer; 	// AAC, frame/buffer/sec
    private AudioRecord audioRecord;
    private boolean isStopped = false;
    private OnReadListener listener;

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
                listener.onRead(buf);
            }
        }
    }

    public void stopStream() {
        isStopped = true;
        audioRecord.stop();
    }
}
