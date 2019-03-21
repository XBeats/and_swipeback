package com.aitangba.swipeback;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;


/**
 * Created by fhf11991 on 2016/7/25.
 */
public class SwipeBackActivity extends FragmentActivity implements SwipeBackHelper.SlideBackManager {

    private SwipeBackHelper mSwipeBackHelper;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!supportSlideBack()) {
            return super.dispatchTouchEvent(ev);
        }
        if (mSwipeBackHelper == null) {
            mSwipeBackHelper = new SwipeBackHelper(this, new SlideActivityAdapter());
            mSwipeBackHelper.setOnSlideFinishListener(new SwipeBackHelper.OnSlideFinishListener() {
                @Override
                public void onFinish() {
                    SwipeBackActivity.this.finish();
                    overridePendingTransition(android.R.anim.fade_in, R.anim.hold_on);
                }
            });
        }
        return mSwipeBackHelper.processTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }

    @Override
    public void finish() {
        if (mSwipeBackHelper != null) {
            mSwipeBackHelper.finishSwipeImmediately();
        }
        super.finish();
    }

    @Override
    public boolean supportSlideBack() {
        return true;
    }

    @Override
    public boolean canBeSlideBack() {
        return true;
    }

    private static class SlideActivityAdapter implements SlideActivityCallback {

        @Override
        public Activity getPreviousActivity() {
            return ActivityLifecycleHelper.getPreviousActivity();
        }
    }
}
