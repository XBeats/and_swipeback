package com.aitangba.swipeback;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

public class SwipeHelper implements SwipeIntercept {

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
        mCurrentContentView = getContentView(mActivity);

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
                if (!mViewManager.addViews()) {
                    return false;
                }
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
                    onActionUp();
                    return false;
                }

                if (mIsSliding && actionIndex == 0) { // 取消滑动 或 手势抬起 ，而且手势事件是第一手势，开始滑动动画
                    mIsSliding = false;
                    onActionUp();
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

    private void onActionUp() {
        final int width = mActivity.getResources().getDisplayMetrics().widthPixels;
        if (mDistanceX == 0) {
            resetViewsAndStatus();
        } else if (mDistanceX > width / 4) {
            startSlideAnim(false);
        } else {
            startSlideAnim(true);
        }
    }

    private void resetViewsAndStatus() {
        mDistanceX = 0;
        mIsSliding = false;
        mIsSlideAnimPlaying = false;

        mViewManager.removeViews();
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
            resetViewsAndStatus();
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
        float preViewStart = mDistanceX / 3 - width / 3;
        float preViewStop = slideCanceled ? -width / 3 : 0;
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
                mAnimatorSet.cancel();
                if (slideCanceled) {
                    resetViewsAndStatus();
                } else {
                    mActivity.finish();
                    mActivity.overridePendingTransition(android.R.anim.fade_in, R.anim.hold_on);
                }
            }
        });
        mAnimatorSet.start();
        mIsSlideAnimPlaying = true;
    }

    private FrameLayout getContentView(Activity activity) {
        return (FrameLayout) activity.findViewById(Window.ID_ANDROID_CONTENT);
    }

    private class ViewManager {
        private Activity mPreviousActivity;
        private PreviousPageView mPreviousContentView;
        private View mShadowView;

        /**
         * Remove view from previous Activity and add into current Activity
         *
         * @return Is view added successfully
         */
        private boolean addViews() {
            if (mCurrentContentView.getChildCount() == 0) {
                mPreviousActivity = null;
                mPreviousContentView = null;
                return false;
            }

            mPreviousActivity = ActivityLifecycleHelper.getPreviousActivity();
            if (mPreviousActivity == null) {
                mPreviousActivity = null;
                mPreviousContentView = null;
                return false;
            }

            // previous activity not support to be swipeBack...
            if (mPreviousActivity instanceof SlideBackManager &&
                    !((SlideBackManager) mPreviousActivity).canBeSlideBack()) {
                mPreviousActivity = null;
                mPreviousContentView = null;
                return false;
            }

            ViewGroup previousActivityContainer = getContentView(mPreviousActivity);
            if (previousActivityContainer == null || previousActivityContainer.getChildCount() == 0) {
                mPreviousActivity = null;
                mPreviousContentView = null;
                return false;
            }

            // add shadow view on the left of content view
            mShadowView = new ShadowView(mActivity);
            mShadowView.setX(-SHADOW_WIDTH);
            final FrameLayout.LayoutParams shadowLayoutParams = new FrameLayout.LayoutParams(
                    SHADOW_WIDTH, FrameLayout.LayoutParams.MATCH_PARENT);
            mCurrentContentView.addView(this.mShadowView, 0, shadowLayoutParams);

            // add the cache view which cache the view of previous activity
            View view = previousActivityContainer.getChildAt(0);
            mPreviousContentView = new PreviousPageView(mActivity);
            mPreviousContentView.cacheView(view);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(view.getMeasuredWidth(), view.getMeasuredHeight());
            mCurrentContentView.addView(mPreviousContentView, 0, layoutParams);
            return true;
        }

        private void removeViews() {
            // remove the shadowView at current Activity
            if (mShadowView != null) {
                mCurrentContentView.removeView(mShadowView);
                mShadowView = null;
            }

            // remove the previousContentView at current Activity
            if (mPreviousContentView != null) {
                mPreviousContentView.cacheView(null);
                mCurrentContentView.removeView(mPreviousContentView);
                mPreviousContentView = null;
            }

            mPreviousActivity = null;
        }

        private View getDisplayView() {
            int index = 0;
            if (mPreviousContentView != null) {
                index = index + 1;
            }

            if (mShadowView != null) {
                index = index + 1;
            }
            return mCurrentContentView.getChildAt(index);
        }
    }
}
