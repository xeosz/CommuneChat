package my.edu.tarc.kusm_wa14student.communechat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import my.edu.tarc.kusm_wa14student.communechat.internal.ContactDBHandler;
import my.edu.tarc.kusm_wa14student.communechat.internal.MqttHelper;
import my.edu.tarc.kusm_wa14student.communechat.internal.MqttMessageHandler;
import my.edu.tarc.kusm_wa14student.communechat.model.Contact;

/*
*   Future implementations such as Photos, Timeline/Wall
*   should request data from the web server and replace data in internal db
*   instead of just retrieve data from previous activity.
* */


public class ProfileActivity extends AppCompatActivity {

    private static final long TASK_TIMEOUT = 8000;
    private static final int TYPE_MALE = 1;
    private static final int TYPE_FEMALE = 2;
    private static final int TYPE_EXISTS = 1;
    private static final int TYPE_NOT_EXISTS = 2;
    private String message = "";
    private Contact contact;
    private ContactDBHandler contactDb;

    //Views
    private TextView tvNickname, tvUsername, tvMessage;
    private ImageView ivGender;
    private ImageButton ibCall, ibMessage, ibSettings;
    private ProgressBar progressBar;
    private Button btnBack;
    private FrameLayout frame;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            message = intent.getStringExtra("message");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        contactDb = new ContactDBHandler(getApplicationContext(), ContactDBHandler.CACHE_DATABASE);
        //Register to listen broadcast message
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("MessageEvent"));

        //Get bundle
        Bundle extras = getIntent().getExtras();
        String fid = extras.getString("FID");
        contact = new Contact();

        //Initialize views
        btnBack = (Button) findViewById(R.id.button_back_user_profile);
        tvNickname = (TextView) findViewById(R.id.textView_user_profile_nickname);
        tvUsername = (TextView) findViewById(R.id.textView_user_profile_username);
        ivGender = (ImageView) findViewById(R.id.imageView_user_gender);
        ibCall = (ImageButton) findViewById(R.id.imageButton_profile_call);
        ibMessage = (ImageButton) findViewById(R.id.imageButton_profile_message);
        ibSettings = (ImageButton) findViewById(R.id.imageButton_profile_setting);
        progressBar = (ProgressBar) findViewById(R.id.progressBar_profile);
        tvMessage = (TextView) findViewById(R.id.textView_user_profile_message);
        frame = (FrameLayout) findViewById(R.id.frame_user_profile);

        final Animation onClickAnimation = new AlphaAnimation(0.3f, 1.0f);
        onClickAnimation.setDuration(1000);

        loadContact(fid);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setAnimation(onClickAnimation);
                onBackPressed();
            }
        });

        ibMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setAnimation(onClickAnimation);
            }
        });

    }

    private void loadContact(String fid) {
        if (contactDb.isContactExists(fid)) {
            contact = contactDb.getContact(fid);
            refreshProfileDetails();
            runSearchByNameTask(fid, TYPE_EXISTS);
        } else
            runSearchByNameTask(fid, TYPE_NOT_EXISTS);
    }

    private void refreshProfileDetails() {
        tvNickname.setText(contact.getNickname());
        tvUsername.setText("ID: " + contact.getUsername());

        if (contact.getGender() == TYPE_MALE) {
            ivGender.setImageDrawable(getResources().getDrawable(R.drawable.ic_boys));
        }
        if (contact.getGender() == TYPE_FEMALE) {
            ivGender.setImageDrawable(getResources().getDrawable(R.drawable.ic_girls));
        }
    }

    private void runSearchByNameTask(String string, int type) {
        final LoadDetailsTask task = new LoadDetailsTask(string, type);
        task.execute();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (task.getStatus() == AsyncTask.Status.RUNNING) {
                    task.cancel(true);
                    progressBar.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    frame.bringToFront();
                    frame.bringChildToFront(tvMessage);
                    tvMessage.setVisibility(View.VISIBLE);
                    tvMessage.setText("Connection Timeout.");
                }
            }
        }, TASK_TIMEOUT);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class LoadDetailsTask extends AsyncTask<Void, Void, Integer> {

        private String uid;
        private int type;
        private MqttMessageHandler handler = new MqttMessageHandler();

        public LoadDetailsTask(String uid, int type) {
            this.uid = uid;
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (type == TYPE_NOT_EXISTS) {
                frame.setVisibility(View.VISIBLE);
                frame.bringToFront();
                frame.bringChildToFront(progressBar);
                progressBar.setVisibility(View.VISIBLE);
                tvMessage.setText("");
                tvMessage.setVisibility(View.INVISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
            handler.encode(MqttMessageHandler.MqttCommand.REQ_CONTACT_DETAILS, this.uid);
            MqttHelper.publish(MqttHelper.getUserTopic(), handler.getPublish());
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            if (!isCancelled()) {
                if (isNetworkAvailable()) {
                    try {
                        Thread.sleep(2000);
                        if (!message.isEmpty()) {
                            handler.setReceived(message);
                            message = "";
                            if (handler.mqttCommand == MqttMessageHandler.MqttCommand.ACK_CONTACT_DETAILS) {
                                contact = handler.getContactDetails();
                                if (type == TYPE_NOT_EXISTS)
                                    contactDb.addContact(contact);
                                contactDb.updateSingleContact(contact);
                                return 1;
                            }
                        } else {
                            this.doInBackground();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else
                    return 2;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if (type == TYPE_NOT_EXISTS) {
                progressBar.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                frame.animate().alpha(0.0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        frame.setVisibility(View.GONE);
                    }
                });
            }
            if (integer == 1) {
                refreshProfileDetails();
            }
        }
    }

}


