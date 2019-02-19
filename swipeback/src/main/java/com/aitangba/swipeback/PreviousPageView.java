package com.aitangba.swipeback;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Created by fhf11991 on 2017/1/19.
 */

class PreviousPageView extends View {

    private static final int MSG_SHOW_FRAME = 1;
    private View mView;
    private final InnerHandler mInnerHandler;

    public PreviousPageView(Context context) {
        super(context);
        mInnerHandler = new InnerHandler(this);
    }

    public void cacheView(View view) {
        mView = view;
        display(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d("PreviousPageView", "onDraw --- ");
        if (mView != null) {
            mView.draw(canvas);
//            mView = null;
        }
    }

    private void display(boolean delay) {
        invalidate();
        mInnerHandler.sendEmptyMessageDelayed(MSG_SHOW_FRAME, delay ? 60 : 0);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mInnerHandler.removeCallbacksAndMessages(null);
    }

    private static class InnerHandler extends Handler {
        private WeakReference<PreviousPageView> mWeakReference;

        private InnerHandler(PreviousPageView previousPageView) {
            super(Looper.getMainLooper());
            mWeakReference = new WeakReference<>(previousPageView);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mWeakReference.get() != null) {
                mWeakReference.get().display(true);
            }
        }
    }
}
