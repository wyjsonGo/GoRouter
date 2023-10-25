package com.wyjson.module_user;

import android.app.Application;

import com.wyjson.module_common.application.IApplication;
import com.wyjson.router.module.ModuleUserGoRouter;

public class UserApplication implements IApplication {

    @Override
    public void init(Application app) {
        ModuleUserGoRouter.load();
    }
}
