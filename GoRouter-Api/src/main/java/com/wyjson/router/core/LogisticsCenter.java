package com.wyjson.router.core;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wyjson.router.GoRouter;
import com.wyjson.router.enums.ParamType;
import com.wyjson.router.exception.NoFoundRouteException;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.interfaces.IInterceptor;
import com.wyjson.router.interfaces.IService;
import com.wyjson.router.model.Card;
import com.wyjson.router.model.CardMeta;
import com.wyjson.router.model.ParamMeta;
import com.wyjson.router.model.ServiceMeta;
import com.wyjson.router.module.interfaces.IRouteModuleGroup;
import com.wyjson.router.utils.MapUtils;
import com.wyjson.router.utils.TextUtils;

import java.lang.reflect.Field;
import java.util.Map;

public class LogisticsCenter {

    public static Map<String, IRouteModuleGroup> getRouteGroups() {
        return Warehouse.routeGroups;
    }

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
                        // If this group is included, but it has not been loaded
                        // load this group first, because dynamic route has high priority.
                        Warehouse.routeGroups.get(card.getGroup()).load();
                        Warehouse.routeGroups.remove(card.getGroup());
                    }

                    if (GoRouter.isDebug()) {
                        GoRouter.logger.debug(null, "[getCardMeta] The group [" + card.getGroup() + "] has already been loaded, trigger by [" + card.getPath() + "]");
                    }
                } catch (Exception e) {
                    throw new RouterException("Fatal exception when loading group meta. [" + e.getMessage() + "]");
                }
                return getCardMeta(card);// Reload
            }
        } else {
            GoRouter.logger.info(null, "[getCardMeta] " + cardMeta);
        }
        return cardMeta;
    }

    public static void addCardMeta(CardMeta cardMeta) {
        if (TextUtils.isEmpty(cardMeta.getPath())) {
            throw new RouterException("path Parameter is invalid!");
        }
        // 检查路由是否有重复提交的情况
        if (GoRouter.isDebug()) {
            for (Map.Entry<String, CardMeta> cardMetaEntry : Warehouse.routes.entrySet()) {
                if (TextUtils.equals(cardMetaEntry.getKey(), cardMeta.getPath())) {
                    GoRouter.logger.error(null, "[addCardMeta] Path duplicate commit!!! path[" + cardMetaEntry.getValue().getPath() + "]");
                    break;
                } else if (cardMetaEntry.getValue().getPathClass() == cardMeta.getPathClass()) {
                    GoRouter.logger.error(null, "[addCardMeta] PathClass duplicate commit!!! pathClass[" + cardMetaEntry.getValue().getPathClass() + "]");
                    break;
                }
            }
        }
        Warehouse.routes.put(cardMeta.getPath(), cardMeta);
        GoRouter.logger.debug(null, "[addCardMeta] size:" + Warehouse.routes.size() + ", commit:" + cardMeta);
    }


    /**
     * 实现相同接口的service会被覆盖(更新)
     * 调用时机可以在application或插件模块加载时
     *
     * @param serviceClass 实现类.class
     */
    public static void addService(Class<? extends IService> serviceClass) {
        Class<? extends IService> serviceInterfaceClass = (Class<? extends IService>) serviceClass.getInterfaces()[0];
        Warehouse.services.put(serviceInterfaceClass, new ServiceMeta(serviceClass));
        GoRouter.logger.debug(null, "[addService] size:" + Warehouse.services.size() + ", " + serviceInterfaceClass.getSimpleName() + " -> " + serviceClass.getSimpleName());
    }

    /**
     * 获取service接口的实现
     *
     * @param serviceClass 接口.class
     * @param <T>
     * @return
     */
    @Nullable
    public static <T> T getService(Class<? extends T> serviceClass) {
        ServiceMeta meta = Warehouse.services.get(serviceClass);
        if (meta != null) {
            if (serviceClass.isAssignableFrom(meta.getServiceClass())) {
                IService instance = meta.getService();
                if (instance == null) {
                    try {
                        instance = meta.getServiceClass().getConstructor().newInstance();
                        instance.init();
                        meta.setService(instance);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RouterException("serviceClass constructor new instance failed!");
                    }
                }
                GoRouter.logger.info(null, "[getService] " + serviceClass.getSimpleName() + " -> " + meta.getServiceClass().getSimpleName());
                return (T) instance;
            }
        }
        GoRouter.logger.warning(null, "[getService] " + serviceClass.getSimpleName() + ", No registered service found!");
        return null;
    }

    /**
     * 相同优先级添加会catch
     * 调用时机可以在application或插件模块加载时
     *
     * @param priority
     * @param interceptor
     * @param isForce
     */
    public static void addInterceptor(int priority, Class<? extends IInterceptor> interceptor, boolean isForce) {
        try {
            if (isForce) {
                Warehouse.interceptors.remove(priority);
            }
            IInterceptor instance = interceptor.getConstructor().newInstance();
            instance.init();
            Warehouse.interceptors.put(priority, instance);

            String title = isForce ? "[setInterceptor]" : "[addInterceptor]";
            GoRouter.logger.debug(null, title + " size:" + Warehouse.interceptors.size() + ", priority:" + priority + " -> " + interceptor.getSimpleName());
        } catch (Exception e) {
            throw new RouterException(e);
        }
    }

    /**
     * 相同优先级添加会覆盖
     * 调用时机可以在application或插件模块加载时
     *
     * @param priority
     * @param interceptor
     */
    public static void setInterceptor(int priority, Class<? extends IInterceptor> interceptor) {
        addInterceptor(priority, interceptor, true);
    }

    public static void clearInterceptors() {
        Warehouse.interceptors.clear();
    }


    /**
     * 解析参数
     *
     * @param target
     * @param intent
     * @param bundle
     * @param <T>
     */
    public static <T> void inject(T target, Intent intent, Bundle bundle) {
        GoRouter.logger.debug(null, "[inject] Auto Inject Start!");

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
                throw new RouterException("inject() method does not get bundle!");
            }
        }

        String path = bundle.getString(GoRouter.ROUTER_CURRENT_PATH);
        if (TextUtils.isEmpty(path)) {
            GoRouter.logger.error(null, "[inject] path Parameter is invalid!");
            return;
        }

        CardMeta cardMeta = GoRouter.getInstance().build(path).getCardMeta();
        if (cardMeta != null) {
            Map<String, ParamMeta> paramsType = cardMeta.getParamsType();
            for (Map.Entry<String, ParamMeta> params : paramsType.entrySet()) {
                String paramName = params.getValue().getName();
                Object value = bundle.get(paramName);
                if (value == null)
                    continue;
                GoRouter.logger.debug(null, "[inject] " + paramName + ":" + value);
                try {
                    Field injectField = getDeclaredField(target.getClass(), params.getKey());
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

    public static synchronized void assembleRouteCard(@NonNull Card card) throws NoFoundRouteException {
        CardMeta cardMeta = getCardMeta(card);
        if (cardMeta != null) {
            card.setCardMeta(cardMeta.getType(), cardMeta.getPathClass(), cardMeta.getTag());

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
                card.withString(GoRouter.ROUTER_RAW_URI, rawUri.toString());
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
