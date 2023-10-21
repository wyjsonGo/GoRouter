package com.wyjson.router.core;

import androidx.annotation.NonNull;

import com.wyjson.router.enums.ParamType;
import com.wyjson.router.enums.RouteType;
import com.wyjson.router.utils.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class CardMeta {
    private String path;
    private RouteType type;
    private Class<?> pathClass;
    private int tag;// 额外的标记
    private Map<String, ParamType> paramsType;// <参数名, 参数类型>

    protected CardMeta() {
    }

    public CardMeta(String path, Class<?> pathClass, int tag, Map<String, ParamType> paramsType) {
        setPath(path);
        this.pathClass = pathClass;
        this.tag = tag;
        this.paramsType = paramsType;
    }

    public String getPath() {
        return path;
    }

    protected void setPath(String path) {
        this.path = path;
        this.type = extractType(path);
    }

    public RouteType getType() {
        return type;
    }

    public Class<?> getPathClass() {
        return pathClass;
    }

    protected void setPathClass(Class<?> pathClass) {
        this.pathClass = pathClass;
    }

    public int getTag() {
        return tag;
    }

    protected void setTag(int tag) {
        this.tag = tag;
    }

    public Map<String, ParamType> getParamsType() {
        if (paramsType == null) {
            paramsType = new HashMap<>();
        }
        return paramsType;
    }

    private RouteType extractType(String path) {
        RouteType routeType = null;
        try {
            String defaultType = path.substring(path.lastIndexOf('/'));
            if (!TextUtils.isEmpty(defaultType)) {
                routeType = RouteType.getType(defaultType);
            }
        } catch (Exception e) {
            GoRouter.logger.warning(null, "[extractType] " + e.getMessage());
        }
        if (routeType == null) {
            GoRouter.logger.error(null, "[extractType] The route type is incorrect! The path[" + path + "] type can only end with " + RouteType.toStringByValues());
        }
        return routeType;
    }


    public void commit(Class<?> cls) {
        GoRouter.getInstance().addCardMeta(new CardMeta(this.path, cls, this.tag, this.paramsType));
    }

    public CardMeta putTag(int tag) {
        this.tag = tag;
        return this;
    }

    public CardMeta putString(String key) {
        getParamsType().put(key, ParamType.String);
        return this;
    }

    public CardMeta putBoolean(String key) {
        getParamsType().put(key, ParamType.Boolean);
        return this;
    }

    public CardMeta putShort(String key) {
        getParamsType().put(key, ParamType.Short);
        return this;
    }

    public CardMeta putInt(String key) {
        getParamsType().put(key, ParamType.Int);
        return this;
    }

    public CardMeta putLong(String key) {
        getParamsType().put(key, ParamType.Long);
        return this;
    }

    public CardMeta putDouble(String key) {
        getParamsType().put(key, ParamType.Double);
        return this;
    }

    public CardMeta putByte(String key) {
        getParamsType().put(key, ParamType.Byte);
        return this;
    }

    public CardMeta putChar(String key) {
        getParamsType().put(key, ParamType.Char);
        return this;
    }

    public CardMeta putFloat(String key) {
        getParamsType().put(key, ParamType.Float);
        return this;
    }

    public CardMeta putSerializable(String key) {
        getParamsType().put(key, ParamType.Serializable);
        return this;
    }

    public CardMeta putParcelable(String key) {
        getParamsType().put(key, ParamType.Parcelable);
        return this;
    }

    @NonNull
    public String toString() {
        if (!GoRouter.logger.isShowLog()) {
            return "";
        }
        return "CardMeta{" +
                "path='" + path + '\'' +
                ", type=" + type +
                ", pathClass=" + pathClass +
                ", tag=" + tag +
                ", paramsType=" + paramsType +
                '}';
    }
}