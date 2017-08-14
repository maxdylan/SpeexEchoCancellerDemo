package com.xintu.speexechocanceller;

import android.app.Application;

/**
 * Created by Tong on 2017/8/14.
 */

public class MyApp extends Application {
    private CrashHandler crashHandler = CrashHandler.getInstance();

    @Override
    public void onCreate() {
        super.onCreate();
        crashHandler.init(this);

    }
}
