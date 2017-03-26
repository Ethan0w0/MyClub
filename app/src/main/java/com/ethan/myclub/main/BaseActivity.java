package com.ethan.myclub.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.ethan.myclub.R;
import com.ethan.myclub.global.Preferences;
import com.ethan.myclub.user.login.view.LoginActivity;
import com.ethan.myclub.util.CacheUtil;

import org.jsoup.Connection;

import io.reactivex.disposables.Disposable;

public abstract class BaseActivity extends AppCompatActivity {

    public static final int REQUEST_LOGIN = 10304;
    public static final int REQUEST_REGESTER = 10305;

    protected ViewGroup mRootLayout;

    public void showSnackbar(String text) {
        showSnackbar(text, null, null);
    }

    public void showSnackbar(String text, String actionText, View.OnClickListener onClickListener) {
        Snackbar snackbar = Snackbar.make(mRootLayout, text, Snackbar.LENGTH_LONG);
        if (onClickListener != null)
            snackbar.setAction(actionText, onClickListener);
        snackbar.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_LOGIN) {
                showSnackbar("登录成功！");
            }
            if (requestCode == REQUEST_REGESTER) {
                showSnackbar("注册成功！已经帮您自动登录！");
            }
            if (requestCode == REQUEST_REGESTER || requestCode == REQUEST_LOGIN)
                //清除缓存
                CacheUtil.get(this).remove(Preferences.CACHE_USER_INFO);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutId) {
        View view = View.inflate(this, layoutId, null);
        setRootLayout(view);
        super.setContentView(view);
    }

    @Override
    public void setContentView(View view) {
        setRootLayout(view);
        super.setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        setRootLayout(view);
        super.setContentView(view, params);
    }

    private void setRootLayout(View view) {
        if (!(view instanceof ViewGroup))
            throw new RuntimeException("Content view of a BaseActivity must be a ViewGroup!");
        mRootLayout = (ViewGroup) view;

//        View decorView = getWindow().getDecorView();
//        ViewGroup contentView = (ViewGroup) decorView.findViewById(android.R.id.content);
//        final View childView = contentView.getChildAt(0);

    }


    public static class ToolbarWrapper {

        private BaseActivity mBaseActivity;
        private int mMenuResId = -1;
        private boolean mIsShowBackIcon = false;
        private String mTittle;
        private Toolbar.OnMenuItemClickListener mOnMenuItemClickListener;
        private boolean mIsNeedMoveFirstChildDown = false;

        public ToolbarWrapper(BaseActivity baseActivity, String tittle) {
            mBaseActivity = baseActivity;
            if (mBaseActivity.mRootLayout == null)
                throw new RuntimeException("You must call setContentView before initToolbar!");
            mTittle = tittle;
        }

        public ToolbarWrapper setMenuAndListener(@MenuRes int resId, Toolbar.OnMenuItemClickListener onMenuItemClickListener) {
            mMenuResId = resId;
            mOnMenuItemClickListener = onMenuItemClickListener;
            return this;
        }

        public ToolbarWrapper showBackIcon() {
            mIsShowBackIcon = true;
            return this;
        }

        public ToolbarWrapper moveFirstChildDown() {
            mIsNeedMoveFirstChildDown = true;
            return this;
        }

        public void show() {
            if (mIsNeedMoveFirstChildDown) {
                int childCount = mBaseActivity.mRootLayout.getChildCount();
                if (childCount == 0)
                    throw new RuntimeException("Root view group must have a child to move down!");
                View child = mBaseActivity.mRootLayout.getChildAt(0);
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) child.getLayoutParams();

                TypedValue typedValue = new TypedValue();
                mBaseActivity.getTheme().resolveAttribute(R.attr.actionBarSize, typedValue, true);
                layoutParams.topMargin = mBaseActivity.getResources().getDimensionPixelSize(typedValue.resourceId);
                child.setLayoutParams(layoutParams);
            }

            View appBarLayout = View.inflate(mBaseActivity, R.layout.view_toolbar, mBaseActivity.mRootLayout);
            Toolbar toolbar = (Toolbar) appBarLayout.findViewById(R.id.toolbar);
            toolbar.setTitle(mTittle);
            if (mMenuResId != -1 && mOnMenuItemClickListener != null) {
                toolbar.inflateMenu(mMenuResId);
                toolbar.setOnMenuItemClickListener(mOnMenuItemClickListener);
            }


            if (mIsShowBackIcon) {
                final TypedValue typedValueAttr = new TypedValue();
                mBaseActivity.getTheme().resolveAttribute(R.attr.homeAsUpIndicator, typedValueAttr, true);
                toolbar.setNavigationIcon(typedValueAttr.resourceId);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //ActivityCompat.finishAfterTransition(BaseActivity.this);
                        mBaseActivity.onBackPressed();
                    }
                });
            }
        }


    }


    public ProgressDialog mProgressDialog;
    public Disposable mDisposable;

    private void showDialog(String tittle, String message, Disposable disposable, int style) {

        hideKeyboard();//隐藏
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(style);// 设置进度条的形式为圆形转动的进度条
        dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        dialog.setTitle(tittle);
        dialog.setMessage(message);
        if (disposable != null) {
            mDisposable = disposable;
        }

        if (mDisposable != null) {
            dialog.setCancelable(true);// 设置是否可以通过点击Back键取消
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cancelCurrentProgress();
                }
            });
            // 监听cancel事件
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    cancelCurrentProgress();
                    //点击back
                }
            });
        } else
            dialog.setCancelable(false);

        mProgressDialog = dialog;
        mProgressDialog.show();
    }


    public void showWaitingDialog(String tittle, String message, Disposable d) {
        showDialog(tittle, message, d, ProgressDialog.STYLE_SPINNER);
    }

    public void showWaitingDialog(String tittle, String message) {
        showWaitingDialog(tittle, message, null);
    }

    public void showProgressDialog(String tittle, String message, Disposable d) {
        showDialog(tittle, message, d, ProgressDialog.STYLE_HORIZONTAL);
    }

    public void showProgressDialog(String tittle, String message) {
        showProgressDialog(tittle, message, null);
    }

    public void dismissDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
            mDisposable = null;
        }
    }

    public void cancelCurrentProgress() {
        if (mDisposable != null)
            if (!mDisposable.isDisposed())
                mDisposable.dispose();
    }

    public void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void showLoginSnackbar(String message) {
        showSnackbar(message, "登录", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(BaseActivity.this, LoginActivity.class);
                startActivityForResult(intent, BaseActivity.REQUEST_LOGIN);
            }
        });
    }
}