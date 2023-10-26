package com.wyjson.go_router;

import com.wyjson.module_common.CommonApplication;
import com.wyjson.module_main.MainApplication;
import com.wyjson.module_user.UserApplication;
import com.wyjson.module_common.application.SuperApplication;

public class MyApplication extends SuperApplication {

    @Override
    protected void initModuleApplication() {
        registerModuleApplication(CommonApplication.class);
        registerModuleApplication(MainApplication.class);
        registerModuleApplication(UserApplication.class);
    }

}
