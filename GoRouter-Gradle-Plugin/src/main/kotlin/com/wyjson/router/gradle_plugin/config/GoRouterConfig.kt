package com.wyjson.router.gradle_plugin.config

open class GoRouterConfig {
    // 允许执行自动注册任务的集合，最好不要写debug,以节省开发阶段build时间
    var runAutoRegisterBuildTypes: Array<String> = emptyArray()
    // 指定根模块项目名称，开启自动生成路由帮助类功能
    var helperToRootModuleName: String = ""
}