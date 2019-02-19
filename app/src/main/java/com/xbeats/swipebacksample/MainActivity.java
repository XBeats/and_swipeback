package com.xbeats.swipebacksample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

/**
 * Created by fhf11991 on 2016/7/25.
 */

public class MainActivity extends AppCompatActivity {
    public static int Page = 1;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = findViewById(R.id.swipe_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Page = 1;
                startActivity(new Intent(MainActivity.this, SwipeActivity.class));
            }
        });

        mWebView = findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });
        mWebView.loadUrl("http://www.baidu.com/");

        button.postDelayed(new Runnable() {
            @Override
            public void run() {
                button.setText("测试22");
            }
        }, 2000);

        button.postDelayed(new Runnable() {
            @Override
            public void run() {
                button.setText("测试44");
            }
        }, 4000);


        button.postDelayed(new Runnable() {
            @Override
            public void run() {
                button.setText("测试66");
            }
        }, 6000);}

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
    }
}
