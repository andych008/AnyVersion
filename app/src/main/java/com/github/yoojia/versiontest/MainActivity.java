package com.github.yoojia.versiontest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;

import com.github.yoojia.anyversion.AnyVersion;
import com.github.yoojia.anyversion.Callback;
import com.github.yoojia.anyversion.NotifyStyle;
import com.github.yoojia.anyversion.Version;
import com.github.yoojia.anyversion.VersionReceiver;
import com.github.yoojia.versiontest.api.param.CheckUpdateParam;
import com.github.yoojia.versiontest.model.MyModel;
import com.github.yoojia.versiontest.model.UpdateModel;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-04
 */
public class MainActivity extends Activity implements ILoading{

    static class NewVersionReceiver extends VersionReceiver {

        @Override
        protected void onVersion(Version newVersion) {
            System.out.println(">> Broadcast === \n" + newVersion);
        }
    }

    private NewVersionReceiver newVersionReceiver = new NewVersionReceiver();
    private ProgressDialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        init();
    }

    private void init(){
        AnyVersion.getInstance().setCallback(new Callback() {
            @Override
            public void onVersion(Version version) {
                System.out.println(">> Callback == \n" + version);
            }
        });

        findViewById(R.id.broadcast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(NotifyStyle.Broadcast);
            }
        });

        findViewById(R.id.callback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(NotifyStyle.Callback);
            }
        });

        findViewById(R.id.dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             update(NotifyStyle.Dialog);
            }
        });

        findViewById(R.id.test_toSortedList).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getTop();
//                test_onErrorReturn();
//                test_onErrorResumeNext();
                test_filter();
            }
        });
    }

    void update(NotifyStyle style){
        update(style, false);
    }

    void update(final NotifyStyle style, boolean flag) {
        UpdateModel.getInstance().update(new CheckUpdateParam(1, "1"), flag)
                .compose(RxUtils.<Version>showLoading(MainActivity.this))
                .subscribe(new Subscriber<Version>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Tools.toast(e.getMessage());
                    }

                    @Override
                    public void onNext(Version version) {
                        AnyVersion anyVersion = AnyVersion.getInstance();
                        anyVersion.check(version, style);
                    }
                });
    }

    void getTop(){
        MyModel.getInstance().getTop(2)
                .compose(RxUtils.<List<MyModel.MyData>>showLoading(MainActivity.this))
                .subscribe(new Subscriber<List<MyModel.MyData>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<MyModel.MyData> list) {
                        Tools.toast(list.toString());
                    }
                });
    }

    void test_onErrorReturn() {
        MyModel.getInstance().test_onErrorReturn()
                .compose(RxUtils.<String>showLoading(MainActivity.this))
                .subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Timber.i(s);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Tools.showConfirmError(MainActivity.this, throwable);
            }
        });
    }

    void test_onErrorResumeNext() {
        MyModel.getInstance().test_onErrorResumeNext()
                .compose(RxUtils.<List<String>>showLoading(MainActivity.this))
                .subscribe(new Action1<List<String>>() {
            @Override
            public void call(List<String> s) {
                Timber.i(s.toString());
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Tools.showConfirmError(MainActivity.this, throwable);
            }
        });
    }

    void test_filter() {
        MyModel.getInstance().test_filter()
                .compose(RxUtils.<List<Integer>>showLoading(MainActivity.this))
                .subscribe(new Action1<List<Integer>>() {
            @Override
            public void call(List<Integer> s) {
                Timber.i(s.toString());
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Tools.showConfirmError(MainActivity.this, throwable);
            }
        });
    }

    @Override
    public void showLoading() {
        showLoading(getString(R.string.loading));
    }

    @Override
    public void hideLoading() {
        Observable.empty()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        if (loadingDialog != null && loadingDialog.isShowing()) {
                            loadingDialog.hide();
                        }
                    }
                })
                .subscribe();
    }

    public void showLoading(String tip) {
        Observable.just(tip)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        if (loadingDialog == null) {
                            loadingDialog = new ProgressDialog(MainActivity.this);
                            loadingDialog.setCanceledOnTouchOutside(false);
                        }
                        loadingDialog.setMessage(s);
                        loadingDialog.show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        AnyVersion.registerReceiver(this, newVersionReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        AnyVersion.unregisterReceiver(this, newVersionReceiver);
    }
}
