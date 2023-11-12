package com.wyjson.router.core.interfaces;

import com.wyjson.router.callback.InterceptorCallback;
import com.wyjson.router.model.Card;
import com.wyjson.router.interfaces.IService;

public interface IInterceptorService extends IService {
    void doInterceptions(Card card, InterceptorCallback callback);
}