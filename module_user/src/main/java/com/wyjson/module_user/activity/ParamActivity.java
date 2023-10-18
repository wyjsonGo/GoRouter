package com.wyjson.module_user.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.wyjson.module_user.databinding.UserActivityParamBinding;
import com.wyjson.router.core.GoRouter;

public class ParamActivity extends FragmentActivity {

    UserActivityParamBinding vb;

    @Keep
    private int age = 18;
    @Keep
    private String name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = UserActivityParamBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        GoRouter.getInstance().inject(this);
        vb.tvTitle.setText("age:" + age + ",name:" + name);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        GoRouter.getInstance().inject(intent);
        vb.tvTitle.setText("age:" + age + ",name:" + name);
    }
}