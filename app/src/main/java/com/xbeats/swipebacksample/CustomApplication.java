package com.xbeats.swipebacksample;

import android.app.Application;

import com.aitangba.swipeback.ActivityLifecycleHelper;


/**
 * Created by fhf11991 on 2016/7/18.
 */

public class CustomApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(ActivityLifecycleHelper.build());
    }

}
