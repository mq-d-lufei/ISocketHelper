package com.ivt.sockethelper;

import android.app.Application;

/**
 * Created by feaoes on 2017/11/16.
 */

public class MyApplication extends Application {

    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static MyApplication getInstance() {
        return instance;
    }
}
