package my.edu.tarc.kusm_wa14student.communechat.internal;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by Xeosz on 29-Sep-17.
 */

public class MessageService extends Service {

    private String TAG = "MessageService";

    private boolean isRunning;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isRunning == false) {
            isRunning = true;

            //Start MQTT Connection
            MqttHelper.startMqtt(getApplicationContext());

            //Mqtt Callback Handler
            MqttHelper.setCallback(new MqttCallbackExtended() {

                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                }

                @Override
                public void connectionLost(Throwable cause) {
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    sendMessage(message.toString());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

            /* ---! Subscribe to default topic *
                Change if you don't want subscribe by default
             */
            MqttHelper.subscribe(MqttHelper.defaultTopic);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    // Send an Intent with an action named "MessageEvent".
    // The Intent sent should be received by the registered ReceiverActivity.
    private void sendMessage(String message) {
        Log.i(TAG, "Broadcasting message");
        Intent intent = new Intent("MessageEvent");

        MqttMessageHandler handler = new MqttMessageHandler();
        handler.setReceived(message);

        if (handler.isReceiving()) {
            intent.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

}
