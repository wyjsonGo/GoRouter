package com.wyjson.module_common.route.enums;

import java.util.ArrayList;

/**
 * 单个int有4字节，也就是32位，可以配置32个开关
 */
public enum RouteTag {
    LOGIN(1),
    AUTHENTICATION(1 << 1),
//    SAFETY(1 << 2),
    ;

    private final int value;

    public int getValue() {
        return value;
    }

    RouteTag(int value) {
        this.value = value;
    }

    /**
     * 是否存在
     *
     * @param v
     * @param flag
     * @return
     */
    public static boolean isExist(int v, RouteTag flag) {
        return (v & flag.getValue()) > 0;
    }

    /**
     * 存在多少个
     *
     * @param v
     * @return
     */
    public static int isExistCount(int v) {
        return Integer.bitCount(v);
    }

    /**
     * 取反
     *
     * @param v
     * @return
     */
    public static int getNegation(int v) {
        return ~v;
    }

    /**
     * 添加
     *
     * @param v
     * @param item
     * @return
     */
    public static int addItem(int v, int item) {
        return v | item;
    }

    /**
     * 删除
     *
     * @param v
     * @param item
     * @return
     */
    public static int deleteItem(int v, int item) {
        return v & ~item;
    }

    /**
     * 获取所有存在的
     *
     * @param v
     * @return
     */
    public static ArrayList<RouteTag> getExistList(int v) {
        ArrayList<RouteTag> resultList = new ArrayList<>();
        for (RouteTag each : values()) {
            if ((v & each.getValue()) > 0)
                resultList.add(each);
        }
        return resultList;
    }
}