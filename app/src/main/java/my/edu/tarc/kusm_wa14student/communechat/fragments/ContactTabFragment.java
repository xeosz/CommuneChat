package my.edu.tarc.kusm_wa14student.communechat.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
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
import my.edu.tarc.kusm_wa14student.communechat.model.User;

public class ContactTabFragment extends Fragment {
    UpdateListTask task;
    //ListViewAdapter variables
    private ArrayList<Contact> contacts = new ArrayList<>();
    private CustomAdapter adapter;

    //Fragment Views
    private ListView contactListView;
    private TextView tvMain;
    private ProgressBar progressBar;

    //var
    private User user;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private ContactDBHandler db;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            task = new UpdateListTask();
            task.execute(message);
        }
    };

    public ContactTabFragment() {
        // Required empty public constructor
    }

    public static void clearAsyncTask(AsyncTask<?, ?, ?> asyncTask) {
        if (asyncTask != null) {
            if (!asyncTask.isCancelled()) {
                asyncTask.cancel(true);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        clearAsyncTask(task);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_contact_tab, container, false);

        contacts = new ArrayList<>();
        user = new User();

        //Share preferences
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        editor = pref.edit();
        if (pref != null) {
            user.setUid(pref.getInt("uid", 0));
        }

        //Listen to message
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessageReceiver, new IntentFilter("MessageEvent"));

        // Inflate the layout for this fragment

        tvMain = rootView.findViewById(R.id.tv_contact_fragment);
        contactListView = rootView.findViewById(R.id.listView_contact);
        progressBar = rootView.findViewById(R.id.progressBar_contact_fragment);
        progressBar.setVisibility(View.INVISIBLE);
        adapter = new CustomAdapter(contacts, 0, getActivity());
        contactListView.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in), 0.3f));
        contactListView.setAdapter(adapter);

        final Animation onClickAnimation = new AlphaAnimation(0.3f, 1.0f);
        onClickAnimation.setDuration(1000);

        db = new ContactDBHandler(getActivity().getApplicationContext(), ContactDBHandler.CONTACT_DATABASE);

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

                bundle.putString("FID", String.valueOf(tempContact.getUid()));

                intent.putExtras(bundle);
                getActivity().startActivity(intent);

                //OnClick Animation
                view.startAnimation(onClickAnimation);
            }
        });
        getContactList();
        requestContactData(String.valueOf(user.getUid()));
        //Check list contents
        checkContent(contacts, rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void getContactList() {
        contacts.clear();
        ArrayList<Contact> dbContacts = (ArrayList<Contact>) db.getAllContacts();
        for (Contact c : dbContacts)
            contacts.add(c);
        adapter.notifyDataSetChanged();
    }

    private void checkContent(ArrayList contacts, View rootView) {
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
        msg.encode(MqttMessageHandler.MqttCommand.REQ_CONTACT_LIST, uid);
        MqttHelper.publish(MqttHelper.getUserTopic(), msg.getPublish());
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
                        case ACK_CONTACT_LIST: {
                            db.clearTable();

                            ArrayList<Contact> contactList = handler.getContactList();
                            for (Contact temp : contactList)
                                db.addContact(temp);
                            break;
                        }

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

    private class CustomAdapter extends ArrayAdapter<Contact> {
        Context mContext;

        private CustomAdapter(ArrayList<Contact> contacts, int resources, Context context) {
            super(context, resources, contacts);
            this.mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            Contact contact = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.contact_frame, parent, false);
                viewHolder.tvName = (TextView) convertView.findViewById(R.id.contact_frame_name);
                viewHolder.tvStatus = (TextView) convertView.findViewById(R.id.contact_frame_bottomlayer);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tvName.setText(contact.getNickname());
            viewHolder.tvStatus.setText(contact.getStatus());

            return convertView;
        }
    }
}