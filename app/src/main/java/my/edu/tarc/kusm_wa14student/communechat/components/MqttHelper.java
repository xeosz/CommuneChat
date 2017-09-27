package my.edu.tarc.kusm_wa14student.communechat.components;

import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;

import java.util.Enumeration;

import static org.eclipse.paho.client.mqttv3.MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT;

/**
 * Created by Xeosz on 24-Sep-17.
 */

public class MqttHelper {

    private static final String TAG = "MQTTHelper";
    public MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
    public DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
    private String serverUri;
    private String clientId;


    public MqttHelper(Context context, String serverUrl, String clientId){
        this.serverUri = serverUrl;
        this.clientId = clientId;
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    public void setConnectionOptions(String username, String password) {
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());
        mqttConnectOptions.setKeepAliveInterval(KEEP_ALIVE_INTERVAL_DEFAULT);
    }
    private void setDisconnectBufferOption(){
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(true);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
    }

    private IMqttToken connect() throws MqttException {
        return mqttAndroidClient.connect(mqttConnectOptions);
    }

    public void subscribe(final String subscriptionTopic, final int qos) {
        if(!mqttAndroidClient.isConnected()){
            IMqttToken token;
            try {
                token = this.connect();
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        setDisconnectBufferOption();
                        try {
                            mqttAndroidClient.subscribe(subscriptionTopic,qos);
                        } catch (MqttException e) {
                            Log.w(TAG, "Failed to subscribe to topic: "+subscriptionTopic+".");
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                setDisconnectBufferOption();
                mqttAndroidClient.subscribe(subscriptionTopic,qos);
            } catch (MqttException e) {
                Log.w(TAG, "Failed to subscribe to topic: "+subscriptionTopic+".");
                e.printStackTrace();
            }
        }
    }

    public void publish(final String publishTopic, final String payload, final int qos, final boolean retain){
        final MqttMessage message = new MqttMessage();
        message.setPayload(payload.getBytes());
        message.setQos(qos);
        message.setRetained(retain);
        if(!mqttAndroidClient.isConnected()){
            IMqttToken token = null;
            try {
                token = this.connect();
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        setDisconnectBufferOption();
                        try {
                            mqttAndroidClient.publish(publishTopic, message);
                        } catch (MqttException e) {
                            Log.w(TAG, "Failed to publish messsage: \""+payload+"\" on topic: "+publishTopic+".");
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                setDisconnectBufferOption();
                mqttAndroidClient.publish(publishTopic, message);
            } catch (MqttException e) {
                Log.w(TAG, "Failed to publish messsage: \"" + payload + "\" on topic: " + publishTopic + ".");
                e.printStackTrace();
            }
        }
    }

    public void unsubscribe(final String subscriptionTopic){
        if(!mqttAndroidClient.isConnected()) {
            try {
                IMqttToken token = mqttAndroidClient.connect(mqttConnectOptions);
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        try {
                            mqttAndroidClient.unsubscribe(subscriptionTopic, null, new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    Log.w(TAG, "Unsubscribeb to topic: " + subscriptionTopic + ".");
                                }

                                @Override
                                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                    Log.w(TAG, "Failed to unsubscribe topic: " + subscriptionTopic + ".");
                                }
                            });
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                mqttAndroidClient.unsubscribe(subscriptionTopic, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.w(TAG, "Unsubscribeb to topic: " + subscriptionTopic + ".");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.w(TAG, "Failed to unsubscribe topic: " + subscriptionTopic + ".");
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect(){
        try {
            IMqttToken token = mqttAndroidClient.disconnect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w(TAG, "Disconnected to the server "+serverUri);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
