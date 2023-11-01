package com.wyjson.router.compiler.doc.model;

public class ParamModel {

    private String name;
    private String type;
    private boolean required;
    private String remark;

    public ParamModel() {
    }

    public ParamModel(String name, String type, boolean required, String remark) {
        this.name = name;
        this.type = type;
        this.required = required;
        this.remark = remark;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
