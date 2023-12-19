package com.wyjson.router.helper.module_main.group_main;

import android.content.Context;
import com.wyjson.router.GoRouter;
import com.wyjson.router.model.Card;
import java.lang.String;

/**
 * DO NOT EDIT THIS FILE!!! IT WAS GENERATED BY GOROUTER.
 * 事件页面
 * {@link com.wyjson.module_main.activity.EventActivity}
 */
public class MainEventActivityGoRouter {
    public static String getPath() {
        return "/main/event/activity";
    }

    public static Card build() {
        return GoRouter.getInstance().build(getPath());
    }

    public static void go(Context context) {
        build().go(context);
    }
}