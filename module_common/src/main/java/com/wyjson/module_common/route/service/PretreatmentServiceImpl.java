package com.wyjson.module_common.route.service;

import android.content.Context;
import android.content.Intent;

import com.wyjson.module_common.route.UserRoute;
import com.wyjson.router.annotation.Service;
import com.wyjson.router.core.Card;
import com.wyjson.router.interfaces.PretreatmentService;

@Service(remark = "预处理服务")
public class PretreatmentServiceImpl implements PretreatmentService {

    @Override
    public void init() {

    }

    @Override
    public boolean onPretreatment(Context context, Card card) {
        // 登录页面预处理
        if (UserRoute.SignInActivity.equals(card.getPath())) {
            card.withFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        return true;
    }
}