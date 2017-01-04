# SwipeBack

# Features

- 和Activity的theme并无关系
- 不影响activity的生命周期
- 只需继承BaseActivity  
- `isSupportSwipeBack` 唯一API方法,简单实用   
- 无需设置 `<item name="android:windowIsTranslucent">true</item>`  
- 支持Dialog的滑动返回

# Getting started

**Firstly,add the following lines to your app/build.gradle.** 
```gradle
dependencies {  
    compile 'com.aitangba:swipeback:1.0.0'
}
```  
**Secondly, add the following lines to your application.**
``` java
public class CustomApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(ActivityLifecycleHelper.build());
    }

}
```
**Finally, set the activity which need to swipe extends the SwipeBackActivity.**
``` java
public class BaseActivity extends SwipeBackActivity {

}
```

# Usage 
### API
Application在Api14之后添加了新的Callback方法  

``` java  
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {

    }
```
  
这样就可以根据activity的生命周期缓存所有Activity，通过list获取上一个activity的实例，从而获取id为content的ContentView的子View（即setContentView中的View），并进行滑动展示。  

默认SwipeBackActivity是支持滑动返回的，不需要滑动返回时则需要复写SwipeBackActivity的方法`supportSlideBack`，其中方法`canBeSlideBack`意思是能否返回至本Activity；两个方法相互配合使用，以应对各种需求。 
```java
 
   public class SwipeBackActivity extends AppCompatActivity {

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
     * @return
     */
    protected boolean supportSlideBack() {
        return true;
    }

    /**
     * 能否滑动返回至当前Activity
     * @return
     */
    protected boolean canBeSlideBack() {
        return true;
    }
   }
```
### 6种事件状态  

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

***Tips:***  
在设计过程中遇到也有过其他思路：  
1）设置Activity的透明theme，可是发现只要activity的层级变多就会变得非常卡顿；  
2）动态设置Activity的theme，这需要通过反射，而且还需要判断api，部分手机还不兼容；  
3）在滑动展示上个Activity的View时，直接将上个Activity的contentView截图保存在内存卡上，然后显示在当前Activity的view上，但是有明显的卡顿感；    
以上都是在设计过程中想到的方案，也逐个实践了一下，发现问题还是比较多的，想想还不如另辟蹊径，就有了现在的方案，目前看来还是能兼容大部分手机的。


# ScreenShot

![image](./screenshot/swipeback.gif)

# License

    Copyright 2016-2017 XBeats

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.