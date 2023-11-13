package com.wyjson.router.utils;

import androidx.annotation.Nullable;

import com.wyjson.router.GoRouter;
import com.wyjson.router.model.CardMeta;

import java.util.HashMap;
import java.util.Map;

public class RouteHashMap extends HashMap<String, CardMeta> {

    @Nullable
    @Override
    public CardMeta put(String key, CardMeta value) {
        // 检查路由是否有重复提交的情况(仅对使用java注册方式有效)
        if (GoRouter.isDebug()) {
            if (containsKey(key)) {
                GoRouter.logger.error(null, "route path[" + key + "] duplicate commit!!!");
            } else if (containsValue(value)) {
                GoRouter.logger.error(null, "route pathClass[" + value.getPathClass() + "] duplicate commit!!!");
            }
        }
        return super.put(key, value);
    }

    public boolean containsValue(CardMeta value) {
        if (size() == 0)
            return false;
        for (Map.Entry<String, CardMeta> entry : entrySet()) {
            if (entry.getValue().getPathClass() == value.getPathClass()) {
                return true;
            }
        }
        return false;
    }
}
