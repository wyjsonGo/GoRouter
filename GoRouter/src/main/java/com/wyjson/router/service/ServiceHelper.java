package com.wyjson.router.service;

import androidx.annotation.Nullable;

import com.wyjson.router.core.GoRouter;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.interfaces.IService;

import java.util.HashMap;
import java.util.Map;

public class ServiceHelper {

    private ServiceHelper() {
    }

    private static class InstanceHolder {
        private static final ServiceHelper mInstance = new ServiceHelper();
    }

    public static ServiceHelper getInstance() {
        return InstanceHolder.mInstance;
    }

    private static final Map<Class<? extends IService>, ServiceMeta> services = new ServiceHashMap<>();

    public Map<Class<? extends IService>, ServiceMeta> getServices() {
        return services;
    }

    /**
     * 实现相同接口的service会被覆盖(更新)
     * 调用时机可以在application或插件模块加载时
     *
     * @param serviceClass 实现类.class
     */
    public void addService(Class<? extends IService> serviceClass) {
        Class<? extends IService> serviceInterfaceClass = (Class<? extends IService>) serviceClass.getInterfaces()[0];
        services.put(serviceInterfaceClass, new ServiceMeta(serviceClass));
        GoRouter.logger.debug(null, "[addService] size:" + services.size() + ", " + serviceInterfaceClass.getSimpleName() + " -> " + serviceClass.getSimpleName());
    }

    /**
     * 获取service接口的实现
     *
     * @param serviceClass 接口.class
     * @param <T>
     * @return
     */
    @Nullable
    public <T> T getService(Class<? extends T> serviceClass) {
        ServiceMeta meta = services.get(serviceClass);
        if (meta != null) {
            if (serviceClass.isAssignableFrom(meta.getServiceClass())) {
                IService instance = meta.getService();
                if (instance == null) {
                    try {
                        instance = meta.getServiceClass().getConstructor().newInstance();
                        instance.init();
                        meta.setService(instance);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RouterException("serviceClass constructor new instance failed!");
                    }
                }
                GoRouter.logger.info(null, "[getService] " + serviceClass.getSimpleName() + " -> " + meta.getServiceClass().getSimpleName());
                return (T) instance;
            }
        }
        GoRouter.logger.warning(null, "[getService] " + serviceClass.getSimpleName() + ", No registered service found!");
        return null;
    }
}