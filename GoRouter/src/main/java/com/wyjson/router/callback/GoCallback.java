package com.wyjson.router.callback;

import com.wyjson.router.core.Card;

public interface GoCallback {

    /**
     * 当找到目的地时回调
     *
     * @param card
     */
    void onFound(Card card);

    /**
     * 迷路后再回电话。
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
    void onInterrupt(Card card, Throwable exception);
}
