package com.wyjson.router.utils;

import androidx.annotation.NonNull;

import com.wyjson.router.GoRouter;
import com.wyjson.router.exception.RouterException;

import java.util.Iterator;
import java.util.TreeMap;

/**
 * UniqueKeyTreeMap
 *
 * @param <K>
 * @param <V>
 */
public class InterceptorTreeMap<K, V> extends TreeMap<K, V> {
    private final String tipText;

    public InterceptorTreeMap(String exceptionText) {
        super();
        tipText = exceptionText;
    }

    @Override
    public V put(K key, V value) {
        if (containsKey(key)) {
            throw new RouterException(String.format(tipText, key));
        } else {
            return super.put(key, value);
        }
    }

    @NonNull
    @Override
    public String toString() {
        if (!GoRouter.isDebug()) {
            return "";
        }
        Iterator<Entry<K, V>> i = entrySet().iterator();
        if (!i.hasNext())
            return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (; ; ) {
            Entry<K, V> e = i.next();
            K key = e.getKey();
            V value = e.getValue();
            sb.append(key);
            sb.append("->");
            sb.append(value.getClass().getSimpleName());
            if (!i.hasNext())
                return sb.append('}').toString();
            sb.append(',').append(' ');
        }
    }
}
