package com.wyjson.router.core;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityOptionsCompat;

import com.wyjson.router.callback.GoCallback;

import java.io.Serializable;
import java.util.ArrayList;

public final class Card extends CardMeta {

    private final Uri uri;
    private Bundle mBundle;
    private int flags = 0;
    private boolean greenChannel;
    private String action;
    private Context context;

    private Bundle optionsCompat;
    private int enterAnim = -1;
    private int exitAnim = -1;

    private Throwable interceptorException;// 拦截执行中断异常信息
    private int timeout = 300;// go() timeout, TimeUnit.Second

    public Uri getUri() {
        return uri;
    }

    public Bundle getOptionsBundle() {
        return optionsCompat;
    }

    public int getEnterAnim() {
        return enterAnim;
    }

    public int getExitAnim() {
        return exitAnim;
    }

    public Card(Uri uri) {
        this(uri.getPath(), uri, null);
    }

    public Card(String path, Bundle bundle) {
        this(path, null, bundle);
    }

    public Card(String path, Uri uri, Bundle bundle) {
        setPath(path);
        this.uri = uri;
        this.mBundle = (null == bundle ? new Bundle() : bundle);
    }

    @Nullable
    public Object go(Context context) {
        return go(context, this, -1, null);
    }

    @Nullable
    public Object go(Context context, GoCallback callback) {
        return go(context, this, -1, callback);
    }

    @Nullable
    public Object go(Context context, int requestCode) {
        return go(context, this, requestCode, null);
    }

    @Nullable
    public Object go(Context context, int requestCode, GoCallback callback) {
        return go(context, this, requestCode, callback);
    }

    @Nullable
    private Object go(Context context, Card card, int requestCode, GoCallback callback) {
        return GoRouter.getInstance().go(context, card, requestCode, callback);
    }

    @Nullable
    public CardMeta getCardMeta() {
        return GoRouter.getInstance().getCardMeta(this);
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
    public Card withOptionsCompat(ActivityOptionsCompat compat) {
        if (null != compat) {
            this.optionsCompat = compat.toBundle();
        }
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

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}