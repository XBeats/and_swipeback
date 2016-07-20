package com.xbeats.swipebacksample.headviewactivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xbeats.swipebacksample.R;

import java.util.Random;

/**
 * Created by fhf11991 on 2016/7/19.
 */

public class HeadViewActivity extends AppCompatActivity{


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
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        TextView headView = new TextView(this);
        headView.setText("我是头部");

        ViewGroup contentView = (ViewGroup) findViewById(android.R.id.content);
        if(contentView.getChildCount() == 0) {
            contentView.addView(contentView);
        } else {
            View content = contentView.getChildAt(0);
            contentView.removeView(content);
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            contentView.addView(linearLayout);
            linearLayout.addView(headView);
            linearLayout.addView(content);
        }
    }

    public void nextPage(View v) {
        getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);
    }
}
