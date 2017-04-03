package com.ethan.myclub.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;


import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.ethan.myclub.R;
import com.ethan.myclub.discover.main.DiscoverFragment;
import com.ethan.myclub.global.Preferences;
import com.ethan.myclub.user.login.view.RegisterActivity2;

public class MainActivity extends BaseActivity {

    public static final int REQUEST_ADD_CLUB = 10306;
    public static final int REQUEST_EDIT_INFO = 10307;

    private BaseFragment currentFragment;
    private BaseViewPagerAdapter adapter;
    private AHBottomNavigationAdapter navigationAdapter;
    // UI
    private AHBottomNavigationViewPager viewPager;

    private AHBottomNavigation bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String FRAGMENTS_TAG = "android:support:fragments";
            // remove掉保存的Fragment
            savedInstanceState.remove(FRAGMENTS_TAG);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }

    private void initUI() {
        bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);
        viewPager = (AHBottomNavigationViewPager) findViewById(R.id.view_pager);

        navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.bottom_navigation);
        navigationAdapter.setupWithBottomNavigation(bottomNavigation);

        bottomNavigation.setInactiveColor(ContextCompat.getColor(this, R.color.colorBottomNavigationInactive));
        bottomNavigation.setAccentColor(ContextCompat.getColor(this, R.color.colorAccent));

        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {

                if (currentFragment == null) {
                    currentFragment = adapter.getCurrentFragment();
                }

                if (wasSelected) {
                    currentFragment.refresh();
                    return true;
                }
                //切换到个人页面前
                if (position == 2) {
                    //检查是否已经登录
                    if (!Preferences.sIsLogin.get()) {
                        showLoginSnackbar("您还没有登录哦");
                        return false;
                    }
                }

                if (currentFragment != null) {
                    currentFragment.willBeHidden();
                }

                viewPager.setCurrentItem(position, false);
                currentFragment = adapter.getItem(position);
                currentFragment.willBeDisplayed();

//                if (position == 1) {
//                    bottomNavigation.setNotification("", 1);
//
//                }

                return true;
            }
        });
        viewPager.setOffscreenPageLimit(4);

        adapter = new BaseViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //如果登录/注册成功，刷新个人信息
        if (resultCode == RESULT_OK) {
            switch(requestCode){
                case REQUEST_REGISTER:
                case REQUEST_LOGIN:
                    if (viewPager.getCurrentItem() == 1) {
                        adapter.getItem(1).refresh();
                    }
                    if (viewPager.getCurrentItem() == 2) {
                        adapter.getItem(2).refresh();
                    }
                    break;
                case REQUEST_ADD_CLUB:
                    ((DiscoverFragment)adapter.getItem(0)).setCurrentTab(1);
                    bottomNavigation.setCurrentItem(0);
                    break;
                case REQUEST_EDIT_INFO:
                    adapter.getItem(2).refresh();
                    break;
            }

        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        onActivityResult(intent.getIntExtra("RequestCode", 0), intent.getIntExtra("ResultCode", 0), intent);
    }

    public static void startActivity(Activity activity, Bundle bundle) {
        Intent intent = new Intent(activity, MainActivity.class);
        ActivityCompat.startActivity(activity, intent, bundle);

    }

    public static void startActivity(Activity activity, int requestCode, int resultCode) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra("RequestCode", requestCode);
        intent.putExtra("ResultCode", resultCode);
        activity.startActivity(intent);


    }
}
