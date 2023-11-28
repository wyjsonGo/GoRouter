package com.wyjson.module_user.activity;

import android.os.Bundle;
import android.util.Log;

import com.wyjson.module_common.model.TestModel;
import com.wyjson.router.GoRouter;
import com.wyjson.router.exception.ParamException;
import com.wyjson.router.exception.RouterException;
import com.wyjson.router.interfaces.IJsonService;

import java.util.ArrayList;

public class ParamActivityInject {

    public static void inject(ParamActivity activity) {
        try {
            injectCheck(activity);
        } catch (ParamException | NullPointerException ignored) {
        }
    }

    public static void injectCheck(ParamActivity activity) throws ParamException, NullPointerException {
        Bundle bundle = activity.getIntent().getExtras();
        if (bundle == null) {
            // 判断存不存在required=true的情况
            if (true) {
                throw new NullPointerException("The bundle in the intent is empty!");
            }
            return;
        }
        IJsonService jsonService = GoRouter.getInstance().getService(IJsonService.class);

        activity.age = bundle.getInt("age", activity.age);

        activity.base = bundle.getInt("base", activity.base);

        // required=true的情况
        if (bundle.containsKey("nickname")) {
            activity.name = bundle.getString("nickname", activity.name);
        } else {
            throw new ParamException("nickname");
        }

        if (bundle.containsKey("test")) {
            if (jsonService != null) {
                activity.testModel = jsonService.parseObject(bundle.getString("test"), TestModel.class);
            } else {
                throw new RouterException("To use withObject() method, you need to implement IJsonService");
            }
        } else {
            throw new ParamException("test");
        }

        if (bundle.containsKey("value1")) {
            activity.value1 = (int[]) bundle.getSerializable("value1");
        }

        if (bundle.containsKey("value2")) {
            activity.value2 = (ArrayList<Integer>) bundle.getSerializable("value2");
        }
        Log.e("1", "2");
    }

}
