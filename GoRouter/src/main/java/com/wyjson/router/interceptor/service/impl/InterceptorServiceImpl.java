package com.wyjson.router.interceptor.service.impl;

import androidx.annotation.NonNull;

import com.wyjson.router.core.Card;
import com.wyjson.router.core.GoRouter;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.interceptor.InterceptorCallback;
import com.wyjson.router.interceptor.InterceptorUtils;
import com.wyjson.router.interceptor.service.InterceptorService;
import com.wyjson.router.interfaces.IInterceptor;
import com.wyjson.router.thread.CancelableCountDownLatch;
import com.wyjson.router.utils.MapUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class InterceptorServiceImpl implements InterceptorService {

    @Override
    public void doInterceptions(Card card, InterceptorCallback callback) {
        GoRouter.logger.info(null, "[doInterceptions] " + InterceptorUtils.getInterceptors());
        if (MapUtils.isNotEmpty(InterceptorUtils.getInterceptors())) {
            Iterator<Map.Entry<Integer, IInterceptor>> iterator = InterceptorUtils.getInterceptors().entrySet().iterator();
            GoRouter.getInstance().getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    CancelableCountDownLatch interceptorCounter = new CancelableCountDownLatch(InterceptorUtils.getInterceptors().size());
                    try {
                        execute(card, iterator, interceptorCounter);
                        interceptorCounter.await(card.getTimeout(), TimeUnit.SECONDS);
                        if (interceptorCounter.getCount() > 0) {
                            RouterException exception = new RouterException("The interceptor processing timed out.");
                            GoRouter.logger.warning(null, "[doInterceptions] [onInterrupt] message:" + exception.getMessage());
                            callback.onInterrupt(card, exception);
                        } else if (card.getInterceptorException() != null) {
                            callback.onInterrupt(card, card.getInterceptorException());
                        } else {
                            callback.onContinue(card);
                        }
                    } catch (Exception e) {
                        callback.onInterrupt(card, new RouterException("The interceptor handles exceptions."));
                    }
                }
            });
        } else {
            callback.onContinue(card);
        }
    }

    private static void execute(Card card, Iterator<Map.Entry<Integer, IInterceptor>> iterator, final CancelableCountDownLatch counter) {
        if (iterator.hasNext()) {
            Map.Entry<Integer, IInterceptor> interceptorEntry = iterator.next();
            interceptorEntry.getValue().process(card, new InterceptorCallback() {
                @Override
                public void onContinue(Card card) {
                    counter.countDown();
                    execute(card, iterator, counter);
                }

                @Override
                public void onInterrupt(Card card, @NonNull Throwable exception) {
                    card.setInterceptorException(exception == null ? new RouterException() : exception);
                    GoRouter.logger.warning(null, "[doInterceptions] [onInterrupt] {" + interceptorEntry.getKey() + "->" + interceptorEntry.getValue().getClass().getSimpleName() + "} message:" + card.getInterceptorException().getMessage());
                    counter.cancel();
                }
            });
        }
    }

    @Override
    public void init() {

    }
}
