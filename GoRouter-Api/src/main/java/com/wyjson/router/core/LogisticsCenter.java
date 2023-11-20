package com.wyjson.router.core;

import static com.wyjson.router.core.Constants.ROUTER_CURRENT_PATH;
import static com.wyjson.router.core.Constants.ROUTER_RAW_URI;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

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
     * 实现相同接口的service会被覆盖(更新)
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
     * 重复添加相同序号会catch
     *
     * @param ordinal
     * @param interceptor
     * @param isForce
     */
    public static void addInterceptor(int ordinal, Class<? extends IInterceptor> interceptor, boolean isForce) {
        String title = isForce ? "[setInterceptor]" : "[addInterceptor]";
        try {
            if (isForce) {
                Warehouse.interceptors.remove(ordinal);
            }
            IInterceptor instance = interceptor.getConstructor().newInstance();
            instance.init();
            Warehouse.interceptors.put(ordinal, instance);
            GoRouter.logger.debug(null, title + " size:" + Warehouse.interceptors.size() + ", ordinal:" + ordinal + " -> " + interceptor.getSimpleName());
        } catch (Exception e) {
            throw new RouterException(title + " " + e.getMessage());
        }
    }

    /**
     * 重复添加相同序号会覆盖(更新)
     *
     * @param ordinal
     * @param interceptor
     */
    public static void setInterceptor(int ordinal, Class<? extends IInterceptor> interceptor) {
        addInterceptor(ordinal, interceptor, true);
    }

    public static void clearInterceptors() {
        Warehouse.interceptors.clear();
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

    public static <T> void registerEvent(LifecycleOwner owner, @NonNull Class<T> type, boolean isForever, @NonNull Observer<T> observer) {
        if (!(owner instanceof Activity) && !(owner instanceof Fragment)) {
            /**
             * 正常通过api调用是不会走到这里,除非直接调用了此方法才有可能出现这种情况
             * 可能是FragmentViewLifecycleOwner类型,需要导包才能直接判断,为了少导入包,就这么写判断吧.
             */
            throw new RouterException("The owner can only be an Activity or Fragment");
        }
        if (type == null) {
            throw new RouterException("type cannot be empty!");
        }

        String path = getCurrentPath(owner);
        if (TextUtils.isEmpty(path)) {
            GoRouter.logger.error(null, "[registerEvent] The " + ROUTER_CURRENT_PATH + " parameter was not found in the intent");
            return;
        }

        String key = path + "$" + type.getCanonicalName();
        MutableLiveData<T> liveData;
        if (Warehouse.events.containsKey(key)) {
            liveData = Warehouse.events.get(key);
        } else {
            liveData = new MutableLiveData<>();
            Warehouse.events.put(key, liveData);
            addLifecycleObserver(owner, getLifecycleObserver(key));
        }
        if (liveData != null) {
            if (isForever) {
                liveData.observeForever(observer);
            } else {
                liveData.observe(owner, observer);
            }
        } else {
            GoRouter.logger.error(null, "[registerEvent] LiveData is empty??");
        }
    }

    public static <T> void unRegisterEvent(LifecycleOwner owner, @NonNull Class<T> type, Observer<T> observer) {
        if (type == null) {
            throw new RouterException("type cannot be empty!");
        }

        String path = getCurrentPath(owner);
        if (TextUtils.isEmpty(path)) {
            GoRouter.logger.error(null, "[registerEvent] The " + ROUTER_CURRENT_PATH + " parameter was not found in the intent");
            return;
        }

        String key = path + "$" + type.getCanonicalName();
        if (Warehouse.events.containsKey(key)) {
            MutableLiveData<T> liveData = Warehouse.events.get(key);
            if (liveData != null) {
                if (observer != null) {
                    liveData.removeObserver(observer);
                } else {
                    liveData.removeObservers(owner);
                }
                if (!liveData.hasObservers()) {
                    Warehouse.events.remove(key);
                }
            }
        } else {
            GoRouter.logger.warning(null, "[unRegisterEvent] No observer was found for this event");
        }
    }

    /**
     * 页面销毁时删除当前页面注册的事件
     *
     * @param key
     * @param <T>
     * @return
     */
    @NonNull
    private static <T> LifecycleEventObserver getLifecycleObserver(String key) {
        return new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner owner, @NonNull Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    owner.getLifecycle().removeObserver(this);
                    if (Warehouse.events.containsKey(key)) {
                        MutableLiveData<T> liveData = Warehouse.events.get(key);
                        if (liveData != null && liveData.hasObservers()) {
                            liveData.removeObservers(owner);
                        }
                        Warehouse.events.remove(key);
                    }
                }
            }
        };
    }

    private static void addLifecycleObserver(final LifecycleOwner lifecycleOwner, @NonNull LifecycleObserver lifecycleObserver) {
        if (lifecycleOwner == null)
            return;
        Runnable runnable = () -> lifecycleOwner.getLifecycle().addObserver(lifecycleObserver);
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }

    public static <T> void postEvent(@NonNull String path, @NonNull T value) {
        if (TextUtils.isEmpty(path)) {
            throw new RouterException("path Parameter is invalid!");
        }
        if (value == null) {
            throw new RouterException("value cannot be empty!");
        }
        String key = path + "$" + value.getClass().getCanonicalName();
        if (Warehouse.events.containsKey(key)) {
            MutableLiveData<T> liveData = Warehouse.events.get(key);
            if (liveData != null) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    liveData.setValue(value);
                } else {
                    liveData.postValue(value);
                }
            } else {
                GoRouter.logger.error(null, "[postEvent] LiveData is empty??");
            }
        } else {
            GoRouter.logger.warning(null, "[postEvent] No observer was found for this event");
        }
    }


}
