package com.xbeats.swipebacksample.copylastactivityview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xbeats.swipebacksample.R;
import com.xbeats.swipebacksample.applicationtest.CustomApplication;

import java.util.Random;

/**
 * Created by fhf11991 on 2016/7/20.
 */

public class CopyLastViewActivity extends AppCompatActivity{

    private static int Page = 1;
    private int page;
    private SlideBackLayout mSlideBackLayout;

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

        overridePendingTransition(0, android.R.anim.fade_out);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mSlideBackLayout = new SlideBackLayout(this);
        mSlideBackLayout.attachViewToActivity(this);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, android.R.anim.fade_out);
    }

    public void nextPage(View v) {
        startActivity(new Intent(this, CopyLastViewActivity.class));
        Page ++;
    }

    private void removeLastView() {
        if (getApplicationContext() instanceof CustomApplication) {
            CustomApplication customApplication = ((CustomApplication) getApplicationContext());
            CopyLastViewActivity lastActivity = (CopyLastViewActivity) customApplication.getActivityLifecycleHelper().getCurrentActivity();
            if(lastActivity == this)return;
            ViewGroup leftView = lastActivity.mSlideBackLayout.leftView;

            ViewGroup contentView = (ViewGroup) findViewById(android.R.id.content);

            View displayView = leftView.getChildAt(0);
            leftView.removeView(displayView);
            contentView.addView(displayView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        removeLastView();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
