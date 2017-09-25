package my.edu.tarc.kusm_wa14student.communechat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.*;
import org.w3c.dom.Text;

import java.lang.reflect.Field;

import my.edu.tarc.kusm_wa14student.communechat.assets.MqttHelper;
import my.edu.tarc.kusm_wa14student.communechat.assets.ViewPagerAdapter;
import my.edu.tarc.kusm_wa14student.communechat.fragments.ChatFragment;
import my.edu.tarc.kusm_wa14student.communechat.fragments.ContactFragment;
import my.edu.tarc.kusm_wa14student.communechat.fragments.SearchFragment;
import my.edu.tarc.kusm_wa14student.communechat.fragments.UserFragment;

public class MainActivity extends AppCompatActivity {
    private MqttHelper mqttHelper;

    private ViewPager viewPager;
    private BottomNavigationView bottomNavView;

    private ContactFragment contactFragment;
    private SearchFragment searchFragment;
    private UserFragment userFragment;
    private ChatFragment chatFragment;

    private MenuItem prevMenuItem;
    private SelectedBundle selectedBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize views
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        bottomNavView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.removeShiftMode(bottomNavView);

        bottomNavView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
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

       /*  //Disable ViewPager Swipe
       viewPager.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return true;
            }
        });
        */
        setupViewPager(viewPager);


        startMqtt();
    }
    private void startMqtt() {
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Debug",mqttMessage.toString());
                Bundle bundle = new Bundle();
                bundle.putString("message",mqttMessage.toString());
                selectedBundle.onBundleSelect(bundle);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        contactFragment =new ContactFragment();
        chatFragment = new ChatFragment();
        searchFragment= new SearchFragment();
        userFragment = new UserFragment();
        adapter.addFragment(contactFragment);
        adapter.addFragment(chatFragment);
        adapter.addFragment(searchFragment);
        adapter.addFragment(userFragment);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //selectedBundle.onBundleSelect(bundle);
    }
    public void setOnBundleSelected(SelectedBundle selectedBundle) {
        this.selectedBundle = selectedBundle;
    }
    public interface SelectedBundle {
        void onBundleSelect(Bundle bundle);
    }
}
