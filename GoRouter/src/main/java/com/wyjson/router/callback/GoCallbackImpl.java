package com.wyjson.router.callback;

import com.wyjson.router.core.Card;

public abstract class GoCallbackImpl implements GoCallback {
    @Override
    public void onFound(Card card) {

    }

    @Override
    public void onLost(Card card) {

    }

    @Override
    public abstract void onArrival(Card card);

    @Override
    public void onInterrupt(Card card, Throwable exception) {

    }
}
