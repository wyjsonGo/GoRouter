package com.wyjson.router.callback;

import androidx.annotation.NonNull;

import com.wyjson.router.model.Card;

public interface GoCallback {

    /**
     * 当找到目的地的回调
     *
     * @param card
     */
    void onFound(Card card);

    /**
     * 迷路后的回调。
     *
     * @param card
     */
    void onLost(Card card);

    /**
     * 导航后的回调
     *
     * @param card
     */
    void onArrival(Card card);

    /**
     * 中断时的回调
     *
     * @param card
     */
    void onInterrupt(Card card, @NonNull Throwable exception);
}
