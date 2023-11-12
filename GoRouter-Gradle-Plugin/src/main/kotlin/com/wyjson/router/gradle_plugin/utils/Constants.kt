package com.wyjson.router.gradle_plugin.utils

object Constants {
    const val GENERATE_ROUTE_DOC_TASK_NAME = "generateRouteDoc";
    const val QUICK_GENERATE_ROUTE_DOC_TASK_NAME = "quickGenerateRouteDoc";
    const val ASSEMBLE_ROUTE_MODULE_TASK_TASK_NAME = "AssembleRouteModuleTask";
    const val ASSEMBLE_APPLICATION_MODULE_TASK_TASK_NAME = "AssembleApplicationModuleTask";

    private const val SEPARATOR = "$$";
    private const val PACKAGE_NAME = "com.wyjson.router"

    const val PROJECT = "GoRouter";
    const val DOCUMENT_FILE_NAME = "route-doc.json"
    const val _CLASS = ".class"

    // 路由注册生成类名后缀$$Route
    const val ROUTE_MODULE_NAME_SUFFIX = SEPARATOR + "Route"

    // 需要扫描注入的类,所在的包名
    const val ROUTE_MODULE_SCAN_TARGET_INJECT_PACKAGE_NAME = "$PACKAGE_NAME.module.route"

    // 注入到这个类下
    const val ROUTE_MODULE_INJECT_CLASS_NAME = "$PACKAGE_NAME.core.RouteModuleCenter"

    // 注入到这个方法
    const val ROUTE_MODULE_INJECT_METHOD_NAME = "loadByPlugin"

    // 注入内容方法名
    const val ROUTE_MODULE_INJECT_TARGET_METHOD_NAME = "register"


    /**
     * application module
     */

    // application module生成的代理类名后缀$$AP
    const val APPLICATION_MODULE_NAME_SUFFIX = SEPARATOR + "AP"

    // 需要扫描注入的类,所在的包名
    const val APPLICATION_MODULE_SCAN_TARGET_INJECT_PACKAGE_NAME = "$PACKAGE_NAME.module.application"

    // 注入到这个类下
    const val APPLICATION_MODULE_INJECT_CLASS_NAME = "$PACKAGE_NAME.core.ApplicationModuleCenter"

    // 注入到这个方法
    const val APPLICATION_MODULE_INJECT_METHOD_NAME = "loadByPlugin"

    // 注入内容方法名
    const val APPLICATION_MODULE_INJECT_TARGET_METHOD_NAME = "register"


    fun dotToSlash(str: String): String {
        return str.replace(".", "/")
    }

    fun slashToDot(str: String): String {
        return str.replace("/", ".")
    }

}