package com.wyjson.module_common.application;

import androidx.core.util.Consumer;

import java.util.ArrayList;
import java.util.List;

class ApplicationUtils {

    private static final List<IApplication> applications = new ArrayList<>();

    /**
     * 相同的类会被覆盖(更新)
     * 调用时机可以在application或插件模块加载时
     *
     * @param application 实现类.class
     */
    public static void add(Class<? extends IApplication> application) {
        try {
            IApplication instance = application.getConstructor().newInstance();
            applications.add(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行接口的实现
     *
     * @param consumer
     */
    public static void execute(Consumer<IApplication> consumer) {
        for (IApplication application : applications) {
            consumer.accept(application);
        }
    }

    /**
     * 清除List
     */
    public static void clearApplication() {
        applications.clear();
    }
}