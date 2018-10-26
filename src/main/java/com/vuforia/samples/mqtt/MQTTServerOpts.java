package com.vuforia.samples.mqtt;

public class MQTTServerOpts {
    private String ip = "163.180.117.216";
    private int port = 1883;
    private String topic = "";

    public MQTTServerOpts(String ip, int port, String topic) {
        this.ip = ip;
        this.port = port;
        this.topic = topic;
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

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
