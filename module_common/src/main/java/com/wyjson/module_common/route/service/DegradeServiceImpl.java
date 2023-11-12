package com.wyjson.module_common.route.service;

import android.content.Context;
import android.widget.Toast;

import com.wyjson.router.annotation.Service;
import com.wyjson.router.model.Card;
import com.wyjson.router.interfaces.IDegradeService;

@Service(remark = "全局降级策略")
public class DegradeServiceImpl implements IDegradeService {

    @Override
    public void onLost(Context context, Card card) {
        Toast.makeText(context, "当前版本不支持，请升级!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void init() {

    }
}