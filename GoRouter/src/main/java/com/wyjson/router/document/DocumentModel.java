package com.wyjson.router.document;

import com.wyjson.router.core.CardMeta;
import com.wyjson.router.interfaces.IInterceptor;
import com.wyjson.router.interfaces.IService;
import com.wyjson.router.service.ServiceMeta;

import java.io.Serializable;
import java.util.Map;

public class DocumentModel implements Serializable {

    private final Map<String, CardMeta> routes;
    private final Map<Class<? extends IService>, ServiceMeta> services;
    private final Map<Integer, IInterceptor> interceptors;

    public DocumentModel(Map<String, CardMeta> routes, Map<Class<? extends IService>, ServiceMeta> services, Map<Integer, IInterceptor> interceptors) {
        this.routes = routes;
        this.services = services;
        this.interceptors = interceptors;
    }

    public Map<String, CardMeta> getRoutes() {
        return routes;
    }

    public Map<Class<? extends IService>, ServiceMeta> getServices() {
        return services;
    }

    public Map<Integer, IInterceptor> getInterceptors() {
        return interceptors;
    }

}
