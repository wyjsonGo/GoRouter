package com.wyjson.router.gradle_plugin.helper

import com.google.gson.Gson
import com.wyjson.router.gradle_plugin.helper.model.RouteHelperModel
import com.wyjson.router.gradle_plugin.utils.Constants
import com.wyjson.router.gradle_plugin.utils.Constants.GOROUTER_HELPER_CLASS_NAME
import com.wyjson.router.gradle_plugin.utils.Logger
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateGoRouterHelperTask : DefaultTask() {

    private val TAG = "RH"

    @get:Input
    var variantName: String? = null

    @get:OutputDirectory
    abstract val outputFolder: DirectoryProperty

    private var routeHelperModel: RouteHelperModel? = null

    @TaskAction
    fun taskAction() {
        if (!scanRouteModule()) return
        val className = GOROUTER_HELPER_CLASS_NAME;
        val outputFile = File(
            project.project(":module_common").projectDir,
            "/src/${variantName}/java/com/wyjson/router/${className}.java"
        )
        outputFile.parentFile.mkdirs()
        outputFile.writeText(AssembleGoRouteHelperCode(routeHelperModel!!).toJavaCode(className), Charsets.UTF_8)
    }

    private fun scanRouteModule(): Boolean {
        project.dependProject().plus(project).forEach { curProject ->
            val genFile = curProject.file("${curProject.buildDir}/generated/ap_generated_sources/${variantName}").listFiles()
            val collection = curProject.files(genFile).asFileTree.filter {
                it.name.endsWith(Constants.DOCUMENT_FILE_NAME)
            }
            if (collection.isEmpty) {
                Logger.w(TAG, "project[${curProject.name}] scan 0 route.")
            } else {
                val file = collection.first()
                Logger.i(TAG, "project[${curProject.name}] found the file[${file.name}].")
                mergeRouteModule(curProject, file)
            }
        }
        if (routeHelperModel == null) {
            Logger.e(TAG, "Failed to generate the route!")
            return false
        }
        return true
    }

    private fun mergeRouteModule(curProject: Project, file: File) {
        if (file.readLines().isNotEmpty()) {
            try {
                val model = Gson().fromJson(file.readLines()[0], RouteHelperModel::class.java);
                if (routeHelperModel == null) {
                    routeHelperModel = model;
                } else {
                    routeHelperModel!!.services.putAll(model.services)
                    routeHelperModel!!.routes.putAll(model.routes)
                }
            } catch (e: Exception) {
                Logger.e(
                    TAG,
                    "module[${curProject.name}] route document parsing failed, do not modify the generated route file, use the '${Constants.GENERATE_ROUTE_DOC}' task to generate a new route document."
                )
            }
        } else {
            Logger.e(
                TAG,
                "module[${curProject.name}] route document content is empty and a new route document is generated using the '${Constants.GENERATE_ROUTE_DOC}' task."
            )
        }
    }

    /**
     * 查询项目下的依赖项目,递归子项目
     */
    private fun Project.dependProject(): List<Project> {
        val projects = ArrayList<Project>()
        arrayOf("api", "implementation").forEach { name ->
            val dependencyProjects = configurations.getByName(name).dependencies
                .filterIsInstance<DefaultProjectDependency>()
                .filter { it.dependencyProject.isAndroid() }
                .map { it.dependencyProject }
            projects.addAll(dependencyProjects)
            dependencyProjects.forEach { projects.addAll(it.dependProject()) }
        }
        return projects.distinct()
    }

    private fun Project.isAndroid() =
        plugins.hasPlugin("com.android.application") || plugins.hasPlugin("com.android.library")


}