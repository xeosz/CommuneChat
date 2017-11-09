package my.edu.tarc.kusm_wa14student.communechat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import my.edu.tarc.kusm_wa14student.communechat.R;

public class SearchTabFragment extends Fragment {

    private static final int SEARCH_BY_NAME = 1;
    private static final int SEARCH_CATEGORY = 1;
    private static final int SEARCH_NEARBY = 3;
    private static final int SEARCH_REC = 4;
    //Views
    private EditText etSearch;
    private Button btnCategory, btnRecommend, btnNearby, btnChat, btnFriendRequest;


    public SearchTabFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_tab, container, false);

        //Initialize Views
        etSearch = rootView.findViewById(R.id.editText_search_box);
        btnCategory = rootView.findViewById(R.id.btn_search_category);
        btnRecommend = rootView.findViewById(R.id.btn_search_recommendation);
        btnNearby = rootView.findViewById(R.id.btn_search_nearby);
        btnChat = rootView.findViewById(R.id.btn_search_localchat);
        btnFriendRequest = rootView.findViewById(R.id.btn_search_friend_request);

        final Animation onClickAnimation = new AlphaAnimation(0.3f, 1.0f);
        onClickAnimation.setDuration(1000);

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                    //Hide IME keyboard
                    etSearch.clearFocus();
                    InputMethodManager inputManager = (InputMethodManager)
                            getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.toggleSoftInput(0, 0);

                    //Pass data into fragment
                    Bundle args = new Bundle();
                    args.putString("SEARCH_STRING", etSearch.getText().toString());
                    args.putInt("TYPE", SEARCH_BY_NAME);

                    Fragment fragment = new SearchResultFragment();
                    fragment.setArguments(args);

                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.setCustomAnimations(R.anim.slide_up, R.anim.down_from_top, R.anim.slide_up, R.anim.slide_left);
                    ft.add(R.id.main_frame, fragment)
                            .addToBackStack("search_result_fragment")
                            .commit();

                    return true;
                }
                return false;
            }
        });

        btnCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                view.startAnimation(onClickAnimation);

                Bundle args = new Bundle();
                args.putString("TITLE", "Search By Category");
                args.putInt("TYPE", SEARCH_CATEGORY);

                Fragment fragment = new SearchCategoryFragment();
                fragment.setArguments(args);

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_up, R.anim.down_from_top, R.anim.slide_up, R.anim.slide_left);
                ft.add(R.id.main_frame, fragment)
                        .addToBackStack("search_result_fragment")
                        .commit();
            }
        });

        btnFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(onClickAnimation);

                Bundle args = new Bundle();
                Fragment fragment = new FriendRequestFragment();

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_up, R.anim.down_from_top, R.anim.slide_up, R.anim.slide_left);
                ft.add(R.id.main_frame, fragment)
                        .addToBackStack("search_result_fragment")
                        .commit();
            }
        });

        btnNearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                view.startAnimation(onClickAnimation);

                Bundle args = new Bundle();
                args.putString("TITLE", "Nearby Friends");
                args.putInt("TYPE", SEARCH_NEARBY);

                Fragment fragment = new SearchResultFragment();
                fragment.setArguments(args);

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_up, R.anim.down_from_top, R.anim.slide_up, R.anim.slide_left);
                ft.add(R.id.main_frame, fragment)
                        .addToBackStack("search_result_fragment")
                        .commit();
            }
        });

        btnRecommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                view.startAnimation(onClickAnimation);

                Bundle args = new Bundle();
                args.putString("TITLE", "Suggested Friends");
                args.putInt("TYPE", SEARCH_REC);

                Fragment fragment = new SearchResultFragment();
                fragment.setArguments(args);

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_up, R.anim.down_from_top, R.anim.slide_up, R.anim.slide_left);
                ft.add(R.id.main_frame, fragment)
                        .addToBackStack("search_result_fragment")
                        .commit();
            }
        });


        return rootView;
    }

}
