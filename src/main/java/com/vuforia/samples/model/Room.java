package com.vuforia.samples.model;

import com.vuforia.samples.mqtt.MQTTServerOpts;
import com.vuforia.samples.network.SocketOpts;

public class Room {
    private int roomId;
    private String roomTitle;
    private MQTTServerOpts mqttOpts;
    private SocketOpts sockOpts;
    private String sttContainerId;

    public Room(int roomId, String roomTitle, MQTTServerOpts mqttOpts, SocketOpts sockOpts, String sttContainerId) {
        this.roomId = roomId;
        this.roomTitle = roomTitle;
        this.mqttOpts = mqttOpts;
        this.sockOpts = sockOpts;
        this.sttContainerId = sttContainerId;
    }

    public String getRoomTitle() {
        return roomTitle;
    }

    public void setRoomTitle(String roomTitle) {
        this.roomTitle = roomTitle;
    }

    public MQTTServerOpts getMqttOpts() {
        return mqttOpts;
    }

    public void setMqttOpts(MQTTServerOpts mqttOpts) {
        this.mqttOpts = mqttOpts;
    }

    public SocketOpts getSttOpts() {
        return sockOpts;
    }

    public void setSttOpts(SocketOpts sockOpts) {
        this.sockOpts = sockOpts;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getSttContainerId() {
        return sttContainerId;
    }

    public void setSttContainerId(String sttContainerId) {
        this.sttContainerId = sttContainerId;
    }
}
