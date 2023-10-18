package com.wyjson.router.interceptor;

import com.wyjson.router.core.Card;

public interface InterceptorCallback {

    void onContinue(Card card);

    void onInterrupt(Throwable exception);
}
