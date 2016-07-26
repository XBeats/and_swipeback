package com.xbeats.swipebacksample.dispatchactivity;

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
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.xbeats.swipebacksample.applicationtest.CustomApplication;

/**
 * Created by fhf11991 on 2016/7/25.
 */

public class BaseActivity extends AppCompatActivity{

    private GestureDetector mGestureDetector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGestureDetector = new GestureDetector(this, new CustomGestureListener());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

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

    private int mMarginThreshold = 100;
    private boolean mIsSliding;
    private float mDistanceX;

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

    private FrameLayout mContentView;
    public FrameLayout getContentView() {
        if(mContentView == null) {
            mContentView = (FrameLayout) findViewById(Window.ID_ANDROID_CONTENT);
        }
        return mContentView;
    }

    private synchronized void onSliding() {
        final int width = getResources().getDisplayMetrics().widthPixels;
        FrameLayout contentView = getContentView();
        contentView.getChildAt(1).setX(mDistanceX);
        contentView.getChildAt(0).setX(-width + mDistanceX);
    }

    private static final int EVENT_UP = 1;
    private static final int EVENT_SLIDE_TO_BACK = 2;
    private static final int EVENT_SLIDE_BACK = 3;
    private static final int EVENT_SLIDE_TO_FINISH = 4;
    private static final int EVENT_SLIDE_FINISHED = 5;

    private static final int DELAYED_TIME = 50;  //ms
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final int width = getResources().getDisplayMetrics().widthPixels;
            final FrameLayout contentView = getContentView();
            switch (msg.what) {
                case EVENT_UP:
                    if(mDistanceX > width / 4) {
                        handler.sendEmptyMessage(EVENT_SLIDE_TO_FINISH);
                    } else {
                        handler.sendEmptyMessage(EVENT_SLIDE_TO_BACK);
                    }
                    break;
                case EVENT_SLIDE_TO_BACK:
                    mDistanceX = mDistanceX - 100;
                    if(mDistanceX <= 0) {
                        mDistanceX = 0;
                        onSliding();
                        handler.sendEmptyMessage(EVENT_SLIDE_BACK);
                    } else {
                        onSliding();
                        handler.sendEmptyMessageDelayed(EVENT_SLIDE_TO_BACK, DELAYED_TIME);
                    }
                    break;
                case EVENT_SLIDE_BACK:
                    resetPreView();
                    break;
                case EVENT_SLIDE_TO_FINISH:
                    mDistanceX = mDistanceX + 100;
                    if(mDistanceX >= width) {
                        mDistanceX = width;
                        onSliding();
                        handler.sendEmptyMessage(EVENT_SLIDE_FINISHED);
                    } else {
                        onSliding();
                        handler.sendEmptyMessageDelayed(EVENT_SLIDE_TO_FINISH, DELAYED_TIME);
                    }
                    break;
                case EVENT_SLIDE_FINISHED:
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
}
