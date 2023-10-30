package com.wyjson.router.param;

import com.wyjson.router.enums.ParamType;

public class ParamMeta {
    private String name;// 自定义参数名,未定义使用变量名
    private ParamType type;// 参数类型
    private boolean required;// 如果需要，当value为null时，应用程序将崩溃。原始类型不会被检查!

    public ParamMeta(String name, ParamType type, boolean required) {
        this.name = name;
        this.type = type;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public ParamType getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }
}
