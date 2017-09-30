package my.edu.tarc.kusm_wa14student.communechat;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import java.lang.reflect.Field;

import my.edu.tarc.kusm_wa14student.communechat.adapter.ViewPagerAdapter;
import my.edu.tarc.kusm_wa14student.communechat.fragments.ChatFragment;
import my.edu.tarc.kusm_wa14student.communechat.fragments.ContactFragment;
import my.edu.tarc.kusm_wa14student.communechat.fragments.SearchFragment;
import my.edu.tarc.kusm_wa14student.communechat.fragments.UserFragment;
import my.edu.tarc.kusm_wa14student.communechat.internal.MessageService;
import my.edu.tarc.kusm_wa14student.communechat.internal.MqttHelper;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    //CustomViewPagerAdapter Variables;
    private ViewPagerAdapter adapter;
    private int NUMBER_OF_SCREENS = 4;
    private MenuItem prevMenuItem;

    //MainActivity Views
    private ViewPager viewPager;
    private BottomNavigationView bottomNavView;

    //Viewpager's Fragments
    private ContactFragment contactFragment;
    private SearchFragment searchFragment;
    private UserFragment userFragment;
    private ChatFragment chatFragment;

    private BroadcastReceiver mMessageReceiver;

    //Static Mqtt Connection Variables
    private MqttHelper mqttHelper;
    private String clientId = "1000000000";
    private String serverUri = "tcp://m11.cloudmqtt.com:17391";
    private String mqttUsername = "ehvfrtgx";
    private String mqttPassword = "YPcMC08pYYpr";
    private String clientTopic = "sensor/test";
    private int QoS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Start service
        startService(new Intent(MainActivity.this, MessageService.class));

        //Initialize views
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        bottomNavView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        contactFragment = new ContactFragment();
        chatFragment = new ChatFragment();
        searchFragment = new SearchFragment();
        userFragment = new UserFragment();

        adapter.addFragment(contactFragment);
        adapter.addFragment(chatFragment);
        adapter.addFragment(searchFragment);
        adapter.addFragment(userFragment);



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

        viewPager.setAdapter(adapter);
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
