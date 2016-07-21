package com.xbeats.swipebacksample.copylastactivityview;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xbeats.swipebacksample.R;

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
        overridePendingTransition(0, 0);
    }

    public void nextPage(View v) {
//        TextView textView = new TextView(this);
//        textView.setText("测试");
//        textView.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
//        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
//        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
//        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
////        layoutParams.token = getWindow().getDecorView().getWindowToken();
//        getWindowManager().addView(textView, layoutParams);

        startActivity(new Intent(this, CopyLastViewActivity.class));
//        Page ++;
    }
}
