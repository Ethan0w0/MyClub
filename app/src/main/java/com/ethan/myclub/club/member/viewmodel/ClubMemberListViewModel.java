package com.ethan.myclub.club.member.viewmodel;

import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.ethan.myclub.R;
import com.ethan.myclub.activity.detail.view.ActivityDetailActivity;
import com.ethan.myclub.activity.edit.view.ActivityEditActivity;
import com.ethan.myclub.club.activitylist.view.ClubActivityListActivity;
import com.ethan.myclub.club.member.adapter.MemberAdapter;
import com.ethan.myclub.club.member.view.ClubMemberListActivity;
import com.ethan.myclub.club.model.MemberResult;
import com.ethan.myclub.club.my.model.MyClub;
import com.ethan.myclub.club.my.view.EmptyView;
import com.ethan.myclub.databinding.ActivityClubMemberListBinding;
import com.ethan.myclub.discover.activity.adapter.ActivityAdapter;
import com.ethan.myclub.discover.activity.model.ActivityResult;
import com.ethan.myclub.main.BaseActivity;
import com.ethan.myclub.network.ApiHelper;
import com.ethan.myclub.user.detail.view.UserDetailActivity;
import com.ethan.myclub.user.detail.viewmodel.UserDetailViewModel;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class ClubMemberListViewModel {

    private ClubMemberListActivity mActivity;
    private ActivityClubMemberListBinding mBinding;
    private MyClub mMyClub;
    private final EmptyView mEmptyView;
    private MemberAdapter mAdapter;

    public ClubMemberListViewModel(ClubMemberListActivity activity, ActivityClubMemberListBinding binding, MyClub myClub) {
        mActivity = activity;
        mBinding = binding;
        mBinding.setViewModel(this);
        mMyClub = myClub;
        mActivity.getToolbarWrapper()
                .setTitle("社团通讯录")
                .showBackIcon()
                .show();
        mEmptyView = new EmptyView(mActivity);

        mBinding.swipeLayout.setColorSchemeResources(R.color.colorAccent);
        mBinding.swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                update();
            }
        });

        if (myClub.isCreator)
            mAdapter = new MemberAdapter(R.layout.item_club_member_creator, null, mActivity, myClub, this);
        else
            mAdapter = new MemberAdapter(R.layout.item_club_member, null, mActivity, myClub, this);
        mAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);

        mBinding.list.setLayoutManager(new LinearLayoutManager(mActivity));
        mBinding.list.setAdapter(mAdapter);
        update();

    }

    public void update() {
        ApiHelper.getProxyWithoutToken(mActivity)
                .getClubMemberList(String.valueOf(mMyClub.clubId))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<MemberResult>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mBinding.swipeLayout.setRefreshing(true);
                    }

                    @Override
                    public void onNext(List<MemberResult> memberResults) {

                        if (memberResults == null || memberResults.size() == 0) {
                            mEmptyView.showErrorView(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    update();
                                }
                            });
                            mAdapter.setNewData(null);
                            mBinding.list.setLayoutFrozen(true);
                            mAdapter.setEmptyView(mEmptyView);
                        } else {
                            mBinding.list.setLayoutFrozen(false);
                            mAdapter.setNewData(memberResults);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mBinding.swipeLayout.setRefreshing(false);
                        mEmptyView.showErrorView(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                update();
                            }
                        });
                        mAdapter.setNewData(null);
                        mBinding.list.setLayoutFrozen(true);
                        mAdapter.setEmptyView(mEmptyView);
                        mActivity.showSnackbar("错误：" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        mBinding.swipeLayout.setRefreshing(false);
                    }
                });
    }
}