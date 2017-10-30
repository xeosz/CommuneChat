package my.edu.tarc.kusm_wa14student.communechat.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import my.edu.tarc.kusm_wa14student.communechat.R;
import my.edu.tarc.kusm_wa14student.communechat.model.User;

/**
 * A simple {@link Fragment} subclass.
 */

public class ChatRoomFragment extends Fragment {

    private static final int TYPE_ONE_TO_ONE = 1;
    private static final int TYPE_GROUP = 2;
    //Views
    private ListView lvMessage;
    private TextView tvTitleName, tvAlertMessage;
    private Button btnSend;
    private EditText etSendMessage;
    private ImageButton ibMoreOp, ibBack;
    private ProgressBar progressBar;

    //Var
    private User user;
    private SharedPreferences pref;
    private String fid;
    private String message = "";
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            message = intent.getStringExtra("message");
        }
    };
    private String uniqueTopic = "sensor/test";

    public ChatRoomFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chat_room, container, false);

        //Listen to message
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessageReceiver, new IntentFilter("MessageEvent"));

        user = new User();

        //Shared preferences
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        if (pref != null) {
            user.setUid(pref.getInt("uid", 0));
        }

        //Initialize views
        lvMessage = rootView.findViewById(R.id.listView_chat_message);
        tvTitleName = rootView.findViewById(R.id.textView_chatroom_title);
        tvAlertMessage = rootView.findViewById(R.id.textView_chatroom_alert);
        btnSend = rootView.findViewById(R.id.btn_chatroom_send);
        etSendMessage = rootView.findViewById(R.id.editText_send_message);
        ibMoreOp = rootView.findViewById(R.id.imageButton_chatroom_setting);
        ibBack = rootView.findViewById(R.id.imageButton_chatroom_back);
        progressBar = rootView.findViewById(R.id.progressBar_chatroom);

        final Animation onClickAnimation = new AlphaAnimation(0.3f, 1.0f);
        onClickAnimation.setDuration(1000);

        //Retrive bundle
        if (getArguments() != null) {
            int type = getArguments().getInt("TYPE");
            switch (type) {
                case TYPE_ONE_TO_ONE: {
                    break;
                }
                case TYPE_GROUP: {
                    break;
                }
                default:
                    break;
            }
        }

        return rootView;
    }

}
