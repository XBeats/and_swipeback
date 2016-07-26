package com.xbeats.swipebacksample.dispatchactivity;

import android.animation.Animator;
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

    private GestureDetector mGestureDetector;

    private final int mMarginThreshold = 60;  //px 拦截手势区间 0~60
    private final int mShadowWidth = 50; //px 阴影宽度
    private boolean mIsSliding; //是否正在滑动
    private boolean mIsSlideAnimPlaying; //滑动动画展示过程中
    private float mDistanceX;  //px 当前滑动距离 （正数或0）

    private FrameLayout mContentView;
    public FrameLayout getContentView() {
        if(mContentView == null) {
            mContentView = (FrameLayout) findViewById(Window.ID_ANDROID_CONTENT);
        }
        return mContentView;
    }

    private int getWindowBackgroundColor() {
        TypedArray array = null;
        try {
            array = getTheme().obtainStyledAttributes(new int[]{
                    android.R.attr.windowBackground,
            });
            return array.getColor(0, getResources().getColor(android.R.color.transparent));
        }finally {
            if (array != null) {
                array.recycle();
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGestureDetector = new GestureDetector(this, new CustomGestureListener());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if(!isSupportSlideBack())return super.dispatchTouchEvent(ev);

        if(mIsSlideAnimPlaying)return true;

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                final int x = (int) (ev.getX());
                boolean isSlideAction = x >= 0 && x < mMarginThreshold;
                if(isSlideAction && !mIsSliding) {
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
                    mIsSliding = false;
                    handler.sendEmptyMessage(MSG_ACTION_UP);
                }
                break;
            default:
                mIsSliding = false;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private class CustomGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            handler.sendEmptyMessage(MSG_ACTION_DOWN);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mDistanceX = mDistanceX + (-distanceX);
            if(mDistanceX < 0)mDistanceX = 0;
            handler.sendEmptyMessage(MSG_ACTION_MOVE);
            return true;
        }
    }

    private static final int MSG_ACTION_DOWN = 1; //点击事件
    private static final int MSG_ACTION_MOVE = 2; //滑动事件
    private static final int MSG_ACTION_UP = 3;  //点击结束
    private static final int MSG_TO_SLIDE_CLOSE = 4; //开始滑动，不返回上一页面
    private static final int MSG_SLIDE_CLOSED = 5;  //结束滑动，不返回上一页面
    private static final int MSG_TO_SLIDE_BACK = 6; //开始滑动，返回上一页面
    private static final int MSG_BACK = 7;//结束滑动，返回上一页面

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final int width = getResources().getDisplayMetrics().widthPixels;
            final FrameLayout contentView = getContentView();
            switch (msg.what) {
                case MSG_ACTION_DOWN:
                    CustomApplication application = (CustomApplication) getApplication();
                    Activity preActivity = application.getActivityLifecycleHelper().getPreActivity();
                    if(preActivity == null)return;

                    //关闭输入法
                    InputMethodManager inputMethod = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    View view = getCurrentFocus();
                    if (view != null) {
                        inputMethod.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                    ShadowView shadowView = new ShadowView(BaseActivity.this);
                    FrameLayout.LayoutParams layoutParams =
                            new FrameLayout.LayoutParams(mShadowWidth, FrameLayout.LayoutParams.MATCH_PARENT);
                    contentView.addView(shadowView, 0, layoutParams);

                    ViewGroup preContentView = (ViewGroup) preActivity.findViewById(android.R.id.content);
                    View addPreView = preContentView.getChildAt(0);
                    preContentView.removeView(addPreView);
                    contentView.addView(addPreView, 0);

                    if(contentView.getChildCount() >= 3) {
                        View curView = contentView.getChildAt(2);
                        if(curView.getBackground() == null) {
                            int color = getWindowBackgroundColor();
                            curView.setBackgroundColor(color);
                        }
                    }
                    break;
                case MSG_ACTION_MOVE:
                    onSliding();
                    break;
                case MSG_ACTION_UP:
                    if(mDistanceX == 0) {
                        resetPreView(0);
                    }else if(mDistanceX > width / 4) {
                        handler.sendEmptyMessage(MSG_TO_SLIDE_BACK);
                    } else {
                        handler.sendEmptyMessage(MSG_TO_SLIDE_CLOSE);
                    }
                    break;
                case MSG_TO_SLIDE_CLOSE:
                    startSlideAnim(true);
                    break;
                case MSG_SLIDE_CLOSED:
                    mDistanceX = 0;
                    mIsSliding = false;
                    contentView.removeViewAt(1);
                    resetPreView(0);
                    break;
                case MSG_TO_SLIDE_BACK:
                    startSlideAnim(false);
                    break;
                case MSG_BACK:
                    final View preView = contentView.getChildAt(0);
                    DisplayView customSurfaceView = new DisplayView(BaseActivity.this);
                    contentView.addView(customSurfaceView, 0);
                    customSurfaceView.setCopyView(preView);
                    resetPreView(1);
                    finish();
                    overridePendingTransition(0, 0);
                    break;
                default:break;
            }
        }
    };

    /**
     * 手动处理滑动事件
     */
    private synchronized void onSliding() {
        final int width = getResources().getDisplayMetrics().widthPixels;
        FrameLayout contentView = getContentView();
        View preView = contentView.getChildAt(0);
        View shadowView = contentView.getChildAt(1);
        View curView = contentView.getChildAt(2);
        if(preView == null || curView == null || shadowView == null)return;
        preView.setX(-width/3 + mDistanceX/3);
        shadowView.setX(mDistanceX - mShadowWidth);
        curView.setX(mDistanceX);
    }

    /**
     * 重置上个activity的view状态
     * @param preViewIndex
     */
    private void resetPreView(int preViewIndex) {
        CustomApplication application = (CustomApplication) getApplication();
        Activity preActivity = application.getActivityLifecycleHelper().getPreActivity();
        if(preActivity == null)return;
        final FrameLayout contentView = getContentView();
        final View preView = contentView.getChildAt(preViewIndex);
        contentView.removeView(preView);
        ViewGroup preContentView = (ViewGroup) preActivity.findViewById(android.R.id.content);
        preContentView.addView(preView);
    }

    /**
     * 开始自动滑动动画
     * @param isBack 是不是要返回（true则不关闭当前页面）
     */
    private void startSlideAnim(final boolean isBack) {
        final FrameLayout contentView = getContentView();
        final View preView = contentView.getChildAt(0);
        final View shadowView = contentView.getChildAt(1);
        final View curView = contentView.getChildAt(2);
        if(preView == null || curView == null)return;

        final int width = getResources().getDisplayMetrics().widthPixels;
        final int ANIMATION_DURATION = isBack ? 150 : 300;

        float preViewStart = mDistanceX / 3 - width / 3;
        float preViewStop = isBack ? -width / 3 : 0;

        float shadowViewStart = mDistanceX - mShadowWidth;
        float shadowViewEnd = isBack ? mShadowWidth : width + mShadowWidth;

        float curViewStart = mDistanceX;
        float curViewStop = isBack ? 0 : width;

        Interpolator sExpandInterpolator = new DecelerateInterpolator(2f);
        AnimatorSet tranAnimSet = new AnimatorSet();
        tranAnimSet.setDuration(ANIMATION_DURATION);

        ObjectAnimator preViewAnim = new ObjectAnimator();
        preViewAnim.setInterpolator(sExpandInterpolator);
        preViewAnim.setProperty(View.TRANSLATION_X);
        preViewAnim.setFloatValues(preViewStart, preViewStop);
        preViewAnim.setTarget(preView);

        ObjectAnimator shadowViewAnim = new ObjectAnimator();
        shadowViewAnim.setInterpolator(sExpandInterpolator);
        shadowViewAnim.setProperty(View.TRANSLATION_X);
        shadowViewAnim.setFloatValues(shadowViewStart, shadowViewEnd);
        shadowViewAnim.setTarget(shadowView);

        ObjectAnimator curViewAnim = new ObjectAnimator();
        curViewAnim.setInterpolator(sExpandInterpolator);
        curViewAnim.setProperty(View.TRANSLATION_X);
        curViewAnim.setFloatValues(curViewStart, curViewStop);
        curViewAnim.setTarget(curView);

        tranAnimSet.playTogether(preViewAnim, shadowViewAnim, curViewAnim);
        tranAnimSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsSlideAnimPlaying = false;
                if(isBack) {
                    preView.setX(0);
                    shadowView.setX(-mShadowWidth);
                    curView.setX(0);
                    handler.sendEmptyMessage(MSG_SLIDE_CLOSED);
                } else {
                    handler.sendEmptyMessage(MSG_BACK);
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
        mIsSlideAnimPlaying = true;
    }

    /**
     * 是否支持滑动返回
     * @return
     */
    protected boolean isSupportSlideBack() {
        return true;
    }

    private static class DisplayView extends View{

        private View mView;
        public DisplayView(Context context) {
            super(context);
        }

        public void setCopyView(View view) {
            mView = view;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if(mView != null) {
                mView.draw(canvas);
                mView = null;
            }
        }
    }

    private static class ShadowView extends View {
        private Drawable mShadowDrawable;
        public ShadowView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            drawShadow(canvas);
        }

        public void drawShadow(Canvas canvas) {
            if(mShadowDrawable == null) {
                int colors[] = {0x00000000, 0x17000000, 0x43000000};//分别为开始颜色，中间夜色，结束颜色
                mShadowDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
            }
            mShadowDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
            mShadowDrawable.draw(canvas);
        }
    }
}
