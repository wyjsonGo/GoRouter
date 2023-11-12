package com.wyjson.module_user.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.wyjson.module_common.route.UserRoute;
import com.wyjson.module_user.databinding.UserActivityParamBinding;
import com.wyjson.router.annotation.Param;
import com.wyjson.router.annotation.Route;
import com.wyjson.router.GoRouter;

@Route(path = UserRoute.ParamActivity, remark = "参数页面")
public class ParamActivity extends BaseParamActivity {

    UserActivityParamBinding vb;

    @Param
    private int age = 18;

    @Param(name = "nickname", remark = "昵称", required = true)
    private String name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = UserActivityParamBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        GoRouter.getInstance().inject(this);
        vb.tvTitle.setText("base:" + base + ",age:" + age + ",name:" + name);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        GoRouter.getInstance().inject(this, intent);
        vb.tvTitle.setText("base:" + base + ",age:" + age + ",name:" + name);
    }
}