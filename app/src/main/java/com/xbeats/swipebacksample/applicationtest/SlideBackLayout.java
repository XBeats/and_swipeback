package com.xbeats.swipebacksample.applicationtest;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.xbeats.swipebacksample.Utils;

import java.lang.reflect.Field;

/**
 * Created by fhf11991 on 2016/7/11.
 */
public class SlideBackLayout extends SlidingPaneLayout {

    private static final String TAG = "CustomSlidingPaneLayout";

    private final int DEFAULT_OVERHANG_SIZE = 0; //

    private final int MARGIN_THRESHOLD = 48; // dips


    private int mShadowWidth = 50; //px
    private boolean mIsSlidingAvailable = true;
    private boolean mIsCurrentTouchAllowed;
    private Drawable mShadowDrawable;
    private int mMarginThreshold;
    private CustomBehindView leftView;

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

    public void attachViewToActivity(final Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            return;
        }

        setPanelSlideListener(new android.support.v4.widget.SlidingPaneLayout.PanelSlideListener() {
            @Override
            public void onPanelOpened(View panel) {
                activity.finish();
            }

            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                View displayView = getLastActivityContentView(getContext());
                if(displayView != null) {
                    final int width = getResources().getDisplayMetrics().widthPixels;
                    final float left = panel.getMeasuredWidth() * slideOffset;
                    float distance = -width / 3 + left / 3;
                    displayView.setX(distance);
                }
            }

            @Override
            public void onPanelClosed(View panel) {
                View displayView = getLastActivityContentView(getContext());
                if(displayView != null) {
                    Utils.convertActivityFromTranslucent(activity);
                    displayView.setX(displayView.getLeft());
                }
            }
        });
        setSliderFadeColor(getResources().getColor(android.R.color.transparent));

        leftView = new CustomBehindView(activity);
        leftView.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        leftView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(leftView, 0);

        final ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
        View displayView;
        if(contentView.getChildCount() == 0) {
            displayView = new View(getContext());
        } else {
            displayView = contentView.getChildAt(0);
            contentView.removeView(displayView);
        }

        contentView.addView(this, 0);
        addView(displayView, 1);

        activity.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        activity.getWindow().getDecorView().setBackgroundDrawable(null);
    }

    private static View getLastActivityContentView(Context context) {
        if(context.getApplicationContext() instanceof CustomApplication) {
            CustomApplication customApplication = ((CustomApplication)context.getApplicationContext());
            Activity lastActivity = customApplication.getActivityLifecycleHelper().getLastActivity();
            View contentView;
            contentView = lastActivity.findViewById(android.R.id.content);
            return contentView;
        }
        return null;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mIsCurrentTouchAllowed = canEventSlide(ev);

        if(mIsCurrentTouchAllowed) {
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
                Utils.convertActivityToTranslucent((Activity) getContext());
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

        if(isSlideNotAvailable) {
            isTouchAllowed = false;
        } else if(isActionDown) {
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
            int colors[] = {0x00000000, 0x17000000, 0x43000000};//分别为开始颜色，中间夜色，结束颜色
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

}
