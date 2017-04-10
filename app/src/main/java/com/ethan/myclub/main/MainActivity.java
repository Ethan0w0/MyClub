package com.ethan.myclub.main;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;


import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.ethan.myclub.R;
import com.ethan.myclub.club.my.view.MyClubFragment;
import com.ethan.myclub.discover.main.DiscoverFragment;
import com.ethan.myclub.user.main.view.UserFragment;
public class MainActivity extends BaseActivity {

    public static final int REQUEST_ADD_CLUB = 10306;
    public static final int REQUEST_CREATE_CLUB = 10308;
    public static final int REQUEST_GIVE_CLUB = 10309;
    private static final String TAG = "MainActivity";

    private BaseFragment currentFragment;
    private BaseViewPagerAdapter adapter;
    private AHBottomNavigationAdapter navigationAdapter;
    // UI
    private AHBottomNavigationViewPager viewPager;

    public AHBottomNavigation bottomNavigation;

    static public class needUpdateFlag{
        static public boolean clubList = true;
        static public boolean userProfile = true;
        static public boolean userUnreadCount = true;
    }

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
                    if (!MyApplication.isLogin()) {
                        showLoginSnackbar("您还没有登录哦");
                        return false;
                    }
                }
                if (position == 1)
                    bottomNavigation.setBehaviorTranslationEnabled(false);
                else
                    bottomNavigation.setBehaviorTranslationEnabled(true);

                if (currentFragment != null) {
                    currentFragment.willBeHidden();
                }

                viewPager.setCurrentItem(position, false);
                currentFragment = adapter.getItem(position);
                currentFragment.willBeDisplayed();

                return true;
            }
        });
        viewPager.setOffscreenPageLimit(4);

        adapter = new BaseViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_ADD_CLUB:
                    ((DiscoverFragment) adapter.getItem(0)).setCurrentTab(1);
                    bottomNavigation.setCurrentItem(0);
                    break;
                case REQUEST_CREATE_CLUB:
                    showSnackbar("创建社团成功！");
                    break;
                case REQUEST_GIVE_CLUB:
                    showSnackbar("社团转让成功！您现在是管理员。");
                    break;
                case REQUEST_LOGOUT:
                    bottomNavigation.setCurrentItem(0);
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

    @Override
    protected void onResume() {
        super.onResume();
        getCache();
    }

    private void getCache() {
        UserFragment userFragment = (UserFragment) adapter.getItem(2);
        if (userFragment.mViewModel != null)
            userFragment.mViewModel.updateUserProfileAttempt();

        MyClubFragment clubFragment = (MyClubFragment) adapter.getItem(1);
        if (clubFragment.mViewModel != null)
            clubFragment.mViewModel.updateUserClubListAttempt();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }


}
