package com.wyjson.router.core;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.wyjson.router.callback.GoCallback;
import com.wyjson.router.enums.ParamType;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.interceptor.InterceptorCallback;
import com.wyjson.router.interceptor.InterceptorUtils;
import com.wyjson.router.interceptor.service.InterceptorService;
import com.wyjson.router.interceptor.service.impl.InterceptorServiceImpl;
import com.wyjson.router.interfaces.DegradeService;
import com.wyjson.router.interfaces.IInterceptor;
import com.wyjson.router.interfaces.IService;
import com.wyjson.router.interfaces.PathReplaceService;
import com.wyjson.router.interfaces.PretreatmentService;
import com.wyjson.router.service.ServiceHelper;
import com.wyjson.router.utils.MapUtils;
import com.wyjson.router.utils.TextUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public final class GoRouter {

    private GoRouter() {
        mHandler = new Handler(Looper.getMainLooper());
        InterceptorUtils.clearIterator();
        addService(InterceptorServiceImpl.class);
    }

    private static class InstanceHolder {
        private static final GoRouter mInstance = new GoRouter();
    }

    public static GoRouter getInstance() {
        return InstanceHolder.mInstance;
    }

    public static final String ROUTER_RAW_URI = "router_raw_uri";
    public static final String ROUTER_PARAM_INJECT = "router_param_inject";
    private final Handler mHandler;

    private static final Map<String, CardMeta> routes = new HashMap<>();

    @Nullable
    CardMeta getCardMeta(Card card) {
        return routes.get(card.getPath());
    }

    void addCardMeta(CardMeta cardMeta) {
        routes.put(cardMeta.getPath(), cardMeta);
    }

    /**
     * 实现相同接口的service会被覆盖(更新)
     * 调用时机可以在application或插件模块加载时
     *
     * @param service 实现类.class
     */
    public void addService(Class<? extends IService> service) {
        ServiceHelper.getInstance().addService(service);
    }

    /**
     * 获取service接口的实现
     *
     * @param service 接口.class
     * @param <T>
     * @return
     */
    @Nullable
    public <T> T getService(Class<? extends T> service) {
        return ServiceHelper.getInstance().getService(service);
    }

    /**
     * 相同优先级添加会catch
     * 调用时机可以在application或插件模块加载时
     *
     * @param priority
     * @param interceptor
     */
    public void addInterceptor(int priority, Class<? extends IInterceptor> interceptor) {
        InterceptorUtils.addInterceptor(priority, interceptor, false);
    }

    /**
     * 相同优先级添加会覆盖
     * 调用时机可以在application或插件模块加载时
     *
     * @param priority
     * @param interceptor
     */
    public void setInterceptor(int priority, Class<? extends IInterceptor> interceptor) {
        InterceptorUtils.setInterceptor(priority, interceptor);
    }

    public Card build(String path) {
        return build(path, null);
    }

    public Card build(String path, Bundle bundle) {
        if (TextUtils.isEmpty(path)) {
            throw new RouterException("[path] Parameter is invalid!");
        } else {
            PathReplaceService pService = getService(PathReplaceService.class);
            if (pService != null) {
                path = pService.forString(path);
            }
        }
        return new Card(path, bundle);
    }

    public Card build(Uri uri) {
        if (uri == null || TextUtils.isEmpty(uri.toString())) {
            throw new RouterException("[uri] Parameter invalid!");
        } else {
            PathReplaceService pService = getService(PathReplaceService.class);
            if (pService != null) {
                uri = pService.forUri(uri);
            }
        }
        return new Card(uri);
    }

    private void runInMainThread(Runnable runnable) {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            mHandler.post(runnable);
        } else {
            runnable.run();
        }
    }

    /**
     * 解析参数
     *
     * @param target
     * @param <T>
     */
    public <T> void inject(T target) {
        Bundle bundle;
        if (target instanceof Activity) {
            bundle = ((Activity) target).getIntent().getExtras();
        } else if (target instanceof Fragment) {
            bundle = ((Fragment) target).getArguments();
        } else if (target instanceof Intent) {
            bundle = ((Intent) target).getExtras();
        } else if (target instanceof Bundle) {
            bundle = (Bundle) target;
        } else {
            throw new RouterException("The target instance can only be an Activity, Fragment, Intent, or Bundle");
        }
        String[] autoInjectParams = bundle.getStringArray(GoRouter.ROUTER_PARAM_INJECT);
        if (null != autoInjectParams && autoInjectParams.length > 0) {
            for (String paramsName : autoInjectParams) {
                Object value = bundle.get(paramsName);
                if (value == null)
                    continue;
                try {
                    Field injectField = target.getClass().getDeclaredField(paramsName);
                    injectField.setAccessible(true);
                    injectField.set(target, value);
                } catch (Exception e) {
                    throw new RouterException("Inject values for activity error! [" + e.getMessage() + "]");
                }
            }
        }
    }

    @Nullable
    Object go(Context context, Card card, int requestCode, GoCallback callback) {
        PretreatmentService pretreatmentService = getService(PretreatmentService.class);
        if (pretreatmentService != null && !pretreatmentService.onPretreatment(context, card)) {
            // 预处理失败，导航取消
            return null;
        }
        card.setContext(context);
        CardMeta cardMeta = getCardMeta(card);
        if (cardMeta != null) {
            card.setPathClass(cardMeta.getPathClass());
            card.setTag(cardMeta.getTag());

            Map<String, ParamType> paramsType = cardMeta.getParamsType();
            if (MapUtils.isNotEmpty(paramsType)) {
                // 保存需要注入的参数名
                card.getExtras().putStringArray(GoRouter.ROUTER_PARAM_INJECT, paramsType.keySet().toArray(new String[]{}));
            }
            Uri rawUri = card.getUri();
            if (rawUri != null) {
                Map<String, String> resultMap = TextUtils.splitQueryParameters(rawUri);
                if (MapUtils.isNotEmpty(paramsType)) {
                    // 按类型设置值
                    for (Map.Entry<String, ParamType> params : paramsType.entrySet()) {
                        setValue(card,
                                params.getValue(),
                                params.getKey(),
                                resultMap.get(params.getKey()));
                    }
                }
                // 保存原始uri
                card.withString(GoRouter.ROUTER_RAW_URI, rawUri.toString());
            }

            if (callback != null) {
                callback.onFound(card);
            }
            switch (card.getType()) {
                case ACTIVITY:
                    InterceptorService interceptorService = getService(InterceptorService.class);
                    if (interceptorService != null && !card.isGreenChannel()) {
                        interceptorService.doInterceptions(card, new InterceptorCallback() {
                            @Override
                            public void onContinue(Card card) {
                                goActivity(context, card, requestCode, cardMeta.getPathClass(), callback);
                            }

                            @Override
                            public void onInterrupt(Throwable exception) {
                                if (callback != null) {
                                    callback.onInterrupt(card);
                                }
                            }
                        });
                    } else {
                        goActivity(context, card, requestCode, cardMeta.getPathClass(), callback);
                    }
                    break;
                case FRAGMENT:
                case DIALOG_FRAGMENT:
                    return goFragment(context, card, cardMeta.getPathClass(), callback);
            }
        } else {
            // There is no route.
            onLost(context, card, callback);
        }
        return null;
    }

    /**
     * 按已知类型设置值
     *
     * @param card
     * @param type
     * @param key
     * @param value
     */
    private void setValue(Card card, ParamType type, String key, String value) {
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

    private void onLost(Context context, Card card, GoCallback callback) {
        if (callback != null) {
            callback.onLost(card);
        } else {
            DegradeService degradeService = getService(DegradeService.class);
            if (degradeService != null) {
                degradeService.onLost(context, card);
            }
        }
    }

    @SuppressLint("WrongConstant")
    private void goActivity(Context context, Card card, int requestCode, Class<?> cls, GoCallback callback) {
        Intent intent = new Intent(context, cls);

        intent.putExtras(card.getExtras());

        int flags = card.getFlags();
        if (0 != flags) {
            intent.setFlags(flags);
        }

        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        String action = card.getAction();
        if (!TextUtils.isEmpty(action)) {
            intent.setAction(action);
        }

        runInMainThread(() -> {
            if (requestCode >= 0) {
                if (context instanceof Activity) {
                    ActivityCompat.startActivityForResult((Activity) context, intent, requestCode, card.getOptionsBundle());
                } else {
                    throw new RouterException("Must use [go(activity, ...)] to support [startActivityForResult]!");
                }
            } else {
                ActivityCompat.startActivity(context, intent, card.getOptionsBundle());
            }

            if ((-1 != card.getEnterAnim() && -1 != card.getExitAnim()) && context instanceof Activity) {
                ((Activity) context).overridePendingTransition(card.getEnterAnim(), card.getExitAnim());
            }

            if (callback != null) {
                callback.onArrival(card);
            }
        });
    }

    @NonNull
    private Object goFragment(Context context, Card card, Class<?> cls, GoCallback callback) {
        try {
            Object instance = cls.getConstructor().newInstance();
            if (instance instanceof Fragment) {
                ((Fragment) instance).setArguments(card.getExtras());
            }
            if (callback != null) {
                callback.onArrival(card);
            }
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RouterException("fragment constructor new instance failed!");
        }
    }

}
