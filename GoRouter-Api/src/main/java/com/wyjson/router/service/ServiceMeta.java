package com.wyjson.router.service;

import com.wyjson.router.interfaces.IService;

public class ServiceMeta {
    private final Class<? extends IService> serviceClass;
    private IService service;

    public ServiceMeta(Class<? extends IService> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public Class<? extends IService> getServiceClass() {
        return serviceClass;
    }

    public IService getService() {
        return service;
    }

    public void setService(IService service) {
        this.service = service;
    }
}