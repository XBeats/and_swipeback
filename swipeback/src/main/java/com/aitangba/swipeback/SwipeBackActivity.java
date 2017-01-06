package com.aitangba.swipeback;

import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;


/**
 * Created by fhf11991 on 2016/7/25.
 */
public class SwipeBackActivity extends AppCompatActivity implements SwipeBackHelper.SlideBackManager {

    private static final String TAG = "SwipeBackActivity";

    private SwipeBackHelper mSwipeBackHelper;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(!supportSlideBack()) {
            return super.dispatchTouchEvent(ev);
        }

        if(mSwipeBackHelper == null) {
            mSwipeBackHelper = new SwipeBackHelper(getWindow());
        }
        return mSwipeBackHelper.processTouchEvent(ev) || super.dispatchTouchEvent(ev);
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
        }
        super.finish();
    }
}
