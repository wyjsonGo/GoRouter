package com.wyjson.module_user;

import android.app.Application;

import com.wyjson.module_common.application.IApplication;
import com.wyjson.module_common.route.UserRoute;
import com.wyjson.module_user.activity.ParamActivity;
import com.wyjson.module_user.fragment.ParamFragment;
import com.wyjson.router.core.GoRouter;
import com.wyjson.router.module.GoRouter_module_user;

public class UserApplication implements IApplication {

    @Override
    public void init(Application app) {
        GoRouter_module_user.loadInto();
        GoRouter.getInstance().build(UserRoute.ParamActivity)
                .putInt("age")
                .putString("name")
                .commitActivity(ParamActivity.class);
        GoRouter.getInstance().build(UserRoute.ParamFragment)
                .putInt("age")
                .putString("name")
                .commitFragment(ParamFragment.class);
    }

}
