package com.wyjson.router.core;

class Constants {

    static final String GOROUTER_SP_CACHE_KEY = "SP_GOROUTER_CACHE";
    static final String SEPARATOR = "$$";
    static final String PACKAGE_NAME = "com.wyjson.router";


    static final String GOROUTER_SP_KEY_ROUTE_MODULE_MAP = "ROUTE_MODULE_MAP";
    // 路由注册生成类所在包名
    static final String ROUTE_MODULE_PACKAGE = PACKAGE_NAME + ".module.route";
    // 路由注册生成类名后缀$$Route
    static final String ROUTE_MODULE_NAME_SUFFIX = SEPARATOR + "Route";


    static final String GOROUTER_SP_KEY_APPLICATION_MODULE_MAP = "APPLICATION_MODULE_MAP";
    // 路由注册生成类所在包名
    static final String APPLICATION_MODULE_PACKAGE = PACKAGE_NAME + ".module.application";
    // 路由注册生成类名后缀$$AP
    static final String APPLICATION_MODULE_NAME_SUFFIX = SEPARATOR + "AP";// ApplicationProxy
}