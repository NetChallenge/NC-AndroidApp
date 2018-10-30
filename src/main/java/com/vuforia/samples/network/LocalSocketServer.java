package com.vuforia.samples.network;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class LocalSocketServer extends Thread{
    public interface OnRecvListener {
        void onRecv(byte[] recv);
    }
    public static String SOCKET_ADDRESS = "com.android.test.abstract";
    public static String TAG = "LocalSocketServer";
    int bufferSize;
    byte[] buffer;
    int bytesRead;
    int totalBytesRead;
    int posOffset;
    LocalServerSocket server;
    LocalSocket receiver;
    InputStream input;
    private OnRecvListener onRecvListener;
    private volatile boolean stopThread;

    private boolean init() {
        Log.d(TAG, " +++ Begin of localSocketServer() +++ ");
        buffer = new byte[bufferSize];
        bytesRead = 0;
        totalBytesRead = 0;
        posOffset = 0;

        try {
            server = new LocalServerSocket(SOCKET_ADDRESS);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "The localSocketServer created failed !!!");
            e.printStackTrace();
            return false;
        }

        LocalSocketAddress localSocketAddress;
        localSocketAddress = server.getLocalSocketAddress();
        String name = localSocketAddress.getName();
        LocalSocketAddress.Namespace namespace = localSocketAddress.getNamespace();

        Log.d(TAG, "The LocalSocketAddress = " + name + " " + namespace.toString());

        stopThread = false;
        return true;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setOnRecvListener(OnRecvListener onRecvListener) {
        this.onRecvListener = onRecvListener;
    }

    @Override
    public void run() {
        if(!init())
            return;

        Log.d(TAG, " +++ Begin of run() +++ ");
        while (!stopThread) {

            if (null == server){
                Log.d(TAG, "The localSocketServer is NULL !!!");
                stopThread = true;
                break;
            }

            try {
                Log.d(TAG, "localSocketServer begins to accept()");
                receiver = server.accept();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, "localSocketServer accept() failed !!!");
                e.printStackTrace();
                continue;
            }

            try {
                input = receiver.getInputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, "getInputStream() failed !!!");
                e.printStackTrace();
                continue;
            }

            Log.d(TAG, "The client connect to LocalServerSocket");

            while (receiver != null) {

                try {
                    bytesRead = input.read(buffer, posOffset,
                            (bufferSize - totalBytesRead));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.d(TAG, "There is an exception when reading socket");
                    e.printStackTrace();
                    break;
                }

                if (bytesRead >= 0) {
                    Log.d(TAG, "Receive data from socket, bytesRead = "
                            + bytesRead);
                    posOffset += bytesRead;
                    totalBytesRead += bytesRead;
                }

                if (totalBytesRead == bufferSize) {
                    /*
                    Log.d(TAG, "The buffer is full !!!");
                    String str = new String(buffer);
                    Log.d(TAG, "The context of buffer is : " + str);
                    */
                    bytesRead = 0;
                    totalBytesRead = 0;
                    posOffset = 0;

                    if(onRecvListener != null) {
                        byte[] copy = new byte[bufferSize];
                        System.arraycopy(buffer, 0, copy, 0, bufferSize);
                        onRecvListener.onRecv(copy);
                    }
                }

            }
            Log.d(TAG, "The client socket is NULL !!!");
        }
        Log.d(TAG, "The LocalSocketServer thread is going to stop !!!");
        if (receiver != null){
            try {
                receiver.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (server != null){
            try {
                server.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void setStopThread(boolean value){
        stopThread = value;
        Thread.currentThread().interrupt(); // TODO : Check
    }

}