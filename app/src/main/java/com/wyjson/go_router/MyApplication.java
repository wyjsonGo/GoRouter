package com.wyjson.go_router;

import android.app.Application;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

import com.wyjson.module_common.BuildConfig;
import com.wyjson.router.GoRouter;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            GoRouter.openDebug();
        }
        // 这部分是多模块application,如不使用多模块application可以不调用
        GoRouter.callAMOnCreate(this);
        
        // 自动加载所有路由模块的路由
        GoRouter.autoLoadRouteModule(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // 这部分是多模块application,如不使用多模块application可以不调用
        GoRouter.callAMOnTerminate();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 这部分是多模块application,如不使用多模块application可以不调用
        GoRouter.callAMOnConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // 这部分是多模块application,如不使用多模块application可以不调用
        GoRouter.callAMOnLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        // 这部分是多模块application,如不使用多模块application可以不调用
        GoRouter.callAMOnTrimMemory(level);
    }
}
