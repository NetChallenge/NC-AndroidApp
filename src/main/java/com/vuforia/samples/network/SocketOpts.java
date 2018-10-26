package com.vuforia.samples.network;

public class SocketOpts {
    private String ip;
    private int port;
    private int maxReadSize;

    public SocketOpts(String ip, int port, int maxReadSize) {
        this.ip = ip;
        this.port = port;
        this.maxReadSize = maxReadSize;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMaxReadSize() {
        return maxReadSize;
    }

    public void setMaxReadSize(int maxReadSize) {
        this.maxReadSize = maxReadSize;
    }
}
