package com.xbeats.swipebacksample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aitangba.swipeback.SwipeBackActivity;

import java.util.Random;

/**
 * Created by fhf11991 on 2016/7/25.
 */

public class SwipeActivity extends SwipeBackActivity {

    private static final String TAG = "SwipeActivity";

    private int page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        RelativeLayout containerLayout = (RelativeLayout) findViewById(R.id.container);

        //随机色
        Random random = new Random();
        int red = random.nextInt(255);
        int green = random.nextInt(255);
        int blue = random.nextInt(255);

        containerLayout.setBackgroundColor(Color.argb(255, red, green, blue));

        TextView textView = (TextView) findViewById(R.id.text);
        page = MainActivity.Page;
        textView.setText("当前页" + page);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "点击了当前页", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.next_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.Page ++;
                startActivity(new Intent(SwipeActivity.this, SwipeActivity.class));
            }
        });
        Log.d(TAG, "onCreate----------  Page = " + page);
    }

    @Override
    public void onBackPressed() {
        MainActivity.Page --;
        super.onBackPressed();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState=====  Page = " + page);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState----------  Page = " + page);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult----------  Page = " + page);
    }

    @Override
    public boolean supportSlideBack() {
        return super.supportSlideBack();
    }

    @Override
    public boolean canBeSlideBack() {
        return super.canBeSlideBack();
    }
}
