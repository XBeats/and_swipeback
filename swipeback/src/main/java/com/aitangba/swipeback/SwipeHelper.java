package com.aitangba.swipeback;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

public class SwipeHelper implements SwipeIntercept {

    private static final String TAG = "SwipeBackHelper";

    private static final int MSG_ACTION_DOWN = 1; //点击事件
    private static final int MSG_ACTION_UP = 3;  //点击结束
    private static final int MSG_SLIDE_CANCEL = 4; //开始滑动，不返回前一个页面
    private static final int MSG_SLIDE_CANCELED = 5;  //结束滑动，不返回前一个页面
    private static final int MSG_SLIDE_PROCEED = 6; //开始滑动，返回前一个页面
    private static final int MSG_SLIDE_FINISHED = 7;//结束滑动，返回前一个页面

    private static final int EDGE_SIZE = 20;  //dp 默认拦截手势区间
    private static final int SHADOW_WIDTH = 50; //px 阴影宽度

    private final boolean mIsSupportSlideBack; // 是否支持滑动返回
    private final int mTouchSlop; // 判断滑动事件触发
    private final int mEdgeSize;  //px 拦截手势区间

    private boolean mIsInThresholdArea; // 点击事件是否在监控区域（action_down事件不在范围内，则后续所有事件都不作处理）
    private float mLastPointX;  //记录手势在屏幕上的X轴坐标

    private boolean mIsSlideAnimPlaying; //滑动动画展示过程中
    private boolean mIsSliding; //是否正在滑动
    private float mDistanceX;  //px 当前滑动距离 （正数或0）

    private final ViewManager mViewManager;
    private final Activity mActivity;
    private final FrameLayout mCurrentContentView;
    private AnimatorSet mAnimatorSet;

    public SwipeHelper(SlideBackManager slideBackManager) {
        mActivity = slideBackManager.getSlideActivity();
        mViewManager = new ViewManager();
        mIsSupportSlideBack = slideBackManager.supportSlideBack();
        mCurrentContentView = (FrameLayout) mActivity.findViewById(Window.ID_ANDROID_CONTENT);

        mTouchSlop = ViewConfiguration.get(mActivity).getScaledTouchSlop();

        final float density = mActivity.getResources().getDisplayMetrics().density;
        mEdgeSize = (int) (EDGE_SIZE * density + 0.5f); //滑动拦截事件的区域
    }

    @Override
    public boolean processTouchEvent(MotionEvent ev) {
        if (!mIsSupportSlideBack) { //不支持滑动返回，则手势事件交给View处理
            return false;
        }

        if (mIsSlideAnimPlaying) {  //正在滑动动画播放中，直接消费手势事件
            return true;
        }

        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            mLastPointX = ev.getRawX();
            mIsInThresholdArea = mLastPointX >= 0 && mLastPointX <= mEdgeSize;
        }

        if (!mIsInThresholdArea) {  //不满足滑动区域，不做处理
            return false;
        }

        final int actionIndex = ev.getActionIndex();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                changeStatus(MSG_ACTION_DOWN);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (mIsSliding) {  //有第二个手势事件加入，而且正在滑动事件中，则直接消费事件
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                //一旦触发滑动机制，拦截所有其他手指的滑动事件
                if (actionIndex != 0) {
                    return mIsSliding;
                }

                final float curPointX = ev.getRawX();

                boolean isSliding = mIsSliding;
                if (!isSliding) {
                    if (Math.abs(curPointX - mLastPointX) < mTouchSlop) { //判断是否满足滑动
                        return false;
                    } else {
                        mIsSliding = true;
                    }
                }
                onSliding(curPointX);
                if (isSliding == mIsSliding) {
                    return true;
                } else {
                    MotionEvent cancelEvent = MotionEvent.obtain(ev); //首次判定为滑动需要修正事件：手动修改事件为 ACTION_CANCEL，并通知底层View
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                    mActivity.getWindow().superDispatchTouchEvent(cancelEvent);
                    return true;
                }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_OUTSIDE:
                if (mDistanceX == 0) { //没有进行滑动
                    mIsSliding = false;
                    changeStatus(MSG_ACTION_UP);
                    return false;
                }

                if (mIsSliding && actionIndex == 0) { // 取消滑动 或 手势抬起 ，而且手势事件是第一手势，开始滑动动画
                    mIsSliding = false;
                    changeStatus(MSG_ACTION_UP);
                    return true;
                } else if (mIsSliding) {
                    return true;
                }
                break;
            default:
                mIsSliding = false;
                break;
        }
        return false;
    }

    @Override
    public void finishSwipeImmediately() {
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }
    }

    /**
     * 处理事件（滑动事件除外）
     * @param status
     */
    private void changeStatus(int status) {
        switch (status) {
            case MSG_ACTION_DOWN:
                // hide input method
                InputMethodManager inputMethod = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                View view = mActivity.getCurrentFocus();
                if (view != null && inputMethod != null) {
                    inputMethod.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                if (!mViewManager.addViewFromPreviousActivity()) return;
                // add shadow view on the left of content view
                mViewManager.addShadowView();
                if (mCurrentContentView.getChildCount() >= 3) {
                    View curView = mViewManager.getDisplayView();
                    if (curView.getBackground() == null) {
                        int color = getWindowBackgroundColor();
                        curView.setBackgroundColor(color);
                    }
                }
                break;
            case MSG_ACTION_UP:
                final int width = mActivity.getResources().getDisplayMetrics().widthPixels;
                if (mDistanceX == 0) {
                    if (mCurrentContentView.getChildCount() >= 3) {
                        mViewManager.removeShadowView();
                        mViewManager.resetPreviousView();
                    }
                } else if (mDistanceX > width / 4) {
                    changeStatus(MSG_SLIDE_PROCEED);
                } else {
                    changeStatus(MSG_SLIDE_CANCEL);
                }
                break;
            case MSG_SLIDE_CANCEL:
                startSlideAnim(true);
                break;
            case MSG_SLIDE_CANCELED:
                mDistanceX = 0;
                mIsSliding = false;
                mViewManager.removeShadowView();
                mViewManager.resetPreviousView();
                break;
            case MSG_SLIDE_PROCEED:
                startSlideAnim(false);
                break;
            case MSG_SLIDE_FINISHED:
                mViewManager.addCacheView();
                mViewManager.removeShadowView();
                mViewManager.resetPreviousView();
                break;
            default:
                break;
        }
    }

    /**
     * 手动处理滑动事件
     */
    private void onSliding(float curPointX) {
        final int width = mActivity.getResources().getDisplayMetrics().widthPixels;
        View previewActivityContentView = mViewManager.mPreviousContentView;
        View shadowView = mViewManager.mShadowView;
        View currentActivityContentView = mViewManager.getDisplayView();

        if (previewActivityContentView == null || currentActivityContentView == null || shadowView == null) {
            changeStatus(MSG_SLIDE_CANCELED);
            return;
        }

        final float distanceX = curPointX - mLastPointX;
        mLastPointX = curPointX;
        mDistanceX = mDistanceX + distanceX;
        if (mDistanceX < 0) {
            mDistanceX = 0;
        }

        previewActivityContentView.setX((-width + mDistanceX) / 3);
        shadowView.setX(mDistanceX - SHADOW_WIDTH);
        currentActivityContentView.setX(mDistanceX);
    }

    /**
     * 开始自动滑动动画
     *
     * @param slideCanceled 是不是要返回（true则不关闭当前页面）
     */
    private void startSlideAnim(final boolean slideCanceled) {
        final View previewView = mViewManager.mPreviousContentView;
        final View shadowView = mViewManager.mShadowView;
        final View currentView = mViewManager.getDisplayView();

        if (previewView == null || currentView == null) {
            return;
        }

        int width = mActivity.getResources().getDisplayMetrics().widthPixels;
        Interpolator interpolator = new DecelerateInterpolator(2f);

        // preview activity's animation
        ObjectAnimator previewViewAnim = new ObjectAnimator();
        previewViewAnim.setInterpolator(interpolator);
        previewViewAnim.setProperty(View.TRANSLATION_X);
        float preViewStart = (mDistanceX - width) / 3 ;
        float preViewStop = slideCanceled ? (-(float) width / 3) : 0;
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
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.setDuration(slideCanceled ? 150 : 300);
        mAnimatorSet.playTogether(previewViewAnim, shadowViewAnim, currentViewAnim);
        mAnimatorSet.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                animation.removeAllListeners();
                if(mAnimatorSet != null) {
                    mAnimatorSet.removeListener(this);
                }
                if (slideCanceled) {
                    mIsSlideAnimPlaying = false;
                    previewView.setX(0);
                    shadowView.setX(-SHADOW_WIDTH);
                    currentView.setX(0);
                    changeStatus(MSG_SLIDE_CANCELED);
                } else {
                    changeStatus(MSG_SLIDE_FINISHED);
                }
            }
        });
        mAnimatorSet.start();
        mIsSlideAnimPlaying = true;
    }

    private class ViewManager {
        private Activity mPreviousActivity;
        private View mPreviousContentView;
        private View mShadowView;


        /**
         * Remove view from previous Activity and add into current Activity
         *
         * @return Is view added successfully
         */
        private boolean addViewFromPreviousActivity() {
            if (mCurrentContentView.getChildCount() == 0) {
                mPreviousActivity = null;
                mPreviousContentView = null;
                return false;
            }
            mPreviousActivity = ActivityLifecycleHelper.getPreviousActivity();
            if (mPreviousActivity == null) {
                mPreviousContentView = null;
                return false;
            }
            //Previous activity not support to be swipeBack...
            if (mPreviousActivity instanceof SlideBackManager &&
                    !((SlideBackManager) mPreviousActivity).canBeSlideBack()) {
                mPreviousActivity = null;
                mPreviousContentView = null;
                return false;
            }
            ViewGroup previousActivityContainer = (ViewGroup) mPreviousActivity.findViewById(Window.ID_ANDROID_CONTENT);
            if (previousActivityContainer == null || previousActivityContainer.getChildCount() == 0) {
                mPreviousActivity = null;
                mPreviousContentView = null;
                return false;
            }
            mPreviousContentView = previousActivityContainer.getChildAt(0);
            previousActivityContainer.removeView(mPreviousContentView);
            mCurrentContentView.addView(mPreviousContentView, 0);
            return true;
        }

        /**
         * Remove the PreviousContentView at current Activity and put it into previous Activity.
         */
        private void resetPreviousView() {
            if (mPreviousContentView == null) return;
            View view = mPreviousContentView;
            view.setX(0);
            mCurrentContentView.removeView(view);
            mPreviousContentView = null;
            if (mPreviousActivity == null || mPreviousActivity.isFinishing()) return;
            Activity preActivity = mPreviousActivity;
            final ViewGroup previewContentView = (ViewGroup) preActivity.findViewById(Window.ID_ANDROID_CONTENT);
            previewContentView.addView(view);
            mPreviousActivity = null;
        }

        /**
         * add shadow view on the left of content view
         */
        private synchronized void addShadowView() {
            if (mShadowView == null) {
                mShadowView = new ShadowView(mActivity);
                mShadowView.setX(-SHADOW_WIDTH);
            }
            final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    SHADOW_WIDTH, FrameLayout.LayoutParams.MATCH_PARENT);
            if (this.mShadowView.getParent() == null) {
                mCurrentContentView.addView(this.mShadowView, 1, layoutParams);
            } else {
                this.removeShadowView();
                this.addShadowView();
            }
        }

        private void removeShadowView() {
            if (mShadowView == null) return;
            mCurrentContentView.removeView(mShadowView);
            mShadowView = null;
        }

        private void addCacheView() {
            final View previousView = mPreviousContentView;
            PreviousPageView previousPageView = new PreviousPageView(mActivity);
            mCurrentContentView.addView(previousPageView, 0);
            previousPageView.cacheView(previousView);
        }


        private View getDisplayView() {
            int index = 0;
            if (mViewManager.mPreviousContentView != null) {
                index = index + 1;
            }
            if (mViewManager.mShadowView != null) {
                index = index + 1;
            }
            return mCurrentContentView.getChildAt(index);
        }
    }

    private int getWindowBackgroundColor() {
        TypedArray array = null;
        try {
            array = mActivity.getTheme().obtainStyledAttributes(new int[]{android.R.attr.windowBackground});
            return array.getColor(0, ContextCompat.getColor(mActivity, android.R.color.transparent));
        } catch (Exception e) {
            return ContextCompat.getColor(mActivity, android.R.color.transparent);
        } finally {
            if (array != null) {
                array.recycle();
            }
        }
    }
}
