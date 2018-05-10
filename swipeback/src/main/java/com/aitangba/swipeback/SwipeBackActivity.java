package com.aitangba.swipeback;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;


/**
 * Created by fhf11991 on 2016/7/25.
 */

public class SwipeBackActivity extends AppCompatActivity implements SlideBackManager {

    private static final String TAG = "SwipeBackActivity";

    private SwipeIntercept mSwipeBackHelper;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(mSwipeBackHelper == null) {
            mSwipeBackHelper = new SwipeHelper(this);
        }
        return mSwipeBackHelper.processTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }

    @Override
    public Activity getSlideActivity() {
        return this;
    }

    @Override
    public boolean supportSlideBack() {
        return true;
    }

    @Override
    public boolean canBeSlideBack() {
        return true;
    }

    @Override
    public void finish() {
        if(mSwipeBackHelper != null) {
            mSwipeBackHelper.finishSwipeImmediately();
            mSwipeBackHelper = null;
        }
        super.finish();
    }
}
