package com.wyjson.router.gradle_plugin.doc.model

data class ParamModel(
    val name: String,
    val type: String,
    val required: Boolean,
    val remark: String?
)