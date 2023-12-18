package com.wyjson.router.gradle_plugin.model

data class InterceptorModel(
    val ordinal: Int,
    val remark: String?,
    val moduleName: String,
    val className: String
)