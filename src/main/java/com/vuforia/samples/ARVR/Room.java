package com.vuforia.samples.ARVR;

public class Room {
    private int roomId;
    private String roomTitle;
    private MQTTServerOpts mqttOpts;
    private STTServerOpts sttOpts;

    public Room(int roomId, String roomTitle, MQTTServerOpts mqttOpts, STTServerOpts sttOpts) {
        this.roomId = roomId;
        this.roomTitle = roomTitle;
        this.mqttOpts = mqttOpts;
        this.sttOpts = sttOpts;
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

    public STTServerOpts getSttOpts() {
        return sttOpts;
    }

    public void setSttOpts(STTServerOpts sttOpts) {
        this.sttOpts = sttOpts;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
