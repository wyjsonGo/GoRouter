package com.wyjson.router;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.wyjson.module_common.route.service.user.PayService;
import com.wyjson.module_common.route.service.user.UserService;
import com.wyjson.router.model.Card;

/**
 * DO NOT EDIT THIS FILE!!! IT WAS GENERATED BY GOROUTER.
 */
public class GoRouterHelper {
    /**
     * 微信Pay服务
     * {@link com.wyjson.module_user.route.service.WechatPayServiceImpl}
     */
    @Nullable
    public static PayService getPayServiceForWechatPay() {
        return GoRouter.getInstance().getService(PayService.class, "WechatPay");
    }

    /**
     * AliPay服务
     * {@link com.wyjson.module_user.route.service.AliPayServiceImpl}
     */
    @Nullable
    public static PayService getPayServiceForAlipay() {
        return GoRouter.getInstance().getService(PayService.class, "Alipay");
    }

    /**
     * 用户服务
     * {@link com.wyjson.module_user.route.service.UserServiceImpl}
     */
    @Nullable
    public static UserService getUserService() {
        return GoRouter.getInstance().getService(UserService.class);
    }

    /**
     * 参数页面
     * {@link com.wyjson.module_user.activity.ParamActivity}
     */
    public static Card buildParamActivity(String nickname,
            com.wyjson.module_common.model.TestModel test) {
        return GoRouter.getInstance().build("/new/param/activity")
                // 昵称
                .withString("nickname", nickname)
                // 自定义类型
                .withObject("test", test);
    }

    /**
     * 参数页面
     * {@link com.wyjson.module_user.activity.ParamActivity}
     */
    public static void goParamActivity(Context context, String nickname,
            com.wyjson.module_common.model.TestModel test) {
        buildParamActivity(nickname, test).go(context);
    }

    /**
     * 参数页面
     * {@link com.wyjson.module_user.activity.ParamActivity}
     */
    public static Card buildParamActivity(String nickname,
            com.wyjson.module_common.model.TestModel test, int base, int age) {
        return GoRouter.getInstance().build("/new/param/activity")
                // 昵称
                .withString("nickname", nickname)
                // 自定义类型
                .withObject("test", test)
                // 我是一个父类字段
                .withInt("base", base)
                .withInt("age", age);
    }

    /**
     * 参数页面
     * {@link com.wyjson.module_user.activity.ParamActivity}
     */
    public static void goParamActivity(Context context, String nickname,
            com.wyjson.module_common.model.TestModel test, int base, int age) {
        buildParamActivity(nickname, test, base, age).go(context);
    }

    /**
     * 参数片段
     * {@link com.wyjson.module_user.fragment.ParamFragment}
     */
    public static Card buildParamFragment() {
        return GoRouter.getInstance().build("/new/param/fragment");
    }

    /**
     * 参数片段
     * {@link com.wyjson.module_user.fragment.ParamFragment}
     */
    public static Fragment goParamFragment(Context context) {
        return (Fragment) buildParamFragment().go(context);
    }

    /**
     * 参数片段
     * {@link com.wyjson.module_user.fragment.ParamFragment}
     */
    public static Card buildParamFragment(int age, String name) {
        return GoRouter.getInstance().build("/new/param/fragment")
                .withInt("age", age)
                .withString("name", name);
    }

    /**
     * 参数片段
     * {@link com.wyjson.module_user.fragment.ParamFragment}
     */
    public static Fragment goParamFragment(Context context, int age, String name) {
        return (Fragment) buildParamFragment(age, name).go(context);
    }

    /**
     * 主页
     * {@link com.wyjson.module_main.activity.MainActivity}
     */
    public static Card buildMainActivity() {
        return GoRouter.getInstance().build("/main/activity");
    }

    /**
     * 主页
     * {@link com.wyjson.module_main.activity.MainActivity}
     */
    public static void goMainActivity(Context context) {
        buildMainActivity().go(context);
    }

    /**
     * 事件页面
     * {@link com.wyjson.module_main.activity.EventActivity}
     */
    public static Card buildEventActivity() {
        return GoRouter.getInstance().build("/main/event/activity");
    }

    /**
     * 事件页面
     * {@link com.wyjson.module_main.activity.EventActivity}
     */
    public static void goEventActivity(Context context) {
        buildEventActivity().go(context);
    }

    /**
     * 事件片段
     * {@link com.wyjson.module_main.fragment.EventFragment}
     */
    public static Card buildEventFragment() {
        return GoRouter.getInstance().build("/main/event/fragment");
    }

    /**
     * 事件片段
     * {@link com.wyjson.module_main.fragment.EventFragment}
     */
    public static Fragment goEventFragment(Context context) {
        return (Fragment) buildEventFragment().go(context);
    }

    /**
     * 欢迎页
     * {@link com.wyjson.module_main.activity.SplashActivity}
     */
    public static Card buildSplashActivity() {
        return GoRouter.getInstance().build("/main/splash/activity");
    }

    /**
     * 欢迎页
     * {@link com.wyjson.module_main.activity.SplashActivity}
     */
    public static void goSplashActivity(Context context) {
        buildSplashActivity().go(context);
    }

    /**
     * 卡片片段
     * {@link com.wyjson.module_user.fragment.CardFragment}
     */
    public static Card buildCardFragment() {
        return GoRouter.getInstance().build("/user/card/fragment");
    }

    /**
     * 卡片片段
     * {@link com.wyjson.module_user.fragment.CardFragment}
     */
    public static Fragment goCardFragment(Context context) {
        return (Fragment) buildCardFragment().go(context);
    }

    /**
     * 用户信息页面
     * {@link com.wyjson.module_user.activity.UserInfoActivity}
     */
    public static Card buildUserInfoActivity() {
        return GoRouter.getInstance().build("/user/info/activity");
    }

    /**
     * 用户信息页面
     * {@link com.wyjson.module_user.activity.UserInfoActivity}
     */
    public static void goUserInfoActivity(Context context) {
        buildUserInfoActivity().go(context);
    }

    /**
     * 登录页面
     * {@link com.wyjson.module_user.activity.SignInActivity}
     */
    public static Card buildSignInActivity() {
        return GoRouter.getInstance().build("/user/sign_in/activity");
    }

    /**
     * 登录页面
     * {@link com.wyjson.module_user.activity.SignInActivity}
     */
    public static void goSignInActivity(Context context) {
        buildSignInActivity().go(context);
    }
}
