package com.wyjson.router.interfaces;

public interface IService {

    /**
     * 服务接口的初始化方法，会在首次调用的时候触发，仅会调用一次
     */
    void init();
}