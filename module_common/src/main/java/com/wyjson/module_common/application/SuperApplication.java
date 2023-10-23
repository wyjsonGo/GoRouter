package com.wyjson.module_common.application;

import android.app.Application;

public abstract class SuperApplication extends Application {

    protected abstract void initModuleApplication();

    protected void registerModuleApplication(Class<? extends IApplication> application) {
        ApplicationUtils.add(application);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initModuleApplication();
        ApplicationUtils.execute(i -> i.init(this));
    }

}
