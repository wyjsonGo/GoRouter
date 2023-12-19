package com.wyjson.router.helper.module_user.group_new;

import android.content.Context;
import com.wyjson.router.GoRouter;
import com.wyjson.router.model.Card;
import java.lang.String;

/**
 * DO NOT EDIT THIS FILE!!! IT WAS GENERATED BY GOROUTER.
 * 参数页面
 * {@link com.wyjson.module_user.activity.ParamActivity}
 */
public class NewParamActivityGoRouter {
    public static String getPath() {
        return "/new/param/activity";
    }

    public static Card build(String nickname, com.wyjson.module_common.model.TestModel test) {
        return GoRouter.getInstance().build(getPath())
                // 昵称
                .withString("nickname", nickname)
                // 自定义类型
                .withObject("test", test);
    }

    public static void go(Context context, String nickname,
            com.wyjson.module_common.model.TestModel test) {
        build(nickname, test).go(context);
    }

    public static Builder get(String nickname, com.wyjson.module_common.model.TestModel test) {
        return new Builder(nickname, test);
    }

    public static Card build(String nickname, com.wyjson.module_common.model.TestModel test,
            int base, int age) {
        return get(nickname, test).setBase(base).setAge(age).build();
    }

    public static void go(Context context, String nickname,
            com.wyjson.module_common.model.TestModel test, int base, int age) {
        build(nickname, test, base, age).go(context);
    }

    public static class Builder {
        private final Card mCard;

        public Builder(String nickname, com.wyjson.module_common.model.TestModel test) {
            mCard = GoRouter.getInstance().build(getPath())
                    // 昵称
                    .withString("nickname", nickname)
                    // 自定义类型
                    .withObject("test", test);
        }

        /**
         * 我是一个父类字段
         */
        public Builder setBase(int base) {
            mCard.withInt("base", base);
            return this;
        }

        public Builder setAge(int age) {
            mCard.withInt("age", age);
            return this;
        }

        public Card build() {
            return mCard;
        }
    }
}