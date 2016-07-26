package com.xbeats.swipebacksample.dispatchactivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.xbeats.swipebacksample.applicationtest.CustomApplication;

/**
 * Created by fhf11991 on 2016/7/25.
 */

public class BaseActivity extends AppCompatActivity{

    private GestureDetector mGestureDetector;

    private int mMarginThreshold = 100;  //拦截手势区间 0~100
    private boolean mIsSliding; //是否正在滑动
    private float mDistanceX;  //当前滑动距离 （正数或0）

    private FrameLayout mContentView;
    public FrameLayout getContentView() {
        if(mContentView == null) {
            mContentView = (FrameLayout) findViewById(Window.ID_ANDROID_CONTENT);
        }
        return mContentView;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGestureDetector = new GestureDetector(this, new CustomGestureListener());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if(!isSupportSlideBack())return super.dispatchTouchEvent(ev);

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                final int x = (int) (ev.getX());
                boolean isSlideAction = x >= 0 && x < mMarginThreshold;
                if(isSlideAction) {
                    mIsSliding = true;
                    return mGestureDetector.onTouchEvent(ev);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(mIsSliding) {
                    return mGestureDetector.onTouchEvent(ev);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(mIsSliding) {
                    handler.sendEmptyMessage(EVENT_UP);
                }
                break;
            default:break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private class CustomGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            CustomApplication application = (CustomApplication) getApplication();
            Activity preActivity = application.getActivityLifecycleHelper().getPreActivity();
            ViewGroup preContentView = (ViewGroup) preActivity.findViewById(android.R.id.content);
            View preView = preContentView.getChildAt(0);
            preContentView.removeView(preView);
            getContentView().addView(preView, 0);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mDistanceX = mDistanceX + (-distanceX);
            if(mDistanceX < 0)mDistanceX = 0;
            onSliding();
            return true;
        }
    }

    private synchronized void onSliding() {
        final int width = getResources().getDisplayMetrics().widthPixels;
        FrameLayout contentView = getContentView();
        View preView = contentView.getChildAt(0);
        View curView = contentView.getChildAt(1);
        if(preView == null || curView == null)return;
        preView.setX(-width/3 + mDistanceX/3);
        curView.setX(mDistanceX);
    }

    private static final int EVENT_UP = 1;
    private static final int EVENT_TO_SLIDE_CLOSE = 2;
    private static final int EVENT_SLIDE_CLOSED = 3;
    private static final int EVENT_TO_SLIDE_BACK = 4;
    private static final int EVENT_BACK = 5;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final int width = getResources().getDisplayMetrics().widthPixels;
            final FrameLayout contentView = getContentView();
            switch (msg.what) {
                case EVENT_UP:
                    if(mDistanceX > width / 4) {
                        handler.sendEmptyMessage(EVENT_TO_SLIDE_BACK);
                    } else {
                        handler.sendEmptyMessage(EVENT_TO_SLIDE_CLOSE);
                    }
                    break;
                case EVENT_TO_SLIDE_CLOSE:
                    startSlideAnim(true);
                    break;
                case EVENT_SLIDE_CLOSED:
                    mDistanceX = 0;
                    mIsSliding = false;
                    resetPreView();
                    break;
                case EVENT_TO_SLIDE_BACK:
                    startSlideAnim(false);
                    break;
                case EVENT_BACK:
                    final View preView = contentView.getChildAt(0);
                    resetPreView();

                    final int viewWidth = preView.getMeasuredWidth();
                    final int viewHeight = preView.getMeasuredHeight();
                    Bitmap bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    preView.draw(canvas);
                    ImageView imageView = new ImageView(BaseActivity.this);
                    contentView.addView(imageView, 0);
                    imageView.setImageBitmap(bitmap);
                    finish();
                    overridePendingTransition(0, 0);
                    break;
                default:break;
            }
        }
    };

    private void resetPreView() {
        final FrameLayout contentView = getContentView();
        final View preView = contentView.getChildAt(0);
        contentView.removeView(preView);
        CustomApplication application = (CustomApplication) getApplication();
        Activity preActivity = application.getActivityLifecycleHelper().getPreActivity();
        ViewGroup preContentView = (ViewGroup) preActivity.findViewById(android.R.id.content);
        preContentView.addView(preView);
    }

    private void startSlideAnim(final boolean isBack) {
        final FrameLayout contentView = getContentView();
        final View preView = contentView.getChildAt(0);
        final View curView = contentView.getChildAt(1);
        if(preView == null || curView == null)return;

        final int width = getResources().getDisplayMetrics().widthPixels;
        final int ANIMATION_DURATION = isBack ? 200 : 500;

        float preViewStart = mDistanceX / 3 - width / 3;
        float preViewStop = isBack ? -width / 3 : 0;

        float curViewStart = mDistanceX;
        float curViewStop = isBack ? 0 : width;

        Interpolator sExpandInterpolator = new DecelerateInterpolator(2f);
        AnimatorSet tranAnimSet = new AnimatorSet();
        tranAnimSet.setDuration(ANIMATION_DURATION);

        ObjectAnimator animatorFirst = new ObjectAnimator();
        animatorFirst.setInterpolator(sExpandInterpolator);
        animatorFirst.setProperty(View.TRANSLATION_X);
        animatorFirst.setFloatValues(preViewStart, preViewStop);
        animatorFirst.setTarget(preView);


        ObjectAnimator animatorSecond = new ObjectAnimator();
        animatorSecond.setInterpolator(sExpandInterpolator);
        animatorSecond.setProperty(View.TRANSLATION_X);
        animatorSecond.setFloatValues(curViewStart, curViewStop);
        animatorSecond.setTarget(curView);

        tranAnimSet.playTogether(animatorFirst, animatorSecond);
        tranAnimSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(isBack) {
                    preView.setX(0);
                    curView.setX(0);
                    handler.sendEmptyMessage(EVENT_SLIDE_CLOSED);
                } else {
                    handler.sendEmptyMessage(EVENT_BACK);
                }

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        tranAnimSet.start();
    }

    /**
     * 是否支持滑动返回
     * @return
     */
    protected boolean isSupportSlideBack() {
        return true;
    }
}
