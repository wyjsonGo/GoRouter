package com.wyjson.application;

import android.app.Application;

import com.wyjson.module_common.application.IApplication;
import com.wyjson.module_common.route.UserRoute;
import com.wyjson.module_common.route.enums.RouteExtra;
import com.wyjson.module_user.activity.ParamActivity;
import com.wyjson.module_user.activity.SignInActivity;
import com.wyjson.module_user.activity.UserInfoActivity;
import com.wyjson.module_user.fragment.CardFragment;
import com.wyjson.module_user.fragment.ParamFragment;
import com.wyjson.module_user.route.interceptor.SignInInterceptor;
import com.wyjson.module_user.route.service.UserServiceImpl;
import com.wyjson.router.core.GoRouter;

public class UserApplication implements IApplication {

    @Override
    public void init(Application app) {
        GoRouter.getInstance().addInterceptor(1, SignInInterceptor.class);
        GoRouter.getInstance().addService(UserServiceImpl.class);
        
        GoRouter.getInstance().build(UserRoute.UserInfoActivity)
                .putExtra(RouteExtra.LOGIN.getValue())
                .commit(UserInfoActivity.class);

        GoRouter.getInstance().build(UserRoute.SignInActivity).commit(SignInActivity.class);
        GoRouter.getInstance().build(UserRoute.CardFragment).commit(CardFragment.class);
        GoRouter.getInstance().build(UserRoute.ParamActivity)
                .putInt("age")
                .putString("name")
                .commit(ParamActivity.class);
        GoRouter.getInstance().build(UserRoute.ParamFragment)
                .putInt("age")
                .putString("name")
                .commit(ParamFragment.class);
    }

}
