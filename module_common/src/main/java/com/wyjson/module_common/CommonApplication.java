package com.wyjson.module_common;

import android.app.Application;
import android.util.Log;

import com.wyjson.router.annotation.ApplicationModule;
import com.wyjson.router.interfaces.IApplicationModule;

@ApplicationModule
public class CommonApplication implements IApplicationModule {

    @Override
    public int setPriority() {
        return PRIORITY_MAX;
    }

    @Override
    public void onCreate(Application app) {
        Log.d("CommonApplication", "onCreate()");
    }

    @Override
    public void onLoadAsync(Application app) {
        Log.d("CommonApplication", "loadAsync()");
    }

}
