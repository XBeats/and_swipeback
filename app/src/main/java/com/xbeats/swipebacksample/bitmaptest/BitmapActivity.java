package com.xbeats.swipebacksample.bitmaptest;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.xbeats.swipebacksample.R;
import com.xbeats.swipebacksample.dispatchactivity.BaseActivity;

/**
 * Created by fhf11991 on 2016/7/25.
 */

public class BitmapActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitmap);

        final View parentView =  findViewById(R.id.llt_parent);
        final ImageView view = (ImageView) findViewById(R.id.image);

        findViewById(R.id.bt_create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int viewWidth = parentView.getMeasuredWidth();
                int viewHeight = parentView.getMeasuredHeight();
                Bitmap bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(bitmap);
                parentView.draw(canvas);
                view.setImageBitmap(bitmap);
            }
        });
    }

}