package com.xbeats.swipebacksample.windowmanager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xbeats.swipebacksample.R;
import com.xbeats.swipebacksample.applicationtest.CustomApplication;
import com.xbeats.swipebacksample.copylastactivityview.CopyLastViewActivity;

import java.util.Random;

/**
 * Created by fhf11991 on 2016/7/21.
 */

public class BaseActivity extends AppCompatActivity{

    private static int Page = 1;
    private int page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        RelativeLayout containerRl = (RelativeLayout) findViewById(R.id.container);

        //随机色

        Random random = new Random();
        int red = random.nextInt(255);
        int green = random.nextInt(255);
        int blue = random.nextInt(255);

        containerRl.setBackgroundColor(Color.argb(255,red,green,blue));

        TextView textView = (TextView) findViewById(R.id.text);
        page = Page;
        textView.setText("当前页" + page);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    public void nextPage(View v) {
        startActivity(new Intent(this, BaseActivity.class));
    }
    private int mMarginThreshold = 100;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                final int x = (int) (ev.getX());
                boolean isSlideAction = x >= 0 && x < mMarginThreshold;
                if(isSlideAction) {
                    startSlideBack();
                    return true;
                }
            break;
            default:break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private final void startSlideBack() {
        final ViewGroup.LayoutParams layoutParams =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);

        SlideBackLayout slideBackLayout = new SlideBackLayout(this);
        slideBackLayout.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelOpened(View panel) {

            }

            @Override
            public void onPanelClosed(View panel) {

            }
        });

//        CustomApplication application = (CustomApplication) getApplication();
//        Activity preActivity = application.getActivityLifecycleHelper().getPreActivity();
//        ViewGroup preContentView = (ViewGroup) preActivity.findViewById(android.R.id.content);
//        View preView = preContentView.getChildAt(0);
//        preContentView.removeView(preView);
//        slideBackLayout.addView(preView, 0, layoutParams);
        slideBackLayout.addView(new View(this), 0, layoutParams);

        ViewGroup currentContentView = (ViewGroup) this.findViewById(android.R.id.content);
        View currentView = currentContentView.getChildAt(0);
        currentContentView.removeView(currentView);
//        slideBackLayout.addView(currentView, 1, layoutParams);
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.addView(currentView, layoutParams);


        WindowManager.LayoutParams displayLayoutParams = new WindowManager.LayoutParams();
        displayLayoutParams.format = PixelFormat.RGBA_8888;
        displayLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        displayLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        displayLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        displayLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        getWindowManager().addView(frameLayout, displayLayoutParams);
    }
}
