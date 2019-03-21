package com.aitangba.swipeback;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Created by XBeats on 2019/3/20
 */
class TemporaryView extends View {

    private WeakReference<View> mView;
    private Drawable mDrawable;
    private int mShadowWidth;

    private Paint mPaint;

    public TemporaryView(Context context) {
        super(context);
        int colors[] = {0x00000000, 0x17000000, 0x43000000};//分别为开始颜色，中间夜色，结束颜色
        mDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
    }

    public void setShadowWidth(int shadowWidth) {
        mShadowWidth = shadowWidth;
    }

    public void setBgColor(@ColorInt int bgColor) {
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
        }

        mPaint.setColor(bgColor == 0 ? Color.WHITE : bgColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mView != null && mView.get() != null) {
            mView.get().draw(canvas);
        } else {
            mDrawable.setBounds(0, 0, mShadowWidth, getMeasuredHeight());
            mDrawable.draw(canvas);

            if (mPaint != null) {
                canvas.drawRect(mShadowWidth, 0, getMeasuredWidth(), getMeasuredHeight(), mPaint);
            }
        }
    }

    public void cacheView(View view) {
        mView = new WeakReference<>(view);

        invalidate();
    }
}
