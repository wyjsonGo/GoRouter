package com.wyjson.module_main;

import android.app.Application;

import com.wyjson.module_common.application.IApplication;
import com.wyjson.router.module.ModuleMainGoRouter;

public class MainApplication implements IApplication {

    @Override
    public void init(Application app) {
        ModuleMainGoRouter.load();
    }

}
