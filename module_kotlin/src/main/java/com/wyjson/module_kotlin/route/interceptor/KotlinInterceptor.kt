package com.wyjson.module_kotlin.route.interceptor

import com.wyjson.router.annotation.Interceptor
import com.wyjson.router.callback.InterceptorCallback
import com.wyjson.router.interfaces.IInterceptor
import com.wyjson.router.model.Card

@Interceptor(ordinal = 50, remark = "Kotlin拦截器")
class KotlinInterceptor : IInterceptor {
    override fun init() {

    }

    override fun process(card: Card?, callback: InterceptorCallback) {
        callback.onContinue(card)
    }
}