package my.edu.tarc.kusm_wa14student.communechat.internal;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import static org.eclipse.paho.client.mqttv3.MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT;

/**
 * Created by Xeosz on 24-Sep-17.
 */

public final class MqttHelper {

    private static final String TAG = "MQTTHelper";
    public static MqttAndroidClient mqttAndroidClient;
    public static String defaultTopic = "sensor/test";
    private static MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
    private static DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
    //Static MQTT Connection Variables
    private static String clientId = "1000000000";
    private static String serverUri = "tcp://m11.cloudmqtt.com:17391";
    private static String mqttUsername = "ehvfrtgx";
    private static String mqttPassword = "YPcMC08pYYpr";
    private static int QoS = 1;
    private static boolean retain = false;

    public static void startMqtt(Context context) {
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
    }

    public static void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    public static void setConnectionOptions(String username, String password) {
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());
        mqttConnectOptions.setKeepAliveInterval(KEEP_ALIVE_INTERVAL_DEFAULT);
    }

    private static void setDisconnectBufferOption() {
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(true);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
    }

    private static IMqttToken connect() throws MqttException {
        setConnectionOptions(mqttUsername, mqttPassword);
        return mqttAndroidClient.connect(mqttConnectOptions);
    }

    public static void subscribe(final String subscriptionTopic) {
        if (!mqttAndroidClient.isConnected()) {
            IMqttToken token;
            try {
                token = connect();
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        setDisconnectBufferOption();
                        try {
                            mqttAndroidClient.subscribe(subscriptionTopic, QoS);
                        } catch (MqttException e) {
                            Log.w(TAG, "Failed to subscribe to topic: " + subscriptionTopic + ".");
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
        } else {
            try {
                setDisconnectBufferOption();
                mqttAndroidClient.subscribe(subscriptionTopic, QoS);
            } catch (MqttException e) {
                Log.w(TAG, "Failed to subscribe to topic: " + subscriptionTopic + ".");
                e.printStackTrace();
            }
        }
    }

    public static void publish(final String publishTopic, final String payload) {
        final MqttMessage message = new MqttMessage();
        message.setPayload(payload.getBytes());
        message.setQos(QoS);
        message.setRetained(retain);
        if (!mqttAndroidClient.isConnected()) {
            IMqttToken token = null;
            try {
                token = connect();
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        setDisconnectBufferOption();
                        try {
                            mqttAndroidClient.publish(publishTopic, message);
                        } catch (MqttException e) {
                            Log.w(TAG, "Failed to publish messsage: \"" + payload + "\" on topic: " + publishTopic + ".");
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
        } else {
            try {
                setDisconnectBufferOption();
                mqttAndroidClient.publish(publishTopic, message);
            } catch (MqttException e) {
                Log.w(TAG, "Failed to publish messsage: \"" + payload + "\" on topic: " + publishTopic + ".");
                e.printStackTrace();
            }
        }
    }

    public static void unsubscribe(final String subscriptionTopic) {
        if (!mqttAndroidClient.isConnected()) {
            try {
                IMqttToken token = mqttAndroidClient.connect(mqttConnectOptions);
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        try {
                            mqttAndroidClient.unsubscribe(subscriptionTopic, null, new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    Log.i(TAG, "Unsubscribeb to topic: " + subscriptionTopic + ".");
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
        } else {
            try {
                mqttAndroidClient.unsubscribe(subscriptionTopic, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.i(TAG, "Unsubscribeb to topic: " + subscriptionTopic + ".");
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

    public static void disconnect() {
        try {
            IMqttToken token = mqttAndroidClient.disconnect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "Disconnected to the server " + serverUri);
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
