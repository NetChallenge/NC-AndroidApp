package com.vuforia.samples.model;

import com.vuforia.samples.mqtt.MQTTServerOpts;
import com.vuforia.samples.network.SocketOpts;

public class Room {
    private int roomId;
    private String roomTitle;
    private MQTTServerOpts mqttOpts;
    private SocketOpts sockOpts;

    public Room(int roomId, String roomTitle, MQTTServerOpts mqttOpts, SocketOpts sockOpts) {
        this.roomId = roomId;
        this.roomTitle = roomTitle;
        this.mqttOpts = mqttOpts;
        this.sockOpts = sockOpts;
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
}
