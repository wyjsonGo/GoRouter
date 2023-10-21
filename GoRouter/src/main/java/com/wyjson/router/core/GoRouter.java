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
import androidx.arch.core.util.Function;
import androidx.core.app.ActivityCompat;
import androidx.core.util.Supplier;
import androidx.fragment.app.Fragment;

import com.wyjson.router.callback.GoCallback;
import com.wyjson.router.document.DocumentModel;
import com.wyjson.router.document.DocumentUtils;
import com.wyjson.router.enums.ParamType;
import com.wyjson.router.enums.RouteType;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.interceptor.InterceptorCallback;
import com.wyjson.router.interceptor.InterceptorUtils;
import com.wyjson.router.interceptor.service.InterceptorService;
import com.wyjson.router.interceptor.service.impl.InterceptorServiceImpl;
import com.wyjson.router.interfaces.DegradeService;
import com.wyjson.router.interfaces.IInterceptor;
import com.wyjson.router.interfaces.IService;
import com.wyjson.router.interfaces.PretreatmentService;
import com.wyjson.router.logger.DefaultLogger;
import com.wyjson.router.logger.ILogger;
import com.wyjson.router.service.ServiceHelper;
import com.wyjson.router.thread.DefaultPoolExecutor;
import com.wyjson.router.utils.MapUtils;
import com.wyjson.router.utils.TextUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public final class GoRouter {

    public static final String ROUTER_RAW_URI = "router_raw_uri";
    public static final String ROUTER_PARAM_INJECT = "router_param_inject";

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private static final Map<String, CardMeta> routes = new RouteHashMap<>();
    private volatile static ThreadPoolExecutor executor = DefaultPoolExecutor.getInstance();
    public static ILogger logger = new DefaultLogger("GoRouter");

    private GoRouter() {
        logger.info(null, "[GoRouter] init!");
        InterceptorUtils.clearIterator();
        addService(InterceptorServiceImpl.class);
    }

    private static class InstanceHolder {
        private static final GoRouter mInstance = new GoRouter();
    }

    public static GoRouter getInstance() {
        return InstanceHolder.mInstance;
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

    public static synchronized void openLog() {
        logger.showLog(true);
        logger.info(null, "[openLog]");
    }

    public static synchronized void printStackTrace() {
        logger.showStackTrace(true);
        logger.info(null, "[printStackTrace]");
    }

    public static String generateDocument() {
        return generateDocument(null);
    }

    /**
     * 生成JSON格式文档
     *
     * @param tagFunction 不处理返回默认int类型tag,实现方法可自定义返回tag,示例[LOGIN, AUTHENTICATION]
     * @return JSON格式文档
     */
    public static String generateDocument(Function<Integer, String> tagFunction) {
        return DocumentUtils.generate(new DocumentModel(routes, ServiceHelper.getInstance().getServices(), InterceptorUtils.getInterceptors()), tagFunction);
    }

    @Nullable
    CardMeta getCardMeta(Card card) {
        CardMeta cardMeta = routes.get(card.getPath());
        if (cardMeta != null) {
            logger.info(null, "[getCardMeta] " + cardMeta.toString());
        } else {
            logger.warning(null, "[getCardMeta] null");
        }
        return cardMeta;
    }

    void addCardMeta(CardMeta cardMeta) {
        if (cardMeta.getType() != null) {
            // 检查路由是否有重复提交的情况
            if (logger.isShowLog()) {
                for (Map.Entry<String, CardMeta> cardMetaEntry : routes.entrySet()) {
                    if (TextUtils.equals(cardMetaEntry.getKey(), cardMeta.getPath())) {
                        logger.error(null, "[addCardMeta] Path duplicate commit!!! path[" + cardMetaEntry.getValue().getPath() + "]");
                        break;
                    } else if (cardMetaEntry.getValue().getPathClass() == cardMeta.getPathClass()) {
                        logger.error(null, "[addCardMeta] PathClass duplicate commit!!! pathClass[" + cardMetaEntry.getValue().getPathClass() + "]");
                        break;
                    }
                }
            }
            routes.put(cardMeta.getPath(), cardMeta);
            logger.debug(null, "[addCardMeta] size:" + routes.size() + ", commit:" + cardMeta.toString());
        } else {
            throw new RouterException("The route type is incorrect! The path[" + cardMeta.getPath() + "] type can only end with " + RouteType.toStringByValues());
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
        }
        return new Card(path, bundle);
    }

    public Card build(Uri uri) {
        if (uri == null || TextUtils.isEmpty(uri.toString())) {
            throw new RouterException("[uri] Parameter is invalid!");
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
                logger.debug(null, "[inject] " + paramsName + ":" + value);
                try {
                    Field injectField = target.getClass().getDeclaredField(paramsName);
                    injectField.setAccessible(true);
                    injectField.set(target, value);
                } catch (Exception e) {
                    throw new RouterException("Inject values for activity error! [" + e.getMessage() + "]");
                }
            }
        }
        logger.debug(null, "[inject] Auto Inject Success!");
    }

    @Nullable
    Object go(Context context, Card card, int requestCode, GoCallback callback) {
        card.setContext(context);
        card.setInterceptorException(null);
        logger.debug(null, "[go] " + card.toString());
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

            runInMainThread(() -> {
                logger.debug(null, "[go] [onFound] " + card.toString());
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
                case DIALOG_FRAGMENT:
                    return goFragment(context, card, cardMeta.getPathClass(), callback);
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
    private Object goFragment(Context context, Card card, Class<?> cls, GoCallback callback) {
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
