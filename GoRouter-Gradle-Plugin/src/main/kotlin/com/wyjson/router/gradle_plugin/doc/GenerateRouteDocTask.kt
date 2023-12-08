package com.wyjson.router.gradle_plugin.doc

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.wyjson.router.gradle_plugin.doc.model.DocumentModel
import com.wyjson.router.gradle_plugin.utils.Constants
import com.wyjson.router.gradle_plugin.utils.Constants.GENERATE_ROUTE_DOC_TASK_NAME
import com.wyjson.router.gradle_plugin.utils.Logger
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateRouteDocTask : DefaultTask() {

    private val TAG = "Doc"

    init {
        group = Constants.PROJECT
    }

    private val documentName = "${project.rootProject.name}-${Constants.DOCUMENT_FILE_NAME}"
    private val docOutFilePath = "${project.projectDir}/${documentName}"
    private var document: DocumentModel? = null

    @TaskAction
    fun taskAction() {
        Logger.i(TAG, "Generate GoRouter document task start.")
        project.dependProject().plus(project).forEach { curProject ->
            val genFile = curProject.file("${curProject.buildDir}/generated").listFiles()
            val collection = curProject.files(genFile).asFileTree.filter { it.name.endsWith(Constants.DOCUMENT_FILE_NAME) }
            if (collection.isEmpty) {
                Logger.w(TAG, "project[${curProject.name}] scan 0 route document.")
            } else {
                val file = collection.first()
                Logger.i(TAG, "project[${curProject.name}] found the file[${file.name}].")
                mergeRouteModuleDoc(curProject, file)
            }
        }
        if (document == null) {
            if (GENERATE_ROUTE_DOC_TASK_NAME == name) {
                Logger.e(TAG, "Failed to generate the route document!")
            } else {
                Logger.e(TAG, "Failed to generate the route document! Use the '${GENERATE_ROUTE_DOC_TASK_NAME}' task to generate a new route document.")
            }
            return
        }
        val json = GsonBuilder().setPrettyPrinting().create().toJson(document)
        File(docOutFilePath).writeText(json, Charsets.UTF_8)
        Logger.i(TAG, "Generate GoRouter document task end.")
        Logger.i(TAG, "Success! route document name[${documentName}] $docOutFilePath")
    }

    private fun mergeRouteModuleDoc(curProject: Project, file: File) {
        if (file.readLines().isNotEmpty()) {
            try {
                val documentModel = Gson().fromJson(file.readLines()[0], DocumentModel::class.java);
                if (document == null) {
                    document = documentModel;
                } else {
                    document!!.services.putAll(documentModel.services)
                    document!!.interceptors.addAll(documentModel.interceptors)
                    document!!.routes.putAll(documentModel.routes)
                }
                document!!.interceptors.sortBy { interceptor -> interceptor.ordinal }
            } catch (e: Exception) {
                Logger.e(TAG, "module[${curProject.name}] route document parsing failed, do not modify the generated route file, use the '${GENERATE_ROUTE_DOC_TASK_NAME}' task to generate a new route document.")
            }
        } else {
            Logger.e(TAG, "module[${curProject.name}] route document content is empty and a new route document is generated using the '${GENERATE_ROUTE_DOC_TASK_NAME}' task.")
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

    private fun Project.isAndroid() = plugins.hasPlugin("com.android.application") || plugins.hasPlugin("com.android.library")

}