package com.wyjson.router.compiler.doc.model;

public class InterceptorModel {

    private int priority;
    private String remark;
    private String className;

    public InterceptorModel() {
    }

    public InterceptorModel(int priority, String className, String remark) {
        this.priority = priority;
        this.className = className;
        this.remark = remark;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
