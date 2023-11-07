package com.wyjson.router.gradle_plugin.utils

import com.wyjson.router.gradle_plugin.utils.Constants.PROJECT

object Logger {

    fun i(info: String) {
        println("[info] ${PROJECT}::Gradle-Plugin >>> $info")
    }

    fun w(info: String) {
        println("[warning] ${PROJECT}::Gradle-Plugin >>> $info")
    }

    fun e(info: String) {
        error("[error] ${PROJECT}::Gradle-Plugin >>> $info")
    }

}