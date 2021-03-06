package my.edu.tarc.kusm_wa14student.communechat.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import my.edu.tarc.kusm_wa14student.communechat.R;

public class ChatTabFragment extends Fragment {

    private ListView chatListView;

    private ArrayList<String> list = new ArrayList<>();
    private CustomAdapter adapter;
    private Bundle bundle = new Bundle();

    private String message = "";
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            message = intent.getStringExtra("message");
        }
    };

    public ChatTabFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat_tab, container, false);

        //Listen to message
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessageReceiver, new IntentFilter("MessageEvent"));

        //Initialize views
        chatListView = (ListView) rootView.findViewById(R.id.listView_chat);

        list = new ArrayList<String>();

        adapter = new CustomAdapter(list, 0, getActivity());
        chatListView.setAdapter(adapter);

        chatListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                //OnClick Animation
                Animation onClickAnimation = new AlphaAnimation(0.3f, 1.0f);
                onClickAnimation.setDuration(2000);
                view.startAnimation(onClickAnimation);

            }
        });

        return rootView;
        }

    private static class ViewHolder {
        TextView tvName;
        TextView tvStatus;
        //ImageView info;
    }

    private class CustomAdapter extends ArrayAdapter<String> {

        Context mContext;

        public CustomAdapter(ArrayList<String> txt, int resources, Context context) {
            super(context, resources, txt);
            this.mContext=context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            String txt = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.contact_frame, parent, false);
                viewHolder.tvName = (TextView) convertView.findViewById(R.id.contact_frame_name);
                viewHolder.tvStatus = (TextView) convertView.findViewById(R.id.contact_frame_bottomlayer);
                convertView.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.tvName.setText("User");
            viewHolder.tvStatus.setText(txt.toString());

            return convertView;
        }
    }

}
