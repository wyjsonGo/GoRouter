package com.wyjson.router.interfaces;

import android.content.Context;

import com.wyjson.router.model.Card;

/**
 * 预处理
 */
public interface IPretreatmentService extends IService {

    /**
     * 跳转前预处理
     *
     * @param context
     * @param card
     * @return true继续执行, 如果需要自行处理跳转该方法返回 false 即可
     */
    boolean onPretreatment(Context context, Card card);
}
