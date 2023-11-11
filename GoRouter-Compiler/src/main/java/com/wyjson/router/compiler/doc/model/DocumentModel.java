package com.wyjson.router.compiler.doc.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentModel {

    private Map<String, ServiceModel> services;
    private List<InterceptorModel> interceptors;
    private Map<String, List<RouteModel>> routes;

    public DocumentModel() {
    }

    public Map<String, ServiceModel> getServices() {
        if (services == null) {
            services = new HashMap<>();
        }
        return services;
    }

    public void setServices(Map<String, ServiceModel> services) {
        this.services = services;
    }

    public List<InterceptorModel> getInterceptors() {
        if (interceptors == null) {
            interceptors = new ArrayList<>();
        }
        return interceptors;
    }

    public void setInterceptors(List<InterceptorModel> interceptors) {
        this.interceptors = interceptors;
    }

    public Map<String, List<RouteModel>> getRoutes() {
        if (routes == null) {
            routes = new HashMap<>();
        }
        return routes;
    }

    public void setRoutes(Map<String, List<RouteModel>> routes) {
        this.routes = routes;
    }

}
