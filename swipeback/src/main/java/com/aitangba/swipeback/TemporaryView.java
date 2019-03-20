package com.aitangba.swipeback;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

/**
 * Created by XBeats on 2019/3/20
 */
class TemporaryView extends View {

    private Drawable mDrawable;
    private View mView;
    private int mShadowWidth;

    public TemporaryView(Context context) {
        super(context);
        int colors[] = {0x00000000, 0x17000000, 0x43000000};//分别为开始颜色，中间夜色，结束颜色
        mDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
    }

    public void setShadowWidth(int shadowWidth) {
        mShadowWidth = shadowWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mView == null) {
            mDrawable.setBounds(0, 0, mShadowWidth, getMeasuredHeight());
            mDrawable.draw(canvas);
        } else {
            mView.draw(canvas);
        }
    }

    public void cacheView(View view) {
        mView = view;
        invalidate();
    }
}
