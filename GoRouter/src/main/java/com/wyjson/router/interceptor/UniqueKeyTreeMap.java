package com.wyjson.router.interceptor;

import com.wyjson.router.exception.RouterException;

import java.util.TreeMap;

public class UniqueKeyTreeMap<K, V> extends TreeMap<K, V> {
    private final String tipText;

    public UniqueKeyTreeMap(String exceptionText) {
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
}
