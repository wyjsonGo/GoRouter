package com.wyjson.router;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.wyjson.router.callback.GoCallback;
import com.wyjson.router.callback.InterceptorCallback;
import com.wyjson.router.core.InterceptorServiceImpl;
import com.wyjson.router.core.LogisticsCenter;
import com.wyjson.router.core.RouteModuleLoadCenter;
import com.wyjson.router.core.interfaces.IInterceptorService;
import com.wyjson.router.exception.NoFoundRouteException;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.interfaces.IDegradeService;
import com.wyjson.router.interfaces.IInterceptor;
import com.wyjson.router.interfaces.IPretreatmentService;
import com.wyjson.router.interfaces.IService;
import com.wyjson.router.logger.DefaultLogger;
import com.wyjson.router.logger.ILogger;
import com.wyjson.router.model.Card;
import com.wyjson.router.module.interfaces.IRouteModuleGroup;
import com.wyjson.router.thread.DefaultPoolExecutor;
import com.wyjson.router.utils.TextUtils;

import java.util.concurrent.ThreadPoolExecutor;

public final class GoRouter {

    public static final String ROUTER_CURRENT_PATH = "go_router_current_path";
    public static final String ROUTER_RAW_URI = "go_router_raw_uri";

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private volatile static ThreadPoolExecutor executor = DefaultPoolExecutor.getInstance();
    public static ILogger logger = new DefaultLogger();
    private volatile static boolean isDebug = false;

    private GoRouter() {
        LogisticsCenter.clearInterceptors();
        LogisticsCenter.addService(InterceptorServiceImpl.class);
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
        RouteModuleLoadCenter.loadModuleRoute(application);
    }

    /**
     * 获取路由注册模式
     *
     * @return true [GoRouter-Gradle-Plugin] ,false [scan dex file]
     */
    public boolean isRouteRegisterMode() {
        return RouteModuleLoadCenter.isRegisterByPlugin();
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
     *
     * @param service 实现类.class
     */
    public void addService(Class<? extends IService> service) {
        LogisticsCenter.addService(service);
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
        return LogisticsCenter.getService(service);
    }

    /**
     * 重复添加相同序号会catch
     *
     * @param ordinal
     * @param interceptor
     */
    public void addInterceptor(int ordinal, Class<? extends IInterceptor> interceptor) {
        LogisticsCenter.addInterceptor(ordinal, interceptor, false);
    }

    /**
     * 重复添加相同序号会覆盖(更新)
     *
     * @param ordinal
     * @param interceptor
     */
    public void setInterceptor(int ordinal, Class<? extends IInterceptor> interceptor) {
        LogisticsCenter.setInterceptor(ordinal, interceptor);
    }

    /**
     * 动态添加路由分组,按需加载路由
     */
    public void addRouterGroup(String group, IRouteModuleGroup routeModuleGroup) {
        LogisticsCenter.addRouterGroup(group, routeModuleGroup);
    }

    private void runInMainThread(Runnable runnable) {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            mHandler.post(runnable);
        } else {
            runnable.run();
        }
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

    public void inject(Activity activity) {
        LogisticsCenter.inject(activity, null, null);
    }

    public void inject(Activity activity, Intent intent) {
        LogisticsCenter.inject(activity, intent, null);
    }

    public void inject(Activity activity, Bundle bundle) {
        LogisticsCenter.inject(activity, null, bundle);
    }

    public void inject(Fragment fragment) {
        LogisticsCenter.inject(fragment, null, null);
    }

    public void inject(Fragment fragment, Intent intent) {
        LogisticsCenter.inject(fragment, intent, null);
    }

    public void inject(Fragment fragment, Bundle bundle) {
        LogisticsCenter.inject(fragment, null, bundle);
    }

    @Nullable
    public Object go(Context context, Card card, int requestCode, GoCallback callback) {
        card.setContext(context);
        card.setInterceptorException(null);
        card.withString(GoRouter.ROUTER_CURRENT_PATH, card.getPath());

        logger.debug(null, "[go] " + card);
        IPretreatmentService pretreatmentService = getService(IPretreatmentService.class);
        if (pretreatmentService != null) {
            if (!pretreatmentService.onPretreatment(context, card)) {
                // 预处理失败，导航取消
                logger.debug(null, "[go] PretreatmentService Failure!");
                return null;
            }
        } else {
            logger.warning(null, "[go] This [PretreatmentService] was not found!");
        }

        try {
            LogisticsCenter.assembleRouteCard(card);
        } catch (NoFoundRouteException e) {
            logger.warning(null, e.getMessage());

            if (isDebug()) {
                runInMainThread(() -> Toast.makeText(context, "There's no route matched!\n" +
                        " Path = [" + card.getPath() + "]\n" +
                        " Group = [" + card.getGroup() + "]", Toast.LENGTH_LONG).show());
            }

            onLost(context, card, callback);
            return null;
        }

        runInMainThread(() -> {
            logger.debug(null, "[go] [onFound] " + card);
            if (callback != null) {
                callback.onFound(card);
            }
        });

        switch (card.getType()) {
            case ACTIVITY:
                IInterceptorService interceptorService = getService(IInterceptorService.class);
                if (interceptorService != null && !card.isGreenChannel()) {
                    interceptorService.doInterceptions(card, new InterceptorCallback() {
                        @Override
                        public void onContinue(Card card) {
                            goActivity(context, card, requestCode, callback);
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
                    goActivity(context, card, requestCode, callback);
                }
                break;
            case FRAGMENT:
                return goFragment(card, callback);
        }
        return null;
    }

    private void onLost(Context context, Card card, GoCallback callback) {
        runInMainThread(() -> {
            logger.error(null, "[onLost] There is no route. path[" + card.getPath() + "]");
            if (callback != null) {
                callback.onLost(card);
            } else {
                IDegradeService degradeService = getService(IDegradeService.class);
                if (degradeService != null) {
                    degradeService.onLost(context, card);
                } else {
                    logger.warning(null, "[onLost] This [DegradeService] was not found!");
                }
            }
        });
    }

    @SuppressLint("WrongConstant")
    private void goActivity(Context context, Card card, int requestCode, GoCallback callback) {
        Intent intent = new Intent(context, card.getPathClass());

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
    private Object goFragment(Card card, GoCallback callback) {
        try {
            Object instance = card.getPathClass().getConstructor().newInstance();
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
