package my.edu.tarc.kusm_wa14student.communechat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.Date;

import my.edu.tarc.kusm_wa14student.communechat.MainActivity;
import my.edu.tarc.kusm_wa14student.communechat.R;
import my.edu.tarc.kusm_wa14student.communechat.components.MqttHelper;
import my.edu.tarc.kusm_wa14student.communechat.components.ContactDBHandler;
import my.edu.tarc.kusm_wa14student.communechat.model.Contact;
import my.edu.tarc.kusm_wa14student.communechat.model.User;

public class ContactFragment extends Fragment {
    private ArrayList<Contact> contacts = new ArrayList<>();

    private ListView listView;
    private TextView tvMain;

    private CustomAdapter adapter;

    //MQTTClient configuration
    public MqttAndroidClient mqttAndroidClient;
    final private String serverUri = "tcp://m11.cloudmqtt.com:17391";
    final private String clientId = "ear12312a";
    final private String subscriptionTopic = "sensor/test";
    final private String USER_TOPIC = "testuser";
    final private String username = "ehvfrtgx";
    final private String password = "YPcMC08pYYpr";
    private MqttHelper mqttHelper;

    private String TAG = "ContactFragment";

    public ContactFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contact, container, false);
        // Inflate the layout for this fragment
        tvMain = (TextView) rootView.findViewById(R.id.tv_contact_fragment);

        contacts = new ArrayList<>();
        ContactDBHandler db = new ContactDBHandler(getActivity());

        if(db.getContactsCount()<=0){
            // Inserting Contacts
            populateContactDB();
        }

        // Reading all contacts
        contacts = (ArrayList<Contact>) db.getAllContacts();
        checkContent(contacts, rootView);
        return rootView;
    }
    private void populateContactDB(){
        ContactDBHandler db = new ContactDBHandler(getActivity());
        Contact c = new Contact(103210005, "username", "nickname0", 1, "ok", 1232415641, "0123456789");
        db.addContact(c);
        db.addContact(new Contact(100000006, "username", "nickname1", 1, "status", (int) (new Date().getTime() * 1000), "0123456789"));
        db.addContact(new Contact(100000007, "username", "nickname2", 1, "status", (int) (new Date().getTime() * 1000), "0123456789"));
        db.addContact(new Contact(100000008, "username", "nickname3", 1, "status", (int) (new Date().getTime() * 1000), "0123456789"));
        db.addContact(new Contact(100000009, "username", "nickname4", 1, "status", (int) (new Date().getTime() * 1000), "0123456789"));
        db.addContact(new Contact(100000000, "username", "nickname5", 1, "status", (int) (new Date().getTime() * 1000), "0123456789"));
        db.addContact(new Contact(100000001, "username", "nickname6", 1, "status", (int) (new Date().getTime() * 1000), "0123456789"));
        db.addContact(new Contact(100000002, "username", "nickname7", 1, "status", (int) (new Date().getTime() * 1000), "0123456789"));
        db.addContact(new Contact(100000003, "username", "nickname9", 1, "status", (int) (new Date().getTime() * 1000), "0123456789"));
        db.addContact(new Contact(100000004, "username", "nickname9", 1, "status", (int) (new Date().getTime() * 1000), "0123456789"));
        db.addContact(new Contact(100000012, "username", "nickname0", 1, "status", (int) (new Date().getTime() * 1000), "0123456789"));
        db.addContact(new Contact(100000032, "username", "nicknm123", 1, "status", (int) (new Date().getTime() * 1000), "0123456789"));
    }
    public void checkContent(ArrayList contacts, View rootView){
        if(!contacts.isEmpty()){
            tvMain.setVisibility(View.INVISIBLE);
            adapter = new CustomAdapter(contacts, 0, getActivity());
            listView = (ListView) rootView.findViewById(R.id.listView_contact);
            listView.setAdapter(adapter);
        }else
        {
            tvMain.setText("Opps! No contact at the moment.");
            tvMain.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private ArrayList<Contact> requestContactData(String uid){
        ArrayList<Contact> result = new ArrayList<>();
        ((MainActivity)getActivity()).setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
        ((MainActivity)getActivity()).mqttPublishMessage(USER_TOPIC, uid);
        return result;
    }

    private static class ViewHolder {
        TextView tvName;
        TextView tvStatus;
        //ImageView info;
    }

    public class CustomAdapter extends ArrayAdapter<Contact>{
        int lastPosition = -1;

        Context mContext;
        public CustomAdapter(ArrayList<Contact> contacts,int resources, Context context) {
            super(context, resources, contacts);
            this.mContext=context;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            Contact contact = getItem(position);
            final View result;
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.contact_frame, parent, false);
                viewHolder.tvName = (TextView) convertView.findViewById(R.id.contact_frame_name);
                viewHolder.tvStatus  = (TextView) convertView.findViewById(R.id.contact_frame_status);
                result = convertView;
                convertView.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) convertView.getTag();
                result = convertView;
            }

            Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
            result.startAnimation(animation);
            lastPosition = position;

            viewHolder.tvName.setText(contact.getNickname());
            viewHolder.tvStatus.setText(contact.getStatus());

            return convertView;
        }
    }
}
