package com.wyjson.router.gradle_plugin.helper.tag

import com.google.gson.Gson
import com.wyjson.router.gradle_plugin.model.RouteHelperModel
import com.wyjson.router.gradle_plugin.utils.Constants
import com.wyjson.router.gradle_plugin.utils.Constants.GOROUTER_HELPER_CLASS_NAME
import com.wyjson.router.gradle_plugin.utils.Logger
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateGoRouterHelperTagTask : DefaultTask() {

    init {
        group = Constants.PROJECT
    }

    @get:OutputDirectory
    abstract val outputFolder: DirectoryProperty

    private val TAG = "RH(TAG)"

    private var routeHelperModel: RouteHelperModel? = null

    private val catalog: String = "main"

    @TaskAction
    fun taskAction() {
        Logger.i(TAG, "GoRouterHelper(TAG) task start.")

        if (!searchJSONFile()){
            Logger.w(TAG, "GoRouterHelper(TAG) task end.")
            return
        }
        if (routeHelperModel != null) {
            val className = GOROUTER_HELPER_CLASS_NAME
            val dir = project.buildDir
            val path = "/generated/source/gorouter/${catalog}/com/wyjson/router/${className}.java"
            val outputFile = File(dir, path)
            outputFile.parentFile.mkdirs()
            outputFile.writeText(AssembleGoRouteHelperTagCode(routeHelperModel!!).toJavaCode(className), Charsets.UTF_8)
            Logger.i(TAG, "GoRouterHelper(TAG) task end. ${dir}${path}")
        } else {
            Logger.w(TAG, "GoRouterHelper(TAG) task end.")
        }
    }

    private fun searchJSONFile(): Boolean {
        val genFile = project.file("${project.projectDir}/gorouter").listFiles()
        val collection = project.files(genFile).asFileTree.filter { it.name.endsWith(Constants.HELPER_JSON_FILE_NAME) }
        if (collection.isEmpty) {
            Logger.w(TAG, "0 files were scanned under the root project path[${project.name}/gorouter]")
            return false
        } else {
            collection.forEach {
                mergeRouteModule(it)
            }
            return true
        }
    }

    private fun mergeRouteModule(file: File) {
        if (file.readLines().isNotEmpty()) {
            try {
                val model = Gson().fromJson(file.readText(), RouteHelperModel::class.java)
                if (routeHelperModel == null) {
                    routeHelperModel = model
                } else {
                    routeHelperModel!!.services.putAll(model.services)
                    routeHelperModel!!.routes.putAll(model.routes)
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Module[${project.name}] route json parsing failed, do not modify the generated route json.")
            }
        } else {
            Logger.e(TAG, "Module[${project.name}] route json content is empty.")
        }
    }


}