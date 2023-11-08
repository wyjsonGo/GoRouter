package com.wyjson.router.callback;

import androidx.annotation.NonNull;

import com.wyjson.router.card.Card;

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
    public void onInterrupt(Card card, @NonNull Throwable exception) {

    }
}