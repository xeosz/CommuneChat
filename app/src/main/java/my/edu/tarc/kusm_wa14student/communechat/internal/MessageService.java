package my.edu.tarc.kusm_wa14student.communechat.internal;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.List;

/**
 * Created by Xeosz on 29-Sep-17.
 */

public class MessageService extends Service {

    private SharedPreferences pref;
    private String TAG = "MessageService";
    private boolean isRunning;
    private MqttMessageBuffer buffer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = false;
        buffer = new MqttMessageBuffer(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (isRunning == false) {
            isRunning = true;

            //Start MQTT Connection
            if (pref.getInt("uid", 0) != 0) {
                MqttHelper.startMqtt(getApplicationContext(), String.valueOf(pref.getInt("uid", 0)));
                Log.i("[Service] ", "" + pref.getInt("uid", 0) + "" + MqttHelper.getUserTopic());
                MqttHelper.subscribe(MqttHelper.getUserTopic());
            } else {
                MqttHelper.startMqtt(getApplicationContext());
            }

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
                    if (isReceiving(message.toString())) {
                        if (appInForeground(getApplicationContext())) {
                            sendMessage(topic, message.toString());
                            Log.i(TAG, "Foreground" + message.toString());
                        } else {
                            Log.i(TAG, "background" + message.toString());
                            buffer.push(topic, message.toString());
                        }
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    private boolean appInForeground(@NonNull Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        if (runningAppProcesses == null) {
            return false;
        }

        for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
            if (runningAppProcess.processName.equals(context.getPackageName()) &&
                    runningAppProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    private boolean isReceiving(String msg) {
        MqttMessageHandler handler = new MqttMessageHandler();
        handler.setReceived(msg);
        return handler.isReceiving();
    }

    // Send an Intent with an action named "MessageEvent".
    // The Intent sent should be received by the registered ReceiverActivity.
    private void sendMessage(String topic, String message) {
        Intent intent = new Intent("MessageEvent");

        intent.putExtra("topic", topic);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

}
