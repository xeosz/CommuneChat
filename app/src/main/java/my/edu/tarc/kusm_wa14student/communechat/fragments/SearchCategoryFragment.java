package my.edu.tarc.kusm_wa14student.communechat.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import java.util.ArrayList;

import my.edu.tarc.kusm_wa14student.communechat.R;
import my.edu.tarc.kusm_wa14student.communechat.internal.MqttHelper;
import my.edu.tarc.kusm_wa14student.communechat.internal.MqttMessageHandler;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchCategoryFragment extends Fragment {

    //Reusable fragment
    //Search by criteria flow : Faculty > Year > Session/Intake > Courses > Group
    private static final int SEARCH_CATEGORY = 1;
    private static final int SEARCH_YEAR = 2;
    private static final int SEARCH_SESSION = 3;
    private static final int SEARCH_COURSES = 4;
    private static final int SEARCH_GROUP = 5;
    private static final int SEARCH_CATEGORY_RESULT = 6;
    private static final long TASK_TIMEOUT = 6000;
    private static final String TITLE_KEY = "TITLE";
    private static final String TYPE_KEY = "TYPE";
    private static final String DATA_KEY = "DATA";
    private static final int SEARCH_CATEGORY_SIZE = 5;

    //Views
    private TextView tvTitle, tvMessage, tvSelect;
    private ProgressBar progressBar;
    private ImageButton ibBack;
    private ListView lvResult;
    private Button btn;

    //Var
    private int type;
    private String title;
    private String message = "";
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            message = intent.getStringExtra("message");
        }
    };
    private CustomAdapter adapter;
    private ArrayList<String> resultList;
    private String[] strings;


    public SearchCategoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_category, container, false);

        resultList = new ArrayList<>();
        strings = new String[SEARCH_CATEGORY_SIZE];

        //Get passed arguments
        if (!getArguments().isEmpty()) {
            title = getArguments().getString(TITLE_KEY);
            type = getArguments().getInt(TYPE_KEY, 0);
        }
        if (getArguments().containsKey(DATA_KEY)) {
            strings = getArguments().getStringArray(DATA_KEY);
        }

        //Listen to message
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessageReceiver, new IntentFilter("MessageEvent"));

        //Initialize views
        tvTitle = rootView.findViewById(R.id.textView_search_category_title);
        tvMessage = rootView.findViewById(R.id.textView_search_category_message);
        tvSelect = rootView.findViewById(R.id.textView_search_category_select_title);
        progressBar = rootView.findViewById(R.id.progressBar_search_category);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#8f1ffc"), android.graphics.PorterDuff.Mode.MULTIPLY);
        ibBack = rootView.findViewById(R.id.btn_search_category_back);
        lvResult = rootView.findViewById(R.id.listView_search_category);

        final Animation onClickAnimation = new AlphaAnimation(0.3f, 1.0f);
        onClickAnimation.setDuration(1000);

        tvTitle.setText(title);
        tvTitle.setSelected(true);
        runLoadingTask(type, strings);

        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(onClickAnimation);
                getActivity().onBackPressed();
            }
        });

        lvResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                view.startAnimation(onClickAnimation);

                String tempTitle = (String) lvResult.getItemAtPosition(i);
                proceed((String) lvResult.getItemAtPosition(i), type);
            }
        });

        return rootView;
    }

    private void runLoadingTask(int type, String[] data) {
        final LoadingTask task = new LoadingTask(type);
        if (data != null) {
            task.put(data);
        }
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


    private void renderView() {
        if (!resultList.isEmpty() && getActivity() != null) {
            adapter = new CustomAdapter(resultList, 0, getActivity());
            lvResult.setLayoutAnimation(new LayoutAnimationController(AnimationUtils.loadAnimation(getActivity(),
                    android.R.anim.fade_in), 0.5f));
            lvResult.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            switch (type) {
                case SEARCH_CATEGORY: {
                    tvSelect.setText("Faculty :");
                    break;
                }
                case SEARCH_YEAR: {
                    tvSelect.setText("Year :");
                    break;
                }
                case SEARCH_SESSION: {
                    tvSelect.setText("Session :");
                    break;
                }
                case SEARCH_COURSES: {
                    tvSelect.setText("Course Code :");
                    break;
                }
                case SEARCH_GROUP: {
                    tvSelect.setText("Tutorial Group :");
                    break;
                }
            }

        } else {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText("No records");
            tvMessage.bringToFront();
        }
    }

    private void proceed(String title, int type) {
        Bundle args = new Bundle();
        this.title = "";
        switch (type) {
            case SEARCH_CATEGORY: {
                strings[0] = title;
                strings[1] = null;
                strings[2] = null;
                strings[3] = null;
                strings[4] = null;
                args.putInt(TYPE_KEY, SEARCH_YEAR);
                break;
            }
            case SEARCH_YEAR: {
                strings[1] = title;
                strings[2] = null;
                strings[3] = null;
                strings[4] = null;
                args.putInt(TYPE_KEY, SEARCH_SESSION);
                break;
            }
            case SEARCH_SESSION: {
                strings[2] = title;
                strings[3] = null;
                strings[4] = null;
                args.putInt(TYPE_KEY, SEARCH_COURSES);
                break;
            }
            case SEARCH_COURSES: {
                strings[3] = title;
                strings[4] = null;
                args.putInt(TYPE_KEY, SEARCH_GROUP);
                break;
            }
            case SEARCH_GROUP: {
                strings[4] = title;
                args.putInt(TYPE_KEY, SEARCH_CATEGORY_RESULT);

                for (String s : strings) {
                    if (s != null)
                        this.title += "/" + s;
                }

                args.putString(TITLE_KEY, this.title);
                args.putStringArray(DATA_KEY, strings);

                Fragment fragment = new SearchResultFragment();
                fragment.setArguments(args);

                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_from_right_to_mid, R.anim.slide_right, R.anim.slide_from_right_to_mid, R.anim.slide_right);
                ft.add(R.id.main_frame, fragment)
                        .addToBackStack("search_result_fragment")
                        .commit();

                return; //stop further code in this method.
            }
        }
        for (String s : strings) {
            if (s != null)
                this.title += "/" + s;
        }
        args.putString(TITLE_KEY, this.title);
        args.putStringArray(DATA_KEY, strings);

        Fragment fragment = new SearchCategoryFragment();
        fragment.setArguments(args);

        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_from_right_to_mid, R.anim.slide_right, R.anim.slide_from_right_to_mid, R.anim.slide_right);
        ft.add(R.id.main_frame, fragment)
                .addToBackStack("search_result_fragment")
                .commit();
    }

    private static class ViewHolder {
        TextView tvTitle;
        //ImageView info;
    }

    private class LoadingTask extends AsyncTask<Void, Void, Integer> {
        private int type;
        private String[] data;
        private MqttMessageHandler handler = new MqttMessageHandler();

        public LoadingTask(int type) {
            this.type = type;
        }

        public void put(String[] data) {
            this.data = data;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            progressBar.bringToFront();
            tvMessage.setVisibility(View.GONE);
            switch (this.type) {
                case SEARCH_CATEGORY: {
                    handler.encode(MqttMessageHandler.MqttCommand.REQ_SEARCH_CATEGORY_FACULTY, null);
                    break;
                }
                case SEARCH_YEAR: {
                    handler.encode(MqttMessageHandler.MqttCommand.REQ_SEARCH_CATEGORY_YEAR, data);
                    break;
                }
                case SEARCH_SESSION: {
                    handler.encode(MqttMessageHandler.MqttCommand.REQ_SEARCH_CATEGORY_SESSION, data);
                    break;
                }
                case SEARCH_COURSES: {
                    handler.encode(MqttMessageHandler.MqttCommand.REQ_SEARCH_CATEGORY_COURSES, data);
                    break;
                }
                case SEARCH_GROUP: {
                    handler.encode(MqttMessageHandler.MqttCommand.REQ_SEARCH_CATEGORY_GROUP, data);
                    break;
                }
            }
            MqttHelper.publish(MqttHelper.getPublishTopic(), handler.getPublish());
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
                        switch (this.type) {
                            case SEARCH_CATEGORY: {
                                resultList = handler.getFaculties();
                                result = 1;
                                break;
                            }
                            case SEARCH_YEAR: {
                                resultList = handler.getYears();
                                result = 1;
                                break;
                            }
                            case SEARCH_SESSION: {
                                resultList = handler.getSessions();
                                result = 1;
                                break;
                            }
                            case SEARCH_COURSES: {
                                resultList = handler.getCourses();
                                result = 1;
                                break;
                            }
                            case SEARCH_GROUP: {
                                resultList = handler.getGroups();
                                result = 1;
                                break;
                            }
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
            if (integer == 1) {
                renderView();
            }
        }
    }

    private class CustomAdapter extends ArrayAdapter<String> {
        private Context mContext;

        public CustomAdapter(ArrayList<String> list, int resources, Context context) {
            super(context, resources, list);
            this.mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            String data = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_frame, parent, false);
                viewHolder.tvTitle = convertView.findViewById(R.id.textView_list_item_frame);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (type == SEARCH_GROUP) {
                viewHolder.tvTitle.setText("Group " + Integer.parseInt(data));
            } else {
            viewHolder.tvTitle.setText(data);
            }

            return convertView;
        }
    }
}
