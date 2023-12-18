package com.wyjson.router.compiler.doc.model;

import org.apache.commons.lang3.StringUtils;

public class ServiceModel {

    private String alias;
    private String remark;
    private String moduleName;
    private String prototype;
    private String className;

    public ServiceModel() {
    }

    public ServiceModel(String alias, String prototype, String className, String remark, String moduleName) {
        if (!StringUtils.isEmpty(alias)) {
            this.alias = alias;
        }
        this.prototype = prototype;
        this.className = className;
        this.moduleName = moduleName;
        if (!StringUtils.isEmpty(remark)) {
            this.remark = remark;
        }
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
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

    public String getModuleName() {
        return moduleName;
    }

    public ServiceModel setModuleName(String moduleName) {
        this.moduleName = moduleName;
        return this;
    }
}
