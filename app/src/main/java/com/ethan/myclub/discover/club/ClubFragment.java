package com.ethan.myclub.discover.club;

import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.ethan.myclub.club.detail.view.ClubInfoActivity;
import com.ethan.myclub.club.my.model.MyClub;
import com.ethan.myclub.discover.club.adapter.ClubAdapter;
import com.ethan.myclub.discover.club.model.ClubResult;
import com.ethan.myclub.discover.club.model.Hit;
import com.ethan.myclub.discover.main.TabFragment;
import com.ethan.myclub.main.BaseActivity;
import com.ethan.myclub.network.ApiHelper;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class ClubFragment extends TabFragment {

    private static final String TAG = "Discover Club";

    public ClubFragment() {
        mLayoutManager = new GridLayoutManager(getContext(), 2);
        mAdapter = new ClubAdapter(null);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                MyClub club = new MyClub();
                club.clubId = Integer.parseInt(((Hit) adapter.getItem(position)).id);
                ClubInfoActivity.start(getActivity(), club);
            }
        });
    }

    public void update(final int page, final int items) {
        ApiHelper.getProxyWithoutToken((BaseActivity) getActivity())
                .searchClub(mKeyWord, page, items)
                .delay(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ClubResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        //下拉刷新动画开始
                        if (page == 1)
                            mSwipeRefreshLayout.setRefreshing(true);
                    }

                    @Override
                    public void onNext(ClubResult clubResult) {
                        Log.i(TAG, "updateClubDetail: 获取ClubList完成");
                        //允许读取更多
                        mAdapter.setEnableLoadMore(true);
                        mCurrentPage++;
                        for (Hit hit : clubResult.hits.hits) {
                            hit.source.badge += "?imageView2/0/w/300/h/300";
                            hit.source.tagStr = "";
                            if (hit.source.tags == null || hit.source.tags.isEmpty())
                                hit.source.tagStr = "";
                            else {
                                for (String tag : hit.source.tags) {
                                    hit.source.tagStr += tag + " ";
                                }
                            }


                        }
                        if (page == 1) {
                            if (clubResult.hits.hits.size() == 0) {
                                mEmptyView.showEmptyView("还没有这个社团", "你可以创建这个社团哦！");
                                mAdapter.setNewData(null);
                                mRecyclerView.setLayoutFrozen(true);
                                mAdapter.setEmptyView(mEmptyView);
                            } else {
                                mRecyclerView.setLayoutFrozen(false);
                                mAdapter.setNewData(clubResult.hits.hits);
                            }
                        } else {
                            //允许下拉刷新
                            mSwipeRefreshLayout.setEnabled(true);
                            mAdapter.loadMoreComplete();
                            mAdapter.addData(clubResult.hits.hits);
                        }
                        if (clubResult.hits.hits.size() < 10) {
                            mIsNoMore = true;
                            mAdapter.loadMoreEnd();
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        //允许读取更多
                        mAdapter.setEnableLoadMore(true);
                        e.printStackTrace();
                        Log.i(TAG, "updateClubDetail: 获取Discover Club失败");
                        if (page == 1) {
                            mSwipeRefreshLayout.setRefreshing(false);
                            mEmptyView.showErrorView(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    update(1, 10);
                                }
                            });
                            mAdapter.setNewData(null);
                            mRecyclerView.setLayoutFrozen(true);
                            mAdapter.setEmptyView(mEmptyView);
                        } else {
                            //允许下拉刷新
                            mSwipeRefreshLayout.setEnabled(true);
                            mAdapter.loadMoreFail();
                        }


                    }

                    @Override
                    public void onComplete() {
                        if (page == 1)
                            mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    @Override
    public Observable<List<String>> getSuggestionObservable(String query, BaseActivity activity) {
        return ApiHelper.getProxyWithoutToken(activity).getClubSuggestion(query);
    }
}
