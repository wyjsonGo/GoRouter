package com.wyjson.module_common.route;

import com.wyjson.router.enums.RouteType;

public interface BaseRoute {

    String IS_LOGIN = "isLogin";

    String ACTIVITY_ROUTE_SUFFIX = RouteType.ACTIVITY.getType();
    String FRAGMENT_ROUTE_SUFFIX = RouteType.FRAGMENT.getType();
    String DIALOG_FRAGMENT_ROUTE_SUFFIX = RouteType.DIALOG_FRAGMENT.getType();

}