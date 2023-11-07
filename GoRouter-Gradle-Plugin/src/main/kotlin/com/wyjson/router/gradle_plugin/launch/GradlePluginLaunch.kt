package com.wyjson.router.gradle_plugin.launch

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppPlugin
import com.wyjson.router.gradle_plugin.config.GoRouterConfig
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
        project.extensions.add("GoRouter", GoRouterConfig::class.java)
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            // 处理允许执行自动注册任务的集合,未设置表示全部任务都可执行
            val goRouterConfig = project.extensions.getByType(GoRouterConfig::class.java)
            var isRunTask = false;
            if (goRouterConfig.buildTypes.isNotEmpty()) {
                for (buildType in goRouterConfig.buildTypes) {
                    if (buildType.equals(variant.name, true)) {
                        isRunTask = true;
                        break
                    }
                }
            } else {
                isRunTask = true
            }
            if (isRunTask) {
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
}