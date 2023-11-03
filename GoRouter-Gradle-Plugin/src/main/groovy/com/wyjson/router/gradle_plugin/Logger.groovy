package com.wyjson.router.gradle_plugin

import org.gradle.api.Project

class Logger {
    static org.gradle.api.logging.Logger logger

    static void make(Project project) {
        logger = project.getLogger()
    }

    static void i(String info) {
        if (null != info && null != logger) {
            logger.info("GoRouter::Gradle-Plugin >>> " + info)
        }
    }

    static void e(String error) {
        if (null != error && null != logger) {
            logger.error("GoRouter::Gradle-Plugin >>> " + error)
        }
    }

    static void w(String warning) {
        if (null != warning && null != logger) {
            logger.warn("GoRouter::Gradle-Plugin >>> " + warning)
        }
    }
}
