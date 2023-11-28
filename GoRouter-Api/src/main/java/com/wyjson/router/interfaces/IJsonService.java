package com.wyjson.router.interfaces;

import java.lang.reflect.Type;

public interface IJsonService extends IService {

    /**
     * Object to json
     *
     * @param instance obj
     * @return json string
     */
    String toJson(Object instance);

    /**
     * Parse json to object
     *
     * @param input   json string
     * @param typeOfT The specific genericized type of src
     * @param <T>
     * @return instance of object
     */
    <T> T parseObject(String input, Type typeOfT);
}
