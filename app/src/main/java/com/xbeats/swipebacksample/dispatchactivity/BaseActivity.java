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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
    private static final String CURRENT_POINT_X = "currentPointX"; //点击事件

    private static final int MSG_ACTION_DOWN = 1; //点击事件
    private static final int MSG_ACTION_MOVE = 2; //滑动事件
    private static final int MSG_ACTION_UP = 3;  //点击结束
    private static final int MSG_SLIDE_CANCEL = 4; //开始滑动，不返回前一个页面
    private static final int MSG_SLIDE_CANCELED = 5;  //结束滑动，不返回前一个页面
    private static final int MSG_SLIDE_PROCEED = 6; //开始滑动，返回前一个页面
    private static final int MSG_SLIDE_FINISHED = 7;//结束滑动，返回前一个页面

    private static final int SHADOW_WIDTH = 50; //px 阴影宽度
    private static final int MARGIN_THRESHOLD = 60;  //px 拦截手势区间 0~60

    private FrameLayout mContentView;
    private View mPreviousActivityContentView;
    private View mShadowView;

    private boolean mIsSliding; //是否正在滑动
    private boolean mIsSlideAnimPlaying; //滑动动画展示过程中
    private float mDistanceX;  //px 当前滑动距离 （正数或0）
    private float mLastPointX;  //记录手势在屏幕上的X轴坐标

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(!supportSlideBack() || isFinishing()) { //不支持滑动返回，则手势事件交给View处理
            return super.dispatchTouchEvent(ev);
        }

        if(mIsSlideAnimPlaying) {  //正在滑动动画播放中，直接消费手势事件
            return true;
        }

        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        final int actionIndex = ev.getActionIndex();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastPointX = ev.getRawX();
                boolean inThresholdArea = mLastPointX >= 0 && mLastPointX <= MARGIN_THRESHOLD;

                if(inThresholdArea && mIsSliding) {
                    return true;
                } else if(inThresholdArea && !mIsSliding) { //开始滑动
                    mIsSliding = true;
                    mActionHandler.sendEmptyMessage(MSG_ACTION_DOWN);
                    return true;
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if(mIsSliding) {  //有第二个手势事件加入，而且正在滑动事件中，则直接消费事件
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mIsSliding && actionIndex == 0) { //开始滑动
                    Message message = mActionHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putFloat(CURRENT_POINT_X, ev.getRawX());
                    message.what = MSG_ACTION_MOVE;
                    message.setData(bundle);
                    mActionHandler.sendMessage(message);
                    return true;
                } else if(mIsSliding && actionIndex != 0){
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_UP:
                if (mIsSliding && actionIndex == 0) { // 取消滑动 或 手势抬起 ，而且手势事件是第一手势，开始滑动动画
                    mIsSliding = false;
                    mActionHandler.sendEmptyMessage(MSG_ACTION_UP);
                    return true;
                } else if (mIsSliding && actionIndex != 0) {
                    return true;
                }
                break;
            default:
                mIsSliding = false;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private Handler mActionHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final int width = getResources().getDisplayMetrics().widthPixels;
            final FrameLayout contentView = getContentView();

            switch (msg.what) {
                case MSG_ACTION_DOWN:
                    // hide input method
                    InputMethodManager inputMethod = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    View view = getCurrentFocus();
                    if (view != null) {
                        inputMethod.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    if (contentView.getChildCount() == 0) return;

                    CustomApplication application = (CustomApplication) getApplication();
                    Activity previousActivity = application.getActivityLifecycleHelper().getPreActivity();
                    if (previousActivity == null) {
                        return;
                    }

                    // add content view
                    ViewGroup previousActivityContainer = (ViewGroup) previousActivity.findViewById(android.R.id.content);
                    if(previousActivityContainer == null || previousActivityContainer.getChildCount() == 0) {
                        return;
                    }

                    View previousActivityContentView = previousActivityContainer.getChildAt(0);
                    if(previousActivityContentView == null) {
                        return;
                    }
                    mPreviousActivityContentView = previousActivityContentView;
                    previousActivityContainer.removeView(previousActivityContentView);
                    contentView.addView(previousActivityContentView, 0);

                    // add shadow view on the left of content view
                    mShadowView = new ShadowView(BaseActivity.this);
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                            SHADOW_WIDTH, FrameLayout.LayoutParams.MATCH_PARENT);
                    mShadowView.setX(-SHADOW_WIDTH);
                    contentView.addView(mShadowView, 1, layoutParams);

                    if (contentView.getChildCount() >= 3) {
                        View curView = getDisplayView(contentView);
                        if (curView.getBackground() == null) {
                            int color = getWindowBackgroundColor();
                            curView.setBackgroundColor(color);
                        }
                    }
                    break;

                case MSG_ACTION_MOVE:
                    final float curPointX = msg.getData().getFloat(CURRENT_POINT_X);
                    onSliding(curPointX);
                    break;

                case MSG_ACTION_UP:
                    if (mDistanceX == 0) {
                        if(contentView.getChildCount() >= 3) {
                            resetShadowView();
                            resetPreviewView();
                        }
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
                    resetShadowView();
                    resetPreviewView();
                    break;

                case MSG_SLIDE_PROCEED:
                    startSlideAnim(false);
                    break;

                case MSG_SLIDE_FINISHED:
                    View previousView = mPreviousActivityContentView;
                    PreviousPageView previousPageView = new PreviousPageView(BaseActivity.this);
                    contentView.addView(previousPageView, 0);
                    previousPageView.cacheView(previousView);
                    resetShadowView();
                    resetPreviewView();
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
    private synchronized void onSliding(float curPointX) {
        final int width = getResources().getDisplayMetrics().widthPixels;
        FrameLayout contentView = getContentView();
        View previewActivityContentView = mPreviousActivityContentView;
        View shadowView = mShadowView;
        View currentActivityContentView = getDisplayView(contentView);

        if (previewActivityContentView == null || currentActivityContentView == null || shadowView == null){
            mActionHandler.sendEmptyMessage(MSG_SLIDE_CANCELED);
            return;
        }

        final float distanceX = curPointX - mLastPointX;
        mLastPointX = curPointX;
        mDistanceX = mDistanceX + distanceX;
        if (mDistanceX < 0) {
            mDistanceX = 0;
        }

        previewActivityContentView.setX(-width / 3 + mDistanceX / 3);
        shadowView.setX(mDistanceX - SHADOW_WIDTH);
        currentActivityContentView.setX(mDistanceX);
    }

    /**
     * 重置上个activity的view状态
     *
     */
    private void resetPreviewView() {
        if (mPreviousActivityContentView == null) return;
        View view = mPreviousActivityContentView;
        FrameLayout contentView = getContentView();
        view.setX(0);
        contentView.removeView(view);

        CustomApplication application = (CustomApplication) getApplication();
        Activity preActivity = application.getActivityLifecycleHelper().getPreActivity();
        if (preActivity == null) return;
        ViewGroup previewContentView = (ViewGroup) preActivity.findViewById(android.R.id.content);
        previewContentView.addView(view);
        mPreviousActivityContentView = null;
    }

    private void resetShadowView() {
        if(mShadowView == null) return;
        FrameLayout contentView = getContentView();
        contentView.removeView(mShadowView);
        mShadowView = null;
    }

    private View getDisplayView(FrameLayout contentView) {
        int index = 0;
        if(mPreviousActivityContentView != null) {
            index = index + 1;
        }

        if(mShadowView != null) {
            index = index + 1;
        }
        return contentView.getChildAt(index);
    }

    /**
     * 开始自动滑动动画
     *
     * @param slideCanceled 是不是要返回（true则不关闭当前页面）
     */
    private void startSlideAnim(final boolean slideCanceled) {
        final FrameLayout contentView = getContentView();
        final View previewView = mPreviousActivityContentView;
        final View shadowView = mShadowView;
        final View currentView = getDisplayView(contentView);

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
                if (slideCanceled) {
                    mIsSlideAnimPlaying = false;
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
