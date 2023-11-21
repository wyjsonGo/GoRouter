package com.wyjson.router.core;

import com.wyjson.router.GoRouter;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.interfaces.IInterceptor;

public class InterceptorCenter {

    /**
     * 重复添加相同序号会catch
     *
     * @param ordinal
     * @param interceptor
     * @param isForce
     */
    public static void addInterceptor(int ordinal, Class<? extends IInterceptor> interceptor, boolean isForce) {
        String title = isForce ? "[setInterceptor]" : "[addInterceptor]";
        try {
            if (isForce) {
                Warehouse.interceptors.remove(ordinal);
            }
            IInterceptor instance = interceptor.getConstructor().newInstance();
            instance.init();
            Warehouse.interceptors.put(ordinal, instance);
            GoRouter.logger.debug(null, title + " size:" + Warehouse.interceptors.size() + ", ordinal:" + ordinal + " -> " + interceptor.getSimpleName());
        } catch (Exception e) {
            throw new RouterException(title + " " + e.getMessage());
        }
    }

    /**
     * 重复添加相同序号会覆盖(更新)
     *
     * @param ordinal
     * @param interceptor
     */
    public static void setInterceptor(int ordinal, Class<? extends IInterceptor> interceptor) {
        addInterceptor(ordinal, interceptor, true);
    }

    public static void clearInterceptors() {
        Warehouse.interceptors.clear();
    }

}
