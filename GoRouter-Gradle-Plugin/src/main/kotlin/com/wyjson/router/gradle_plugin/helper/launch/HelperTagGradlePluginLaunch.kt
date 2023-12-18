package com.wyjson.router.gradle_plugin.helper.launch

import com.android.build.api.variant.AndroidComponentsExtension
import com.wyjson.router.gradle_plugin.helper.tag.GenerateGoRouterHelperTagTask
import com.wyjson.router.gradle_plugin.utils.Constants.GENERATE_GOROUTER_HELPER_TODO
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized

class HelperTagGradlePluginLaunch : Plugin<Project> {

    override fun apply(project: Project) {
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            val variantName = variant.name
            val variantNameCapitalized = variantName.capitalized()

            variant.sources.java?.let {
                val path = "build/generated/source/gorouter/main"
                variant.sources.java?.addStaticSourceDirectory(path)
                val rhTODOTask = project.tasks.register("${GENERATE_GOROUTER_HELPER_TODO}$variantNameCapitalized", GenerateGoRouterHelperTagTask::class.java)
                variant.sources.java?.addGeneratedSourceDirectory(rhTODOTask, GenerateGoRouterHelperTagTask::outputFolder)
            }
        }
    }
}
