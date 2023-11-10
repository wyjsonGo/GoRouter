package com.wyjson.router.core;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
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
import com.wyjson.router.card.Card;
import com.wyjson.router.card.CardMeta;
import com.wyjson.router.enums.ParamType;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.interceptor.InterceptorCallback;
import com.wyjson.router.interceptor.InterceptorHelper;
import com.wyjson.router.interceptor.service.InterceptorService;
import com.wyjson.router.interceptor.service.impl.InterceptorServiceImpl;
import com.wyjson.router.interfaces.DegradeService;
import com.wyjson.router.interfaces.IInterceptor;
import com.wyjson.router.interfaces.IService;
import com.wyjson.router.interfaces.PretreatmentService;
import com.wyjson.router.load.RouteModuleLoadUtils;
import com.wyjson.router.logger.DefaultLogger;
import com.wyjson.router.logger.ILogger;
import com.wyjson.router.param.ParamMeta;
import com.wyjson.router.route.RouteHelper;
import com.wyjson.router.service.ServiceHelper;
import com.wyjson.router.thread.DefaultPoolExecutor;
import com.wyjson.router.utils.MapUtils;
import com.wyjson.router.utils.TextUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public final class GoRouter {

    public static final String ROUTER_CURRENT_PATH = "go_router_current_path";
    public static final String ROUTER_RAW_URI = "go_router_raw_uri";

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private volatile static ThreadPoolExecutor executor = DefaultPoolExecutor.getInstance();
    public static ILogger logger = new DefaultLogger("GoRouter");
    private volatile static boolean isDebug = false;

    private GoRouter() {
        logger.info(null, "[GoRouter] constructor!");
        InterceptorHelper.getInstance().clearIterator();
        ServiceHelper.getInstance().addService(InterceptorServiceImpl.class);
    }

    private static class InstanceHolder {
        private static final GoRouter mInstance = new GoRouter();
    }

    public static GoRouter getInstance() {
        return InstanceHolder.mInstance;
    }

    /**
     * 自动加载模块路由
     *
     * @param application
     */
    public static synchronized void autoLoadModuleRoute(Application application) {
        logger.info(null, "[GoRouter] autoLoadModuleRoute!");
        RouteModuleLoadUtils.loadModuleRoute(application);
    }

    /**
     * 获取路由注册模式
     *
     * @return true [GoRouter-Gradle-Plugin] ,false [scan dex file]
     */
    public boolean isRouteRegisterMode() {
        return RouteModuleLoadUtils.isRegisterByPlugin();
    }

    public static synchronized void openDebug() {
        isDebug = true;
        logger.showLog(isDebug);
        logger.info(null, "[openDebug]");
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static synchronized void printStackTrace() {
        logger.showStackTrace(true);
        logger.info(null, "[printStackTrace]");
    }

    public static synchronized void setExecutor(ThreadPoolExecutor tpe) {
        executor = tpe;
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    public static void setLogger(ILogger userLogger) {
        if (userLogger != null) {
            logger = userLogger;
        }
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
        InterceptorHelper.getInstance().addInterceptor(priority, interceptor, false);
    }

    /**
     * 相同优先级添加会覆盖
     * 调用时机可以在application或插件模块加载时
     *
     * @param priority
     * @param interceptor
     */
    public void setInterceptor(int priority, Class<? extends IInterceptor> interceptor) {
        InterceptorHelper.getInstance().setInterceptor(priority, interceptor);
    }

    public Card build(String path) {
        return build(path, null);
    }

    public Card build(String path, Bundle bundle) {
        return new Card(path, bundle);
    }

    public Card build(Uri uri) {
        return new Card(uri);
    }

    private void runInMainThread(Runnable runnable) {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            mHandler.post(runnable);
        } else {
            runnable.run();
        }
    }

    public void inject(Activity activity) {
        inject(activity, null, null);
    }

    public void inject(Activity activity, Intent intent) {
        inject(activity, intent, null);
    }

    public void inject(Activity activity, Bundle bundle) {
        inject(activity, null, bundle);
    }

    public void inject(Fragment fragment) {
        inject(fragment, null, null);
    }

    public void inject(Fragment fragment, Intent intent) {
        inject(fragment, intent, null);
    }

    public void inject(Fragment fragment, Bundle bundle) {
        inject(fragment, null, bundle);
    }

    /**
     * 解析参数
     *
     * @param target
     * @param intent
     * @param bundle
     * @param <T>
     */
    private <T> void inject(T target, Intent intent, Bundle bundle) {
        logger.debug(null, "[inject] Auto Inject Start!");

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
            logger.error(null, "[inject] path Parameter is invalid!");
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
                logger.debug(null, "[inject] " + paramName + ":" + value);
                try {
                    Field injectField = getDeclaredField(target.getClass(), params.getKey());
                    injectField.setAccessible(true);
                    injectField.set(target, value);
                } catch (Exception e) {
                    throw new RouterException("Inject values for activity/fragment error! [" + e.getMessage() + "]");
                }
            }
        }
        logger.debug(null, "[inject] Auto Inject End!");
    }

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

    @Nullable
    public Object go(Context context, Card card, int requestCode, GoCallback callback) {
        card.setContext(context);
        card.setInterceptorException(null);
        card.withString(GoRouter.ROUTER_CURRENT_PATH, card.getPath());

        logger.debug(null, "[go] " + card);
        PretreatmentService pretreatmentService = getService(PretreatmentService.class);
        if (pretreatmentService != null) {
            if (!pretreatmentService.onPretreatment(context, card)) {
                // 预处理失败，导航取消
                logger.debug(null, "[go] PretreatmentService Failure!");
                return null;
            }
        } else {
            logger.warning(null, "[go] This [PretreatmentService] was not found!");
        }

        CardMeta cardMeta = card.getCardMeta();
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

            runInMainThread(() -> {
                logger.debug(null, "[go] [onFound] " + card);
                if (callback != null) {
                    callback.onFound(card);
                }
            });

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
                            public void onInterrupt(Card card, @NonNull Throwable exception) {
                                runInMainThread(() -> {
                                    if (callback != null) {
                                        callback.onInterrupt(card, exception);
                                    }
                                });
                            }
                        });
                    } else {
                        goActivity(context, card, requestCode, cardMeta.getPathClass(), callback);
                    }
                    break;
                case FRAGMENT:
                    return goFragment(card, cardMeta.getPathClass(), callback);
            }
        } else {
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
        runInMainThread(() -> {
            logger.error(null, "[onLost] There is no route. path[" + card.getPath() + "]");
            if (callback != null) {
                callback.onLost(card);
            } else {
                DegradeService degradeService = getService(DegradeService.class);
                if (degradeService != null) {
                    degradeService.onLost(context, card);
                } else {
                    logger.warning(null, "[onLost] This [DegradeService] was not found!");
                }
            }
        });
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

            logger.debug(null, "[goActivity] [onArrival] Complete!");
            if (callback != null) {
                callback.onArrival(card);
            }
        });
    }

    @NonNull
    private Object goFragment(Card card, Class<?> cls, GoCallback callback) {
        try {
            Object instance = cls.getConstructor().newInstance();
            if (instance instanceof Fragment) {
                ((Fragment) instance).setArguments(card.getExtras());
            }
            runInMainThread(() -> {
                logger.debug(null, "[goFragment] [onArrival] Complete!");
                if (callback != null) {
                    callback.onArrival(card);
                }
            });
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RouterException("fragment constructor new instance failed!");
        }
    }

}
