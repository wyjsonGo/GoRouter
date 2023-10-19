package com.wyjson.router.interceptor;

import com.wyjson.router.exception.RouterException;
import com.wyjson.router.interfaces.IInterceptor;

import java.util.Map;

public class InterceptorUtils {

    private InterceptorUtils() {
    }

    private static final Map<Integer, IInterceptor> interceptors = new UniqueKeyTreeMap<>("More than one interceptors use same priority [%s]");

    public static Map<Integer, IInterceptor> getInterceptors() {
        return interceptors;
    }

    /**
     * 相同优先级添加会catch
     * 调用时机可以在application或插件模块加载时
     *
     * @param priority
     * @param interceptor
     * @param isForce
     */
    public static void addInterceptor(int priority, Class<? extends IInterceptor> interceptor, boolean isForce) {
        try {
            if (isForce) {
                interceptors.remove(priority);
            }
            IInterceptor instance = interceptor.getConstructor().newInstance();
            instance.init();
            interceptors.put(priority, instance);
        } catch (Exception e) {
            throw new RouterException(e);
        }
    }

    /**
     * 相同优先级添加会覆盖
     * 调用时机可以在application或插件模块加载时
     *
     * @param priority
     * @param interceptor
     */
    public static void setInterceptor(int priority, Class<? extends IInterceptor> interceptor) {
        addInterceptor(priority, interceptor, true);
    }

    public static void clearIterator() {
        interceptors.clear();
    }
}