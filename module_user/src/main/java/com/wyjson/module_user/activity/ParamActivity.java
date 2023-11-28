package com.wyjson.module_user.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.wyjson.module_common.model.TestModel;
import com.wyjson.module_common.route.UserRoute;
import com.wyjson.module_common.utils.ToastUtils;
import com.wyjson.module_user.databinding.UserActivityParamBinding;
import com.wyjson.router.GoRouter;
import com.wyjson.router.annotation.Param;
import com.wyjson.router.annotation.Route;
import com.wyjson.router.exception.ParamException;

import java.util.ArrayList;

@Route(path = UserRoute.ParamActivity, remark = "参数页面")
public class ParamActivity extends BaseParamActivity {

    UserActivityParamBinding vb;

    @Param
    int age = 18;

    @Param(name = "nickname", remark = "昵称", required = true)
    String name;

    @Param(name = "test", remark = "自定义类型", required = true)
    TestModel testModel;

    @Param(name = "value1")
    int[] value1;

    @Param(name = "value2")
    ArrayList<Integer> value2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = UserActivityParamBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        try {
            ParamActivityInject.injectCheck(this);
//            GoRouter.getInstance().injectCheck(this);
        } catch (ParamException e) {
            String paramName = e.getParamName();
            ToastUtils.makeText(this, e.getMessage());
            finish();
            return;
        }

        vb.tvTitle.setText("base:" + base + ",age:" + age + ",name:" + name + "\ntest:" + testModel.toString());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        GoRouter.getInstance().inject(this, intent);
        vb.tvTitle.setText("base:" + base + ",age:" + age + ",name:" + name);
    }
}