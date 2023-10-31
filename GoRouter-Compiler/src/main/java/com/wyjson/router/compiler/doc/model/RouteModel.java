package com.wyjson.router.compiler.doc.model;

import java.util.List;

public class RouteModel {

    private String path;
    private String remark;
    private String type;
    private String pathClass;
    private Integer tag;
    private List<ParamModel> paramsType;

    public RouteModel() {
    }

    public RouteModel(String path, String type, String pathClass, Integer tag, List<ParamModel> paramsType, String remark) {
        this.path = path;
        this.type = type;
        this.pathClass = pathClass;
        this.tag = tag;
        this.paramsType = paramsType;
        this.remark = remark;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPathClass(String pathClass) {
        this.pathClass = pathClass;
    }

    public void setTag(Integer tag) {
        this.tag = tag;
    }

    public void setParamsType(List<ParamModel> paramsType) {
        this.paramsType = paramsType;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
