package com.wyjson.module_user.route.interceptor;

import static com.wyjson.module_common.route.utils.RouteTagUtils.TagEnum.AUTHENTICATION;

import com.wyjson.module_common.route.utils.RouteTagUtils;
import com.wyjson.router.annotation.Interceptor;
import com.wyjson.router.core.Card;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.interceptor.InterceptorCallback;
import com.wyjson.router.interfaces.IInterceptor;

@Interceptor(priority = 100, remark = "身份验证拦截器")
public class AuthenticationInterceptor implements IInterceptor {

    @Override
    public void init() {

    }

    @Override
    public void process(Card card, InterceptorCallback callback) {
        if (RouteTagUtils.TagEnum.isExist(card.getTag(), AUTHENTICATION)) {
            if (true) {// 判断用户是否身份验证了
                callback.onInterrupt(card, new RouterException("未身份认证,拦截!"));
                return;
            }
        }
        callback.onContinue(card);
    }
}