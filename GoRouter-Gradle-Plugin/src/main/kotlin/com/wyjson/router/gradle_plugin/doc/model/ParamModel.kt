package com.wyjson.router.gradle_plugin.doc.model

data class ParamModel(
    var name: String?,
    var type: String?,
    var required: Boolean,
    var remark: String?
)