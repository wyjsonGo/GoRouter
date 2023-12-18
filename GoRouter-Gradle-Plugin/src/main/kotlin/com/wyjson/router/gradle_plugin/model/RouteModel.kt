package com.wyjson.router.gradle_plugin.model

data class RouteModel(
    val path: String,
    val remark: String?,
    val type: String,
    val pathClass: String,
    val tag: Int?,
    val paramsType: List<ParamModel>?
)