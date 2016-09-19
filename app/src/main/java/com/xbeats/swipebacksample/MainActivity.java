package com.xbeats.swipebacksample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.xbeats.swipebacksample.dispatchactivity.DispatchManagerActivity;

public class MainActivity extends AppCompatActivity {
    public static int Page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void onApplication(View v) {
        Page = 1;
        startActivity(new Intent(this, DispatchManagerActivity.class));
    }

}
