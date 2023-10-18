package com.wyjson.module_user.route.interceptor;

import static com.wyjson.module_common.route.BaseRoute.IS_LOGIN;

import com.wyjson.module_common.route.UserRoute;
import com.wyjson.module_common.route.enums.RouteExtra;
import com.wyjson.router.core.Card;
import com.wyjson.router.core.GoRouter;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.interceptor.InterceptorCallback;
import com.wyjson.router.interfaces.IInterceptor;

public class SignInInterceptor implements IInterceptor {

    @Override
    public void init() {

    }

    @Override
    public void process(Card card, InterceptorCallback callback) {
        if (RouteExtra.isExist(card.getExtra(), RouteExtra.LOGIN) || card.getExtras().getBoolean(IS_LOGIN)) {
            if (false) {// 判断用户是否登录
                GoRouter.getInstance().build(UserRoute.SignInActivity).go(card.getContext());
                callback.onInterrupt(new RouterException("未登录"));
                return;
            }
        }

        callback.onContinue(card);
    }
}