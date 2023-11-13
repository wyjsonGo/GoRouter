package com.wyjson.router.callback;

import androidx.annotation.NonNull;

import com.wyjson.router.model.Card;

public interface InterceptorCallback {

    void onContinue(Card card);

    void onInterrupt(Card card, @NonNull Throwable exception);
}
