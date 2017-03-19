package com.ethan.myclub.user;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ethan.myclub.R;
import com.ethan.myclub.global.Preferences;
import com.ethan.myclub.main.BaseFragment;
import com.ethan.myclub.main.SnackbarActivity;
import com.ethan.myclub.network.ApiHelper;
import com.ethan.myclub.user.info.InfoActivity;
import com.ethan.myclub.user.login.view.LoginActivity;
import com.ethan.myclub.user.schedule.ScheduleActivity;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


public class UserFragment extends BaseFragment {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private AvatarImageView mIvAvatar;

    public UserFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user, container, false);

        mIvAvatar = (AvatarImageView) view.findViewById(R.id.iv_avatar);

        View btnTimeManagement = view.findViewById(R.id.timeManagement);
        btnTimeManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), ScheduleActivity.class);
                startActivity(intent);
            }
        });

        // 登录按钮
        View btnLogin = view.findViewById(R.id.tv_loginBtn);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                getActivity().startActivityForResult(intent, SnackbarActivity.REQUEST_LOGIN);
            }
        });

        // 个人信息按钮
        View btnInfo = view.findViewById(R.id.basicInfo);
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), InfoActivity.class);

                ActivityOptionsCompat options = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(getActivity(),
                                Pair.create((View) mIvAvatar, "trans_iv_avatar"));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    startActivity(intent, options.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        });
        View btnTest = view.findViewById(R.id.settings);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApiHelper.getProxy((SnackbarActivity) getActivity())
                        .test()
                        .subscribe(new Observer<Object>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(Object o) {
                                Log.e("成功", "accept: " +o);

                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("失败", "accept: ", e);
                            }

                            @Override
                            public void onComplete() {
                                Log.e("完成", "accept: ");
                            }
                        });
            }
        });

        View btnTest2 = view.findViewById(R.id.myCollection);
        btnTest2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Preferences.setToken(getActivity(),null);
            }
        });


        return view;
    }

    @Override
    protected void setFragmentContainer() {
        View view = getView();
        if (view != null)
            mFragmentContainer = (ViewGroup) view.findViewById(R.id.fragment_container);
    }
}
