package com.wyjson.router.load;

import android.app.Application;

import com.wyjson.router.core.GoRouter;
import com.wyjson.router.utils.TextUtils;

public class RouteModuleLoadUtils {

    private static boolean registerByPlugin;

    public static void loadModuleRoute(Application application) {
        loadModuleRouteByPlugin();
        if (!registerByPlugin) {
            GoRouter.logger.info(null, "The runtime loads the route by scanning the dex file.");
            loadModuleRouteByDex(application);
        } else {
            GoRouter.logger.info(null, "Load routes by [GoRouter-Gradle-Plugin] plugin.");
        }
    }

    /**
     * GoRouter-Gradle-Plugin plugin will generate code inside this method
     * call this method to register all Routes, Interceptors and Services
     */
    private static void loadModuleRouteByPlugin() {
        registerByPlugin = false;
        // auto generate register code by gradle plugin: GoRouter-Gradle-Plugin
        // looks like below:
        // register("class name");
        // register("class name");
    }

    /**
     * register by class name
     * Sacrificing a bit of efficiency to solve
     * the problem that the main dex file size is too large
     */
    private static void register(String className) {
        if (!TextUtils.isEmpty(className)) {
            try {
                Class<?> clazz = Class.forName(className);
                Object obj = clazz.getConstructor().newInstance();
                if (obj instanceof IRouteModule) {
                    markRegisteredByPlugin();
                    ((IRouteModule) obj).load();
                } else {
                    GoRouter.logger.error(null,
                            "register failed, class name: " + className
                                    + " should implements one of IRouteModule.");
                }
            } catch (Exception e) {
                GoRouter.logger.error(null, "register class error:" + className, e);
            }
        }
    }

    /**
     * mark already registered by GoRouter-Gradle-Plugin plugin
     */
    private static void markRegisteredByPlugin() {
        if (!registerByPlugin) {
            registerByPlugin = true;
        }
    }

    private static void loadModuleRouteByDex(Application application) {
        // TODO: 2023/11/5 :::未完成
    }

}
