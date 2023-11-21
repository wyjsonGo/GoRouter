package com.wyjson.router.core;

import androidx.annotation.Nullable;

import com.wyjson.router.GoRouter;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.interfaces.IService;
import com.wyjson.router.model.ServiceMeta;

public class ServiceCenter {

    /**
     * 实现相同接口的service会被覆盖(更新)
     *
     * @param serviceClass 实现类.class
     */
    public static void addService(Class<? extends IService> serviceClass) {
        Class<? extends IService> serviceInterfaceClass = (Class<? extends IService>) serviceClass.getInterfaces()[0];
        Warehouse.services.put(serviceInterfaceClass, new ServiceMeta(serviceClass));
        GoRouter.logger.debug(null, "[addService] size:" + Warehouse.services.size() + ", " + serviceInterfaceClass.getSimpleName() + " -> " + serviceClass.getSimpleName());
    }

    /**
     * 获取service接口的实现
     *
     * @param serviceClass 接口.class
     * @param <T>
     * @return
     */
    @Nullable
    public static <T> T getService(Class<? extends T> serviceClass) {
        ServiceMeta meta = Warehouse.services.get(serviceClass);
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
