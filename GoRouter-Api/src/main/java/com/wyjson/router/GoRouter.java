package com.wyjson.router;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;

import com.wyjson.router.callback.GoCallback;
import com.wyjson.router.callback.InterceptorCallback;
import com.wyjson.router.core.ApplicationModuleCenter;
import com.wyjson.router.core.EventCenter;
import com.wyjson.router.core.InterceptorCenter;
import com.wyjson.router.core.InterceptorServiceImpl;
import com.wyjson.router.core.RouteCenter;
import com.wyjson.router.core.RouteModuleCenter;
import com.wyjson.router.core.ServiceCenter;
import com.wyjson.router.core.interfaces.IInterceptorService;
import com.wyjson.router.exception.NoFoundRouteException;
import com.wyjson.router.exception.ParamException;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.interfaces.IApplicationModule;
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

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private volatile static ThreadPoolExecutor executor = DefaultPoolExecutor.getInstance();
    public static ILogger logger = new DefaultLogger();
    private volatile static boolean isDebug = false;
    private static Application mApplication;

    private GoRouter() {
        InterceptorCenter.clearInterceptors();
        ServiceCenter.addService(InterceptorServiceImpl.class);
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
    public static synchronized void autoLoadRouteModule(Application application) {
        setApplication(application);
        logger.info(null, "[GoRouter] autoLoadRouteModule!");
        RouteModuleCenter.load(application);
    }

    /**
     * 获取路由注册模式
     *
     * @return true [GoRouter-Gradle-Plugin] ,false [scan dex file]
     */
    public boolean isRouteRegisterMode() {
        return RouteModuleCenter.isRegisterByPlugin();
    }

    public static synchronized void openDebug() {
        isDebug = true;
        logger.showLog(isDebug);
        logger.info(null, "[openDebug]");
    }

    public static void setApplication(Application application) {
        mApplication = application;
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
     * 获取模块application注册模式
     *
     * @return true [GoRouter-Gradle-Plugin] ,false [scan dex file]
     */
    public boolean isAMRegisterMode() {
        return ApplicationModuleCenter.isRegisterByPlugin();
    }

    public static void callAMOnCreate(Application application) {
        setApplication(application);
        ApplicationModuleCenter.callOnCreate(application);
    }

    public static void callAMOnTerminate() {
        ApplicationModuleCenter.callOnTerminate();
    }

    public static void callAMOnConfigurationChanged(@NonNull Configuration newConfig) {
        ApplicationModuleCenter.callOnConfigurationChanged(newConfig);
    }

    public static void callAMOnLowMemory() {
        ApplicationModuleCenter.callOnLowMemory();
    }

    public static void callAMOnTrimMemory(int level) {
        ApplicationModuleCenter.callOnTrimMemory(level);
    }

    /**
     * 动态注册模块application
     *
     * @param am
     */
    public static void registerAM(Class<? extends IApplicationModule> am) {
        ApplicationModuleCenter.register(am);
    }

    /**
     * 实现相同接口的service会被覆盖(更新)
     *
     * @param service 实现类.class
     */
    public void addService(Class<? extends IService> service) {
        ServiceCenter.addService(service);
    }

    /**
     * 实现相同接口的service会被覆盖(更新)
     *
     * @param service 实现类.class
     * @param alias   别名
     */
    public void addService(Class<? extends IService> service, String alias) {
        ServiceCenter.addService(service, alias);
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
        return ServiceCenter.getService(service);
    }

    /**
     * 获取service接口的实现
     *
     * @param service
     * @param alias   别名
     * @param <T>
     * @return
     */
    @Nullable
    public <T> T getService(Class<? extends T> service, String alias) {
        return ServiceCenter.getService(service, alias);
    }

    /**
     * 重复添加相同序号会catch
     *
     * @param ordinal
     * @param interceptor
     */
    public void addInterceptor(int ordinal, Class<? extends IInterceptor> interceptor) {
        InterceptorCenter.addInterceptor(ordinal, interceptor, false);
    }

    /**
     * 重复添加相同序号会覆盖(更新)
     *
     * @param ordinal
     * @param interceptor
     */
    public void setInterceptor(int ordinal, Class<? extends IInterceptor> interceptor) {
        InterceptorCenter.setInterceptor(ordinal, interceptor);
    }

    /**
     * 动态添加路由分组,按需加载路由
     */
    public void addRouterGroup(String group, IRouteModuleGroup routeModuleGroup) {
        RouteCenter.addRouterGroup(group, routeModuleGroup);
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

    /**
     * 获取原始的URI
     *
     * @param activity
     */
    public String getRawURI(Activity activity) {
        return RouteCenter.getRawURI(activity);
    }

    /**
     * 获取原始的URI
     *
     * @param fragment
     */
    public String getRawURI(Fragment fragment) {
        return RouteCenter.getRawURI(fragment);
    }

    /**
     * 获取当前页面路径
     *
     * @param activity
     */
    public String getCurrentPath(Activity activity) {
        return RouteCenter.getCurrentPath(activity);
    }

    /**
     * 获取当前页面路径
     *
     * @param fragment
     */
    public String getCurrentPath(Fragment fragment) {
        return RouteCenter.getCurrentPath(fragment);
    }

    public void inject(Activity activity) {
        inject(activity, null, null, false);
    }

    public void inject(Activity activity, Intent intent) {
        inject(activity, intent, null, false);
    }

    public void inject(Activity activity, Bundle bundle) {
        inject(activity, null, bundle, false);
    }

    public void inject(Fragment fragment) {
        inject(fragment, null, null, false);
    }

    public void inject(Fragment fragment, Intent intent) {
        inject(fragment, intent, null, false);
    }

    public void inject(Fragment fragment, Bundle bundle) {
        inject(fragment, null, bundle, false);
    }

    public void injectCheck(Activity activity) throws ParamException {
        inject(activity, null, null, true);
    }

    public void injectCheck(Activity activity, Intent intent) throws ParamException {
        inject(activity, intent, null, true);
    }

    public void injectCheck(Activity activity, Bundle bundle) throws ParamException {
        inject(activity, null, bundle, true);
    }

    public void injectCheck(Fragment fragment) throws ParamException {
        inject(fragment, null, null, true);
    }

    public void injectCheck(Fragment fragment, Intent intent) throws ParamException {
        inject(fragment, intent, null, true);
    }

    public void injectCheck(Fragment fragment, Bundle bundle) throws ParamException {
        inject(fragment, null, bundle, true);
    }

    private <T> void inject(T target, Intent intent, Bundle bundle, boolean isCheck) throws ParamException {
        RouteCenter.inject(target, intent, bundle, isCheck);
    }

    @Nullable
    public Object go(Context context, Card card, int requestCode, ActivityResultLauncher<Intent> activityResultLauncher, GoCallback callback) {
        card.setContext(context == null ? mApplication : context);
        card.setInterceptorException(null);

        logger.debug(null, "[go] " + card);

        IPretreatmentService pretreatmentService = getService(IPretreatmentService.class);
        if (pretreatmentService != null) {
            if (!pretreatmentService.onPretreatment(card.getContext(), card)) {
                // 预处理失败，导航取消
                logger.debug(null, "[go] IPretreatmentService Failure!");
                return null;
            }
        } else {
            logger.warning(null, "[go] This [IPretreatmentService] was not found!");
        }

        try {
            RouteCenter.assembleRouteCard(card);
        } catch (NoFoundRouteException e) {
            logger.warning(null, e.getMessage());

            if (isDebug()) {
                runInMainThread(() -> Toast.makeText(card.getContext(), "There's no route matched!\n" +
                        " Path = [" + card.getPath() + "]\n" +
                        " Group = [" + card.getGroup() + "]", Toast.LENGTH_LONG).show());
            }

            onLost(card.getContext(), card, callback);
            return null;
        }

        runInMainThread(() -> {
            logger.debug(null, "[go] [onFound] " + card);
            if (callback != null) {
                callback.onFound(card);
            }
        });

        if (isDebug() && card.isDeprecated()) {
            logger.warning(null, "[go] This page has been marked as deprecated. path[" + card.getPath() + "]");
            runInMainThread(() -> Toast.makeText(card.getContext(), "This page has been marked as deprecated!\n" +
                    " Path = [" + card.getPath() + "]\n" +
                    " Group = [" + card.getGroup() + "]", Toast.LENGTH_SHORT).show());
        }

        switch (card.getType()) {
            case ACTIVITY:
                IInterceptorService interceptorService = getService(IInterceptorService.class);
                if (interceptorService != null && !card.isGreenChannel()) {
                    interceptorService.doInterceptions(card, new InterceptorCallback() {
                        @Override
                        public void onContinue(Card card) {
                            goActivity(card.getContext(), card, requestCode, activityResultLauncher, callback);
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
                    goActivity(card.getContext(), card, requestCode, activityResultLauncher, callback);
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
                    logger.warning(null, "[onLost] This [IDegradeService] was not found!");
                }
            }
        });
    }

    @SuppressLint("WrongConstant")
    private void goActivity(Context context, Card card, int requestCode, ActivityResultLauncher<Intent> activityResultLauncher, GoCallback callback) {
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
            ActivityOptionsCompat compat = card.getActivityOptionsCompat();
            if (requestCode >= 0) {
                if (context instanceof Activity) {
                    ActivityCompat.startActivityForResult((Activity) context, intent, requestCode, compat != null ? compat.toBundle() : null);
                } else {
                    throw new RouterException("Must use [go(activity, ...)] to support [startActivityForResult]!");
                }
            } else if (activityResultLauncher != null) {
                activityResultLauncher.launch(intent, compat);
            } else {
                ActivityCompat.startActivity(context, intent, compat != null ? compat.toBundle() : null);
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

    public <T> void registerEvent(FragmentActivity activity, @NonNull Class<T> type, @NonNull Observer<T> observer) {
        EventCenter.registerEvent(activity, type, false, observer);
    }

    public <T> void registerEvent(Fragment fragment, @NonNull Class<T> type, @NonNull Observer<T> observer) {
        EventCenter.registerEvent(fragment, type, false, observer);
    }

    public <T> void registerEventForever(FragmentActivity activity, @NonNull Class<T> type, @NonNull Observer<T> observer) {
        EventCenter.registerEvent(activity, type, true, observer);
    }

    public <T> void registerEventForever(Fragment fragment, @NonNull Class<T> type, @NonNull Observer<T> observer) {
        EventCenter.registerEvent(fragment, type, true, observer);
    }

    public <T> void unRegisterEvent(FragmentActivity activity, @NonNull Class<T> type) {
        EventCenter.unRegisterEvent(activity, type, null);
    }

    public <T> void unRegisterEvent(Fragment fragment, @NonNull Class<T> type) {
        EventCenter.unRegisterEvent(fragment, type, null);
    }

    public <T> void unRegisterEvent(FragmentActivity activity, @NonNull Class<T> type, Observer<T> observer) {
        EventCenter.unRegisterEvent(activity, type, observer);
    }

    public <T> void unRegisterEvent(Fragment fragment, @NonNull Class<T> type, Observer<T> observer) {
        EventCenter.unRegisterEvent(fragment, type, observer);
    }

    public <T> void postEvent(@NonNull String path, @NonNull T value) {
        EventCenter.postEvent(path, value);
    }

}
