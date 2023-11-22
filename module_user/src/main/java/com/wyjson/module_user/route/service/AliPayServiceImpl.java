package com.wyjson.module_user.route.service;

import com.wyjson.module_common.route.service.user.PayService;
import com.wyjson.router.annotation.Service;

@Service(alias = "Alipay", remark = "AliPay服务")
public class AliPayServiceImpl implements PayService {
    @Override
    public void init() {

    }

    @Override
    public String getPayType() {
        return "AliPay";
    }
}
