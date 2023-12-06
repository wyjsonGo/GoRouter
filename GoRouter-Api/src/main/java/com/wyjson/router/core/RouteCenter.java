package com.wyjson.router.core;

import static com.wyjson.router.core.Constants.ROUTER_CURRENT_PATH;
import static com.wyjson.router.core.Constants.ROUTER_RAW_URI;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.wyjson.router.GoRouter;
import com.wyjson.router.enums.ParamType;
import com.wyjson.router.exception.NoFoundRouteException;
import com.wyjson.router.exception.ParamException;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.interfaces.IJsonService;
import com.wyjson.router.model.Card;
import com.wyjson.router.model.CardMeta;
import com.wyjson.router.model.ParamMeta;
import com.wyjson.router.module.interfaces.IRouteModuleGroup;
import com.wyjson.router.utils.MapUtils;
import com.wyjson.router.utils.TextUtils;

import java.lang.reflect.Field;
import java.util.Map;

public class RouteCenter {

    private static IJsonService jsonService;

    public static Map<String, IRouteModuleGroup> getRouteGroups() {
        return Warehouse.routeGroups;
    }

    /**
     * 动态添加路由分组,按需加载路由
     *
     * @param group
     * @param routeModuleGroup
     */
    public static void addRouterGroup(String group, IRouteModuleGroup routeModuleGroup) {
        Warehouse.routeGroups.put(group, routeModuleGroup);
        GoRouter.logger.info(null, "[addRouterGroup] Add a route group[" + group + "] dynamically");
    }

    /**
     * 获取路由元数据
     *
     * @param card
     * @return
     * @throws NoFoundRouteException
     */
    public static CardMeta getCardMeta(Card card) throws NoFoundRouteException {
        CardMeta cardMeta = Warehouse.routes.get(card.getPath());
        if (cardMeta == null) {
            if (!Warehouse.routeGroups.containsKey(card.getGroup())) {
                throw new NoFoundRouteException("There is no route match the path [" + card.getPath() + "], in group [" + card.getGroup() + "]");
            } else {
                try {
                    if (GoRouter.isDebug()) {
                        GoRouter.logger.debug(null, "[getCardMeta] The group [" + card.getGroup() + "] starts loading, trigger by [" + card.getPath() + "]");
                    }

                    // Load route and cache it into memory, then delete from metas.
                    if (Warehouse.routeGroups.containsKey(card.getGroup())) {
                        Warehouse.routeGroups.get(card.getGroup()).load();
                        Warehouse.routeGroups.remove(card.getGroup());
                    }

                    if (GoRouter.isDebug()) {
                        GoRouter.logger.debug(null, "[getCardMeta] The group [" + card.getGroup() + "] has already been loaded, trigger by [" + card.getPath() + "]");
                    }
                } catch (Exception e) {
                    throw new RouterException("Fatal exception when loading route group[" + card.getGroup() + "] meta. [" + e.getMessage() + "]");
                }
                return getCardMeta(card);// Reload
            }
        } else {
            GoRouter.logger.info(null, "[getCardMeta] " + cardMeta);
        }
        return cardMeta;
    }

    /**
     * 添加路由元数据
     *
     * @param cardMeta
     */
    public static void addCardMeta(CardMeta cardMeta) {
        Warehouse.routes.put(cardMeta.getPath(), cardMeta);
        GoRouter.logger.debug(null, "[addCardMeta] size:" + Warehouse.routes.size() + ", commit:" + cardMeta);
    }

    /**
     * 获取原始的URI
     *
     * @param target
     * @param <T>
     */
    public static <T> String getRawURI(T target) {
        Bundle bundle;
        try {
            bundle = getBundle(target, null, null);
        } catch (Exception e) {
            throw new RuntimeException("getRawURI() " + e.getMessage());
        }
        return bundle.getString(ROUTER_RAW_URI);
    }

    /**
     * 获取当前页面路径
     *
     * @param target
     * @param <T>
     */
    public static <T> String getCurrentPath(T target) {
        Bundle bundle;
        if (target instanceof Bundle) {
            bundle = (Bundle) target;
        } else {
            try {
                bundle = getBundle(target, null, null);
            } catch (Exception e) {
                throw new RuntimeException("getCurrentPath() " + e.getMessage());
            }
        }
        return bundle.getString(ROUTER_CURRENT_PATH);
    }

    @NonNull
    private static <T> Bundle getBundle(T target, Intent intent, Bundle bundle) {
        if (bundle == null) {
            if (intent != null) {
                bundle = intent.getExtras();
            } else {
                if (target instanceof Activity) {
                    bundle = ((Activity) target).getIntent().getExtras();
                } else if (target instanceof Fragment) {
                    bundle = ((Fragment) target).getArguments();
                }
            }
            if (bundle == null) {
                throw new RouterException("method does not get bundle!");
            }
        }
        return bundle;
    }

    /**
     * 解析参数
     *
     * @param target
     * @param intent
     * @param bundle
     * @param isCheck 是否检查 isRequired
     * @param <T>
     * @throws ParamException
     * @Deprecated Higher performance methods have been available since version 2.3.2
     */
    @Deprecated(since = "2.3.2")
    public static <T> void inject(T target, Intent intent, Bundle bundle, boolean isCheck) throws ParamException {
        GoRouter.logger.debug(null, "[inject] Auto Inject Start!");

        try {
            bundle = getBundle(target, intent, bundle);
        } catch (Exception e) {
            throw new RuntimeException("inject() " + e.getMessage());
        }

        String path = getCurrentPath(bundle);
        if (TextUtils.isEmpty(path)) {
            GoRouter.logger.error(null, "[inject] The " + ROUTER_CURRENT_PATH + " parameter was not found in the intent");
            return;
        }

        CardMeta cardMeta = GoRouter.getInstance().build(path).getCardMeta();
        if (cardMeta != null) {
            Map<String, ParamMeta> paramsType = cardMeta.getParamsType();
            for (Map.Entry<String, ParamMeta> entry : paramsType.entrySet()) {
                String paramName = entry.getValue().getName();
                Object value = bundle.get(paramName);
                if (value == null) {
                    if (isCheck && entry.getValue().isRequired()) {
                        throw new ParamException(paramName);
                    }
                    continue;
                }
                GoRouter.logger.debug(null, "[inject] " + paramName + ":" + value);
                try {
                    Field injectField = getDeclaredField(target.getClass(), entry.getKey());
                    if (ParamType.Object == entry.getValue().getType() && value instanceof String) {
                        if (jsonService == null) {
                            jsonService = GoRouter.getInstance().getService(IJsonService.class);
                        }
                        if (jsonService == null) {
                            throw new RouterException("To use withObject() method, you need to implement IJsonService");
                        }
                        value = jsonService.parseObject((String) value, injectField.getGenericType());
                    }
                    injectField.setAccessible(true);
                    injectField.set(target, value);
                } catch (Exception e) {
                    throw new RouterException("Inject values for activity/fragment error! [" + e.getMessage() + "]");
                }
            }
        }
        GoRouter.logger.debug(null, "[inject] Auto Inject End!");
    }

    /**
     * 本类找不到就去父类里找,到Android类停止查找
     *
     * @param cls
     * @param key
     * @return
     * @throws NoSuchFieldException
     */
    @NonNull
    private static Field getDeclaredField(Class<?> cls, String key) throws NoSuchFieldException {
        try {
            return cls.getDeclaredField(key);
        } catch (NoSuchFieldException e) {
            Class<?> superclass = cls.getSuperclass();
            if (superclass != null && !superclass.getName().startsWith("android")) {
                return getDeclaredField(superclass, key);
            } else {
                throw new NoSuchFieldException(e.getMessage());
            }
        }
    }

    /**
     * 组装路由原数组
     *
     * @param card
     * @throws NoFoundRouteException
     */
    public static synchronized void assembleRouteCard(@NonNull Card card) throws NoFoundRouteException {
        CardMeta cardMeta = getCardMeta(card);
        if (cardMeta != null) {
            card.setCardMeta(cardMeta.getType(), cardMeta.getPathClass(), cardMeta.getTag());
            card.withString(ROUTER_CURRENT_PATH, card.getPath());

            Map<String, ParamMeta> paramsType = cardMeta.getParamsType();
            Uri rawUri = card.getUri();
            if (rawUri != null) {
                Map<String, String> resultMap = TextUtils.splitQueryParameters(rawUri);
                if (MapUtils.isNotEmpty(paramsType)) {
                    // 按类型设置值
                    for (Map.Entry<String, ParamMeta> params : paramsType.entrySet()) {
                        setValue(card,
                                params.getValue().getType(),
                                params.getValue().getName(),
                                resultMap.get(params.getKey()));
                    }
                }
                // 保存原始uri
                card.withString(ROUTER_RAW_URI, rawUri.toString());
            }
        }
    }

    /**
     * 按已知类型设置值
     *
     * @param card
     * @param type
     * @param key
     * @param value
     */
    private static void setValue(Card card, ParamType type, String key, String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value))
            return;
        try {
            if (type != null) {
                if (type == ParamType.Boolean) {
                    card.withBoolean(key, Boolean.parseBoolean(value));
                } else if (type == ParamType.Byte) {
                    card.withByte(key, Byte.parseByte(value));
                } else if (type == ParamType.Short) {
                    card.withShort(key, Short.parseShort(value));
                } else if (type == ParamType.Int) {
                    card.withInt(key, Integer.parseInt(value));
                } else if (type == ParamType.Long) {
                    card.withLong(key, Long.parseLong(value));
                } else if (type == ParamType.Char) {
                    card.withChar(key, value.charAt(0));
                } else if (type == ParamType.Float) {
                    card.withFloat(key, Float.parseFloat(value));
                } else if (type == ParamType.Double) {
                    card.withDouble(key, Double.parseDouble(value));
                } else if (type == ParamType.String) {
                    card.withString(key, value);
                } else if (type == ParamType.Parcelable) {
                    // TODO : How to description parcelable value with string?
                } else {
                    card.withString(key, value);
                }
            } else {
                card.withString(key, value);
            }
        } catch (Throwable e) {
            throw new RouterException("setValue() failed! " + e.getMessage());
        }
    }

}
