SwipeBack
====================================

Features
--------

- 和Activity的theme并无关系
- 不影响activity的生命周期
- 只需继承BaseActivity  
- `isSupportSwipeBack` 唯一API方法,简单实用   
- 无需设置 `<item name="android:windowIsTranslucent">true</item>`  
- 支持Dialog的滑动返回

Usage
--------
#### 主要思想
Application在Api14之后添加了新的Callback方法  

``` java  
public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {}
```
  
这样就可以根据activity的生命周期缓存所有Activity，通过list获取上一个activity的实例，从而获取id为content的ContentView的子View（即setContentView中的View），并进行滑动展示。


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

1. 在Down手势发生时，只要将上一个Activity的ContentView从parentView中剥离，并加入到当前View的ContentView中；  
2. 在滑动手势发生时，加上阴影View，并进行滑动；同时滑动的有当前Activity的ContentView、上一个Activity的ContentView和自定义的阴影View；  
3. 在Up手势发生时，判断滑动是否超过半屏，触发返回操作，并展示滑动动画；  
4. 滑动取消或滑动返回发生时，需要将上个Activity的ContentView从新加入到上一个Acitivity的布局中。  


#### 自定义方法 
默认activity是支持滑动返回的，不需要返回的则需要复写Baseactivity的以下方法  
```java
 
   public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    private SwipeWindowHelper mSwipeWindowHelper;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(!supportSlideBack()) {
            return super.dispatchTouchEvent(ev);
        }

        if(mSwipeWindowHelper == null) {
            mSwipeWindowHelper = new SwipeWindowHelper(getWindow());
        }
        return mSwipeWindowHelper.processTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }

    /**
     * 是否支持滑动返回
     *
     * @return
     */
    protected boolean supportSlideBack() {
        return true;
    }
   }
```


ScreenShot
---------

![image](./screenshot/swipeback.gif)
