package com.wyjson.router.interceptor;

import androidx.annotation.NonNull;

import com.wyjson.router.card.Card;

public interface InterceptorCallback {

    void onContinue(Card card);

    void onInterrupt(Card card, @NonNull Throwable exception);
}
