package com.wyjson.module_user.route.service;

import com.wyjson.module_common.route.service.user.PayService;
import com.wyjson.router.annotation.Service;

@Service(alias = "WechatPay", remark = "微信Pay服务")
public class WechatPayServiceImpl implements PayService {
    @Override
    public void init() {

    }

    @Override
    public String getPayType() {
        return "WechatPay";
    }
}
