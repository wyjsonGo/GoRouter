package com.wyjson.router.compiler.doc.model;

public class InterceptorModel {

    public int priority;
    public String remark;
    public String className;

    public InterceptorModel() {
    }

    public InterceptorModel(int priority, String className, String remark) {
        this.priority = priority;
        this.className = className;
        this.remark = remark;
    }
}
