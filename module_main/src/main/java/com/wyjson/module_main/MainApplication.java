package com.wyjson.module_main;

import android.app.Application;
import android.util.Log;

import com.wyjson.router.annotation.ApplicationModule;
import com.wyjson.router.interfaces.IApplicationModule;

@ApplicationModule
public class MainApplication implements IApplicationModule {

    @Override
    public void onCreate(Application app) {
        Log.d("MainApplication", "onCreate()");
    }

    @Override
    public void onLoadAsync(Application app) {
        Log.d("MainApplication", "loadAsync()");
    }
}
