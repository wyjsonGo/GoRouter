package com.wyjson.module_common.route;


public interface MainRoute extends BaseRoute {

    String PREFIX = "/main";

    String SplashActivity = PREFIX + "/splash" + ACTIVITY_ROUTE_SUFFIX;

    String MainActivity = PREFIX + ACTIVITY_ROUTE_SUFFIX;

}