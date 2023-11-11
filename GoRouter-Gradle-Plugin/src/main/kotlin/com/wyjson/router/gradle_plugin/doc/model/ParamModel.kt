package com.wyjson.router.gradle_plugin.doc.model

data class ParamModel(
    var name: String?,
    var type: String?,
    var isRequired: Boolean,
    var remark: String?
)