package com.wyjson.module_user;

import android.app.Application;

import com.wyjson.module_common.application.IApplication;
import com.wyjson.module_common.route.UserRoute;
import com.wyjson.module_common.route.enums.RouteTag;
import com.wyjson.module_user.activity.ParamActivity;
import com.wyjson.module_user.activity.SignInActivity;
import com.wyjson.module_user.activity.UserInfoActivity;
import com.wyjson.module_user.fragment.CardFragment;
import com.wyjson.module_user.fragment.ParamFragment;
import com.wyjson.module_user.route.interceptor.AuthenticationInterceptor;
import com.wyjson.module_user.route.interceptor.SignInInterceptor;
import com.wyjson.router.annotation.ApplicationModule;
import com.wyjson.router.core.GoRouter;

@ApplicationModule(priority = 3)
public class UserApplication implements IApplication {

    @Override
    public void init(Application app) {
        // TODO: 2023/10/23 :::Test
//        GoRouter.getInstance().addService(UserServiceImpl.class);
        GoRouter.getInstance().addInterceptor(1, SignInInterceptor.class);
        GoRouter.getInstance().addInterceptor(100, AuthenticationInterceptor.class);

        GoRouter.getInstance().build(UserRoute.UserInfoActivity)
                .putTag(RouteTag.LOGIN.getValue() | RouteTag.AUTHENTICATION.getValue())
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
