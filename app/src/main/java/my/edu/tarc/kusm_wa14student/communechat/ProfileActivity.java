package my.edu.tarc.kusm_wa14student.communechat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
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
import android.widget.Toast;

import java.util.Date;

import my.edu.tarc.kusm_wa14student.communechat.internal.ContactDBHandler;
import my.edu.tarc.kusm_wa14student.communechat.internal.MqttHelper;
import my.edu.tarc.kusm_wa14student.communechat.internal.MqttMessageHandler;
import my.edu.tarc.kusm_wa14student.communechat.model.Contact;
import my.edu.tarc.kusm_wa14student.communechat.model.User;

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

    //Var
    private SharedPreferences pref;
    private User user;
    private Contact contact;
    private ContactDBHandler contactDb;
    private final String CACHE_TABLE = contactDb.TABLE_CACHE;

    //Views
    private TextView tvNickname, tvUsername, tvMessage;
    private ImageView ivGender;
    private ImageButton ibAdd, ibCall, ibMessage, ibSettings;
    private ProgressBar progressBar;
    private Button btnBack;
    private FrameLayout frame;

    private String message = "";
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
        contactDb = new ContactDBHandler(getApplicationContext());
        //Register to listen broadcast message
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("MessageEvent"));

        //Get shared preferences
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        user = new User();
        user.setUid(pref.getInt("uid", 0));  //User's data, to retrieve what is needed

        //Get bundle
        Bundle extras = getIntent().getExtras();
        String fid = extras.getString("FID");
        contact = new Contact();

        //Initialize views
        btnBack = (Button) findViewById(R.id.button_back_user_profile);
        tvNickname = (TextView) findViewById(R.id.textView_user_profile_nickname);
        tvUsername = (TextView) findViewById(R.id.textView_user_profile_username);
        ivGender = (ImageView) findViewById(R.id.imageView_user_gender);
        ibAdd = (ImageButton) findViewById(R.id.imageButton_profile_add);
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

        ibAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setAnimation(onClickAnimation);
                ContextThemeWrapper ctw = new ContextThemeWrapper(ProfileActivity.this, R.style.AlertDialogStyle);
                new AlertDialog.Builder(ctw)
                        .setTitle("Friend Request")
                        .setMessage("Do you wish to add " + contact.getNickname() + " as friend?")
                        .setIcon(R.drawable.ic_user_plus)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                friendRequest(String.valueOf(contact.getUid()));
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

    }

    private void loadContact(String fid) {
        if (contactDb.isContactExists(fid, CACHE_TABLE)) {
            contact = contactDb.getContact(fid, CACHE_TABLE);
            renderView();
            runSearchByNameTask(fid, TYPE_EXISTS);
        } else
            runSearchByNameTask(fid, TYPE_NOT_EXISTS);
    }

    private void renderView() {
        tvNickname.setText(contact.getNickname());
        tvUsername.setText("ID: " + contact.getUsername());

        //Check if the targeted user is in contact list
        //To show Add as friend button
        if (contactDb.isContactExists(String.valueOf(contact.getUid()), ContactDBHandler.TABLE_CONTACTS)) {
            ibAdd.setVisibility(View.GONE);
        } else {
            ibAdd.setVisibility(View.VISIBLE);
        }

        if (contact.getGender() == TYPE_MALE) {
            ivGender.setImageDrawable(getResources().getDrawable(R.drawable.ic_boys));
        }
        if (contact.getGender() == TYPE_FEMALE) {
            ivGender.setImageDrawable(getResources().getDrawable(R.drawable.ic_girls));
        }
    }

    private void friendRequest(String uid) {
        MqttMessageHandler mHandler = new MqttMessageHandler();

        //Check MqttMessageHandler encode() REQ FRIEND REQUEST for more details
        //Value 1 as user's id and value 2 as target id for friend request
        mHandler.encode(MqttMessageHandler.MqttCommand.REQ_FRIEND_REQUEST,
                new String[]{String.valueOf(user.getUid()),
                        uid,
                        String.valueOf((new Date().getTime() / 1000))});

        MqttHelper.publish(MqttHelper.getPublishTopic(), mHandler.getPublish());
        Toast.makeText(this, "Friend request sent to " + contact.getNickname(), Toast.LENGTH_SHORT).show();
    }

    private void runSearchByNameTask(String string, int type) {
        final LoadingTask task = new LoadingTask(string, type);
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

    private class LoadingTask extends AsyncTask<Void, Void, Integer> {

        private String uid;
        private int type;
        private MqttMessageHandler handler = new MqttMessageHandler();

        public LoadingTask(String uid, int type) {
            this.uid = uid;
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //If the user is not stored in cache table
            //Display loading UI
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
            MqttHelper.publish(MqttHelper.getPublishTopic(), handler.getPublish());
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

                                //Add the targeted user data in to cache table.
                                if (type == TYPE_NOT_EXISTS)
                                    contactDb.addContact(contact, CACHE_TABLE);

                                //Update the targeted user data in cache table.
                                contactDb.updateSingleContact(contact, CACHE_TABLE);
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
                renderView();
            }
        }
    }

}


