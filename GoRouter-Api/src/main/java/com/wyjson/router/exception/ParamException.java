package com.wyjson.router.exception;

import java.util.Locale;

public class ParamException extends RuntimeException {
    private String paramName;

    public ParamException(String paramName) {
        super(String.format(Locale.getDefault(), "The '%s' parameter is required", paramName));
        this.paramName = paramName;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }
}
