package com.wyjson.router.core;

import static com.wyjson.router.core.Constants.ROUTER_CURRENT_PATH;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.wyjson.router.GoRouter;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.utils.TextUtils;

public class EventCenter {

    public static <T> void registerEvent(LifecycleOwner owner, Class<T> type, boolean isForever, @NonNull Observer<T> observer) {
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

        String path = RouteCenter.getCurrentPath(owner);
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

    public static <T> void unRegisterEvent(LifecycleOwner owner, Class<T> type, Observer<T> observer) {
        if (type == null) {
            throw new RouterException("type cannot be empty!");
        }

        String path = RouteCenter.getCurrentPath(owner);
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

    public static <T> void postEvent(String path, T value) {
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
