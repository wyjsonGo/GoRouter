package com.wyjson.router.interfaces;

import android.app.Application;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

public interface IApplicationModule {

    int PRIORITY_MAX = 100;
    int PRIORITY_NORM = 50;
    int PRIORITY_MIN = 1;

    /**
     * The priority of the module application,
     * which will execute them in order from largest to smallest execution.
     */
    default int setPriority() {
        return PRIORITY_NORM;
    }

    void onCreate(Application app);

    /**
     * 优化启动速度,一些不着急的初始化可以放在这里做,子线程
     *
     * @param app
     */
    void onLoadAsync(Application app);

    default void onTerminate() {

    }

    default void onConfigurationChanged(@NonNull Configuration newConfig) {

    }

    default void onLowMemory() {

    }

    default void onTrimMemory(int level) {

    }

}
