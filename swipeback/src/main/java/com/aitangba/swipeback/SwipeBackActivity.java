package com.aitangba.swipeback;

import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;


/**
 * Created by fhf11991 on 2016/7/25.
 */
public class SwipeBackActivity extends AppCompatActivity {

    private static final String TAG = "SwipeBackActivity";

    private SwipeWindowHelper mSwipeWindowHelper;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(!supportSlideBack()) {
            return super.dispatchTouchEvent(ev);
        }

        if(mSwipeWindowHelper == null) {
            mSwipeWindowHelper = new SwipeWindowHelper(getWindow());
        }
        return mSwipeWindowHelper.processTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }

    /**
     * 是否支持滑动返回
     *
     * @return
     */
    protected boolean supportSlideBack() {
        return true;
    }

    /**
     * 能否滑动返回至当前Activity
     * @return
     */
    protected boolean canBeSlideBack() {
        return true;
    }
}
