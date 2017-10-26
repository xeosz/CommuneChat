package my.edu.tarc.kusm_wa14student.communechat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import my.edu.tarc.kusm_wa14student.communechat.internal.ContactDBHandler;
import my.edu.tarc.kusm_wa14student.communechat.model.Contact;

/*
*   Future implementations such as Photos, Timeline/Wall
*   should request data from the web server and replace data in internal db
*   instead of just retrieve data from previous activity.
* */


public class ProfileActivity extends AppCompatActivity {

    //Views
    private TextView tvNickname, tvUsername;
    private ImageView ivGender;
    private ImageButton ibCall, ibMessage, ibSettings;
    private Button btnBack;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Register to listen broadcast message
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("MessageEvent"));

        //Get bundle
        Bundle extras = getIntent().getExtras();
        String fid = extras.getString("fid");
        Contact contact = getContactDetails(fid);

        //Initialize views
        btnBack = (Button) findViewById(R.id.button_back_user_profile);
        tvNickname = (TextView) findViewById(R.id.textView_user_profile_nickname);
        tvUsername = (TextView) findViewById(R.id.textView_user_profile_username);
        ivGender = (ImageView) findViewById(R.id.imageView_user_gender);
        ibCall = (ImageButton) findViewById(R.id.imageButton_profile_call);
        ibMessage = (ImageButton) findViewById(R.id.imageButton_profile_message);
        ibSettings = (ImageButton) findViewById(R.id.imageButton_profile_setting);

        final Animation onClickAnimation = new AlphaAnimation(0.3f, 1.0f);
        onClickAnimation.setDuration(1000);

        tvNickname.setText(contact.getNickname());
        tvUsername.setText("ID: " + contact.getUsername());

        if (contact.getGender() == 0) {
            ivGender.setImageDrawable(getResources().getDrawable(R.drawable.ic_boys));
        }
        if (contact.getGender() == 1) {
            ivGender.setImageDrawable(getResources().getDrawable(R.drawable.ic_girls));
        }

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

    private Contact getContactDetails(String id) {
        ContactDBHandler db = new ContactDBHandler(this);
        return db.getContact(id);
    }


}


