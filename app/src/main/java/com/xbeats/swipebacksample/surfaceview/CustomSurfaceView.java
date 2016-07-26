package com.xbeats.swipebacksample.surfaceview;

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
    public CustomSurfaceView(Context context) {
        super(context);
        Canvas canvas = getHolder().lockCanvas();
        new View(context).buildDrawingCache();
    }

    public CustomSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
