package com.wyjson.go_router;

import android.app.Application;

import com.wyjson.module_common.BuildConfig;
import com.wyjson.router.GoRouter;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            GoRouter.openDebug();
//            GoRouter.printStackTrace();
        }
        GoRouter.autoLoadModuleRoute(this);
    }
}
