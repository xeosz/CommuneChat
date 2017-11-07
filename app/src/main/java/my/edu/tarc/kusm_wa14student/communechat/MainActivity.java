package my.edu.tarc.kusm_wa14student.communechat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.Date;

import my.edu.tarc.kusm_wa14student.communechat.adapter.ViewPagerAdapter;
import my.edu.tarc.kusm_wa14student.communechat.fragments.ChatTabFragment;
import my.edu.tarc.kusm_wa14student.communechat.fragments.ContactTabFragment;
import my.edu.tarc.kusm_wa14student.communechat.fragments.SearchResultFragment;
import my.edu.tarc.kusm_wa14student.communechat.fragments.SearchTabFragment;
import my.edu.tarc.kusm_wa14student.communechat.fragments.UserTabFragment;
import my.edu.tarc.kusm_wa14student.communechat.internal.GPSTracker;
import my.edu.tarc.kusm_wa14student.communechat.internal.MessageService;
import my.edu.tarc.kusm_wa14student.communechat.internal.MqttHelper;
import my.edu.tarc.kusm_wa14student.communechat.internal.MqttMessageHandler;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int LOCATION_REQUEST_CODE = 1;
    private static final long KEEPALIVE_INTERVAL = 30000; //ms

    //CustomViewPagerAdapter Variables;
    private ViewPagerAdapter adapter;
    private int NUMBER_OF_SCREENS = 4;
    private MenuItem prevMenuItem;

    //MainActivity Views
    private ViewPager viewPager;
    private BottomNavigationView bottomNavView;

    //Viewpager's Fragments
    private ContactTabFragment contactTabFragment;
    private SearchTabFragment searchTabFragment;
    private UserTabFragment userTabFragment;
    private ChatTabFragment chatTabFragment;

    //Vars
    private SharedPreferences pref;
    private GPSTracker gps;
    private DecimalFormat df = new DecimalFormat("#.000000");
    private Handler keepAliveHandler;
    private Runnable keepAliveTask = new Runnable() {
        @Override
        public void run() {
            updateLocation();
            keepAliveHandler.postDelayed(this, KEEPALIVE_INTERVAL);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Start service
        this.startService(new Intent(MainActivity.this, MessageService.class));

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //Initialize views
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        bottomNavView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        contactTabFragment = new ContactTabFragment();
        chatTabFragment = new ChatTabFragment();
        searchTabFragment = new SearchTabFragment();
        userTabFragment = new UserTabFragment();

        adapter.addFragment(contactTabFragment);
        adapter.addFragment(chatTabFragment);
        adapter.addFragment(searchTabFragment);
        adapter.addFragment(userTabFragment);

        BottomNavigationViewHelper.removeShiftMode(bottomNavView);

        viewPager.setOffscreenPageLimit(NUMBER_OF_SCREENS);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                }
                else
                {
                    bottomNavView.getMenu().getItem(0).setChecked(false);
                }
                Log.d("page", "onPageSelected: "+position);
                bottomNavView.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNavView.getMenu().getItem(position);
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
       /*//Disable ViewPager Swipe
       viewPager.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return true;
            }
        });
        */
        bottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.contact_tab:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.chat_tab:
                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.search_tab:
                        viewPager.setCurrentItem(2);
                        break;
                    case R.id.user_tab:
                        viewPager.setCurrentItem(3);
                        break;
                }
                return false;
            }
        });
        checkLocationPermission();
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        keepAliveHandler.removeCallbacks(keepAliveTask);
    }

    private void keepAliveUpdate() {
        keepAliveHandler = new Handler();
        updateLocation();
        keepAliveHandler.postDelayed(keepAliveTask, KEEPALIVE_INTERVAL);
    }

    private void updateLocation() {
        gps = new GPSTracker(MainActivity.this);
        if (gps.canGetLocation()) {
            String latitude = df.format(gps.getLatitude());
            String longitude = df.format(gps.getLongitude());
            String time = String.valueOf((new Date().getTime() / 1000));

            MqttMessageHandler mHandler = new MqttMessageHandler();
            mHandler.encode(MqttMessageHandler.MqttCommand.KEEP_ALIVE,
                    new String[]{
                            String.valueOf(pref.getInt("uid", 0)),
                            time,
                            latitude,
                            longitude});
            MqttHelper.publish(MqttHelper.getPublishTopic(), mHandler.getPublish());
        } else {
            gps.showSettingsAlert();
        }
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            keepAliveUpdate();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                keepAliveUpdate();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("search_result_fragment");
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (fragment != null && fragment instanceof SearchResultFragment) {
            ft.setCustomAnimations(R.anim.slide_up, R.anim.slide_left, R.anim.slide_up, R.anim.slide_left);
            ft.remove(fragment).commit();
        } else {
            super.onBackPressed();
        }
    }

    static class BottomNavigationViewHelper {

        static void removeShiftMode(BottomNavigationView view) {
            BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
            try {
                Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
                shiftingMode.setAccessible(true);
                shiftingMode.setBoolean(menuView, false);
                shiftingMode.setAccessible(false);
                for (int i = 0; i < menuView.getChildCount(); i++) {
                    BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                    item.setShiftingMode(false);
                    // set once again checked value, so view will be updated
                    item.setChecked(item.getItemData().isChecked());
                }
            } catch (NoSuchFieldException e) {
                Log.e("ERROR NO SUCH FIELD", "Unable to get shift mode field");
            } catch (IllegalAccessException e) {
                Log.e("ERROR ILLEGAL ALG", "Unable to change value of shift mode");
            }
        }
    }
}
