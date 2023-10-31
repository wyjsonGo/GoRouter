package com.wyjson.router.compiler.doc.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentModel {

    public Map<String, ServiceModel> services;
    public List<InterceptorModel> interceptors;
    public List<RouteModel> routes;

    public DocumentModel() {
    }

    public Map<String, ServiceModel> getServices() {
        if (services == null) {
            services = new HashMap<>();
        }
        return services;
    }

    public List<InterceptorModel> getInterceptors() {
        if (interceptors == null) {
            interceptors = new ArrayList<>();
        }
        return interceptors;
    }

    public List<RouteModel> getRoutes() {
        if (routes == null) {
            routes = new ArrayList<>();
        }
        return routes;
    }

}
