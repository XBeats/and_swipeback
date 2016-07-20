package com.xbeats.swipebacksample.noslide;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xbeats.swipebacksample.BaseActivity;
import com.xbeats.swipebacksample.R;

import java.util.Random;

/**
 * Created by fhf11991 on 2016/7/15.
 */

public class NoSlideActivity extends BaseActivity {
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

    public void nextPage(View v) {
        startActivity(new Intent(this, NoSlideActivity.class));
        Page ++;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("NextActivity", "onDestroy  " + "  page = " + page);
    }

    @Override
    protected boolean isSupportBeSlideBack() {
        return false;
    }

    @Override
    protected boolean isSupportToSlideBack() {
        return true;
    }
}
