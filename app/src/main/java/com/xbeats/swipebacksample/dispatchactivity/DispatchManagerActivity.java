package com.xbeats.swipebacksample.dispatchactivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xbeats.swipebacksample.MainActivity;
import com.xbeats.swipebacksample.R;

import java.util.Random;

/**
 * Created by fhf11991 on 2016/7/25.
 */
public class DispatchManagerActivity extends BaseActivity{

    private final String TAG = "DispatchManagerActivity";

    private int page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        RelativeLayout containerRlt = (RelativeLayout) findViewById(R.id.container);

        //随机色
        Random random = new Random();
        int red = random.nextInt(255);
        int green = random.nextInt(255);
        int blue = random.nextInt(255);

        containerRlt.setBackgroundColor(Color.argb(255,red,green,blue));

        TextView textView = (TextView) findViewById(R.id.text);
        page = MainActivity.Page;
        textView.setText("当前页" + page);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog  = new SwipeDialog(DispatchManagerActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
                dialog.setContentView(R.layout.dialog_full_view);
                dialog.show();
            }
        });

    }

    public void nextPage(View v) {
        startActivity(new Intent(this, DispatchManagerActivity.class));
        MainActivity.Page ++;
    }
}
