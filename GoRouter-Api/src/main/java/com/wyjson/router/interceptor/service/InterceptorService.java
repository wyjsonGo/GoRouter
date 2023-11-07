package com.wyjson.router.interceptor.service;

import com.wyjson.router.card.Card;
import com.wyjson.router.interceptor.InterceptorCallback;
import com.wyjson.router.interfaces.IService;

public interface InterceptorService extends IService {
    void doInterceptions(Card card, InterceptorCallback callback);
}