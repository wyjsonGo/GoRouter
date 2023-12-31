package com.wyjson.router.helper.module_user.service;

import androidx.annotation.Nullable;
import com.wyjson.module_common.route.service.user.PayService;
import com.wyjson.router.GoRouter;

/**
 * DO NOT EDIT THIS FILE!!! IT WAS GENERATED BY GOROUTER.
 * 微信Pay服务
 * {@link com.wyjson.module_user.route.service.WechatPayServiceImpl}
 */
public class PayServiceForWechatPayGoRouter {
    @Nullable
    public static PayService get() {
        return GoRouter.getInstance().getService(PayService.class, "WechatPay");
    }
}
