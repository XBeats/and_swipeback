package com.xbeats.swipebacksample.flingback;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by fhf11991 on 2016/7/20.
 */

public class BaseActivity extends AppCompatActivity implements View.OnTouchListener {
    GestureDetector mGestureDetector;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGestureDetector = new GestureDetector(new GestureDetector.OnGestureListener(){

            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if(!mNeedBack)return false;

                if (Math.abs(e2.getY() - e1.getY()) > 200) {//这里是避免有scroolview的页面上划也关闭
                    return false;
                }
                if (e1.getX() - e2.getX() > verticalMinDistance && Math.abs(velocityX) > minVelocity) {//左滑操作
                } else if (e2.getX() - e1.getX() > verticalMinDistance && Math.abs(velocityX) > minVelocity) {//右滑操作
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return mGestureDetector.onTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            mNeedBack = true;
        }
        return super.onTouchEvent(event);
    }

    private boolean mNeedBack;
    private int verticalMinDistance = 20;
    private int minVelocity = 0;
}
