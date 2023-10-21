package com.wyjson.module_user.route.interceptor;

import com.wyjson.module_common.route.enums.RouteTag;
import com.wyjson.router.core.Card;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.interceptor.InterceptorCallback;
import com.wyjson.router.interfaces.IInterceptor;

public class AuthenticationInterceptor implements IInterceptor {

    @Override
    public void init() {

    }

    @Override
    public void process(Card card, InterceptorCallback callback) {
        if (RouteTag.isExist(card.getTag(), RouteTag.AUTHENTICATION)) {
            if (true) {// 判断用户是否身份验证了
                callback.onInterrupt(card, new RouterException("未身份认证,拦截!"));
                return;
            }
        }
        callback.onContinue(card);
    }
}