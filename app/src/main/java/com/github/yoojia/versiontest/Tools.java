package com.github.yoojia.versiontest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Looper;
import android.widget.Toast;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Andy on 4/9/2016.
 */
public class Tools {
    private static Context context;

    public static void init(Context context){
        Tools.context = context;
    }

    public static void toast(String tip) {
        Observable.just(tip)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Toast.makeText(context, s, Toast.LENGTH_LONG).show();
                    }
                });
    }

    public static void showConfirmError(Activity activity, Throwable throwable) {
        showConfirmError(activity, throwable.getMessage());
    }

    public static void showConfirmError(Activity activity, String text) {
        new AlertDialog.Builder(activity).setMessage(text)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }


    public static void requiredWorkThread(){
        if (Looper.getMainLooper() == Looper.myLooper()){
            throw new IllegalStateException("Should run on work thread !");
        }
    }
}
