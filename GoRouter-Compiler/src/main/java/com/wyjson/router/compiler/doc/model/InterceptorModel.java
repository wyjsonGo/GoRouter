package com.wyjson.router.compiler.doc.model;

import org.apache.commons.lang3.StringUtils;

public class InterceptorModel {

    private int ordinal;
    private String remark;
    private String className;

    public InterceptorModel() {
    }

    public InterceptorModel(int ordinal, String className, String remark) {
        this.ordinal = ordinal;
        this.className = className;
        if (!StringUtils.isEmpty(remark)) {
            this.remark = remark;
        }
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
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
