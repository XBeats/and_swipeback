package com.xbeats.swipebacksample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.xbeats.swipebacksample.dispatchactivity.DispatchManagerActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void onApplication(View v) {
        startActivity(new Intent(this, DispatchManagerActivity.class));
    }
}
