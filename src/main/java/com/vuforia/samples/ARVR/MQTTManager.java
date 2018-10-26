package com.vuforia.samples.ARVR;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTManager implements MqttCallback {
    public interface MQTTListener {
        void onConnectionLost();
        void onArrive(MqttMessage message);
    }

    private static String TAG = "MQTTManager";

    @Override
    public void connectionLost(Throwable cause) {
        if(listener != null)
            listener.onConnectionLost();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if(listener != null)
            listener.onArrive(message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public enum MQTT_Err {
        INIT_SUCCESS,
        INIT_FAIL
    }

    //MQTTManager는 오직 한개만 존재하여야 한다.
    private static MQTTManager mqttManager = null;
    private IMqttAsyncClient client = null;
    private MQTTManager.MQTTListener listener = null;

    private MQTTManager() {}

    public static synchronized MQTTManager getInstance() {
        if(mqttManager == null)
            mqttManager = new MQTTManager();

        return mqttManager;
    }

    public void setMqttListener(MQTTManager.MQTTListener listener) {
        this.listener = listener;
    }

    public MQTT_Err initialize(MQTTServerOpts mqttOpts) {
        try {
            client = new MqttAsyncClient(
                    "tcp://" + mqttOpts.getIp() + ":" + mqttOpts.getPort(),
                    User.getCurrentUser().getUserEmail(),
                    new MemoryPersistence());
            client.setCallback(this);
            client.connect();
            client.subscribe(mqttOpts.getTopic(), 0);

            return MQTT_Err.INIT_SUCCESS;
        } catch (MqttException e) {
            e.printStackTrace();
            return MQTT_Err.INIT_FAIL;
        }
    }


}
