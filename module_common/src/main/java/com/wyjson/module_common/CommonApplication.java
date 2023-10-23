package com.wyjson.module_common;

import android.app.Application;

import com.wyjson.module_common.application.IApplication;
import com.wyjson.module_common.route.service.DegradeServiceImpl;
import com.wyjson.module_common.route.service.PretreatmentServiceImpl;
import com.wyjson.router.annotation.ApplicationModule;
import com.wyjson.router.core.GoRouter;

@ApplicationModule(priority = 1)
public class CommonApplication implements IApplication {

    @Override
    public void init(Application app) {
        if (BuildConfig.DEBUG) {
            GoRouter.openLog();
//            GoRouter.printStackTrace();
        }
        GoRouter.getInstance().addService(DegradeServiceImpl.class);
        GoRouter.getInstance().addService(PretreatmentServiceImpl.class);
    }

}
