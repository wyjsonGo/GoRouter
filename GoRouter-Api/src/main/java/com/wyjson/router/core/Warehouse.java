package com.wyjson.router.core;

import com.wyjson.router.interfaces.IInterceptor;
import com.wyjson.router.interfaces.IService;
import com.wyjson.router.model.CardMeta;
import com.wyjson.router.model.ServiceMeta;
import com.wyjson.router.module.interfaces.IRouteModuleGroup;
import com.wyjson.router.utils.InterceptorTreeMap;
import com.wyjson.router.utils.RouteGroupHashMap;

import java.util.HashMap;
import java.util.Map;

class Warehouse {

    static final Map<String, IRouteModuleGroup> routeGroups = new RouteGroupHashMap();
    static final Map<String, CardMeta> routes = new HashMap<>();

    static final Map<Class<? extends IService>, ServiceMeta> services = new HashMap<>();

    static final Map<Integer, IInterceptor> interceptors = new InterceptorTreeMap<>("More than one interceptors use same priority [%s]");

}
