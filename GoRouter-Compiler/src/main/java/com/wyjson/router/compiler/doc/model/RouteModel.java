package com.wyjson.router.compiler.doc.model;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class RouteModel {

    private String path;
    private String remark;
    private String moduleName;
    private String type;
    private String pathClass;
    private Integer tag;
    private Boolean deprecated;
    private Boolean ignoreHelper;
    private List<ParamModel> paramsType;

    public RouteModel() {
    }

    public RouteModel(String path, String type, String pathClass, Integer tag, List<ParamModel> paramsType, String remark, String moduleName, Boolean deprecated, Boolean ignoreHelper) {
        this.path = path;
        this.type = type;
        this.pathClass = pathClass;
        this.tag = tag;
        this.moduleName = moduleName;
        this.deprecated = deprecated;
        this.ignoreHelper = ignoreHelper;
        if (!StringUtils.isEmpty(remark)) {
            this.remark = remark;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPathClass() {
        return pathClass;
    }

    public void setPathClass(String pathClass) {
        this.pathClass = pathClass;
    }

    public Integer getTag() {
        return tag;
    }

    public void setTag(Integer tag) {
        this.tag = tag;
    }

    public List<ParamModel> getParamsType() {
        if (paramsType == null) {
            paramsType = new ArrayList<>();
        }
        return paramsType;
    }

    public void setParamsType(List<ParamModel> paramsType) {
        this.paramsType = paramsType;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public Boolean getDeprecated() {
        return deprecated;
    }

    public void setDeprecated(Boolean deprecated) {
        this.deprecated = deprecated;
    }

    public Boolean getIgnoreHelper() {
        return ignoreHelper;
    }

    public void setIgnoreHelper(Boolean ignoreHelper) {
        this.ignoreHelper = ignoreHelper;
    }
}
