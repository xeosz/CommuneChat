package my.edu.tarc.kusm_wa14student.communechat.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import my.edu.tarc.kusm_wa14student.communechat.LoginActivity;
import my.edu.tarc.kusm_wa14student.communechat.R;
import my.edu.tarc.kusm_wa14student.communechat.internal.ContactDBHandler;
import my.edu.tarc.kusm_wa14student.communechat.internal.MessageService;
import my.edu.tarc.kusm_wa14student.communechat.model.User;

public class UserTabFragment extends Fragment {

    private static final int TYPE_MALE = 1;
    private static final int TYPE_FEMALE = 2;
    //Views
    private TextView tv_Username, tv_Nickname, tv_Status, tv_ContactNum, tv_Logout;
    private ImageView iv_Gender;

    private User user;
    private SharedPreferences pref;
    private ContactDBHandler db;

    public UserTabFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_tab, container, false);

        user = new User();

        //Shared preferences
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        //!Redundant code, shall retrieve what is needed.
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

        //Initialize views
        tv_Username = rootView.findViewById(R.id.textView_userfrag_username);
        tv_Nickname = rootView.findViewById(R.id.textView_userfrag_nickname);
        tv_Status = rootView.findViewById(R.id.textView_userfrag_status_content);
        iv_Gender = rootView.findViewById(R.id.imageView_userfrag_gender);
        tv_ContactNum = rootView.findViewById(R.id.textView_userfrag_number_of_friends);
        tv_Logout = rootView.findViewById(R.id.textView_userfrag_logout);

        renderView();

        tv_Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContextThemeWrapper ctw = new ContextThemeWrapper(getActivity(), R.style.AlertDialogStyle);
                new AlertDialog.Builder(ctw)
                        .setTitle("Log Out")
                        .setMessage("Do you really want to log out?")
                        .setIcon(android.R.drawable.ic_lock_power_off)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (pref.edit().clear().commit()) {
                                    resetApplication();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        return rootView;
    }

    private void renderView() {
        tv_Username.setText("ID: " + user.getUsername());
        tv_Nickname.setText(user.getNickname());
        tv_Status.setText(user.getStatus());
        db = new ContactDBHandler(getContext());

        tv_ContactNum.setText("" + db.getContactsCount(db.TABLE_CONTACTS));

        if (user.getGender() == TYPE_MALE) {
            iv_Gender.setImageDrawable(getResources().getDrawable(R.drawable.ic_boys));
        }
        if (user.getGender() == TYPE_FEMALE) {
            iv_Gender.setImageDrawable(getResources().getDrawable(R.drawable.ic_girls));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        renderView();
    }

    private void resetApplication() {
        //Clear Shared preferences and Database records
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        getActivity().stopService(new Intent(getActivity(), MessageService.class));
        getActivity().finish();
        ContactDBHandler db = new ContactDBHandler(getActivity().getApplicationContext());
        db.clearDatabase();
        startActivity(intent);
    }


}
