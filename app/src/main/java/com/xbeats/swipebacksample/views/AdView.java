package com.xbeats.swipebacksample.views;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.xbeats.swipebacksample.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangfei on 2015/6/14.
 */
public class AdView extends FrameLayout implements ViewPager.OnPageChangeListener {
    private static final int STATUS_RUN = 1;
    private static final int STATUS_STOP = 2;
    private static final int STATUS_RESET = 3;
    private static final int AUTO_RUN_SPACE_TIME = 4000;

    private ViewPager mViewPager;
    private LinearLayout mDotLayout;

    private List<ImageView> mDotViews = new ArrayList<>();
    private int currentPosition = 0;
    private String mEventId;

    private String mEvenId;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case STATUS_STOP:
                    break;
                case STATUS_RUN:
                    if (mDotViews == null || mDotViews.size() == 0) {
                        return;
                    }
                    currentPosition = (currentPosition + 1) % mDotViews.size();
                    mViewPager.setCurrentItem(currentPosition);
                    mHandler.sendEmptyMessageDelayed(STATUS_RUN, AUTO_RUN_SPACE_TIME);
                    break;
                case STATUS_RESET:
                    currentPosition = 0;
                    mViewPager.setCurrentItem(currentPosition);
                    break;
                default:
                    break;
            }
        }
    };

    public AdView(Context context) {
        super(context);
        initView(context);
    }

    public AdView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public AdView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        inflate(context, R.layout.view_advertisement, this);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mDotLayout = (LinearLayout) findViewById(R.id.ll_dots);
        initAdvViews(4);
        startAutoRun();
    }

    private void initAdvViews(int size) {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        List<ImageView> adViews = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ImageView adView = new ImageView(getContext());
            adView.setImageResource(R.mipmap.ic_launcher);
            adView.setLayoutParams(params);
            adView.setScaleType(ImageView.ScaleType.FIT_XY);
            adViews.add(adView);
        }

        mViewPager.setAdapter(new ViewPagerAdapter(adViews));
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setCurrentItem(0);
    }

    @Override
    public void onPageSelected(int position) {
        if (mDotViews == null || mDotViews.size() == 0) {
            return;
        }
        int selectedItem = position % mDotViews.size();
        for (int i = 0; i < mDotViews.size(); i++) {
//            ImageView image = mDotViews.get(i);
//            if (i == selectedItem) {
//                image.setBackgroundResource(R.drawable.ic_ad_dot_focused);
//            } else {
//                image.setBackgroundResource(R.drawable.ic_ad_dot_unfocused);
//            }
        }
    }

    private void startAutoRun() {
        mHandler.sendEmptyMessageDelayed(STATUS_RUN, AUTO_RUN_SPACE_TIME);
    }

    public void stopAutoRun() {
        mHandler.sendEmptyMessage(STATUS_STOP);
    }

    public void resetAutoRun() {
        mHandler.sendEmptyMessage(STATUS_RESET);
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public static class ViewPagerAdapter extends PagerAdapter {
        private List<ImageView> mAdvViews;

        public ViewPagerAdapter(List<ImageView> advViews) {
            if (advViews == null || advViews.size() == 0) {
                throw new IllegalArgumentException("advViews cannot be empty or null");
            }
            mAdvViews = advViews;
        }

        @Override
        public int getCount() {
            return mAdvViews.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mAdvViews.get(position % mAdvViews.size()), 0);
            return mAdvViews.get(position % mAdvViews.size());
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mAdvViews.get(position % mAdvViews.size()));
        }
    }
}
