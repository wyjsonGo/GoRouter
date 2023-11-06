package com.wyjson.router.interceptor;

import com.wyjson.router.core.GoRouter;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.interfaces.IInterceptor;

import java.util.Map;

public class InterceptorHelper {

    private InterceptorHelper() {
    }

    private static class InstanceHolder {
        private static final InterceptorHelper mInstance = new InterceptorHelper();
    }

    public static InterceptorHelper getInstance() {
        return InterceptorHelper.InstanceHolder.mInstance;
    }

    private static final Map<Integer, IInterceptor> interceptors = new InterceptorTreeMap<>("More than one interceptors use same priority [%s]");

    public Map<Integer, IInterceptor> getInterceptors() {
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
    public void addInterceptor(int priority, Class<? extends IInterceptor> interceptor, boolean isForce) {
        try {
            if (isForce) {
                interceptors.remove(priority);
            }
            IInterceptor instance = interceptor.getConstructor().newInstance();
            instance.init();
            interceptors.put(priority, instance);

            String title = isForce ? "[setInterceptor]" : "[addInterceptor]";
            GoRouter.logger.debug(null, title + " size:" + interceptors.size() + ", priority:" + priority + " -> " + interceptor.getSimpleName());
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
    public void setInterceptor(int priority, Class<? extends IInterceptor> interceptor) {
        addInterceptor(priority, interceptor, true);
    }

    public void clearIterator() {
        interceptors.clear();
    }
}