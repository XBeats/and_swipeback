package com.aitangba.swipeback;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

/**
 * Created by fhf11991 on 2017/1/19.
 */

class PreviousPageView extends View {

    private View mView;

    public PreviousPageView(Context context) {
        super(context);
    }

    public void cacheView(View view) {
        mView = view;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mView != null) {
            mView.draw(canvas);
            mView = null;
        }
    }
}
