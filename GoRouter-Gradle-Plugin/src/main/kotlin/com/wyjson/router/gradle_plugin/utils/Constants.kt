package com.wyjson.router.gradle_plugin.utils

object Constants {
    const val GENERATE_ROUTE_DOC_TASK_NAME = "generateRouteDoc";
    const val QUICK_GENERATE_ROUTE_DOC_TASK_NAME = "quickGenerateRouteDoc";
    const val ASSEMBLE_MODULE_ROUTE_TASK_TASK_NAME = "AssembleModuleRouteTask";

    private const val SEPARATOR = "$$";
    private const val PACKAGE_NAME = "com.wyjson.router"

    const val PROJECT = "GoRouter";
    const val DOCUMENT_FILE_NAME = "route-doc.json"
    const val _CLASS = ".class"

    // 路由注册生成类名后缀$$GoRouter
    const val MODULE_ROUTE_NAME_SUFFIX = SEPARATOR + "GoRouter"

    // 需要扫描注入的类,所在的包名
    const val SCAN_TARGET_INJECT_PACKAGE_NAME = "$PACKAGE_NAME.module.route"

    // 注入到这个类下
    const val INJECT_CLASS_NAME = "$PACKAGE_NAME.core.RouteModuleLoadCenter"

    // 注入到这个方法
    const val INJECT_METHOD_NAME = "loadModuleRouteByPlugin"

    // 注入内容方法名
    const val INJECT_TARGET_METHOD_NAME = "register"


    fun dotToSlash(str: String): String {
        return str.replace(".", "/")
    }

    fun slashToDot(str: String): String {
        return str.replace("/", ".")
    }

}