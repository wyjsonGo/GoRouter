package com.wyjson.router.gradle_plugin.utils

import com.wyjson.router.gradle_plugin.utils.Constants.PROJECT

object Logger {

    fun i(tag: String, info: String) {
        println("[info] ${PROJECT}::Gradle-Plugin >>> TAG:$tag $info")
    }

    fun w(tag: String, info: String) {
        println("[warning] ${PROJECT}::Gradle-Plugin >>> TAG:$tag $info")
    }

    fun e(tag: String, info: String) {
        error("[error] ${PROJECT}::Gradle-Plugin >>> TAG:$tag $info")
    }

}