package com.wyjson.router.interfaces;

import android.content.Context;

import com.wyjson.router.core.Card;

/**
 * 为路由器提供降级服务，可以在路由丢失时采取措施。
 */
public interface DegradeService extends IService {

    void onLost(Context context, Card card);
}
