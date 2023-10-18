package com.wyjson.router.interceptor.service.impl;

import com.wyjson.router.core.Card;
import com.wyjson.router.interceptor.InterceptorCallback;
import com.wyjson.router.interceptor.InterceptorUtils;
import com.wyjson.router.interceptor.service.InterceptorService;
import com.wyjson.router.interfaces.IInterceptor;

import java.util.Iterator;
import java.util.Map;

public class InterceptorServiceImpl implements InterceptorService {

    @Override
    public void doInterceptions(Card card, InterceptorCallback callback) {
        if (InterceptorUtils.getIteratorSize() > 0) {
            Iterator<Map.Entry<Integer, IInterceptor>> iterator = InterceptorUtils.getIterator();
            execute(card, iterator, callback);
        } else {
            callback.onContinue(card);
        }
    }


    private static void execute(Card card, Iterator<Map.Entry<Integer, IInterceptor>> iterator, InterceptorCallback callback) {
        if (iterator.hasNext()) {
            Map.Entry<Integer, IInterceptor> entry = iterator.next();
            entry.getValue().process(card, new InterceptorCallback() {
                @Override
                public void onContinue(Card card) {
                    execute(card, iterator, callback);
                }

                @Override
                public void onInterrupt(Throwable exception) {
                    callback.onInterrupt(exception);
                }
            });
        } else {
            callback.onContinue(card);
        }
    }

    @Override
    public void init() {

    }
}
