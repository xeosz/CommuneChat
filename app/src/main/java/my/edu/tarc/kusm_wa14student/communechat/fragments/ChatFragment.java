package my.edu.tarc.kusm_wa14student.communechat.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttToken;

import my.edu.tarc.kusm_wa14student.communechat.MainActivity;
import my.edu.tarc.kusm_wa14student.communechat.R;
import my.edu.tarc.kusm_wa14student.communechat.assets.MqttHelper;

public class ChatFragment extends Fragment {
    TextView textView;
    EditText editText;
    Button btn;
    MqttHelper mqttHelper;
    MqttAndroidClient mqttAndroidClient;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        textView = (TextView)rootView.findViewById(R.id.chatfrag_tv);
        editText = (EditText) rootView.findViewById(R.id.editTextChat);
        btn = (Button) rootView.findViewById(R.id.button);



        ((MainActivity)getActivity()).setOnBundleSelected(new MainActivity.SelectedBundle() {
            @Override
            public void onBundleSelect(Bundle bundle) {
                updateList(bundle);
            }

        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final MqttAndroidClient client = new MqttAndroidClient(getActivity().getApplicationContext(),
                        "tcp://m11.cloudmqtt.com:17391", "xeosz");
                MqttConnectOptions options = new MqttConnectOptions();
                try {
                    options.setUserName("ehvfrtgx");
                    options.setPassword("YPcMC08pYYpr".toCharArray());
                    IMqttToken token = client.connect(options);

                    token.setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            // We are connected
                            Log.d("mqtt", "onSuccess");
//-----------------------------------------------------------------------------------------------
                            //PUBLISH THE MESSAGE
                            MqttMessage message = new MqttMessage(editText.getText().toString().getBytes());
                            message.setQos(0);
                            message.setRetained(false);

                            //mqtt topic
                            String topic = "sensor/test";

                            try {
                                client.publish(topic, message);
                                Log.i("mqtt", "Message published");

                                // client.disconnect();
                                //Log.i("mqtt", "client disconnected");

                            } catch (MqttException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            // Something went wrong e.g. connection timeout or firewall problems
                            Log.d("mqtt", "onFailure");

                        }

                    });


                } catch (MqttException e) {
                    e.printStackTrace();
                }

            }});

        // Inflate the layout for this fragment
        return rootView;
        }

    private void updateList(Bundle bundle) {
        textView.setText(bundle.getString("message"));
    }


}
