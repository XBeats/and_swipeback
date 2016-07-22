package com.xbeats.swipebacksample.windowmanager;

/**
 * Created by fhf11991 on 2016/7/21.
 */

import android.content.Context;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.AttributeSet;
import android.view.View;

import java.lang.reflect.Field;

/**
 * Created by fhf11991 on 2016/7/11.
 */
public class SlideBackLayout extends SlidingPaneLayout {

    private static final String TAG = "CustomSlidingPaneLayout";

    private final int DEFAULT_OVERHANG_SIZE = 0; //

    public SlideBackLayout(Context context) {
        this(context, null);
    }

    public SlideBackLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideBackLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        //通过反射改变mOverhangSize的值为0，这个mOverhangSize值为菜单到右边屏幕的最短距离，默认
        //是32dp，现在给它改成0
        try {
            Field field = SlidingPaneLayout.class.getDeclaredField("mOverhangSize");
            field.setAccessible(true);
            field.set(this, DEFAULT_OVERHANG_SIZE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setSliderFadeColor(getResources().getColor(android.R.color.transparent));
    }

}

