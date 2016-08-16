SwipeBack
====================================

Features
--------

- 和Activity的theme并无关系
- 不影响activity的生命周期
- 只需继承BaseActivity  
- `isSupportSwipeBack` 唯一API方法,简单实用   
- 无需设置 `<item name="android:windowIsTranslucent">true</item>`

Usage
--------
#### 主要思想
Application在Api14之后添加了新的Callback方法  

``` java  
public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {}
```
  
这样就可以根据activity的生命周期缓存所有Activity，通过list获取上一个activity的实例，从而获取DecorView，并进行展示


#### 包括以下6中状态  

```java  

    private static final int MSG_ACTION_DOWN = 1; //点击事件  
    private static final int MSG_ACTION_MOVE = 2; //滑动事件
    private static final int MSG_ACTION_UP = 3;  //点击结束
    private static final int MSG_SLIDE_CANCEL = 4; //开始滑动，不返回前一个页面
    private static final int MSG_SLIDE_CANCELED = 5;  //结束滑动，不返回前一个页面
    private static final int MSG_SLIDE_PROCEED = 6; //开始滑动，返回前一个页面
    private static final int MSG_SLIDE_FINISHED = 7;//结束滑动，返回前一个页面
```

#### 自定义方法 
默认activity是支持滑动返回的，不需要返回的则需要复写Baseactivity的以下方法  
```java

      /**
     * 是否支持滑动返回
     *
     * @return
     */  
    protected boolean supportSlideBack() {
        return true;
    }  
```


ScreenShot
---------

![image](./screenshot/swipeback.gif)
