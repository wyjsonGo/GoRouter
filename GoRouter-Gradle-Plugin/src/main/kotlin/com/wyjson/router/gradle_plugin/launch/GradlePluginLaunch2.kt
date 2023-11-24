package com.wyjson.router.gradle_plugin.launch

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.wyjson.router.gradle_plugin.core.route2.AssembleRouteModuleTask2
import com.wyjson.router.gradle_plugin.doc.GenerateRouteDocTask
import com.wyjson.router.gradle_plugin.utils.Constants.GENERATE_ROUTE_DOC_TASK_NAME
import com.wyjson.router.gradle_plugin.utils.Constants.QUICK_GENERATE_ROUTE_DOC_TASK_NAME
import com.wyjson.router.gradle_plugin.utils.Logger
import org.gradle.api.Plugin
import org.gradle.api.Project

class GradlePluginLaunch2 : Plugin<Project> {

    private val TAG = "Launch2"

    override fun apply(project: Project) {
        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        if (!isApp) {
            Logger.e(TAG, "Plugin ['com.wyjson.Gorouter'] can only be used under the application, not under the module library invalid!")
            return
        }
        project.tasks.register(GENERATE_ROUTE_DOC_TASK_NAME, GenerateRouteDocTask::class.java).dependsOn("build")
        project.tasks.register(QUICK_GENERATE_ROUTE_DOC_TASK_NAME, GenerateRouteDocTask::class.java)

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            variant.instrumentation.transformClassesWith(
                AssembleRouteModuleTask2::class.java,
                InstrumentationScope.ALL
            ) {}
            variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS)
        }
    }
}