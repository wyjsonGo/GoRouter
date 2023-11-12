package com.wyjson.module_user;

import android.app.Application;
import android.util.Log;

import com.wyjson.router.annotation.ApplicationModule;
import com.wyjson.router.interfaces.IApplicationModule;

@ApplicationModule
public class UserApplication implements IApplicationModule {

    @Override
    public void onCreate(Application app) {
        Log.d("UserApplication", "onCreate()");
    }

    @Override
    public void onLoadAsync(Application app) {
        Log.d("UserApplication", "loadAsync()");
    }
}
