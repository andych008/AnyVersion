package com.github.yoojia.versiontest;

import android.app.Application;

import com.github.yoojia.anyversion.AnyVersion;

import timber.log.Timber;

/**
 * Created by Yoojia.Chen
 * yoojia.chen@gmail.com
 * 2015-01-06
 */
public class AnyVersionApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        AnyVersion.init(this);
        Tools.init(this);
    }
}
