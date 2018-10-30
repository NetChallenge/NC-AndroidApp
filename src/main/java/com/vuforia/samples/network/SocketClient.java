package com.vuforia.samples.network;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by icns on 2018-07-11.
 */

public class SocketClient extends Thread {
    public interface SockListener {
        void onConnect();
        void onRecv(byte[] recv);
        void onStopped();
    }

    public static final String TAG = "SocketManager";
    private static SocketClient client = null;

    //socket info
    private String ip;
    private int port;
    private int maxReadSize;

    //
    private Socket sock;
    private InputStream sockIn;
    private OutputStream sockOut;
    private boolean isAlive;
    private byte[] readBuf;

    //
    private SockListener listener;

    public SocketClient(String ip, int port, int maxReadSize, SockListener listener) {
        this.ip = ip;
        this.port = port;
        this.maxReadSize = maxReadSize;
        this.listener = listener;
    }

    public boolean connect() {
        try {
            InetAddress serverAddr = InetAddress.getByName(ip);
            sock = new Socket(serverAddr, port);
            sockIn = sock.getInputStream();
            sockOut = sock.getOutputStream();
            isAlive = true;

            readBuf = new byte[maxReadSize];
            listener.onConnect();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized void close() {
        try {
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        listener.onStopped();
        isAlive = false;
    }

    //first you need to send size of packet and send packet
    public synchronized void write(byte[] buf) {
        try {
            sockOut.write(toBytes(buf.length));
            sockOut.write(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] toBytes(int i) {
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i /*>> 0*/);

        return result;
    }

    public byte[] read() {
        try {
            sockIn.read(readBuf);

            return readBuf;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void run() {
        int readLen = 0;
        while(isAlive) {
            try {
                readLen = sockIn.read(readBuf);
                if(readLen < 0) {
                    Log.d(TAG, "socket read error");
                    close();
                    break;
                }
                byte[] recv = new byte[readLen];
                System.arraycopy(readBuf, 0, recv, 0, readLen);
                listener.onRecv(recv);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}