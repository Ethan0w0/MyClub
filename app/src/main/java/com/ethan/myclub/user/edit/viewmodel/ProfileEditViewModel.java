package com.ethan.myclub.user.edit.viewmodel;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.databinding.ObservableField;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.ethan.myclub.R;
import com.ethan.myclub.databinding.ActivityUserProfileEditBinding;
import com.ethan.myclub.main.ImageSelectActivity;
import com.ethan.myclub.main.MainActivity;
import com.ethan.myclub.main.MyApplication;
import com.ethan.myclub.main.ToolbarWrapper;
import com.ethan.myclub.network.ApiHelper;
import com.ethan.myclub.user.edit.view.ProfileEditActivity;
import com.ethan.myclub.user.model.Profile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by ethan on 2017/3/20.
 */

public class ProfileEditViewModel {

    private static final String TAG = "ProfileEditViewModel";
    private ProfileEditActivity mActivity;
    public ActivityUserProfileEditBinding mBinding;

    public ObservableField<Uri> mImageUri = new ObservableField<>();

    public Profile mProfile;

    private Calendar c = Calendar.getInstance();

    public ProfileEditViewModel(ProfileEditActivity profileEditActivity, ActivityUserProfileEditBinding binding) {
        mActivity = profileEditActivity;
        mBinding = binding;
        mBinding.setViewModel(this);

        mProfile = MyApplication.sProfile;
        if (!TextUtils.isEmpty(mProfile.avatar))
            mImageUri.set(Uri.parse(mProfile.avatar));

        new ToolbarWrapper.Builder(mActivity)
                .setTitle("编辑个人资料")
                .setMenu(R.menu.toolbar_user_info, new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int menuItemId = item.getItemId();
                        switch (menuItemId) {
                            case R.id.action_confirm:
                                saveChanges();
                                break;
                        }
                        return true;
                    }
                })
                .showNavIcon(R.drawable.ic_toolbar_clear_white_24dp, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mActivity.onBackPressed();
                    }
                })
                .show();


        c.setTimeInMillis(System.currentTimeMillis());
        mBinding.etBirthday.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    int mYear = c.get(Calendar.YEAR);
                    int mMonth = c.get(Calendar.MONTH);
                    int mDay = c.get(Calendar.DAY_OF_MONTH);

                    new DatePickerDialog(mActivity, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            c.set(Calendar.YEAR, year);
                            c.set(Calendar.MONTH, month);
                            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                            mProfile.setBirthday(formatter.format(c.getTime()));
                        }
                    }, mYear, mMonth, mDay).show();

                }

            }
        });
    }

    private File mAvatarFile;
    private boolean mIsAvatarEdited = false;

    private void saveChanges() {
        if (mIsAvatarEdited)
            saveAvatar();
        else if (mProfile.mIsEdited)
            saveInfo();
        else
            mActivity.showSnackbar("并没有资料被修改");
    }


    public void editAvatar() {
        mActivity.selectPicture("avatar.temp.jpg", 500, 500, 1, 1, new ImageSelectActivity.OnFinishSelectImageListener() {
            @Override
            public void onFinish(File outputFile, Uri outputUri) {
                mImageUri.set(outputUri);
                mIsAvatarEdited = true;
                mAvatarFile = outputFile;
            }
        });
    }

    private void showAlertDialog() {
        new AlertDialog.Builder(mActivity)
                .setTitle("提示")
                .setMessage("您的修改还未保存，是否保存？")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveChanges();
                    }
                })
                .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.finishAfterTransition(mActivity);
                    }
                })
                .show();
    }

    private void saveAvatar() {
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), mAvatarFile);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("avatar", mAvatarFile.getName(), requestFile);
        ApiHelper.getProxy(mActivity)
                .uploadAvatar(body)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Observer<Object>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                mActivity.showWaitingDialog("请稍候", "上传头像中", d);
                            }

                            @Override
                            public void onNext(Object o) {

                            }

                            @Override
                            public void onError(Throwable e) {
                                mActivity.showSnackbar("上传头像失败！" + e.getMessage());
                                e.printStackTrace();
                                mActivity.dismissDialog();
                            }

                            @Override
                            public void onComplete() {
                                mActivity.dismissDialog();
                                if (!mProfile.mIsEdited) {
                                    finishEdit();
                                } else
                                    saveInfo();
                            }
                        });
    }

    private void finishEdit() {
        MainActivity.needUpdateFlag.userProfile = true;
        mActivity.setResult(Activity.RESULT_OK);
        ActivityCompat.finishAfterTransition(mActivity);
    }

    private void saveInfo() {
        ApiHelper.getProxy(mActivity)
                .modifyUserProfile(mProfile.nickname, mProfile.studentNumber, mProfile.sex.equals("女") ? "1" : "0", mProfile.name, mProfile.birthday, mProfile.briefIntroduction)
                .delay(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        mActivity.showWaitingDialog("请稍候", "修改信息中", d);
                    }

                    @Override
                    public void onNext(Object o) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        mActivity.showSnackbar("修改信息失败！" + e.getMessage());
                        e.printStackTrace();
                        mActivity.dismissDialog();
                    }

                    @Override
                    public void onComplete() {
                        mActivity.dismissDialog();
                        finishEdit();

                    }
                });
    }


    public void onBackPressed() {
        if (mIsAvatarEdited || mProfile.mIsEdited)
            showAlertDialog();
        else
            ActivityCompat.finishAfterTransition(mActivity);
    }


}
