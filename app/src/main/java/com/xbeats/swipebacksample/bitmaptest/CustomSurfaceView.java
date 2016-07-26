package com.xbeats.swipebacksample.bitmaptest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by fhf11991 on 2016/7/25.
 */

public class CustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

    private CustomThread mCustomThread;

    public CustomSurfaceView(Context context) {
        super(context);
        Canvas canvas = getHolder().lockCanvas();
    }

    public void onStart() {
        if(mCustomThread == null) {
            mCustomThread = new CustomThread();
        }
        mCustomThread.start();
    }

    private Bitmap mBitmap;

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
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
         if(mCustomThread != null) {
             mCustomThread.stop();
         }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    class CustomThread extends Thread {

        @Override
        public void run() {
            super.run();
            Canvas canvas = getHolder().lockCanvas();
//            canvas.drawBitmap();
        }
    }
}
