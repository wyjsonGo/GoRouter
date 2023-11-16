package com.wyjson.router.core;

import androidx.lifecycle.MutableLiveData;

import com.wyjson.router.interfaces.IApplicationModule;
import com.wyjson.router.interfaces.IInterceptor;
import com.wyjson.router.interfaces.IService;
import com.wyjson.router.model.CardMeta;
import com.wyjson.router.model.ServiceMeta;
import com.wyjson.router.module.interfaces.IRouteModuleGroup;
import com.wyjson.router.utils.InterceptorTreeMap;
import com.wyjson.router.utils.RouteGroupHashMap;
import com.wyjson.router.utils.RouteHashMap;
import com.wyjson.router.utils.ServiceHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Warehouse {

    static final List<IApplicationModule> applicationModules = new ArrayList<>();

    static final Map<String, IRouteModuleGroup> routeGroups = new RouteGroupHashMap();

    static final Map<String, CardMeta> routes = new RouteHashMap();

    static final Map<Class<? extends IService>, ServiceMeta> services = new ServiceHashMap();

    static final Map<Integer, IInterceptor> interceptors = new InterceptorTreeMap<>("More than one interceptors use same ordinal [%s]");

    static final Map<String, MutableLiveData> events = new HashMap<>();

}
