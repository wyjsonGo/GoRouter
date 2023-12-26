package com.wyjson.router.utils;

import java.util.ArrayList;

/**
 * 单个int有4字节，可以配置31个开关,范围从1到1 << 30
 */
public class RouteTagUtils {

    /**
     * 是否存在
     *
     * @param v
     * @param item
     * @return
     */
    public static boolean isExist(int v, int item) {
        return (v & item) > 0;
    }

    /**
     * 存在多少个
     *
     * @param v
     * @return
     */
    public static int getExistCount(int v) {
        return Integer.bitCount(v);
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
     * 取反
     *
     * @param v
     * @return
     */
    public static int getNegation(int v) {
        return ~v;
    }

    /**
     * 获取所有存在的
     *
     * @param v
     * @param itemList
     * @return
     */
    public static ArrayList<Integer> getExistList(int v, int... itemList) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int item : itemList) {
            if (isExist(v, item))
                list.add(item);
        }
        return list;
    }

}