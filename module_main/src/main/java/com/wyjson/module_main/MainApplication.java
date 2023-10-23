package com.wyjson.module_main;

import android.app.Application;

import com.wyjson.module_common.application.IApplication;
import com.wyjson.module_common.route.MainRoute;
import com.wyjson.module_main.activity.MainActivity;
import com.wyjson.module_main.activity.SplashActivity;
import com.wyjson.module_main.fragment.DocumentFragment;
import com.wyjson.router.annotation.ApplicationModule;
import com.wyjson.router.core.GoRouter;

@ApplicationModule(priority = 2)
public class MainApplication implements IApplication {

    @Override
    public void init(Application app) {
        GoRouter.getInstance().build(MainRoute.SplashActivity).commit(SplashActivity.class);
        GoRouter.getInstance().build(MainRoute.MainActivity).commit(MainActivity.class);
        GoRouter.getInstance().build(MainRoute.DocumentFragment).commit(DocumentFragment.class);
    }

}
