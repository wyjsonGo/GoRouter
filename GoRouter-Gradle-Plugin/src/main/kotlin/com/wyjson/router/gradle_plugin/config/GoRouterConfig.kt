package com.wyjson.router.gradle_plugin.config

open class GoRouterConfig {
    // 允许执行自动注册任务的集合，最好不要写debug,以节省开发阶段build时间
    var runAutoRegisterBuildTypes: Array<String> = emptyArray()
}