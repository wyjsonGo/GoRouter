package com.wyjson.router.model;

import androidx.annotation.NonNull;

import com.wyjson.router.GoRouter;
import com.wyjson.router.enums.ParamType;

public class ParamMeta {
    private final String name;// 自定义参数名,未定义使用变量名
    private final ParamType type;// 参数类型
    private final boolean required;// 如果需要，当value为null时，应用程序将崩溃。原始类型不会被检查!

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

    @NonNull
    @Override
    public String toString() {
        if (!GoRouter.isDebug()) {
            return "";
        }
        return "ParamMeta{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", required=" + required +
                '}';
    }
}
