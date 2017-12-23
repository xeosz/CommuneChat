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
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import my.edu.tarc.kusm_wa14student.communechat.ProfileActivity;
import my.edu.tarc.kusm_wa14student.communechat.R;
import my.edu.tarc.kusm_wa14student.communechat.internal.MqttHelper;
import my.edu.tarc.kusm_wa14student.communechat.internal.MqttMessageHandler;
import my.edu.tarc.kusm_wa14student.communechat.model.Contact;
import my.edu.tarc.kusm_wa14student.communechat.model.User;

public class SearchResultFragment extends Fragment {

    //Beware of the static identifier
    //As it may be used in other fragments
    private static final int SEARCH_BY_NAME = 1;
    private static final int SEARCH_NEARBY = 3;
    private static final int SEARCH_REC = 4;
    private static final int SEARCH_CATEGORY_RESULT = 6; //Must be same as defined in SearchCategoryFragment

    private static final String TITLE_KEY = "TITLE";
    private static final String TYPE_KEY = "TYPE";
    private static final String DATA_KEY = "DATA";
    private static final int SEARCH_CATEGORY_SIZE = 5;

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
    private int type;

    private String message = "";
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            message = intent.getStringExtra("message");
        }
    };

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
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        editor = pref.edit();

        if (pref != null) {
            user.setUid(pref.getInt("uid", 0));
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
        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#8f1ffc"), android.graphics.PorterDuff.Mode.MULTIPLY);
        container = rootView.findViewById(R.id.searchresult_actionbar_editable);

        final Animation onClickAnimation = new AlphaAnimation(0.3f, 1.0f);
        onClickAnimation.setDuration(1000);

        //Retrive bundle
        if (getArguments() != null) {
            type = getArguments().getInt(TYPE_KEY);
            switch (type) {
                case SEARCH_BY_NAME: {
                    searchString = getArguments().getString(DATA_KEY);
                    tvTitle.setVisibility(View.GONE);
                    etTitle.setText(searchString);
                    runSearchByNameTask(searchString);
                    break;
                }

                case SEARCH_NEARBY: {
                    container.setVisibility(View.GONE);
                    tvTitle.setText(getArguments().getString(TITLE_KEY));
                    runSearchNearbyFriends(String.valueOf(user.getUid()));
                    break;
                }
                case SEARCH_REC: {
                    container.setVisibility(View.GONE);
                    tvTitle.setText(getArguments().getString(TITLE_KEY));
                    runSearchRecommendedFriends(String.valueOf(user.getUid()));
                    break;
                }
                case SEARCH_CATEGORY_RESULT: {
                    container.setVisibility(View.GONE);
                    tvTitle.setText(getArguments().getString(TITLE_KEY));
                    tvTitle.setSelected(true);
                    String[] strings;
                    if (getArguments().containsKey(DATA_KEY)) {
                        strings = getArguments().getStringArray(DATA_KEY);
                        runSearchCategoryResult(strings);
                    }
                    break;
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

        listViewResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                view.startAnimation(onClickAnimation);

                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                Contact tempContact = (Contact) listViewResult.getItemAtPosition(i);
                Bundle bundle = new Bundle();

                bundle.putString("FID", String.valueOf(tempContact.getUid()));

                intent.putExtras(bundle);
                getActivity().startActivity(intent);

                //Clear the nested fragments in Search Category
                if (type == SEARCH_CATEGORY_RESULT) {
                    getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            }
        });


        return rootView;
    }

    private void runSearchNearbyFriends(String s) {
        final LoadingTask task = new LoadingTask(s, SEARCH_NEARBY);
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

    private void runSearchByNameTask(String string) {
        final LoadingTask task = new LoadingTask(string, SEARCH_BY_NAME);
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

    private void runSearchRecommendedFriends(String uid) {
        final LoadingTask task = new LoadingTask(uid, SEARCH_REC);
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

    private void runSearchCategoryResult(String[] strings) {
        final LoadingTask task = new LoadingTask(SEARCH_CATEGORY_RESULT);
        task.put(strings);
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

    private void refreshList(int listType) {
        if (contacts.size() <= 0) {
            tvMessage.setText("No user matchings");
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.bringToFront();
        } else {
            adapter = new CustomAdapter(contacts, 0, getActivity(), listType);
            listViewResult.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in), 0.5f));
            listViewResult.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    private static class ViewHolder {
        TextView tvName;
        TextView tvBottom;
        //ImageView info;
    }

    private class LoadingTask extends AsyncTask<Void, Void, Integer> {

        private MqttMessageHandler handler = new MqttMessageHandler();
        private String searchString;
        private String[] searchStrings;
        private int type;

        private LoadingTask(String data, int type) {
            this.searchString = data;
            this.type = type;
        }

        private LoadingTask(int type) {
            this.type = type;
        }

        private void put(String[] strings) {
            this.searchStrings = strings;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            progressBar.bringToFront();
            tvMessage.setText("");
            tvMessage.setVisibility(View.INVISIBLE);

            if (this.type == SEARCH_BY_NAME) {
                handler.encode(MqttMessageHandler.MqttCommand.REQ_SEARCH_USER, searchString);
                MqttHelper.publish(MqttHelper.getPublishTopic(), handler.getPublish());
                contacts.clear();
            } else if (this.type == SEARCH_REC) {
                handler.encode(MqttMessageHandler.MqttCommand.REQ_RECOMMEND_FRIENDS, searchString);
                MqttHelper.publish(MqttHelper.getPublishTopic(), handler.getPublish());
                contacts.clear();
            } else if (this.type == SEARCH_NEARBY) {
                handler.encode(MqttMessageHandler.MqttCommand.REQ_NEARBY_FRIENDS, searchString);
                MqttHelper.publish(MqttHelper.getPublishTopic(), handler.getPublish());
                contacts.clear();
            } else if (this.type == SEARCH_CATEGORY_RESULT) {
                handler.encode(MqttMessageHandler.MqttCommand.REQ_SEARCH_CATEGORY_MEMBER, searchStrings);
                MqttHelper.publish(MqttHelper.getPublishTopic(), handler.getPublish());
                contacts.clear();
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
                        if (handler.mqttCommand == MqttMessageHandler.MqttCommand.ACK_SEARCH_USER) {
                            contacts = handler.getSearchResultByName();
                            return 1;
                        } else if (handler.mqttCommand == MqttMessageHandler.MqttCommand.ACK_RECOMMEND_FRIENDS) {
                            contacts = handler.getRecommendedFriends();
                            return 2;
                        } else if (handler.mqttCommand == MqttMessageHandler.MqttCommand.ACK_NEARBY_FRIENDS) {
                            contacts = handler.getNearbyFriends();
                            return 3;
                        } else if (handler.mqttCommand == MqttMessageHandler.MqttCommand.ACK_SEARCH_CATEGORY_MEMBER) {
                            contacts = handler.getStudents();
                            return 4;
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
            if (integer == 0) {
                tvMessage.setText("No user matchings");
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.bringToFront();
            } else if (integer == 1) {
                refreshList(SEARCH_BY_NAME);
            } else if (integer == 2) {
                refreshList(SEARCH_REC);
            } else if (integer == 3) {
                refreshList(SEARCH_NEARBY);
            } else if (integer == 4) {
                refreshList(SEARCH_CATEGORY_RESULT);
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
                    viewHolder.tvName.setText(contact.getNickname());
                    viewHolder.tvBottom.setText(contact.getDistance() + " metres");
                    break;
                }
                case SEARCH_REC: {
                    viewHolder.tvName.setText(contact.getNickname());
                    viewHolder.tvBottom.setText(contact.getEdges() + " mutual friend(s).");
                    break;
                }
                case SEARCH_CATEGORY_RESULT: {
                    viewHolder.tvName.setText(contact.getNickname());
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
