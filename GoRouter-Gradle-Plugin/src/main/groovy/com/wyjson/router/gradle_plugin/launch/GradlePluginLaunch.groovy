package com.wyjson.router.gradle_plugin.launch

import com.android.build.api.variant.AndroidComponentsExtension
import com.wyjson.router.gradle_plugin.Logger
import org.gradle.api.Plugin
import org.gradle.api.Project

class GradlePluginLaunch implements Plugin<Project> {
    @Override
    void apply(Project project) {
        Logger.make(project)
        Logger.i('Project enable gradle plugin')
        def android = project.extensions.getByType(AndroidComponentsExtension)
    }
}
