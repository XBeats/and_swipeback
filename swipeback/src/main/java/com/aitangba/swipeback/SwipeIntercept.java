package com.aitangba.swipeback;

import android.view.MotionEvent;

public interface SwipeIntercept {
    boolean processTouchEvent(MotionEvent ev);
    void finishSwipeImmediately();
}
