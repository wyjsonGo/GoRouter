package com.wyjson.router.thread;

import java.util.concurrent.CountDownLatch;

public class CancelableCountDownLatch extends CountDownLatch {

    /**
     * 构造一个用给定计数初始化的{@link CountDownLatch}
     *
     * @param count 计算在线程可以通过{@link #await}之前必须调用{@link #countDown}的次数。
     * @throws IllegalArgumentException if {@code count} is negative
     */
    public CancelableCountDownLatch(int count) {
        super(count);
    }

    public void cancel() {
        while (getCount() > 0) {
            countDown();
        }
    }
}
