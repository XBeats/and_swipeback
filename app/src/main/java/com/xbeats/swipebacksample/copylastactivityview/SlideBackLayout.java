package com.xbeats.swipebacksample.copylastactivityview;

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
import android.widget.FrameLayout;

import com.xbeats.swipebacksample.applicationtest.CustomApplication;

import java.lang.reflect.Field;

/**
 * Created by fhf11991 on 2016/7/11.
 */
public class SlideBackLayout extends SlidingPaneLayout {

    private static final String TAG = "CustomSlidingPaneLayout";

    /**
     * Constant value for use with setTouchModeAbove(). Allows the SlidingMenu to be opened with a swipe
     * gesture on the screen's margin
     */
    public static final int TOUCH_MODE_MARGIN = 0;

    /**
     * Constant value for use with setTouchModeAbove(). Allows the SlidingMenu to be opened with a swipe
     * gesture anywhere on the screen
     */
    public static final int TOUCH_MODE_FULLSCREEN = 1;

    private final int DEFAULT_OVERHANG_SIZE = 0; //

    private final int MARGIN_THRESHOLD = 48; // dips


    private int mTouchMode = TOUCH_MODE_MARGIN;
    private int mShadowWidth = 50; //px
    private boolean mIsSlidingAvailable = true;
    private boolean mIsCurrentTouchAllowed;
    private Drawable mShadowDrawable;
    private boolean mIsSliding;
    private int mMarginThreshold;
    public CustomBehindView leftView;

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

    public boolean isPanelOpened;

    public void attachViewToActivity(final Activity activity) {
        setPanelSlideListener(new android.support.v4.widget.SlidingPaneLayout.PanelSlideListener() {
            @Override
            public void onPanelOpened(View panel) {
//                isPanelOpened = true;
//                if (getContext().getApplicationContext() instanceof CustomApplication) {
//                    if (leftView.getChildCount() == 0) return;
//                    CustomApplication customApplication = ((CustomApplication) getContext().getApplicationContext());
//                    Activity lastActivity = customApplication.getActivityLifecycleHelper().getLastActivity();
//                    ViewGroup contentView = (ViewGroup) lastActivity.findViewById(android.R.id.content);
//
//                    View displayView = leftView.getChildAt(0);
//                    leftView.removeView(displayView);
//                    contentView.addView(displayView);
//                    displayView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//                }
//                postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
////                        activity.overridePendingTransition(0, 0);
//                        activity.finish();
//                    }
//                }, 600);
                activity.finish();
            }

            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (getContext().getApplicationContext() instanceof CustomApplication) {
                    if (leftView.getChildCount() == 0) {
                        CustomApplication customApplication = ((CustomApplication) getContext().getApplicationContext());
                        Activity lastActivity = customApplication.getActivityLifecycleHelper().getLastActivity();
                        ViewGroup contentView = (ViewGroup) lastActivity.findViewById(android.R.id.content);
                        View displayView = contentView.getChildAt(0);
                        contentView.removeView(displayView);
                        leftView.addView(displayView);
                    }

                    final int width = getResources().getDisplayMetrics().widthPixels;
                    final float left = panel.getMeasuredWidth() * slideOffset;
                    leftView.getChildAt(0).setX(-width / 3 + left / 3);
                }
            }

            @Override
            public void onPanelClosed(View panel) {
//                leftView.getChildAt(0).setX(getChildAt(1).getLeft());
                if (getContext().getApplicationContext() instanceof CustomApplication) {
                    if (leftView.getChildCount() == 0) return;
                    CustomApplication customApplication = ((CustomApplication) getContext().getApplicationContext());
                    Activity lastActivity = customApplication.getActivityLifecycleHelper().getLastActivity();
                    ViewGroup contentView = (ViewGroup) lastActivity.findViewById(android.R.id.content);

                    View displayView = leftView.getChildAt(0);
                    leftView.removeView(displayView);
                    contentView.addView(displayView);
                }
            }
        });
        setSliderFadeColor(getResources().getColor(android.R.color.transparent));

        leftView = new CustomBehindView(activity);
        leftView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(leftView, 0);


        final ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
        if (contentView.getChildCount() == 0) {
            contentView.addView(this);
        } else {
            final ViewGroup contentChild = (ViewGroup) contentView.getChildAt(0);
            contentView.removeView(contentChild);
            contentView.addView(this);
            addView(contentChild, 1);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mIsCurrentTouchAllowed = canEventSlide(ev);

        if (mIsCurrentTouchAllowed) {
            return mIsSliding = super.onInterceptTouchEvent(ev);
        } else {
            return mIsSliding = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        mIsCurrentTouchAllowed = canEventSlide(ev);

        if (mIsCurrentTouchAllowed) {
            return mIsSliding = super.onTouchEvent(ev);
        } else {
            return mIsSliding = false;
        }
    }

    private boolean canEventSlide(MotionEvent ev) {
        final boolean isSlideNotAvailable = !mIsSlidingAvailable;
        final boolean isActionDown = ev.getAction() == MotionEvent.ACTION_DOWN;

        boolean isTouchAllowed;

        if (isSlideNotAvailable) {
            isTouchAllowed = false;
        } else if (isActionDown && mTouchMode == TOUCH_MODE_FULLSCREEN) {
            isTouchAllowed = true;
        } else if (isActionDown && mTouchMode == TOUCH_MODE_MARGIN) {
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

        if (mShadowDrawable == null) {
            int colors[] = {0x00000000, 0x17000000, 0x43000000};//分别为开始颜色，中间夜色，结束颜色
            mShadowDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        }
        mShadowDrawable.setBounds(left - mShadowWidth, 0, left, getHeight());
        mShadowDrawable.draw(canvas);
    }

    public static class CustomBehindView extends FrameLayout {

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

}

