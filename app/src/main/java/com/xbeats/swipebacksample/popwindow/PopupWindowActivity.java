package com.xbeats.swipebacksample.popwindow;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xbeats.swipebacksample.R;

import java.util.Random;

/**
 * Created by fhf11991 on 2016/7/22.
 */

public class PopupWindowActivity extends AppCompatActivity{

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

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        SlideBackLayout mSlideBackLayout = new SlideBackLayout(this);
        mSlideBackLayout.attachViewToActivity(this);
    }

    public void nextPage(View v) {
        startActivity(new Intent(this, PopupWindowActivity.class));
    }
}
