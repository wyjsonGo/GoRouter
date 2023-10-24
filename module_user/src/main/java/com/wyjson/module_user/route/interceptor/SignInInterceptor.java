package com.wyjson.module_user.route.interceptor;

import static com.wyjson.module_common.route.BaseRoute.IS_LOGIN;

import com.wyjson.module_common.route.UserRoute;
import com.wyjson.module_common.route.enums.RouteTag;
import com.wyjson.router.annotation.Interceptor;
import com.wyjson.router.core.Card;
import com.wyjson.router.core.GoRouter;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.interceptor.InterceptorCallback;
import com.wyjson.router.interfaces.IInterceptor;

@Interceptor(priority = 1, remark = "登录拦截器")
public class SignInInterceptor implements IInterceptor {

    @Override
    public void init() {

    }

    @Override
    public void process(Card card, InterceptorCallback callback) {
        if (RouteTag.isExist(card.getTag(), RouteTag.LOGIN) || card.getExtras().getBoolean(IS_LOGIN)) {
            if (true) {// 判断用户是否登录
                callback.onInterrupt(card, new RouterException("未登录,拦截自动跳转登录页!"));
                GoRouter.getInstance().build(UserRoute.SignInActivity).go(card.getContext());
                return;
            }
        }
        callback.onContinue(card);
    }
}