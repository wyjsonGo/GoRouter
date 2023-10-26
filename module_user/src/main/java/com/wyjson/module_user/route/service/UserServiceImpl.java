package com.wyjson.module_user.route.service;

import com.wyjson.module_common.route.service.user.UserService;
import com.wyjson.router.annotation.Service;

@Service(remark = "用户服务")
public class UserServiceImpl implements UserService {
    @Override
    public void init() {

    }

    @Override
    public long getUserId() {
        return 123;
    }
}
