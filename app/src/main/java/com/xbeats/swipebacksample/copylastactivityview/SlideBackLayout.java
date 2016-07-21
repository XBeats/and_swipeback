package com.xbeats.swipebacksample.copylastactivityview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.xbeats.swipebacksample.Utils;
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
    private int mMarginThreshold;

    public void setShadowDrawable(Drawable shadow) {
        mShadowDrawable = shadow;
    }

    public void setSlidingAvailable(boolean slidingAvailable) {
        mIsSlidingAvailable = slidingAvailable;
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

    private ViewGroup displayView;

    public void attachViewToActivity(final Activity activity) {
        setPanelSlideListener(new android.support.v4.widget.SlidingPaneLayout.PanelSlideListener() {
            @Override
            public void onPanelOpened(View panel) {
                CustomApplication customApplication = ((CustomApplication) panel.getContext().getApplicationContext());
                Activity lastActivity = customApplication.getActivityLifecycleHelper().getLastActivity();
                ViewGroup contentView = (ViewGroup) lastActivity.findViewById(android.R.id.content);

                activity.getWindowManager().removeView(displayView);
                View content = displayView.getChildAt(0);
                displayView.removeView(content);
                contentView.addView(content, 0);

                displayView = null;
                activity.finish();
            }

            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if( !(panel.getContext().getApplicationContext() instanceof CustomApplication))return;

                displayView.setVisibility(View.VISIBLE);
                final int width = getResources().getDisplayMetrics().widthPixels;
                final float left = panel.getMeasuredWidth() * slideOffset;

//                float distance = -width / 3 + left / 3;
                float distance = left - width;
                displayView.getChildAt(0).setX(distance);
            }

            @Override
            public void onPanelClosed(View panel) {
                if( !(panel.getContext().getApplicationContext() instanceof CustomApplication))return;

                CustomApplication customApplication = ((CustomApplication) panel.getContext().getApplicationContext());
                Activity lastActivity = customApplication.getActivityLifecycleHelper().getLastActivity();
                ViewGroup contentView = (ViewGroup) lastActivity.findViewById(android.R.id.content);

                activity.getWindowManager().removeViewImmediate(displayView);
                View content = displayView.getChildAt(0);
                displayView.removeView(content);
                contentView.addView(content, 0);
//                lastActivity.getWindowManager().updateViewLayout(contentView, lastActivity.getWindow().getAttributes());
                displayView = null;
            }
        });
        setSliderFadeColor(getResources().getColor(android.R.color.transparent));

        View leftView = new View(activity);
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
            return super.onInterceptTouchEvent(ev);
        } else {
            return false;
        }
    }

    private boolean isInControlTime = true;
    private boolean isFirstDelayed = true;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        mIsCurrentTouchAllowed = canEventSlide(ev);

        if(!mIsCurrentTouchAllowed)return false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (displayView == null) {
                    CustomApplication customApplication = ((CustomApplication) getContext().getApplicationContext());
                    Activity lastActivity = customApplication.getActivityLifecycleHelper().getLastActivity();
                    ViewGroup contentView = (ViewGroup) lastActivity.findViewById(android.R.id.content);
                    View content = contentView.getChildAt(0);
                    contentView.removeView(content);

                    displayView = new FrameLayout(getContext());
                    displayView.setVisibility(View.INVISIBLE);
                    displayView.addView(content);

                    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                    layoutParams.format = PixelFormat.RGBA_8888;
                    layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                    layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
                    ((Activity)getContext()).getWindowManager().addView(displayView, layoutParams);

//                    final int width = getResources().getDisplayMetrics().widthPixels;
//                    displayView.getChildAt(0).setX(- width / 3);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(isInControlTime) {
                    if(isFirstDelayed) {
                        isFirstDelayed = false;
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isInControlTime = false;
                                isFirstDelayed = true;
                            }
                        }, 60);
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isInControlTime = true;
                break;
            default:break;
        }
        return super.onTouchEvent(ev);
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
//        drawShadow(canvas);
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
}

