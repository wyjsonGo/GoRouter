package com.wyjson.router.gradle_plugin.core.doc.model

data class ParamModel(
    val name: String,
    val type: String,
    val required: Boolean,
    val remark: String?
)