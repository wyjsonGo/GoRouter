package com.wyjson.module_user.activity;

import static com.wyjson.module_common.route.utils.RouteTagUtils.AUTHENTICATION;
import static com.wyjson.module_common.route.utils.RouteTagUtils.LOGIN;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.wyjson.module_user.databinding.UserActivityUserInfoBinding;
import com.wyjson.router.annotation.Route;

/**
 * 这个页面需要登录后才能进入,未登录会触发登录拦截器{@link com.wyjson.module_user.route.interceptor.SignInInterceptor}
 */
@Route(path = "/user/info/activity", remark = "用户信息页面", tag = LOGIN | AUTHENTICATION)
public class UserInfoActivity extends FragmentActivity {

    UserActivityUserInfoBinding vb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = UserActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());
    }
}