package my.edu.tarc.kusm_wa14student.communechat.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import my.edu.tarc.kusm_wa14student.communechat.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchCategoryFragment extends Fragment {

    //Reusable fragment
    //Search by criteria flow : Faculty > Year > Session/Intake > Courses > Group
    private static final int SEARCH_CATEGORY = 0;
    private static final int SEARCH_YEAR = 1;
    private static final int SEARCH_SESSION = 2;
    private static final int SEARCH_COURSES = 3;
    private static final int SEARCH_GROUP = 4;

    //Views
    private TextView tvTitle, tvMessage;
    private ProgressBar progressBar;
    private ImageButton ibBack;
    private ListView lvResult;
    private Button btn;

    //Var
    private int type;
    private String title;


    public SearchCategoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_category, container, false);

        //Get passed arguments
        if (!getArguments().isEmpty()) {
            title = getArguments().getString("TITLE");
            type = getArguments().getInt("TYPE", 0);
        }
        //Initialize views
        tvTitle = rootView.findViewById(R.id.textView_search_category_title);
        tvMessage = rootView.findViewById(R.id.textView_search_category_message);
        progressBar = rootView.findViewById(R.id.progressBar_search_category);
        ibBack = rootView.findViewById(R.id.btn_search_category_back);
        lvResult = rootView.findViewById(R.id.listView_search_category);
        btn = rootView.findViewById(R.id.button3);

        final Animation onClickAnimation = new AlphaAnimation(0.3f, 1.0f);
        onClickAnimation.setDuration(1000);

        tvTitle.setText(title + type);

        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(onClickAnimation);
                getActivity().onBackPressed();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(onClickAnimation);

                //Inflate new fragment according to flow
                proceed(title, type);
            }
        });

        return rootView;
    }


    private void proceed(String title, int type) {
        Bundle args = new Bundle();

        switch (type) {
            case SEARCH_CATEGORY: {
                args.putString("TITLE", title);
                args.putInt("TYPE", SEARCH_YEAR);
                break;
            }
            case SEARCH_YEAR: {
                args.putString("TITLE", title);
                args.putInt("TYPE", SEARCH_SESSION);
                break;
            }
            case SEARCH_SESSION: {
                args.putString("TITLE", title);
                args.putInt("TYPE", SEARCH_COURSES);
                break;
            }
            case SEARCH_COURSES: {
                args.putString("TITLE", title);
                args.putInt("TYPE", SEARCH_GROUP);
                break;
            }
            case SEARCH_GROUP: {
                args.putString("TITLE", title);
                args.putInt("TYPE", SEARCH_GROUP);
                break;
            }
        }
        Fragment fragment = new SearchCategoryFragment();
        fragment.setArguments(args);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_left, R.anim.slide_right, R.anim.slide_left, R.anim.slide_right);
        ft.add(R.id.main_frame, fragment)
                .addToBackStack("search_result_fragment")
                .commit();
    }

}
