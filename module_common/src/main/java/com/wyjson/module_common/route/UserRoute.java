package com.wyjson.module_common.route;


public interface UserRoute extends BaseRoute {

    String PREFIX = "/user";

    String SignInActivity = PREFIX + "/sign_in" + ACTIVITY_ROUTE_SUFFIX;
    String CardFragment = PREFIX + "/card" + FRAGMENT_ROUTE_SUFFIX;
    String UserInfoActivity = PREFIX + "/info" + ACTIVITY_ROUTE_SUFFIX;

    /**
     * 主要为了演示一个模块下多个路由分组
     */
    String NEW_PREFIX = "/new";
    String ParamActivity = NEW_PREFIX + "/param" + ACTIVITY_ROUTE_SUFFIX;
    String ParamFragment = NEW_PREFIX + "/param" + FRAGMENT_ROUTE_SUFFIX;
}