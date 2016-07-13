package com.xbeats.swipebacksample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

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
        if (isSupportToSlideBack() || isSupportBeSlideBack()) {
            getSlideBackLayout().setSlidingAvailable(isSupportToSlideBack());
        } else {
            ViewGroup decor = (ViewGroup) getWindow().getDecorView();
            ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
            decorChild.setBackgroundColor(Color.parseColor("#e8edf2"));
        }
    }

    public SlideBackLayout getSlideBackLayout() {
        if(mSlideBackLayout == null ) {
            mSlideBackLayout = new SlideBackLayout(this);
            mSlideBackLayout.attachViewToActivity(this);
            mSlideBackLayout.setSlidingAvailable(true);
            mSlideBackLayout.setTouchMode(SlideBackLayout.TOUCH_MODE_MARGIN);
            if(isSupportToSlideBack()) {
                mSlideBackLayout.setOnScrollHook(SlideBackLayout.getOnScrollListener());
            } else {
                mSlideBackLayout.setOnScrollHook(null);
            }
        }
        return mSlideBackLayout;
    }

    /**
     * 是否支持滑动返回
     *
     * @return
     */
    protected boolean isSupportToSlideBack() {
        return true;
    }

    /**
     * 是否支持被滑动显示
     * @return
     */
    protected boolean isSupportBeSlideBack() {
        return true;
    }

    private boolean isNeedSlideBack = true;

    /**
     * 是否需要滑动返回
     * @param needSlideBack
     */
    protected void setNeedSlideBack(boolean needSlideBack) {
        isNeedSlideBack = needSlideBack;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        if(isNeedSlideBack && isSupportBeSlideBack()){
            SlideBackLayout.setOnScrollListener(mSlideBackLayout == null ? null : mSlideBackLayout.getOnScrollContainer());
        }
        super.startActivityForResult(intent, requestCode, options);
    }
}
