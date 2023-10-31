package com.wyjson.module_common;

import android.app.Application;

import com.wyjson.module_common.application.IApplication;
import com.wyjson.router.core.GoRouter;
import com.wyjson.router.module.ModuleCommonGoRouter;

public class CommonApplication implements IApplication {

    @Override
    public void init(Application app) {
        if (BuildConfig.DEBUG) {
            GoRouter.openDebug();
//            GoRouter.printStackTrace();
        }
        ModuleCommonGoRouter.load();
    }

}
