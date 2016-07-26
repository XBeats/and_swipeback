package com.xbeats.swipebacksample.multislide;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xbeats.swipebacksample.R;
import com.xbeats.swipebacksample.common.CommonActivity;

import java.util.Random;

/**
 * Created by fhf11991 on 2016/7/25.
 */

public class MultiSlideActivity extends AppCompatActivity {

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
        overridePendingTransition(0, android.R.anim.fade_out);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, android.R.anim.fade_out);
    }

    public void nextPage(View v) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();


            }
        }, 1000);
//        startActivity(new Intent(this, CommonActivity.class));
//        Page ++;
    }

    @Override
    protected void onPause() {
        super.onPause();
        ViewGroup contentView = (ViewGroup) findViewById(Window.ID_ANDROID_CONTENT);
        View displayView = contentView.getChildAt(0);
        contentView.removeView(displayView);

        WindowManager.LayoutParams displayLayoutParams = new WindowManager.LayoutParams();
        displayLayoutParams.format = PixelFormat.RGBA_8888;
        displayLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        displayLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        displayLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        displayLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        getWindowManager().addView(displayView, displayLayoutParams);
    }
}
