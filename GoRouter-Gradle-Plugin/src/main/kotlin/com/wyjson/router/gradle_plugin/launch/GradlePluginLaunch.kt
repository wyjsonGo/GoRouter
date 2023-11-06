package com.wyjson.router.gradle_plugin.launch

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppPlugin
import com.wyjson.router.gradle_plugin.core.AssembleModuleRouteTask
import com.wyjson.router.gradle_plugin.utils.Logger
import org.gradle.api.Plugin
import org.gradle.api.Project

class GradlePluginLaunch : Plugin<Project> {

    override fun apply(project: Project) {
        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        if (!isApp) {
            Logger.e("Plugin ['com.wyjson.Gorouter'] can only be used under the application, not under the module library invalid!")
            return
        }

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            val task = project.tasks.register("${variant.name}AssembleModuleRouteTask", AssembleModuleRouteTask::class.java)
            variant.artifacts
                    .forScope(ScopedArtifacts.Scope.ALL)
                    .use(task)
                    .toTransform(
                            ScopedArtifact.CLASSES,
                            AssembleModuleRouteTask::allJars,
                            AssembleModuleRouteTask::allDirectories,
                            AssembleModuleRouteTask::output
                    )

        }
    }
}