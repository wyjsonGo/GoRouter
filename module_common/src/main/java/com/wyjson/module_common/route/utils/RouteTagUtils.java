package com.wyjson.module_common.route.utils;

import java.util.ArrayList;

/**
 * 单个int有4字节，可以配置31个开关,范围从1到1 << 30
 */
public class RouteTagUtils {

    public static final int LOGIN = 1;
    public static final int AUTHENTICATION = 1 << 1;
    public static final int SAFETY = 1 << 2;

    public enum TagEnum {
        LOGIN(RouteTagUtils.LOGIN),
        AUTHENTICATION(RouteTagUtils.AUTHENTICATION),
        SAFETY(RouteTagUtils.SAFETY),
        ;

        public final int tag;

        public int getTag() {
            return tag;
        }

        TagEnum(int tag) {
            this.tag = tag;
        }

        /**
         * 是否存在
         *
         * @param v
         * @param flag
         * @return
         */
        public static boolean isExist(int v, TagEnum flag) {
            return (v & flag.getTag()) > 0;
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
        public static ArrayList<TagEnum> getExistList(int v) {
            ArrayList<TagEnum> resultList = new ArrayList<>();
            for (TagEnum each : values()) {
                if ((v & each.getTag()) > 0)
                    resultList.add(each);
            }
            return resultList;
        }
    }
}