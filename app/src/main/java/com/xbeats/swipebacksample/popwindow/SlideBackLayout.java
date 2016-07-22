package com.xbeats.swipebacksample.popwindow;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.xbeats.swipebacksample.R;
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
    private TextView mTextView;

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

    private ChangeableWidthFrameLayout displayView;

    public void attachViewToActivity(final Activity activity) {
        setPanelSlideListener(new android.support.v4.widget.SlidingPaneLayout.PanelSlideListener() {
            @Override
            public void onPanelOpened(View panel) {
                CustomApplication customApplication = ((CustomApplication) panel.getContext().getApplicationContext());
                Activity lastActivity = customApplication.getActivityLifecycleHelper().getPreActivity();
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
//                mTextView.setX(panel.getLeft());
            }

            @Override
            public void onPanelClosed(View panel) {
                if( !(panel.getContext().getApplicationContext() instanceof CustomApplication))return;

//                CustomApplication customApplication = ((CustomApplication) panel.getContext().getApplicationContext());
//                Activity lastActivity = customApplication.getActivityLifecycleHelper().getPreActivity();
//                ViewGroup contentView = (ViewGroup) lastActivity.findViewById(android.R.id.content);
//
//                activity.getWindowManager().removeViewImmediate(displayView);
//                View content = displayView.getChildAt(0);
//                displayView.removeView(content);
//                contentView.addView(content, 0);
////                lastActivity.getWindowManager().updateViewLayout(contentView, lastActivity.getWindow().getAttributes());
//                displayView = null;
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
    private PopupWindow mPopupWindow;
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        mIsCurrentTouchAllowed = canEventSlide(ev);

        if(!mIsCurrentTouchAllowed)return false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (displayView == null) {
//                    CustomApplication customApplication = ((CustomApplication) getContext().getApplicationContext());
//                    Activity lastActivity = customApplication.getActivityLifecycleHelper().getPreActivity();
//                    ViewGroup contentView = (ViewGroup) lastActivity.findViewById(android.R.id.content);
//                    View content = contentView.getChildAt(0);
//                    contentView.removeView(content);
//
//                    displayView = new ChangeableWidthFrameLayout(getContext());
//                    displayView.addView(content);

//                    mPopupWindow.showAtLocation(getChildAt(0), Gravity.LEFT, 0, 0);
                }

                if(mPopupWindow == null) {
//                    mTextView = new TextView(getContext());
//                    mTextView.setText("测试");
                    CustomApplication customApplication = ((CustomApplication) getContext().getApplicationContext());
                    Activity lastActivity = customApplication.getActivityLifecycleHelper().getPreActivity();
                    ViewGroup contentView = (ViewGroup) lastActivity.findViewById(android.R.id.content);
                    View content = contentView.getChildAt(0);
                    contentView.removeView(content);

                    displayView = new ChangeableWidthFrameLayout(getContext());
                    displayView.addView(content);
                    mPopupWindow = new PopupWindow(displayView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, false);
                }

                if(!mPopupWindow.isShowing()) {
                    mPopupWindow.showAtLocation(getChildAt(0), Gravity.LEFT, 0, 0);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                displayView.setWidth(getChildAt(1).getLeft());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isInControlTime = true;
                mPopupWindow = null;
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

    private static class ChangeableWidthFrameLayout extends FrameLayout {

        public ChangeableWidthFrameLayout(Context context) {
            super(context);
        }

        public ChangeableWidthFrameLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public ChangeableWidthFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        private int mWidth = 0;

        public void setWidth(int width) {
            mWidth = width;
            requestLayout();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            final int width = mWidth;
            final int height = MeasureSpec.getSize(heightMeasureSpec);
            setMeasuredDimension(width, height);
        }
    }

}

