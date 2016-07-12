package com.xbeats.swipebacksample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.xbeats.swipebacksample.views.SlideBackLayout;

/**
 * Created by fhf11991 on 2016/7/11.
 */
public class BaseActivity extends AppCompatActivity {

    private SlideBackLayout mSlideBackLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSlideBackFinish();
    }

    /**
     * 初始化滑动返回
     */
    private void initSlideBackFinish() {
        if (isSupportSlideBack()) {
            getSlideBackLayout();
        }
    }

    public SlideBackLayout getSlideBackLayout() {
        if(mSlideBackLayout == null ) {
            mSlideBackLayout = new SlideBackLayout(this);
            mSlideBackLayout.attachViewToActivity(this);
            mSlideBackLayout.setSlidingAvailable(true);
            mSlideBackLayout.setTouchMode(SlideBackLayout.TOUCH_MODE_MARGIN);
        }
        return mSlideBackLayout;
    }

    /**
     * 是否支持滑动返回
     *
     * @return
     */
    protected boolean isSupportSlideBack() {
        return true;
    }

}
