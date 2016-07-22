package com.xbeats.swipebacksample;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;

import com.xbeats.swipebacksample.applicationtest.ApplicationTestActivity;
import com.xbeats.swipebacksample.common.CommonActivity;
import com.xbeats.swipebacksample.copylastactivityview.CopyLastViewActivity;
import com.xbeats.swipebacksample.flingback.FlingBackActivity;
import com.xbeats.swipebacksample.noslide.NoSlideActivity;
import com.xbeats.swipebacksample.popwindow.PopupWindowActivity;
import com.xbeats.swipebacksample.slideback.NextActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
//        getWindow().setBackgroundDrawable(new ColorDrawable(Color.YELLOW));
//        getWindow().getDecorView().setBackgroundDrawable(null);
    }

    public void nextPage(View v) {
        startActivity(new Intent(this, NextActivity.class));
    }

    public void nextCommonPage(View v) {
        startActivity(new Intent(this, CommonActivity.class));
    }

    public void nextPageUnSlide(View v) {
        startActivity(new Intent(this, NoSlideActivity.class));
    }

    public void onApplication(View v) {
        startActivity(new Intent(this, PopupWindowActivity.class));
    }
}
