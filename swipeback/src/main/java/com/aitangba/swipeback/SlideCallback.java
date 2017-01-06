package com.aitangba.swipeback;

/**
 * Created by fhf11991 on 2017/1/6.
 */

public interface SlideCallback {

    /**
     * 是否支持滑动返回
     *
     * @return
     */
    boolean supportSlideBack();

    /**
     * 能否滑动返回至当前Activity
     * @return
     */
    boolean canBeSlideBack();

}
