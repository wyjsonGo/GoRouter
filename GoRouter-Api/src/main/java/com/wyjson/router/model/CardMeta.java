package com.wyjson.router.model;

import androidx.annotation.NonNull;

import com.wyjson.router.GoRouter;
import com.wyjson.router.core.RouteCenter;
import com.wyjson.router.enums.ParamType;
import com.wyjson.router.enums.RouteType;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.utils.RouteTagUtils;
import com.wyjson.router.utils.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CardMeta {
    private String path;
    private String group;
    private RouteType type;
    private Class<?> pathClass;
    private int tag;// 额外的标记
    private boolean deprecated;
    private Map<String, ParamMeta> paramsType;// <字段名, 参数元数据对象>

    protected CardMeta() {
    }

    public CardMeta(String path, RouteType type, Class<?> pathClass, int tag, boolean deprecated, Map<String, ParamMeta> paramsType) {
        setPath(path);
        this.type = type;
        this.pathClass = pathClass;
        this.tag = tag;
        this.deprecated = deprecated;
        this.paramsType = paramsType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(@NonNull String path) {
        if (TextUtils.isEmpty(path)) {
            throw new RouterException("path Parameter is invalid!");
        }
        this.path = path;
        this.group = extractGroup(path);
    }

    public String getGroup() {
        return group;
    }

    private String extractGroup(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new RouterException("Extract the path[" + path + "] group failed, the path must be start with '/' and contain more than 2 '/'!");
        }

        try {
            String group = path.substring(1, path.indexOf("/", 1));
            if (TextUtils.isEmpty(group)) {
                throw new RouterException("Extract the path[" + path + "] group failed! There's nothing between 2 '/'!");
            } else {
                return group;
            }
        } catch (Exception e) {
            throw new RouterException("Extract the path[" + path + "] group failed, the path must be start with '/' and contain more than 2 '/'! " + e.getMessage());
        }
    }


    public RouteType getType() {
        return type;
    }

    void setType(RouteType type) {
        this.type = type;
    }

    public Class<?> getPathClass() {
        return pathClass;
    }

    void setPathClass(Class<?> pathClass) {
        this.pathClass = pathClass;
    }

    public int getTag() {
        return tag;
    }

    void setTag(int tag) {
        this.tag = tag;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public Map<String, ParamMeta> getParamsType() {
        if (paramsType == null) {
            paramsType = new HashMap<>();
        }
        return paramsType;
    }

    public void commitActivity(@NonNull Class<?> cls) {
        commit(cls, RouteType.ACTIVITY);
    }

    public void commitFragment(@NonNull Class<?> cls) {
        commit(cls, RouteType.FRAGMENT);
    }

    private void commit(Class<?> cls, RouteType type) {
        if (cls == null) {
            throw new RouterException("Cannot commit empty!");
        }
        RouteCenter.addCardMeta(new CardMeta(this.path, type, cls, this.tag, this.deprecated, this.paramsType));
    }

    public CardMeta putTag(int tag) {
        this.tag = tag;
        return this;
    }

    /**
     * @param deprecated 如果标记为true，框架在openDebug()的情况下，跳转到该页将提示其他开发人员和测试人员
     * @return
     */
    public CardMeta putDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
        return this;
    }

    public CardMeta putObject(String key) {
        return put(key, null, ParamType.Object, false);
    }

    public CardMeta putObject(String key, String name, boolean required) {
        return put(key, name, ParamType.Object, required);
    }

    public CardMeta putString(String key) {
        return put(key, null, ParamType.String, false);
    }

    public CardMeta putString(String key, String name, boolean required) {
        return put(key, name, ParamType.String, required);
    }

    public CardMeta putBoolean(String key) {
        return put(key, null, ParamType.Boolean, false);
    }

    public CardMeta putBoolean(String key, String name, boolean required) {
        return put(key, name, ParamType.Boolean, required);
    }

    public CardMeta putShort(String key) {
        return put(key, null, ParamType.Short, false);
    }

    public CardMeta putShort(String key, String name, boolean required) {
        return put(key, name, ParamType.Short, required);
    }

    public CardMeta putInt(String key) {
        return put(key, null, ParamType.Int, false);
    }

    public CardMeta putInt(String key, String name, boolean required) {
        return put(key, name, ParamType.Int, required);
    }

    public CardMeta putLong(String key) {
        return put(key, null, ParamType.Long, false);
    }

    public CardMeta putLong(String key, String name, boolean required) {
        return put(key, name, ParamType.Long, required);
    }

    public CardMeta putDouble(String key) {
        return put(key, null, ParamType.Double, false);
    }

    public CardMeta putDouble(String key, String name, boolean required) {
        return put(key, name, ParamType.Double, required);
    }

    public CardMeta putByte(String key) {
        return put(key, null, ParamType.Byte, false);
    }

    public CardMeta putByte(String key, String name, boolean required) {
        return put(key, name, ParamType.Byte, required);
    }

    public CardMeta putChar(String key) {
        return put(key, null, ParamType.Char, false);
    }

    public CardMeta putChar(String key, String name, boolean required) {
        return put(key, name, ParamType.Char, required);
    }

    public CardMeta putFloat(String key) {
        return put(key, null, ParamType.Float, false);
    }

    public CardMeta putFloat(String key, String name, boolean required) {
        return put(key, name, ParamType.Float, required);
    }

    public CardMeta putSerializable(String key) {
        return put(key, null, ParamType.Serializable, false);
    }

    public CardMeta putSerializable(String key, String name, boolean required) {
        return put(key, name, ParamType.Serializable, required);
    }

    public CardMeta putParcelable(String key) {
        return put(key, null, ParamType.Parcelable, false);
    }

    public CardMeta putParcelable(String key, String name, boolean required) {
        return put(key, name, ParamType.Parcelable, required);
    }

    private CardMeta put(String key, String name, ParamType type, boolean required) {
        getParamsType().put(key, new ParamMeta(TextUtils.isEmpty(name) ? key : name, type, required));
        return this;
    }

    public boolean isTagExist(int item) {
        return RouteTagUtils.isExist(tag, item);
    }

    public int getTagExistCount() {
        return RouteTagUtils.getExistCount(tag);
    }

    public int addTag(int item) {
        return tag = RouteTagUtils.addItem(tag, item);
    }

    public int deleteTag(int item) {
        return tag = RouteTagUtils.deleteItem(tag, item);
    }

    public int getTagNegation() {
        return RouteTagUtils.getNegation(tag);
    }

    public ArrayList<Integer> getTagExistList(int... itemList) {
        return RouteTagUtils.getExistList(tag, itemList);
    }

    @NonNull
    @Override
    public String toString() {
        if (!GoRouter.isDebug()) {
            return "";
        }
        return "CardMeta{" +
                "path='" + path + '\'' +
                ", group='" + group + '\'' +
                ", type=" + type +
                ", pathClass=" + pathClass +
                ", tag=" + tag +
                ", deprecated=" + deprecated +
                ", paramsType=" + paramsType +
                '}';
    }
}