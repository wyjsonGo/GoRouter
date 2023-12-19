package com.wyjson.router.model;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityOptionsCompat;

import com.wyjson.router.GoRouter;
import com.wyjson.router.callback.GoCallback;
import com.wyjson.router.core.RouteCenter;
import com.wyjson.router.enums.RouteType;
import com.wyjson.router.exception.NoFoundRouteException;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.interfaces.IJsonService;
import com.wyjson.router.utils.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;

public final class Card extends CardMeta {

    private Uri uri;
    private Bundle mBundle;
    private int flags = 0;
    private boolean greenChannel;// 绿色通道(跳过所有的拦截器)
    private String action;
    private Context context;
    private IJsonService jsonService;

    private int enterAnim = -1;// 转场动画
    private int exitAnim = -1;
    private ActivityOptionsCompat activityOptionsCompat;// 转场动画(API16+)

    private Throwable interceptorException;// 拦截执行中断异常信息
    private int timeout = 300;// go() timeout, TimeUnit.Second

    public void setUri(Uri uri) {
        if (uri == null || TextUtils.isEmpty(uri.toString()) || TextUtils.isEmpty(uri.getPath())) {
            throw new RouterException("uri Parameter is invalid!");
        }
        this.uri = uri;
        setPath(uri.getPath());
    }

    public Uri getUri() {
        return uri;
    }

    public ActivityOptionsCompat getActivityOptionsCompat() {
        return activityOptionsCompat;
    }

    public int getEnterAnim() {
        return enterAnim;
    }

    public int getExitAnim() {
        return exitAnim;
    }

    public Card(Uri uri) {
        setUri(uri);
        this.mBundle = new Bundle();
    }

    public Card(String path, Bundle bundle) {
        setPath(path);
        this.mBundle = (null == bundle ? new Bundle() : bundle);
    }

    @Nullable
    public Object go(Context context) {
        return go(context, this, -1, null, null);
    }

    @Nullable
    public Object go(Context context, GoCallback callback) {
        return go(context, this, -1, null, callback);
    }

    @Nullable
    public Object go(Context context, int requestCode) {
        return go(context, this, requestCode, null, null);
    }

    @Nullable
    public Object go(Context context, int requestCode, GoCallback callback) {
        return go(context, this, requestCode, null, callback);
    }

    @Nullable
    public Object go(Context context, ActivityResultLauncher<Intent> activityResultLauncher) {
        return go(context, this, -1, activityResultLauncher, null);
    }

    @Nullable
    public Object go(Context context, ActivityResultLauncher<Intent> activityResultLauncher, GoCallback callback) {
        return go(context, this, -1, activityResultLauncher, callback);
    }

    @Nullable
    private Object go(Context context, Card card, int requestCode, ActivityResultLauncher<Intent> activityResultLauncher, GoCallback callback) {
        return GoRouter.getInstance().go(context, card, requestCode, activityResultLauncher, callback);
    }

    @Nullable
    public CardMeta getCardMeta() {
        try {
            return RouteCenter.getCardMeta(this);
        } catch (NoFoundRouteException e) {
            GoRouter.logger.warning(null, e.getMessage());
        }
        return null;
    }

    public void setCardMeta(RouteType type, Class<?> pathClass, int tag, boolean deprecated) {
        setType(type);
        setPathClass(pathClass);
        setTag(tag);
        setDeprecated(deprecated);
    }

    public Bundle getExtras() {
        return mBundle;
    }

    public Card with(Bundle bundle) {
        if (null != bundle) {
            mBundle = bundle;
        }
        return this;
    }

    public Card withFlags(int flag) {
        this.flags = flag;
        return this;
    }

    public Card addFlags(int flags) {
        this.flags |= flags;
        return this;
    }

    public int getFlags() {
        return flags;
    }

    /**
     * 使用 withObject 传递 List 和 Map 的实现了 Serializable 接口的实现类(ArrayList/HashMap)的时候，
     * 接收该对象的地方不能标注具体的实现类类型应仅标注为 List 或 Map，
     * 否则会影响序列化中类型的判断, 其他类似情况需要同样处理
     *
     * @param key
     * @param value
     * @return
     */
    public Card withObject(@Nullable String key, @Nullable Object value) {
        jsonService = GoRouter.getInstance().getService(IJsonService.class);
        if (jsonService == null) {
            throw new RouterException("To use withObject() method, you need to implement IJsonService");
        }
        mBundle.putString(key, jsonService.toJson(value));
        return this;
    }

    public Card withString(@Nullable String key, @Nullable String value) {
        mBundle.putString(key, value);
        return this;
    }

    public Card withBoolean(@Nullable String key, boolean value) {
        mBundle.putBoolean(key, value);
        return this;
    }

    public Card withShort(@Nullable String key, short value) {
        mBundle.putShort(key, value);
        return this;
    }

    public Card withInt(@Nullable String key, int value) {
        mBundle.putInt(key, value);
        return this;
    }

    public Card withLong(@Nullable String key, long value) {
        mBundle.putLong(key, value);
        return this;
    }

    public Card withDouble(@Nullable String key, double value) {
        mBundle.putDouble(key, value);
        return this;
    }

    public Card withByte(@Nullable String key, byte value) {
        mBundle.putByte(key, value);
        return this;
    }

    public Card withChar(@Nullable String key, char value) {
        mBundle.putChar(key, value);
        return this;
    }

    public Card withFloat(@Nullable String key, float value) {
        mBundle.putFloat(key, value);
        return this;
    }

    public Card withCharSequence(@Nullable String key, @Nullable CharSequence value) {
        mBundle.putCharSequence(key, value);
        return this;
    }

    public Card withParcelable(@Nullable String key, @Nullable Parcelable value) {
        mBundle.putParcelable(key, value);
        return this;
    }

    public Card withParcelableArray(@Nullable String key, @Nullable Parcelable[] value) {
        mBundle.putParcelableArray(key, value);
        return this;
    }

    public Card withParcelableArrayList(@Nullable String key, @Nullable ArrayList<? extends Parcelable> value) {
        mBundle.putParcelableArrayList(key, value);
        return this;
    }

    public Card withSparseParcelableArray(@Nullable String key, @Nullable SparseArray<? extends Parcelable> value) {
        mBundle.putSparseParcelableArray(key, value);
        return this;
    }

    public Card withIntegerArrayList(@Nullable String key, @Nullable ArrayList<Integer> value) {
        mBundle.putIntegerArrayList(key, value);
        return this;
    }

    public Card withStringArrayList(@Nullable String key, @Nullable ArrayList<String> value) {
        mBundle.putStringArrayList(key, value);
        return this;
    }

    public Card withCharSequenceArrayList(@Nullable String key, @Nullable ArrayList<CharSequence> value) {
        mBundle.putCharSequenceArrayList(key, value);
        return this;
    }

    public Card withSerializable(@Nullable String key, @Nullable Serializable value) {
        mBundle.putSerializable(key, value);
        return this;
    }

    public Card withByteArray(@Nullable String key, @Nullable byte[] value) {
        mBundle.putByteArray(key, value);
        return this;
    }

    public Card withShortArray(@Nullable String key, @Nullable short[] value) {
        mBundle.putShortArray(key, value);
        return this;
    }

    public Card withCharArray(@Nullable String key, @Nullable char[] value) {
        mBundle.putCharArray(key, value);
        return this;
    }

    public Card withLongArray(@Nullable String key, @Nullable long[] value) {
        mBundle.putLongArray(key, value);
        return this;
    }

    public Card withIntArray(@Nullable String key, @Nullable int[] value) {
        mBundle.putIntArray(key, value);
        return this;
    }

    public Card withDoubleArray(@Nullable String key, @Nullable double[] value) {
        mBundle.putDoubleArray(key, value);
        return this;
    }

    public Card withBooleanArray(@Nullable String key, @Nullable boolean[] value) {
        mBundle.putBooleanArray(key, value);
        return this;
    }

    public Card withStringArray(@Nullable String key, @Nullable String[] value) {
        mBundle.putStringArray(key, value);
        return this;
    }

    public Card withFloatArray(@Nullable String key, @Nullable float[] value) {
        mBundle.putFloatArray(key, value);
        return this;
    }

    public Card withCharSequenceArray(@Nullable String key, @Nullable CharSequence[] value) {
        mBundle.putCharSequenceArray(key, value);
        return this;
    }

    public Card withBundle(@Nullable String key, @Nullable Bundle value) {
        mBundle.putBundle(key, value);
        return this;
    }

    public Card withTransition(int enterAnim, int exitAnim) {
        this.enterAnim = enterAnim;
        this.exitAnim = exitAnim;
        return this;
    }

    @RequiresApi(16)
    public Card withActivityOptionsCompat(ActivityOptionsCompat compat) {
        this.activityOptionsCompat = compat;
        return this;
    }

    public String getAction() {
        return action;
    }

    public Card withAction(String action) {
        this.action = action;
        return this;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean isGreenChannel() {
        return greenChannel;
    }

    public Card greenChannel() {
        this.greenChannel = true;
        return this;
    }

    public Throwable getInterceptorException() {
        return interceptorException;
    }

    public void setInterceptorException(Throwable interceptorException) {
        this.interceptorException = interceptorException;
    }

    public int getTimeout() {
        return timeout;
    }

    /**
     * @param timeout Second
     */
    public Card setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        if (!GoRouter.isDebug()) {
            return "";
        }
        return "Card{" +
                "path='" + getPath() + '\'' +
                ", uri=" + uri +
                ", mBundle=" + mBundle +
                ", flags=" + flags +
                ", greenChannel=" + greenChannel +
                ", action='" + action + '\'' +
                ", context=" + (context != null ? context.getClass().getSimpleName() : null) +
                ", optionsCompat=" + activityOptionsCompat +
                ", enterAnim=" + enterAnim +
                ", exitAnim=" + exitAnim +
                ", interceptorException=" + interceptorException +
                ", timeout=" + timeout +
                '}';
    }
}