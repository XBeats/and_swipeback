package com.xbeats.swipebacksample.dispatchactivity;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by fhf11991 on 2016/7/25.
 */

public class CustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

    private SurfaceHolder mHolder;
    public CustomSurfaceView(Context context) {
        this(context, null);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    private View mContentView;
    public void setView(View view) {
        mContentView = view;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(mContentView != null) {
            Canvas canvas = holder.lockCanvas();
            mContentView.draw(canvas);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mContentView = null;
    }
}
