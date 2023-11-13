package com.wyjson.router.utils;

import androidx.annotation.Nullable;

import com.wyjson.router.GoRouter;
import com.wyjson.router.interfaces.IService;
import com.wyjson.router.model.ServiceMeta;

import java.util.HashMap;

public class ServiceHashMap extends HashMap<Class<? extends IService>, ServiceMeta> {

    @Nullable
    @Override
    public ServiceMeta put(Class<? extends IService> key, ServiceMeta value) {
        // [xx]服务已经在[xxx]实现中注册，并将被覆盖(更新)。
        if (containsKey(key)) {
            GoRouter.logger.warning(null, "The [" + key.getSimpleName() + "] service has been registered in the [" + value.getServiceClass().getSimpleName() + "] implementation and will be overridden (updated).");
        }
        return super.put(key, value);
    }

}
