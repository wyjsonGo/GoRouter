package com.wyjson.router.gradle_plugin.doc.model

data class RouteModel(
    var path: String?,
    var remark: String?,
    var type: String?,
    var pathClass: String?,
    var tag: Int?,
    var paramsType: List<ParamModel>?
)