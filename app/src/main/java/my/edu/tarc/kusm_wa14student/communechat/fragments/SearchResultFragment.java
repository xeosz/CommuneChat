package my.edu.tarc.kusm_wa14student.communechat.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import my.edu.tarc.kusm_wa14student.communechat.R;
import my.edu.tarc.kusm_wa14student.communechat.internal.MqttHelper;
import my.edu.tarc.kusm_wa14student.communechat.internal.MqttMessageHandler;
import my.edu.tarc.kusm_wa14student.communechat.model.Contact;
import my.edu.tarc.kusm_wa14student.communechat.model.User;

public class SearchResultFragment extends Fragment {

    private static final int SEARCH_BY_NAME = 0;
    private static final int SEARCH_NEARBY = 2;
    private static final int SEARCH_REC = 3;

    private static long TASK_TIMEOUT = 6000;
    //Views
    private ImageButton ibBack, ibSearch;
    private ListView listViewResult;
    private TextView tvMessage, tvTitle;
    private EditText etTitle;
    private ProgressBar progressBar;
    //Var
    private String searchString = "";
    private User user;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private ArrayList<Contact> contacts;
    private CustomAdapter adapter;
    private String message = "";
    private LinearLayout container;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            message = intent.getStringExtra("message");
        }
    };
    private String uniqueTopic = "sensor/test";

    public SearchResultFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_result, container, false);

        //Listen to message
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessageReceiver, new IntentFilter("MessageEvent"));
        contacts = new ArrayList<>();

        user = new User();

        //Share preferences
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = pref.edit();

        if (pref != null) {
            user.setUid(pref.getInt("uid", 0));
            user.setNickname(pref.getString("nickname", null));
            user.setUsername(pref.getString("username", null));
            user.setPassword(pref.getString("password", null));
            user.setGender(pref.getInt("gender", 0));
            user.setBirth_year(pref.getInt("birth_year", 0));
            user.setBirth_month(pref.getInt("birth_month", 0));
            user.setBirth_day(pref.getInt("birth_day", 0));
            user.setEmail(pref.getString("email", null));
            user.setPhone_number(pref.getString("phone_number", null));
            user.setStatus(pref.getString("status", null));
            user.setAddress(pref.getString("address", null));
            user.setState(pref.getString("state", null));
            user.setTown(pref.getString("town", null));
            user.setPostal_code(pref.getString("postal_code", null));
            user.setCountry(pref.getString("country", null));
            user.setStudent_id(pref.getString("student_id", null));
            user.setFaculty(pref.getString("faculty", null));
            user.setCourse(pref.getString("course", null));
            user.setTutorial_group(pref.getInt("tutorial_group", 0));
            user.setIntake(pref.getString("intake", null));
            user.setAcademic_year(pref.getInt("academic_year", 0));
        }

        //Initialize Views
        ibBack = rootView.findViewById(R.id.btn_searchresult_back);
        ibSearch = rootView.findViewById(R.id.imageView_searchresult_search);
        listViewResult = rootView.findViewById(R.id.listView_searchresult);
        etTitle = rootView.findViewById(R.id.editText_searchresult_title);
        tvTitle = rootView.findViewById(R.id.textView_searchresult_title);
        tvMessage = rootView.findViewById(R.id.textView_searchresult_message);
        progressBar = rootView.findViewById(R.id.progressBar_searchresult);
        progressBar.setVisibility(View.INVISIBLE);
        container = rootView.findViewById(R.id.searchresult_actionbar_editable);

        final Animation onClickAnimation = new AlphaAnimation(0.3f, 1.0f);
        onClickAnimation.setDuration(1000);

        //Retrive bundle
        if (getArguments() != null) {
            int type = getArguments().getInt("TYPE");
            switch (type) {
                case SEARCH_BY_NAME: {
                    searchString = getArguments().getString("SEARCH_STRING");
                    tvTitle.setVisibility(View.GONE);
                    etTitle.setText(searchString);
                    runSearchByNameTask(searchString);
                    break;
                }

                case SEARCH_NEARBY: {
                    container.setVisibility(View.GONE);
                    tvTitle.setText(getArguments().getString("TITLE"));
                    break;
                }
                case SEARCH_REC: {
                    container.setVisibility(View.GONE);
                    tvTitle.setText(getArguments().getString("TITLE"));
                    runSearchRecommendedFriends(String.valueOf(user.getUid()));
                }
            }
        }

        etTitle.addTextChangedListener(new TextWatcher() {
            private final long DELAY = 1000;
            Handler handler = new Handler(Looper.getMainLooper());
            Runnable workRunnable;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                handler.removeCallbacks(workRunnable);
                workRunnable = new Runnable() {
                    @Override
                    public void run() {
                        runSearchByNameTask(etTitle.getText().toString());
                    }
                };
                handler.postDelayed(workRunnable, DELAY);
            }
        });

        etTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    //Hide IME
                    etTitle.clearFocus();
                    InputMethodManager inputManager = (InputMethodManager)
                            getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.toggleSoftInput(0, 0);

                    ibSearch.performClick();
                }
                return false;
            }
        });

        ibSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(onClickAnimation);

                runSearchByNameTask(etTitle.getText().toString());
            }
        });

        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(onClickAnimation);

                getActivity().onBackPressed();
            }
        });

        return rootView;
    }


    private void runSearchByNameTask(String string) {
        final SearchByName task = new SearchByName(string);
        task.execute();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (task.getStatus() == AsyncTask.Status.RUNNING) {
                    task.cancel(true);
                    progressBar.setVisibility(View.GONE);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    tvMessage.setVisibility(View.VISIBLE);
                    tvMessage.setText("Connection Timeout.");
                    tvMessage.bringToFront();
                }
            }
        }, TASK_TIMEOUT);
    }

    private void runSearchRecommendedFriends(String uid) {
        final SearchRecommendFriend task = new SearchRecommendFriend(uid);
        task.execute();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (task.getStatus() == AsyncTask.Status.RUNNING) {
                    task.cancel(true);
                    progressBar.setVisibility(View.GONE);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    tvMessage.setVisibility(View.VISIBLE);
                    tvMessage.setText("Connection Timeout.");
                    tvMessage.bringToFront();
                }
            }
        }, TASK_TIMEOUT);
    }

    private void refreshList(int listType) {
        adapter = new CustomAdapter(contacts, 0, getActivity(), listType);
        listViewResult.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in), 0.5f));
        listViewResult.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView tvName;
        TextView tvBottom;
        //ImageView info;
    }

    private class SearchRecommendFriend extends AsyncTask<Void, Void, Integer> {

        private MqttMessageHandler handler = new MqttMessageHandler();
        private String id;

        private SearchRecommendFriend(String id) {
            this.id = id;
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
            handler.encode(MqttMessageHandler.MqttCommand.REQ_REC_FRIENDS, this.id);
            MqttHelper.publish(uniqueTopic, handler.getPublish());
            contacts.clear();
            refreshList(SEARCH_REC);
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
                        if (handler.mqttCommand == MqttMessageHandler.MqttCommand.ACK_REC_FRIENDS) {
                            contacts = handler.getRecommendedFriends();
                            if (contacts.size() > 0) {
                                return 1;
                            } else
                                return 0;
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
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            if (integer == 0) {
                tvMessage.setText("No similar friends");
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.bringToFront();
            } else if (integer == 1) {
                refreshList(SEARCH_REC);
            }
        }
    }

    private class SearchByName extends AsyncTask<Void, Void, Integer> {

        private MqttMessageHandler handler = new MqttMessageHandler();
        private String searchString;

        private SearchByName(String search) {
            this.searchString = search;
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
            handler.encode(MqttMessageHandler.MqttCommand.REQ_SEARCH_USER, searchString);
            MqttHelper.publish(uniqueTopic, handler.getPublish());
            contacts.clear();
            refreshList(SEARCH_BY_NAME);
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
                        if (handler.mqttCommand == MqttMessageHandler.MqttCommand.ACK_SEARCH_USER) {
                            contacts = handler.getSearchResultByName();
                            if (contacts.size() > 0) {
                                return 1;
                            } else
                                return 0;
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
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            if (integer == 0) {
                tvMessage.setText("No user matchings \"" + searchString + "\".");
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.bringToFront();
            } else if (integer == 1) {
                refreshList(SEARCH_BY_NAME);
            }
        }
    }

    private class CustomAdapter extends ArrayAdapter<Contact> {
        int listType;
        private Context mContext;

        public CustomAdapter(ArrayList<Contact> contacts, int resources, Context context, int listType) {
            super(context, resources, contacts);
            this.mContext = context;
            this.listType = listType;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            Contact contact = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.contact_frame, parent, false);
                viewHolder.tvName = convertView.findViewById(R.id.contact_frame_name);
                viewHolder.tvBottom = convertView.findViewById(R.id.contact_frame_bottomlayer);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            switch (listType) {
                case SEARCH_BY_NAME: {
                    viewHolder.tvName.setText(contact.getNickname());
                    viewHolder.tvBottom.setText(contact.getStatus());
                    break;
                }
                case SEARCH_NEARBY: {
                    break;
                }
                case SEARCH_REC: {
                    viewHolder.tvName.setText(contact.getNickname());
                    viewHolder.tvBottom.setText(contact.getEdges() + " mutual friend(s).");
                    break;
                }
                default: {
                    break;
                }
            }
            return convertView;
        }
    }
}
