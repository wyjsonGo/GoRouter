package com.wyjson.module_common.route.service;

import android.content.Context;

import com.wyjson.module_common.utils.ToastUtils;
import com.wyjson.router.annotation.Service;
import com.wyjson.router.interfaces.IDegradeService;
import com.wyjson.router.model.Card;

@Service(remark = "全局降级策略")
public class DegradeServiceImpl implements IDegradeService {

    @Override
    public void onLost(Context context, Card card) {
        ToastUtils.makeText(context, "当前版本不支持，请升级!");
    }

    @Override
    public void init() {

    }
}