package com.xbeats.swipebacksample.dispatchactivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.xbeats.swipebacksample.common.CustomApplication;


/**
 * Created by fhf11991 on 2016/7/25.
 */
public class BaseActivity extends AppCompatActivity {
    private static final int MSG_ACTION_DOWN = 1; //点击事件
    private static final int MSG_ACTION_MOVE = 2; //滑动事件
    private static final int MSG_ACTION_UP = 3;  //点击结束
    private static final int MSG_SLIDE_CANCEL = 4; //开始滑动，不返回前一个页面
    private static final int MSG_SLIDE_CANCELED = 5;  //结束滑动，不返回前一个页面
    private static final int MSG_SLIDE_PROCEED = 6; //开始滑动，返回前一个页面
    private static final int MSG_SLIDE_FINISHED = 7;//结束滑动，返回前一个页面

    private static final int SHADOW_WIDTH = 50; //px 阴影宽度
    private static final int MARGIN_THRESHOLD = 60;  //px 拦截手势区间 0~60

    private GestureDetector mGestureDetector;
    private FrameLayout mContentView;

    private boolean mIsSliding; //是否正在滑动
    private boolean mIsSlideAnimPlaying; //滑动动画展示过程中
    private float mDistanceX;  //px 当前滑动距离 （正数或0）

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGestureDetector = new GestureDetector(this, new SlideGestureListener());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!supportSlideBack()) {
            return super.dispatchTouchEvent(ev);
        }

        if (mIsSlideAnimPlaying) {
            return true;
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                final int x = (int) (ev.getX());
                boolean inThresholdArea = x >= 0 && x < MARGIN_THRESHOLD;
                if (inThresholdArea && !mIsSliding) {
                    mIsSliding = true;
                    return mGestureDetector.onTouchEvent(ev);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mIsSliding) {
                    return mGestureDetector.onTouchEvent(ev);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mIsSliding) {
                    mIsSliding = false;
                    mActionHandler.sendEmptyMessage(MSG_ACTION_UP);
                }
                break;

            default:
                mIsSliding = false;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private class SlideGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            mActionHandler.sendEmptyMessage(MSG_ACTION_DOWN);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mDistanceX = mDistanceX - distanceX;
            if (mDistanceX < 0) {
                mDistanceX = 0;
            }
            mActionHandler.sendEmptyMessage(MSG_ACTION_MOVE);
            return true;
        }
    }

    private Handler mActionHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final int width = getResources().getDisplayMetrics().widthPixels;
            final FrameLayout contentView = getContentView();

            switch (msg.what) {
                case MSG_ACTION_DOWN:
                    CustomApplication application = (CustomApplication) getApplication();
                    Activity previousActivity = application.getActivityLifecycleHelper().getPreActivity();
                    if (previousActivity == null) {
                        return;
                    }

                    // hide input method
                    //关闭输入法
                    InputMethodManager inputMethod = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    View view = getCurrentFocus();
                    if (view != null) {
                        inputMethod.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                    // add shadow view on the left of content view
                    ShadowView shadowView = new ShadowView(BaseActivity.this);
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                            SHADOW_WIDTH, FrameLayout.LayoutParams.MATCH_PARENT);
                    contentView.addView(shadowView, 0, layoutParams);

                    // add content view
                    ViewGroup previousActivityContainer = (ViewGroup) previousActivity.findViewById(android.R.id.content);
                    View previewActivityContentView = previousActivityContainer.getChildAt(0);
                    previousActivityContainer.removeView(previewActivityContentView);
                    contentView.addView(previewActivityContentView, 0);

                    if (contentView.getChildCount() >= 3) {
                        View curView = contentView.getChildAt(2);
                        if (curView.getBackground() == null) {
                            int color = getWindowBackgroundColor();
                            curView.setBackgroundColor(color);
                        }
                    }
                    break;

                case MSG_ACTION_MOVE:
                    onSliding();
                    break;

                case MSG_ACTION_UP:
                    if (mDistanceX == 0) {
                        contentView.removeViewAt(1);
                        resetPreviewView(0);
                    } else if (mDistanceX > width / 4) {
                        mActionHandler.sendEmptyMessage(MSG_SLIDE_PROCEED);
                    } else {
                        mActionHandler.sendEmptyMessage(MSG_SLIDE_CANCEL);
                    }
                    break;

                case MSG_SLIDE_CANCEL:
                    startSlideAnim(true);
                    break;

                case MSG_SLIDE_CANCELED:
                    mDistanceX = 0;
                    mIsSliding = false;
                    contentView.removeViewAt(1);
                    resetPreviewView(0);
                    break;

                case MSG_SLIDE_PROCEED:
                    startSlideAnim(false);
                    break;

                case MSG_SLIDE_FINISHED:
                    View previousView = contentView.getChildAt(0);
                    PreviousPageView previousPageView = new PreviousPageView(BaseActivity.this);
                    contentView.addView(previousPageView, 0);
                    previousPageView.cacheView(previousView);
                    resetPreviewView(1);
                    finish();
                    overridePendingTransition(0, 0);
                    break;

                default:
                    break;
            }
        }
    };

    protected FrameLayout getContentView() {
        if (mContentView == null) {
            mContentView = (FrameLayout) findViewById(Window.ID_ANDROID_CONTENT);
        }
        return mContentView;
    }

    private int getWindowBackgroundColor() {
        TypedArray array = null;
        try {
            array = getTheme().obtainStyledAttributes(new int[]{android.R.attr.windowBackground});
            return array.getColor(0, ContextCompat.getColor(this, android.R.color.transparent));
        } finally {
            if (array != null) {
                array.recycle();
            }
        }
    }

    /**
     * 手动处理滑动事件
     */
    private synchronized void onSliding() {
        final int width = getResources().getDisplayMetrics().widthPixels;
        FrameLayout contentView = getContentView();
        View previewActivityContentView = contentView.getChildAt(0);
        View shadowView = contentView.getChildAt(1);
        View currentActivityContentView = contentView.getChildAt(2);

        if (previewActivityContentView == null || currentActivityContentView == null || shadowView == null){
            return;
        }

        previewActivityContentView.setX(-width / 3 + mDistanceX / 3);
        shadowView.setX(mDistanceX - SHADOW_WIDTH);
        currentActivityContentView.setX(mDistanceX);
    }

    /**
     * 重置上个activity的view状态
     *
     * @param viewIndex
     */
    private void resetPreviewView(int viewIndex) {
        CustomApplication application = (CustomApplication) getApplication();
        Activity preActivity = application.getActivityLifecycleHelper().getPreActivity();
        if (preActivity == null) {
            return;
        }
        FrameLayout contentView = getContentView();
        View view = contentView.getChildAt(viewIndex);
        contentView.removeView(view);
        ViewGroup previewContentView = (ViewGroup) preActivity.findViewById(android.R.id.content);
        previewContentView.addView(view);
    }

    /**
     * 开始自动滑动动画
     *
     * @param slideCanceled 是不是要返回（true则不关闭当前页面）
     */
    private void startSlideAnim(final boolean slideCanceled) {
        final FrameLayout contentView = getContentView();
        final View previewView = contentView.getChildAt(0);
        final View shadowView = contentView.getChildAt(1);
        final View currentView = contentView.getChildAt(2);

        if (previewView == null || currentView == null) {
            return;
        }

        int width = getResources().getDisplayMetrics().widthPixels;
        Interpolator interpolator = new DecelerateInterpolator(2f);

        // preview activity's animation
        ObjectAnimator previewViewAnim = new ObjectAnimator();
        previewViewAnim.setInterpolator(interpolator);
        previewViewAnim.setProperty(View.TRANSLATION_X);
        float preViewStart = mDistanceX / 3 - width / 3;
        float preViewStop = slideCanceled ? - width / 3 : 0;
        previewViewAnim.setFloatValues(preViewStart, preViewStop);
        previewViewAnim.setTarget(previewView);

        // shadow view's animation
        ObjectAnimator shadowViewAnim = new ObjectAnimator();
        shadowViewAnim.setInterpolator(interpolator);
        shadowViewAnim.setProperty(View.TRANSLATION_X);
        float shadowViewStart = mDistanceX - SHADOW_WIDTH;
        float shadowViewEnd = slideCanceled ? SHADOW_WIDTH : width + SHADOW_WIDTH;
        shadowViewAnim.setFloatValues(shadowViewStart, shadowViewEnd);
        shadowViewAnim.setTarget(shadowView);

        // current view's animation
        ObjectAnimator currentViewAnim = new ObjectAnimator();
        currentViewAnim.setInterpolator(interpolator);
        currentViewAnim.setProperty(View.TRANSLATION_X);
        float curViewStart = mDistanceX;
        float curViewStop = slideCanceled ? 0 : width;
        currentViewAnim.setFloatValues(curViewStart, curViewStop);
        currentViewAnim.setTarget(currentView);

        // play animation together
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(slideCanceled ? 150 : 300);
        animatorSet.playTogether(previewViewAnim, shadowViewAnim, currentViewAnim);
        animatorSet.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsSlideAnimPlaying = false;
                if (slideCanceled) {
                    previewView.setX(0);
                    shadowView.setX(-SHADOW_WIDTH);
                    currentView.setX(0);
                    mActionHandler.sendEmptyMessage(MSG_SLIDE_CANCELED);
                } else {
                    mActionHandler.sendEmptyMessage(MSG_SLIDE_FINISHED);
                }
            }
        });
        animatorSet.start();
        mIsSlideAnimPlaying = true;
    }

    /**
     * 是否支持滑动返回
     *
     * @return
     */
    protected boolean supportSlideBack() {
        return true;
    }

    private class PreviousPageView extends View {
        private View mView;

        public PreviousPageView(Context context) {
            super(context);
        }

        public void cacheView(View view) {
            mView = view;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (mView != null) {
                mView.draw(canvas);
                mView = null;
            }
        }
    }

    private class ShadowView extends View {
        private Drawable mDrawable;

        public ShadowView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (mDrawable == null) {
                int colors[] = {0x00000000, 0x17000000, 0x43000000};//分别为开始颜色，中间夜色，结束颜色
                mDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
            }
            mDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
            mDrawable.draw(canvas);
        }
    }
}
