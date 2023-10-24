package com.wyjson.module_common;

import android.app.Application;

import com.wyjson.module_common.application.IApplication;
import com.wyjson.router.core.GoRouter;
import com.wyjson.router.module.module_commonGoRouter;

public class CommonApplication implements IApplication {

    @Override
    public void init(Application app) {
        if (BuildConfig.DEBUG) {
            GoRouter.openLog();
//            GoRouter.printStackTrace();
        }
        module_commonGoRouter.loadInto();
    }

}
