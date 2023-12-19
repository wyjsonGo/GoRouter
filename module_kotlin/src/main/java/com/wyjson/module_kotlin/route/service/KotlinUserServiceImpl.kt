package com.wyjson.module_kotlin.route.service

import com.wyjson.module_common.route.service.user.UserService
import com.wyjson.router.annotation.Service

@Service(remark = "Kotlin模块用户服务", alias = "Kotlin")
class KotlinUserServiceImpl : UserService {
    override fun init() {
    }

    override fun getUserId(): Long {
        return 789
    }
}