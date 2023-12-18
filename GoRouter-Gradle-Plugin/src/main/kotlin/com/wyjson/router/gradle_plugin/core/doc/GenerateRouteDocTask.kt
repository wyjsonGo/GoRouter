package com.wyjson.router.gradle_plugin.core.doc

import com.android.build.api.variant.Variant
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.wyjson.router.gradle_plugin.core.doc.model.DocumentModel
import com.wyjson.router.gradle_plugin.utils.Constants
import com.wyjson.router.gradle_plugin.utils.Constants.GENERATE_ROUTE_DOC
import com.wyjson.router.gradle_plugin.utils.Logger
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateRouteDocTask : DefaultTask() {

    init {
        group = Constants.PROJECT
    }

    @get:Input
    abstract var variant: Variant

    private val TAG = "Doc"

    private var dependModeList: ArrayList<String> = ArrayList()
    private var document: DocumentModel? = null

    @TaskAction
    fun taskAction() {
        Logger.i(TAG, "Generate GoRouter document task start.")
        val variantName = variant.name
        val buildType = variant.buildType
        val flavorName = variant.flavorName
        setDependModeList(variantName, buildType, flavorName)

        if (!scanRouteModule(variantName, buildType)) return
        val json = GsonBuilder().setPrettyPrinting().create().toJson(document)

        val documentName = "${project.rootProject.name}-${variantName}-${Constants.DOCUMENT_FILE_NAME}"
        val docOutFilePath = "${project.projectDir}/${documentName}"
        File(docOutFilePath).writeText(json, Charsets.UTF_8)
        Logger.i(TAG, "Generate GoRouter document[${documentName}] task end. $docOutFilePath")
    }

    private fun scanRouteModule(variantName: String, buildType: String?): Boolean {
        project.dependProject().plus(project).forEach { curProject ->
            var file = searchJSONFile(curProject, variantName)
            if (file == null && buildType != null) {
                file = searchJSONFile(curProject, buildType)
            }
            if (file != null) {
                mergeRouteModuleDoc(curProject, file)
            }
        }
        if (document == null) {
            Logger.e(TAG, "Failed to generate the route document!")
            return false
        }
        return true
    }

    private fun searchJSONFile(curProject: Project, name: String): File? {
        var genFile = curProject.file("${curProject.buildDir}/generated/ap_generated_sources/${name}").listFiles()
        var collection = curProject.files(genFile).asFileTree.filter { it.name.endsWith(Constants.DOCUMENT_FILE_NAME) }
        if (collection.isEmpty) {
            genFile = curProject.file("${curProject.buildDir}/generated/source/kapt/${name}").listFiles()
            collection = curProject.files(genFile).asFileTree.filter { it.name.endsWith(Constants.DOCUMENT_FILE_NAME) }
        }
        if (collection.isEmpty) {
            Logger.w(TAG, "project[${curProject.name}] scan 0 route document.")
            return null
        } else {
            val file = collection.first()
            Logger.i(TAG, "project[${curProject.name}] found the file[${file.name}].")
            return file
        }
    }

    private fun mergeRouteModuleDoc(curProject: Project, file: File) {
        if (file.readLines().isNotEmpty()) {
            try {
                val documentModel = Gson().fromJson(file.readText(), DocumentModel::class.java)
                if (document == null) {
                    document = documentModel
                } else {
                    document!!.services.putAll(documentModel.services)
                    document!!.interceptors.addAll(documentModel.interceptors)
                    document!!.routes.putAll(documentModel.routes)
                }
                document!!.interceptors.sortBy { interceptor -> interceptor.ordinal }
            } catch (e: Exception) {
                Logger.e(TAG, "module[${curProject.name}] route document parsing failed, do not modify the generated route file, use the '${GENERATE_ROUTE_DOC}' task to generate a new route document.")
            }
        } else {
            Logger.e(TAG, "module[${curProject.name}] route document content is empty and a new route document is generated using the '${GENERATE_ROUTE_DOC}' task.")
        }
    }

    private fun setDependModeList(variantName: String, buildType: String?, flavorName: String?) {
        dependModeList.clear()
        dependModeList.add("api")
        dependModeList.add("implementation")
        dependModeList.add("${variantName}Api")
        dependModeList.add("${variantName}Implementation")
        if (variantName != buildType) {
            if (buildType != null) {
                dependModeList.add("${buildType}Api")
                dependModeList.add("${buildType}Implementation")
            }
            if (flavorName != null) {
                dependModeList.add("${flavorName}Api")
                dependModeList.add("${flavorName}Implementation")
            }
        }
    }

    /**
     * 查询项目下的依赖项目,递归子项目
     */
    private fun Project.dependProject(): List<Project> {
        val projects = ArrayList<Project>()
        dependModeList.forEach { name ->
            try {
                val dependencyProjects = configurations.getByName(name).dependencies
                    .filterIsInstance<DefaultProjectDependency>()
                    .filter { it.dependencyProject.isAndroid() }
                    .map { it.dependencyProject }
                projects.addAll(dependencyProjects)
                dependencyProjects.forEach { projects.addAll(it.dependProject()) }
            } catch (_: UnknownConfigurationException) {
            }
        }
        return projects.distinct()
    }

    private fun Project.isAndroid() = plugins.hasPlugin("com.android.application") || plugins.hasPlugin("com.android.library")

}