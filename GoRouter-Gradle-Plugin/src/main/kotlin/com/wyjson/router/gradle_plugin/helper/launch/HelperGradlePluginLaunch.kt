package com.wyjson.router.gradle_plugin.helper.launch

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.wyjson.router.gradle_plugin.helper.core.GenerateGoRouterHelperTask
import com.wyjson.router.gradle_plugin.utils.Constants
import com.wyjson.router.gradle_plugin.utils.Constants.HELPER_PACKAGE_NAME
import com.wyjson.router.gradle_plugin.utils.Logger
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized

class HelperGradlePluginLaunch : Plugin<Project> {

    private val TAG = "HelperLaunch"

    private val variantList: ArrayList<Variant> = ArrayList()

    override fun apply(project: Project) {
        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        if (!isApp) {
            Logger.e(TAG, "Plugin ['${HELPER_PACKAGE_NAME}'] can only be used under the application, not under the module library invalid!")
            return
        }
        variantList.clear()
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            variantList.add(variant)
            val variantName = variant.name
            val variantNameCapitalized = variantName.capitalized()

            project.tasks.register("${Constants.GENERATE_GOROUTER_HELPER}$variantNameCapitalized", GenerateGoRouterHelperTask::class.java) {
                it.variant = variant
            }.dependsOn("assemble${variantNameCapitalized}")
        }
        project.afterEvaluate {
            for (variant in variantList) {
                val variantName = variant.name
                val variantNameCapitalized = variantName.capitalized()
                project.tasks.findByName("assemble${variantNameCapitalized}")?.finalizedBy("${Constants.GENERATE_GOROUTER_HELPER}$variantNameCapitalized")
            }
        }
    }
}
