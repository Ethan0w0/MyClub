package com.ethan.myclub.views.user.login;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ethan.myclub.R;
import com.ethan.myclub.utils.dialogs.WaitingDialogHelper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.CountryListView;
import cn.smssdk.gui.GroupListView;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class RegisterActivity extends AppCompatActivity {
    //private ProgressDialog mProgressDialog;
    private TextView mTvCountryCode;
    private TextView mTvCountryName;
    private Button mBtnSendSMS;
    private EditText mEtPhoneNumber;
    private CardView mBtnNext;
    private TextView mVerifyCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        mTvCountryCode = (TextView) findViewById(R.id.tv_country_code);
        mTvCountryName = (TextView) findViewById(R.id.tv_country_name);
        mVerifyCode = (TextView) findViewById(R.id.et_verify_code);
        mEtPhoneNumber = (EditText) findViewById(R.id.et_phone_number);
        mBtnSendSMS = (Button) findViewById(R.id.btn_sendSMS);
        mBtnNext = (CardView) findViewById(R.id.btn_next);
        //Init SMSSDK
        SMSSDK.initSDK(this, "1b8ee6770f538", "3f490908a071256b009580392a5b312a");
        initCountry();
        initSendSMS();
        initSubmit();

    }

    private void initSubmit() {
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(final ObservableEmitter<Object> e) throws Exception {
                        SMSSDK.registerEventHandler(new EventHandler() {
                            @Override
                            public void afterEvent(int event, int result, Object data) {
                                if (result == SMSSDK.RESULT_COMPLETE) {
                                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                                        e.onNext(data);
                                    }
                                } else {
                                    e.onError((Throwable) data);
                                }
                            }
                        });
                        SMSSDK.submitVerificationCode(mTvCountryCode.getText().toString(), mEtPhoneNumber.getText().toString(), mVerifyCode.getText().toString());
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .doOnNext(new Consumer<Object>() {
                            @Override
                            public void accept(Object arrayList) throws Exception {
                                SMSSDK.unregisterAllEventHandler();//防止内存泄漏
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Object>() {
                            @Override
                            public void accept(final Object o) throws Exception {
                                //Todo: 成功，继续注册

                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                //失败
                                Snackbar.make(findViewById(R.id.activity_login_register), parseErrorMessage(throwable), Snackbar.LENGTH_LONG)
                                        .show();

                            }
                        });
            }
        });


    }

    private void initSendSMS() {
        mBtnSendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Observable.create(new ObservableOnSubscribe<Boolean>() {
                    @Override
                    public void subscribe(final ObservableEmitter<Boolean> e) throws Exception {
                        SMSSDK.registerEventHandler(new EventHandler() {
                            @Override
                            public void afterEvent(int event, int result, Object data) {
                                if (result == SMSSDK.RESULT_COMPLETE) {
                                    if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                                        e.onNext((Boolean) data);
                                    }
                                } else {
                                    e.onError((Throwable) data);
                                }
                            }
                        });
                        //Log.e("1","-------线程:" + Thread.currentThread().getName());
                        SMSSDK.getVerificationCode(mTvCountryCode.getText().toString(), mEtPhoneNumber.getText().toString());
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .doOnNext(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean a) throws Exception {
                                SMSSDK.unregisterAllEventHandler();//防止内存泄漏
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(final Boolean b) throws Exception {
                                //成功
                                Snackbar.make(findViewById(R.id.activity_login_register), "短信已发送，请注意查收", Snackbar.LENGTH_LONG).show();
                                startCounting();
//                                if (b) {
//                                    //通过智能验证
//                                } else {
//                                    //依然走短信验证
//                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                //失败
                                Snackbar.make(findViewById(R.id.activity_login_register), parseErrorMessage(throwable), Snackbar.LENGTH_LONG)
                                        .show();

                            }
                        });
            }
        });
    }

    private String parseErrorMessage(Throwable throwable) {
        String str = throwable.getMessage();
        //throwable.printStackTrace();
        try {
            //is Json?
            JSONObject object = new JSONObject(str);
            String des = object.optString("detail");//错误描述
            int status = object.optInt("status");//错误代码
            if (status > 0 && !TextUtils.isEmpty(des)) {
                return des;
            }
        } catch (Exception e) {
            return str;
        }
        return str;
    }

    private void initCountry() {

        mTvCountryName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WaitingDialogHelper.show(RegisterActivity.this, "正在获取国家列表");

                //Log.e("0", "-------线程:" + Thread.currentThread().getName());
                Observable.create(new ObservableOnSubscribe<ArrayList>() {
                    @Override
                    public void subscribe(final ObservableEmitter<ArrayList> e) throws Exception {
                        SMSSDK.registerEventHandler(new EventHandler() {
                            @Override
                            public void afterEvent(int event, int result, Object data) {
                                if (result == SMSSDK.RESULT_COMPLETE) {
                                    if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
                                        //返回支持发送验证码的国家列表
                                        if (data instanceof ArrayList) {
                                            e.onNext((ArrayList) data);
                                        }
                                    }
                                } else {
                                    e.onError((Throwable) data);
                                }
                            }
                        });
                        SMSSDK.getSupportedCountries();
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .doOnNext(new Consumer<ArrayList>() {
                            @Override
                            public void accept(ArrayList arrayList) throws Exception {
                                SMSSDK.unregisterAllEventHandler();//防止内存泄漏
                            }
                        })
                        .map(new Function<ArrayList, HashMap>() {
                            @Override
                            public HashMap apply(ArrayList arrayList) throws Exception {

                                HashMap<String, String> countryRules = new HashMap<String, String>();
                                for (Object countryObject : arrayList) {
                                    if (countryObject instanceof HashMap) {
                                        Map country = (HashMap) countryObject;
                                        String code = (String) country.get("zone");
                                        String rule = (String) country.get("rule");
                                        if (TextUtils.isEmpty(code) || TextUtils.isEmpty(rule)) {
                                            continue;
                                        }
                                        countryRules.put(code, rule);
                                    }
                                }
                                return countryRules;
                            }
                        })
                        .delay(1, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<HashMap>() {
                            @Override
                            public void accept(final HashMap hashMap) throws Exception {
                                //成功
                                Log.e("3", "-------线程:" + Thread.currentThread().getName());
                                final CountryListView countryListView = new CountryListView(RegisterActivity.this);

                                final AlertDialog dialog = new AlertDialog.Builder(RegisterActivity.this)
                                        .setTitle("请选择国家或地区")
                                        .setView(countryListView)
                                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        })
                                        .create();

                                countryListView.setOnItemClickListener(new GroupListView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(GroupListView groupListView, View view, int group, int position) {
                                        if (position >= 0) {
                                            String[] country = countryListView.getCountry(group, position);
                                            if (hashMap != null && hashMap.containsKey(country[1])) {
                                                Snackbar.make(findViewById(R.id.activity_login_register), "已经设置您的国家代码为：" + country[1], Snackbar.LENGTH_LONG).show();
                                                String countryCode = country[1];
                                                String countryName = country[0];
                                                mTvCountryCode.setText(countryCode);
                                                mTvCountryName.setText(countryName);
                                                dialog.dismiss();
                                            } else {
                                                Snackbar.make(findViewById(R.id.activity_login_register), "暂时不支持这个国家或地区", Snackbar.LENGTH_LONG).show();
                                            }
                                        }
                                    }
                                });

                                WaitingDialogHelper.dismiss();
                                dialog.show();


                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                //失败
                                throwable.printStackTrace();
                                Snackbar.make(findViewById(R.id.activity_login_register), parseErrorMessage(throwable), Snackbar.LENGTH_LONG)
                                        .show();

                            }
                        });
            }
        });
    }

    private void startCounting() {
        mBtnSendSMS.setClickable(false);
        final int countTime = 60;
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<Long, Integer>() {
                    @Override
                    public Integer apply(Long aLong) throws Exception {
                        return countTime - aLong.intValue();
                    }
                })
                .take(countTime + 1)
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        mBtnSendSMS.setText("等待" + integer + "秒");
                    }

                    @Override
                    public void onError(Throwable e) {
                        mBtnSendSMS.setClickable(true);
                        mBtnSendSMS.setText("发送短信");
                    }

                    @Override
                    public void onComplete() {
                        mBtnSendSMS.setClickable(true);
                        mBtnSendSMS.setText("发送短信");
                    }
                });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();
    }
}
