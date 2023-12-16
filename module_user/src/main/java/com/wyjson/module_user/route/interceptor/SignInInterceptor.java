package com.wyjson.module_user.route.interceptor;

import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AlertDialog;

import com.wyjson.module_common.route.utils.RouteTagUtils;
import com.wyjson.router.GoRouterHelper;
import com.wyjson.router.annotation.Interceptor;
import com.wyjson.router.callback.InterceptorCallback;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.interfaces.IInterceptor;
import com.wyjson.router.model.Card;

@Interceptor(ordinal = 1, remark = "登录拦截器")
public class SignInInterceptor implements IInterceptor {

    @Override
    public void init() {

    }

    @Override
    public void process(Card card, InterceptorCallback callback) {
        if (RouteTagUtils.TagEnum.isExist(card.getTag(), RouteTagUtils.TagEnum.LOGIN)) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(card.getContext())
                            .setMessage("登录拦截器,用户是否登录?")
                            .setNegativeButton("没登录", (dialog, which) -> {
                                callback.onInterrupt(card, new RouterException("未登录,拦截自动跳转登录页!"));
                                GoRouterHelper.goSignInActivity(card.getContext());
                            })
                            .setPositiveButton("已登录", (dialog, which) -> {
                                callback.onContinue(card);
                            })
                            .create().show();
                }
            });
            return;
        }
        callback.onContinue(card);
    }
}