package com.wyjson.module_user.activity;

import androidx.fragment.app.FragmentActivity;

import com.wyjson.router.annotation.Param;

public class BaseParamActivity extends FragmentActivity {

    @Param(remark = "我是一个父类字段")
    protected int base;

}