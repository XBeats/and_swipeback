package com.xbeats.swipebacksample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by fhf11991 on 2016/7/25.
 */

public class MainActivity extends AppCompatActivity {
    public static int Page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.swipe_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Page = 1;
                startActivity(new Intent(MainActivity.this, SwipeActivity.class));
            }
        });
    }

}
