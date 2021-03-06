package com.ethan.myclub.club.my.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ethan.myclub.R;
import com.ethan.myclub.main.BaseActivity;

/**
 * Created by ethan on 2017/3/29.
 */

public class EmptyView extends FrameLayout {

    private Button mBtn;
    private TextView mTvMessage;
    private TextView mTvTip;
    private ImageView mIvImg;
    private ProgressBar mProgressBar;

    public EmptyView(Context context) {
        super(context);
        init(context);
    }


    public EmptyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EmptyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_empty, this);
        mBtn = (Button) findViewById(R.id.btn);
        mTvMessage = (TextView) findViewById(R.id.tv_message);
        mTvTip = (TextView) findViewById(R.id.tv_tip);
        mIvImg = (ImageView) findViewById(R.id.iv_img);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    private void showTip(String strTip) {
        mProgressBar.setVisibility(INVISIBLE);
        mIvImg.setVisibility(VISIBLE);
        this.setOnClickListener(null);
        mBtn.setVisibility(INVISIBLE);
        mTvTip.setVisibility(VISIBLE);
        mTvTip.setText(strTip);
    }

    private void showBtn(String strText) {
        mProgressBar.setVisibility(INVISIBLE);
        mBtn.setOnClickListener(null);
        mIvImg.setVisibility(VISIBLE);
        mBtn.setVisibility(VISIBLE);
        mTvTip.setVisibility(INVISIBLE);
        mBtn.setText(strText);
    }

    public void showLoadingView() {
        mProgressBar.setVisibility(VISIBLE);
        mIvImg.setVisibility(INVISIBLE);
        mBtn.setVisibility(INVISIBLE);
        mTvTip.setVisibility(INVISIBLE);
        mTvMessage.setText("加载中");
    }

    public void showNotLoginView() {
        mIvImg.setImageResource(R.drawable.ic_state_login);
        mTvMessage.setText("还没有登录哦");
        showBtn("登录");
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseActivity) getContext()).startLoginActivity();
            }
        });

    }

    public void showEmptyView(String message1,String message2) {
        mIvImg.setImageResource(R.drawable.ic_state_login);
        mTvMessage.setText(message1);
        showTip(message2);
    }

    public void showErrorView(OnClickListener retryListener) {
        mIvImg.setImageResource(R.drawable.ic_state_time_out);
        mTvMessage.setText("出错啦");
        showTip("轻触屏幕再试一次");
        this.setOnClickListener(retryListener);
    }

    public void showNoNetWorkError() {
        mIvImg.setImageResource(R.drawable.ic_state_no_network);
        mTvMessage.setText("出错啦");
        showTip("网络没有连接，是不是忘记打开网络了？");
    }
}
