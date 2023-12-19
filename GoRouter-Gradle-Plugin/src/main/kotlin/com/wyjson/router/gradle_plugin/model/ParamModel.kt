package com.wyjson.router.gradle_plugin.model

data class ParamModel(
    val name: String,
    val type: String,
    val intentType: String,
    val required: Boolean,
    val remark: String?
)