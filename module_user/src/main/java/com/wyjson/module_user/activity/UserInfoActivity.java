package com.wyjson.module_user.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.wyjson.module_user.databinding.UserActivitySignInBinding;
import com.wyjson.module_user.databinding.UserActivityUserInfoBinding;

/**
 * 这个页面需要登录后才能进入,未登录会触发{@link com.wyjson.module_user.route.service.SignInInterceptor}
 */
public class UserInfoActivity extends FragmentActivity {

    UserActivityUserInfoBinding vb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = UserActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());
    }
}