package com.ccproject.whatsaround;

import android.app.Application;

import com.ccproject.whatsaround.location.LocationWorker;

/**
 * Created by lei on 4/25/2018.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LocationWorker.init(this);
    }
}
