package com.wyjson.module_common.route;


public interface UserRoute extends BaseRoute {

    String PREFIX = "/user";

    String SignInActivity = PREFIX + "/sign_in" + ACTIVITY_ROUTE_SUFFIX;
    String ParamActivity = PREFIX + "/param" + ACTIVITY_ROUTE_SUFFIX;
    String CardFragment = PREFIX + "/card" + FRAGMENT_ROUTE_SUFFIX;
    String ParamFragment = PREFIX + "/param" + FRAGMENT_ROUTE_SUFFIX;
    String UserInfoActivity = PREFIX + "/info" + ACTIVITY_ROUTE_SUFFIX;

}