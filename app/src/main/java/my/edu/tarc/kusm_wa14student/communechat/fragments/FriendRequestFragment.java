package my.edu.tarc.kusm_wa14student.communechat.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import my.edu.tarc.kusm_wa14student.communechat.R;
import my.edu.tarc.kusm_wa14student.communechat.internal.MqttHelper;
import my.edu.tarc.kusm_wa14student.communechat.internal.MqttMessageHandler;
import my.edu.tarc.kusm_wa14student.communechat.model.Contact;
import my.edu.tarc.kusm_wa14student.communechat.model.User;

public class FriendRequestFragment extends Fragment {

    //Views
    private TextView tvTitle, tvMessage;
    private ImageButton ibBack;
    private ProgressBar progressBar;
    private ListView lvRequest;
    private ArrayList<Contact> contacts;

    //Var
    private SharedPreferences pref;
    private String message = "";
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            message = intent.getStringExtra("message");
        }
    };
    private User user;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friend_request, container, false);

        //Listen to message
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessageReceiver, new IntentFilter("MessageEvent"));
        contacts = new ArrayList<>();
        user = new User();

        //Share preferences
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        if (pref != null) {
            user.setUid(pref.getInt("uid", 0));
        }

        //Initialize views
        tvTitle = rootView.findViewById(R.id.textView_contact_request_title);
        tvMessage = rootView.findViewById(R.id.textView_contact_request_message);
        ibBack = rootView.findViewById(R.id.btn_contact_request_back);
        lvRequest = rootView.findViewById(R.id.listView_contact_request);
        progressBar = rootView.findViewById(R.id.progressBar_contact_request);

        final Animation onClickAnimation = new AlphaAnimation(0.3f, 1.0f);
        onClickAnimation.setDuration(1000);

        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(onClickAnimation);
                getActivity().onBackPressed();
            }
        });

        return rootView;
    }

    //-----Adapter Class-----
    private static class ViewHolder {
        TextView tvName;
        TextView tvStatus;
        Button btnRequest;
    }

    private class LoadingTask extends AsyncTask<Void, Void, Integer> {

        private String uid;
        private MqttMessageHandler handler = new MqttMessageHandler();

        public LoadingTask(String id) {
            this.uid = id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            progressBar.bringToFront();
            tvMessage.setText("");
            tvMessage.setVisibility(View.INVISIBLE);
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            //TODO
            handler.encode(MqttMessageHandler.MqttCommand.REQ_FRIEND_REQUEST_LIST, this.uid);
            MqttHelper.publish(MqttHelper.getUserTopic(), handler.getPublish());
            contacts.clear();
        }

        @Override
        protected Integer doInBackground(Void... voids) {


            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            progressBar.setVisibility(View.GONE);
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            if (integer == 0) {
                tvMessage.setText("No user matchings \"" + "\".");
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.bringToFront();
            } else if (integer == 1) {

            }
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
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.contact_request_frame, parent, false);
                viewHolder.tvName = convertView.findViewById(R.id.contact_request_name);
                viewHolder.tvStatus = convertView.findViewById(R.id.contact_request_bottomlayer);
                viewHolder.btnRequest = convertView.findViewById(R.id.btn_contact_request_accept);
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
