package com.wyjson.router.compiler.doc.model;

public class ServiceModel {

    public String remark;
    public String prototype;
    public String className;

    public ServiceModel() {
    }

    public ServiceModel(String prototype, String className, String remark) {
        this.prototype = prototype;
        this.className = className;
        this.remark = remark;
    }

}
