package com.wyjson.router.interfaces;

import android.content.Context;

import com.wyjson.router.model.Card;

/**
 * 为路由器提供降级服务，可以在路由丢失时采取措施。
 */
public interface IDegradeService extends IService {

    void onLost(Context context, Card card);
}
