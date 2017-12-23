package my.edu.tarc.kusm_wa14student.communechat.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import my.edu.tarc.kusm_wa14student.communechat.ProfileActivity;
import my.edu.tarc.kusm_wa14student.communechat.R;
import my.edu.tarc.kusm_wa14student.communechat.internal.MqttHelper;
import my.edu.tarc.kusm_wa14student.communechat.internal.MqttMessageHandler;
import my.edu.tarc.kusm_wa14student.communechat.model.Contact;
import my.edu.tarc.kusm_wa14student.communechat.model.User;

public class FriendRequestFragment extends Fragment {
    //Beware of the static identifier
    //As it may be used in other fragments
    private static final long TASK_TIMEOUT = 8000;
    private static final int FRIEND_LIST_LOAD = 1;
    private static final int FRIEND_LIST_FRIEND_REQUEST_RESPONSE = 2;
    private static final String TITLE = "Friend Request";
    private static final String FID_KEY = "FID";

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
    private CustomAdapter adapter;
    private Animation onClickAnimation;
    private User user;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friend_request, container, false);

        //Listen to message
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessageReceiver, new IntentFilter("MessageEvent"));
        contacts = new ArrayList<>();
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        user = new User();
        //Retrive shared preference
        if (pref != null) {
            user.setUid(pref.getInt("uid", 0));
        }

        //Initialize views
        tvTitle = rootView.findViewById(R.id.textView_contact_request_title);
        tvMessage = rootView.findViewById(R.id.textView_contact_request_message);
        ibBack = rootView.findViewById(R.id.btn_contact_request_back);
        lvRequest = rootView.findViewById(R.id.listView_contact_request);
        progressBar = rootView.findViewById(R.id.progressBar_contact_request);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#8f1ffc"), android.graphics.PorterDuff.Mode.MULTIPLY);
        onClickAnimation = new AlphaAnimation(0.3f, 1.0f);
        onClickAnimation.setDuration(1000);

        tvTitle.setText(TITLE);
        lvRequest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                view.startAnimation(onClickAnimation);

                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                Contact tempContact = (Contact) lvRequest.getItemAtPosition(i);
                Bundle bundle = new Bundle();

                bundle.putString(FID_KEY, String.valueOf(tempContact.getUid()));

                intent.putExtras(bundle);
                getActivity().startActivity(intent);

            }
        });

        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(onClickAnimation);
                getActivity().onBackPressed();
            }
        });

        runLoadingTask(String.valueOf(user.getUid()));

        return rootView;
    }

    private void runLoadingTask(String uid) {
        final LoadingTask task = new LoadingTask(uid, FRIEND_LIST_LOAD);
        task.execute();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (task.getStatus() == AsyncTask.Status.RUNNING) {
                    task.cancel(true);
                    progressBar.setVisibility(View.GONE);
                    tvMessage.setVisibility(View.VISIBLE);
                    tvMessage.setText("Connection Timeout.");
                    tvMessage.bringToFront();
                }
            }
        }, TASK_TIMEOUT);
    }

    private void runResponseTask(String uid, String fid, int position) {
        final LoadingTask task = new LoadingTask(uid, fid, position, FRIEND_LIST_FRIEND_REQUEST_RESPONSE);
        task.execute();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (task.getStatus() == AsyncTask.Status.RUNNING) {
                    task.cancel(true);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), "Connection Timeout.", Toast.LENGTH_SHORT).show();
                }
            }
        }, TASK_TIMEOUT);
    }

    private void renderView() {
        adapter = new CustomAdapter(contacts, 0, getActivity());
        lvRequest.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in), 0.5f));
        lvRequest.setAdapter(adapter);
        if (contacts.size() <= 0) {
            tvMessage.setText("No friend requests.");
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.bringToFront();
        }
        adapter.notifyDataSetChanged();
    }

    //-----Adapter Class-----
    private static class ViewHolder {
        TextView tvName;
        TextView tvStatus;
        Button btnRequest;
    }

    private class LoadingTask extends AsyncTask<Void, Void, Integer> {

        private Contact tempC;
        private String uid, fid;
        private int index, type;
        private MqttMessageHandler handler = new MqttMessageHandler();

        public LoadingTask(String id, String fid, int index, int type) {
            this.uid = id;
            this.fid = fid;
            this.index = index;
            this.type = type;
        }

        public LoadingTask(String id, int type) {
            this.uid = id;
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            progressBar.bringToFront();
            tvMessage.setText("");
            tvMessage.setVisibility(View.INVISIBLE);
            if (this.type == FRIEND_LIST_LOAD) {
                handler.encode(MqttMessageHandler.MqttCommand.REQ_FRIEND_REQUEST_LIST, this.uid);
                MqttHelper.publish(MqttHelper.getPublishTopic(), handler.getPublish());
                contacts.clear();
            }
            if (this.type == FRIEND_LIST_FRIEND_REQUEST_RESPONSE) {
                handler.encode(MqttMessageHandler.MqttCommand.REQ_RESPONSE_FRIEND_REQUEST, new String[]{uid, fid});
                MqttHelper.publish(MqttHelper.getPublishTopic(), handler.getPublish());
            }
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            int result = 0;
            if (!isCancelled()) {
                try {
                    Thread.sleep(2000);
                    if (!message.isEmpty()) {
                        handler.setReceived(message);
                        message = "";
                        if (handler.mqttCommand == MqttMessageHandler.MqttCommand.ACK_FRIEND_REQUEST_LIST) {
                            contacts = handler.getFriendRequestList();
                            if (contacts.size() > 0) {
                                result = 1;
                            } else
                                result = 0;
                        }
                        if (handler.mqttCommand == MqttMessageHandler.MqttCommand.ACK_RESPONSE_FRIEND_REQUEST) {
                            tempC = contacts.remove(index);
                            return 1;
                        }
                    } else {
                        this.doInBackground();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            progressBar.setVisibility(View.GONE);
            if (this.type == FRIEND_LIST_LOAD) {
                renderView();
            }
            if (this.type == FRIEND_LIST_FRIEND_REQUEST_RESPONSE) {
                if (integer == 1) {
                    renderView();
                    Snackbar.make(getView(), "You have accepted " + tempC.getNickname() + "'s friend request", Snackbar.LENGTH_SHORT).show();
                }
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            final Contact contact = getItem(position);
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

            viewHolder.btnRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(onClickAnimation);

                    runResponseTask(String.valueOf(user.getUid()), String.valueOf(contact.getUid()), position);
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(getActivity(), ProfileActivity.class);
                    Bundle bundle = new Bundle();

                    bundle.putString(FID_KEY, String.valueOf(contact.getUid()));

                    intent.putExtras(bundle);
                    getActivity().startActivity(intent);
                }
            });

            return convertView;
        }
    }

}
