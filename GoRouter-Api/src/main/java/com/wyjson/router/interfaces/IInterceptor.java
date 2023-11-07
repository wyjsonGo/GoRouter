package com.wyjson.router.interfaces;

import com.wyjson.router.card.Card;
import com.wyjson.router.interceptor.InterceptorCallback;

/**
 * 拦截器
 */
public interface IInterceptor {

    void init();

    /**
     * {@link InterceptorCallback#onContinue(Card)} 继续执行
     * {@link InterceptorCallback#onInterrupt(Card, Throwable)}} 拦截
     *
     * @param card
     * @param callback
     */
    void process(Card card, InterceptorCallback callback);
}