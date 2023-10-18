package com.wyjson.module_common.application;

import android.app.Application;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationModuleUtils.loadApplicationModule(this, (i) -> i.init(this));
    }

}
