package com.ethan.myclub.schedule.viewmodel;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import com.ethan.myclub.R;
import com.ethan.myclub.club.model.MemberResult;
import com.ethan.myclub.club.my.model.MyClub;
import com.ethan.myclub.main.BaseActivity;
import com.ethan.myclub.main.ToolbarWrapper;
import com.ethan.myclub.network.ApiHelper;
import com.ethan.myclub.schedule.model.Course;
import com.ethan.myclub.schedule.model.CourseTime;
import com.ethan.myclub.schedule.model.Members;
import com.ethan.myclub.schedule.model.Schedule;
import com.ethan.myclub.schedule.model.ScheduleResult;
import com.ethan.myclub.schedule.model.ScheduleStatus;
import com.ethan.myclub.schedule.view.ScheduleAnalysisActivity;
import com.ethan.myclub.databinding.ActivityScheduleAnalysisBinding;
import com.ethan.myclub.schedule.view.SchedulePickerView;
import com.ethan.myclub.schedule.view.ScheduleView;
import com.ethan.myclub.user.detail.view.UserDetailActivity;
import com.ethan.myclub.user.detail.viewmodel.UserDetailViewModel;
import com.ethan.myclub.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static android.content.Context.MODE_PRIVATE;
import static com.ethan.myclub.schedule.view.ScheduleActivity.FILE_NAME_SCHEDULE;

public class ScheduleAnalysisViewModel {

    private ArrayList<Schedule> mSchedules = new ArrayList<>();
    private int mCurrentWeek = 1;

    private final MyClub mMyClub;
    private final String[] mUsers;
    private ScheduleAnalysisActivity mActivity;
    private ActivityScheduleAnalysisBinding mBinding;


    public ScheduleAnalysisViewModel(ScheduleAnalysisActivity activity, ActivityScheduleAnalysisBinding binding, MyClub myClub, String[] users) {
        mActivity = activity;
        mBinding = binding;
        mBinding.setViewModel(this);
        mMyClub = myClub;
        mUsers = users;

        new ToolbarWrapper.Builder(mActivity)
                .setTitle("空课表")
                .showBackIcon()
                .show();

        mBinding.swipeLayout.setColorSchemeResources(R.color.colorAccent);
        mBinding.swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                update();
            }
        });
        if (mUsers == null || mUsers.length == 0) {
            new AlertDialog.Builder(mActivity)
                    .setTitle("错误")
                    .setMessage("您没有选择要查询的成员")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mActivity.finish();
                        }
                    })
                    .show();
            return;
        }

        setCurrentWeek();
    }

    private void update() {
        ScheduleStatus scheduleStatus = new ScheduleStatus();

        List<Integer> members = new ArrayList<>();

        for (String user : mUsers) {
            members.add(Integer.valueOf(user));
        }
        scheduleStatus.memberList = members;
        scheduleStatus.term = "2";
        scheduleStatus.week = mCurrentWeek;
        scheduleStatus.year = "2016-2017";

        ApiHelper.getProxy(mActivity)
                .analysisSchedule(String.valueOf(mMyClub.clubId), scheduleStatus)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<List<ScheduleResult>>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mBinding.swipeLayout.setRefreshing(true);
                    }

                    @Override
                    public void onNext(final List<List<ScheduleResult>> lists) {

                        List<Course> courses = new ArrayList<>();
                        for (int day = 0; day < 7; day++) {

                            List<CourseTime> courseTimes = new ArrayList<>();

                            CourseTime.Builder builder = new CourseTime.Builder()
                                    .weekBegin(0)
                                    .weekEnd(20)
                                    .day(day + 1)
                                    .timeBegin(1)
                                    .weekFlag(3);

                            int lastNum = lists.get(day).get(0).spareNumber;
                            for (int time = 0; time < 12; time++) {
                                ScheduleResult result = lists.get(day).get(time);
                                if (result.spareNumber.equals(lastNum) && time != 4 && time != 9)
                                    continue;
                                builder.timeEnd(time);
                                courseTimes.add(builder.build());
                                int rate = (int) (lastNum * 100 / (float) mUsers.length);
                                int color = (int) (255 * (1 - lastNum / (float) mUsers.length));
                                color |= color << 8;
                                color |= 0xAAFF0000;
                                Course course = new Course.Builder()
                                        .time(courseTimes)
                                        .color(color)
                                        .name(lastNum + "人有事\n" + rate + "%")
                                        .build();
                                courses.add(course);
                                courseTimes = new ArrayList<>();
                                builder.timeBegin(time + 1);
                                lastNum = result.spareNumber;

                            }

                            builder.timeEnd(12);
                            courseTimes.add(builder.build());
                            int rate = (int) (lastNum * 100 / (float) mUsers.length);
                            int color = (int) (255 * (1 - lastNum / (float) mUsers.length));
                            color |= color << 8;
                            color |= 0xAAFF0000;
                            Course course = new Course.Builder()
                                    .time(courseTimes)
                                    .color(color)
                                    .name(lastNum + "人有事\n" + rate + "%")
                                    .build();
                            courses.add(course);

                        }
                        Schedule schedule = new Schedule.Builder().courses(courses).build();
                        mBinding.scheduleView.setSchedule(schedule, 1);

                        mBinding.scheduleView.setListener(new ScheduleView.OnClickListener() {
                            @Override
                            public void onClick(CourseTime courseTime, Course course) {
                                final ScheduleResult result = lists.get(courseTime.getDay() - 1).get(courseTime.getTimeBegin() - 1);
                                if (result.spareNumber == 0)
                                    return;
                                List<String> names = new ArrayList<>();
                                for (Members member : result.members) {
                                    names.add(member.nickname);
                                }
                                new AlertDialog.Builder(mActivity)
                                        .setItems(names.toArray(new String[0]), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                MemberResult memberResult = new MemberResult();
                                                memberResult.userAccount = String.valueOf(result.members.get(which).id);
                                                UserDetailActivity.start(mActivity, memberResult);
                                            }
                                        })
                                        .setTitle("有事人员名单")
                                        .setNegativeButton("关闭", null)
                                        .show();
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        mActivity.showSnackbar(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        mBinding.swipeLayout.setRefreshing(false);
                    }
                });
    }

    private void setCurrentWeek() {

        final NumberPicker np = new NumberPicker(mActivity);
        String[] week_id = new String[20];
        for (int i = 0; i < 20; i++) {
            week_id[i] = "第" + (i + 1) + "周";
        }

        np.setDisplayedValues(week_id);
        np.setMinValue(0);
        np.setMaxValue(week_id.length - 1);
        np.setValue(mCurrentWeek - 1);
        np.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

        new AlertDialog.Builder(mActivity)
                .setTitle("请选择你想查询的周")
                .setView(np)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mCurrentWeek = np.getValue() + 1;
                        update();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
}