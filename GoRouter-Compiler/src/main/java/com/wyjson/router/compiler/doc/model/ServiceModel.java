package com.wyjson.router.compiler.doc.model;

import org.apache.commons.lang3.StringUtils;

public class ServiceModel {

    private String remark;
    private String prototype;
    private String className;

    public ServiceModel() {
    }

    public ServiceModel(String prototype, String className, String remark) {
        this.prototype = prototype;
        this.className = className;
        if (!StringUtils.isEmpty(remark)) {
            this.remark = remark;
        }
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPrototype() {
        return prototype;
    }

    public void setPrototype(String prototype) {
        this.prototype = prototype;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
