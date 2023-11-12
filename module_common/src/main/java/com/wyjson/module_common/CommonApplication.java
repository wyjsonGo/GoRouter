package com.wyjson.module_common;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import androidx.annotation.NonNull;

import com.wyjson.router.annotation.ApplicationModule;
import com.wyjson.router.interfaces.IApplicationModule;

@ApplicationModule
public class CommonApplication implements IApplicationModule {

    /**
     * 模块应用程序的优先级，它将按照从大到小的执行顺序执行。
     *
     * @return
     */
    @Override
    public int setPriority() {
        return PRIORITY_MAX;
    }

    @Override
    public void onCreate(Application app) {
        Log.d("CommonApplication", "onCreate()");
    }

    /**
     * 优化启动速度,一些不着急的初始化可以放在这里做,子线程
     *
     * @param app
     */
    @Override
    public void onLoadAsync(Application app) {
        Log.d("CommonApplication", "onLoadAsync()");
    }


    /**
     * 以下方法可以不实现,按需添加
     */

    @Override
    public void onTerminate() {
        Log.d("CommonApplication", "onTerminate()");
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        Log.d("CommonApplication", "onConfigurationChanged()");
    }

    @Override
    public void onLowMemory() {
        Log.d("CommonApplication", "onLowMemory()");
    }

    @Override
    public void onTrimMemory(int level) {
        Log.d("CommonApplication", "onTrimMemory() level:" + level);
    }

}
