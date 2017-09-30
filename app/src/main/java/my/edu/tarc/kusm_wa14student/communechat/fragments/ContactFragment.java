package my.edu.tarc.kusm_wa14student.communechat.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import my.edu.tarc.kusm_wa14student.communechat.ProfileActivity;
import my.edu.tarc.kusm_wa14student.communechat.R;
import my.edu.tarc.kusm_wa14student.communechat.internal.ContactDBHandler;
import my.edu.tarc.kusm_wa14student.communechat.internal.MqttHelper;
import my.edu.tarc.kusm_wa14student.communechat.internal.MqttMessageHandler;
import my.edu.tarc.kusm_wa14student.communechat.model.Contact;
import my.edu.tarc.kusm_wa14student.communechat.model.MqttCommand;

public class ContactFragment extends Fragment {
    //MQTTClient configuration
    final private String subscriptionTopic = "sensor/test";
    UpdateListTask task = new UpdateListTask();
    private String TAG = "ContactFragment";
    //ListViewAdapter variables
    private ArrayList<Contact> contacts = new ArrayList<>();
    private CustomAdapter adapter;
    //Fragment Views
    private ListView contactListView;
    private TextView tvMain;
    private ProgressBar progressBar;
    private ContactDBHandler db;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            task = new UpdateListTask();
            task.execute(message);
        }
    };

    public ContactFragment() {
        // Required empty public constructor
    }

    public static void clearAsyncTask(AsyncTask<?, ?, ?> asyncTask) {
        if (asyncTask != null) {
            if (!asyncTask.isCancelled()) {
                asyncTask.cancel(true);
            }
            asyncTask = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        clearAsyncTask(task);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_contact, container, false);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessageReceiver, new IntentFilter("MessageEvent"));

        // Inflate the layout for this fragment
        contacts = new ArrayList<>();

        tvMain = rootView.findViewById(R.id.tv_contact_fragment);
        contactListView = rootView.findViewById(R.id.listView_contact);
        progressBar = rootView.findViewById(R.id.progressBar_contact_fragment);
        progressBar.setVisibility(View.INVISIBLE);
        adapter = new CustomAdapter(contacts, 0, getActivity());
        contactListView.setAdapter(adapter);

        db = new ContactDBHandler(getContext());

        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkContent(contacts, rootView);
            }
        });
        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                Contact tempContact = (Contact) contactListView.getItemAtPosition(i);
                Bundle bundle = new Bundle();
                intent.putExtras(bundle);
                getActivity().startActivity(intent);

                //OnClick Animation
                Animation onClickAnimation = new AlphaAnimation(0.3f, 1.0f);
                onClickAnimation.setDuration(2000);
                view.startAnimation(onClickAnimation);
            }
        });

        getContactList();

        //TODO: temp function change if login implemented
        requestContactData("1000000000");

        //Check list contents
        checkContent(contacts, rootView);
        return rootView;
    }

    private void getContactList() {
        contacts.clear();
        ArrayList<Contact> dbContacts = (ArrayList<Contact>) db.getAllContacts();
        for (Contact c : dbContacts)
            contacts.add(c);
        adapter.notifyDataSetChanged();
    }

    public void checkContent(ArrayList contacts, View rootView) {
        if (!contacts.isEmpty()) {
            tvMain.setVisibility(View.INVISIBLE);
        } else {
            tvMain.setText("Opps! No contact at the moment.");
            tvMain.setVisibility(View.VISIBLE);
        }
    }

    private ArrayList<Contact> requestContactData(String uid) {
        ArrayList<Contact> result = new ArrayList<>();
        MqttMessageHandler msg = new MqttMessageHandler();
        msg.encode(MqttCommand.REQ_CONTACT_LIST, uid);
        MqttHelper.publish(subscriptionTopic, msg.getPublish());
        return result;
    }

    //-----Adapter Class-----
    private static class ViewHolder {
        TextView tvName;
        TextView tvStatus;
        //ImageView info;
    }

    private class UpdateListTask extends AsyncTask<String, Void, Void> {


        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... strings) {

            if (!strings[0].isEmpty()) {
                MqttMessageHandler handler = new MqttMessageHandler();
                handler.setReceived(strings[0]);
                if (handler.mqttCommand != null) {
                    switch (handler.mqttCommand) {
                        case REQ_CONTACT_LIST:
                            break;
                        case ACK_CONTACT_LIST: {
                            db.clearTable();

                            ArrayList<Contact> contactList = handler.getContactList();
                            for (Contact temp : contactList)
                                db.addContact(temp);
                            break;
                        }
                        case REQ_CONTACT_DETAILS:
                            break;
                        case ACK_CONTACT_DETAILS:
                            break;
                        case REQ_USER_PROFILE:
                            break;
                        case ACK_USER_PROFILE:
                            break;
                        case REQ_SEARCH_USER:
                            break;
                        case ACK_SEARCH_USER:
                            break;
                        case KEEP_ALIVE:
                            break;
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
            getContactList();
        }
    }

    public class CustomAdapter extends ArrayAdapter<Contact> {
        int lastPosition = -1;

        Context mContext;

        public CustomAdapter(ArrayList<Contact> contacts, int resources, Context context) {
            super(context, resources, contacts);
            this.mContext = context;
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
                viewHolder.tvStatus = (TextView) convertView.findViewById(R.id.contact_frame_status);
                result = convertView;
                convertView.setTag(viewHolder);
            } else {
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
