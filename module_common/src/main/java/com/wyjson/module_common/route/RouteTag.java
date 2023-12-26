package com.wyjson.module_common.route;

/**
 * 单个int有4字节，可以配置31个开关,范围从1到1 << 30
 */
public interface RouteTag {

    int LOGIN = 1;
    int AUTHENTICATION = 1 << 1;
    int SAFETY = 1 << 2;

}