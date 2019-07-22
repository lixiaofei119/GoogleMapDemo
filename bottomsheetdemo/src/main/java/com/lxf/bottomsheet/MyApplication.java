package com.lxf.bottomsheet;

import android.app.Application;
import android.content.Context;

/**
 * MyApplication
 *
 * @author: 17040880
 * @time: 2017/9/18 19:59
 */
public class MyApplication extends Application {
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }
}
