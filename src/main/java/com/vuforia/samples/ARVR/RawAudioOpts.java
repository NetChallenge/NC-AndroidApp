package com.vuforia.samples.ARVR;

public class RawAudioOpts {
    private static int sampleRate = 16000;	// 44.1[KHz] is only setting guaranteed to be available on all devices. //for STT, 16000hz is needed
    private static int samplesPerFrame = 1024;	// AAC, bytes/frame/channel
    private static int framesPerBuffer = 25; 	// AAC, frame/buffer/sec

    public static int getSampleRate() {
        return sampleRate;
    }

    public static int getSamplesPerFrame() {
        return samplesPerFrame;
    }

    public static int getFramesPerBuffer() {
        return framesPerBuffer;
    }
}
