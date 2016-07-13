package com.xbeats.swipebacksample.views;


import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Created by fhf11991 on 2016/7/11.
 */
public class SlideBackLayout extends SlidingPaneLayout{

    private static final String TAG = "CustomSlidingPaneLayout";

    /** Constant value for use with setTouchModeAbove(). Allows the SlidingMenu to be opened with a swipe
     * gesture on the screen's margin
     */
    public static final int TOUCH_MODE_MARGIN = 0;

    /** Constant value for use with setTouchModeAbove(). Allows the SlidingMenu to be opened with a swipe
     * gesture anywhere on the screen
     */
    public static final int TOUCH_MODE_FULLSCREEN = 1;

    private final int DEFAULT_OVERHANG_SIZE = 0; //

    private final int MARGIN_THRESHOLD = 48; // dips


    private int mTouchMode = TOUCH_MODE_MARGIN;
    private int mShadowWidth = 15; //px
    private boolean mIsSlidingAvailable = true;
    private boolean mIsCurrentTouchAllowed;
    private Drawable mShadowDrawable;
    private boolean mIsSliding;
    private int mMarginThreshold;

    public void setShadowWidth(int shadowWidth) {
        mShadowWidth = shadowWidth;
    }

    public void setShadowDrawable(Drawable shadow) {
        mShadowDrawable = shadow;
    }

    public void setSlidingAvailable(boolean slidingAvailable) {
        mIsSlidingAvailable = slidingAvailable;
    }

    public boolean isSliding() {
        return mIsSliding;
    }

    public SlideBackLayout(Context context) {
        this(context, null);
    }

    public SlideBackLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideBackLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        //通过反射改变mOverhangSize的值为0，这个mOverhangSize值为菜单到右边屏幕的最短距离，默认
        //是32dp，现在给它改成0
        try {
            Field field = SlidingPaneLayout.class.getDeclaredField("mOverhangSize");
            field.setAccessible(true);
            field.set(this, DEFAULT_OVERHANG_SIZE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMarginThreshold = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                MARGIN_THRESHOLD, getResources().getDisplayMetrics());
    }

    public void setTouchMode(int i) {
        if (i != TOUCH_MODE_MARGIN && i != TOUCH_MODE_FULLSCREEN) {
            throw new IllegalStateException("TouchMode must be set to either" +
                    "TOUCH_MODE_MARGIN or TOUCH_MODE_FULLSCREEN ");
        }
        mTouchMode = i;
    }

    public void attachViewToActivity(final Activity activity) {
        setPanelSlideListener(new android.support.v4.widget.SlidingPaneLayout.PanelSlideListener() {
            @Override
            public void onPanelOpened(View panel) {

                final TranslateAnimation animation = new TranslateAnimation(0, 100,0, 0);
                animation.setDuration(200);//设置动画持续时间
                activity.overridePendingTransition(0, 0);
                activity.finish();
            }

            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if(mOnScrollHook != null) {
                    final float left = panel.getMeasuredWidth() * slideOffset;
                    mOnScrollHook.onScroll(left);
                }
            }

            @Override
            public void onPanelClosed(View panel) {}
        });
        setSliderFadeColor(getResources().getColor(android.R.color.transparent));

        CustomBehindView leftView = new CustomBehindView(activity);
        leftView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(leftView, 0);

        ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
        final ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
        decorChild.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        decor.removeView(decorChild);
        decor.addView(this);
        addView(decorChild, 1);

        final int width = getResources().getDisplayMetrics().widthPixels;
        mOnScrollContainer = new OnScrollListener() {
            @Override
            public void onScroll(float distance) {
                decorChild.setX(-width / 3 + distance / 3);
            }
        };
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mIsCurrentTouchAllowed = canEventSlide(ev);

        if(mIsCurrentTouchAllowed) {
            return mIsSliding = super.onInterceptTouchEvent(ev);
        } else {
            return mIsSliding = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        mIsCurrentTouchAllowed = canEventSlide(ev);

        if(mIsCurrentTouchAllowed) {
            return mIsSliding = super.onTouchEvent(ev);
        } else {
            return mIsSliding = false;
        }
    }

    private boolean canEventSlide(MotionEvent ev) {
        final boolean isSlideNotAvailable = !mIsSlidingAvailable;
        final boolean isActionDown = ev.getAction() == MotionEvent.ACTION_DOWN;

        boolean isTouchAllowed;

        if(isSlideNotAvailable) {
            isTouchAllowed = false;
        } else if(isActionDown && mTouchMode == TOUCH_MODE_FULLSCREEN) {
            isTouchAllowed = true;
        } else if(isActionDown && mTouchMode == TOUCH_MODE_MARGIN) {
            isTouchAllowed = isTouchAllowed(ev);
        } else {
            isTouchAllowed = mIsCurrentTouchAllowed;
        }
        return isTouchAllowed;
    }

    private boolean isTouchAllowed(MotionEvent ev) {
        int x = (int) (ev.getX());
        View content = getChildAt(1);
        int left = content.getLeft();
        return x >= left && x <= mMarginThreshold + left;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawShadow(canvas);
    }

    public void drawShadow(Canvas canvas) {
        final int left = getChildAt(1).getLeft();
        final int mShadowWidth = this.mShadowWidth; //px

        if(mShadowDrawable == null) {
            int colors[] = {0x00000000, 0x11000000, 0x33000000};//分别为开始颜色，中间夜色，结束颜色
            mShadowDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        }
        mShadowDrawable.setBounds(left - mShadowWidth, 0, left, getHeight());
        mShadowDrawable.draw(canvas);
    }

    public static class CustomBehindView extends View {

        public CustomBehindView(Context context) {
            this(context, null);
        }

        public CustomBehindView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public CustomBehindView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }
    }


    private OnScrollListener mOnScrollHook; //钩子
    private OnScrollListener mOnScrollContainer;  //容器

    public OnScrollListener getOnScrollContainer() {
        return mOnScrollContainer;
    }

    public interface OnScrollListener extends Serializable{
        void onScroll(float distance);
    }

    public void setOnScrollHook(OnScrollListener onScrollHook) {
        mOnScrollHook = onScrollHook;
        if(mOnScrollHook == null) {
            mIsSlidingAvailable = false;
        }
        mOnScrollListener = null;
    }

    private static SlideBackLayout.OnScrollListener mOnScrollListener; //中间作用

    public static SlideBackLayout.OnScrollListener getOnScrollListener() {
        return mOnScrollListener;
    }

    public static void setOnScrollListener(SlideBackLayout.OnScrollListener onScrollListener) {
        mOnScrollListener = onScrollListener;
    }
}
