package com.wyjson.router.helper.module_user.group_new;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.wyjson.router.GoRouter;
import com.wyjson.router.model.Card;
import com.wyjson.router.model.CardMeta;
import java.lang.String;

/**
 * DO NOT EDIT THIS FILE!!! IT WAS GENERATED BY GOROUTER.
 * 参数片段
 * {@link com.wyjson.module_user.fragment.ParamFragment}
 */
public class NewParamFragmentGoRouter {
    public static String getPath() {
        return "/new/param/fragment";
    }

    @Nullable
    public static CardMeta getCardMeta() {
        return GoRouter.getInstance().build(getPath()).getCardMeta();
    }

    public static <T> void postEvent(T value) {
        GoRouter.getInstance().postEvent(getPath(), value);
    }

    public static Card build() {
        return GoRouter.getInstance().build(getPath());
    }

    @Nullable
    public static Fragment go() {
        return (Fragment) build().go();
    }

    public static Builder create() {
        return new Builder();
    }

    public static Card build(int age, String name) {
        return create().setAge(age).setName(name).build();
    }

    @Nullable
    public static Fragment go(int age, String name) {
        return (Fragment) build(age, name).go();
    }

    public static class Builder {
        private final Card mCard;

        public Builder() {
            mCard = GoRouter.getInstance().build(getPath());
        }

        public Builder setAge(int age) {
            mCard.withInt("age", age);
            return this;
        }

        public Builder setName(String name) {
            mCard.withString("name", name);
            return this;
        }

        public Card build() {
            return mCard;
        }
    }
}
