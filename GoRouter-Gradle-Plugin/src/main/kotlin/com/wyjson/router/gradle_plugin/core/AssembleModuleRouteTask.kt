package com.wyjson.router.gradle_plugin.core

import com.wyjson.router.gradle_plugin.utils.Constants
import com.wyjson.router.gradle_plugin.utils.Constants.INJECT_CLASS_NAME
import com.wyjson.router.gradle_plugin.utils.Constants.SCAN_TARGET_INJECT_PACKAGE_NAME
import com.wyjson.router.gradle_plugin.utils.Constants._CLASS
import com.wyjson.router.gradle_plugin.utils.Logger
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import kotlin.system.measureTimeMillis

abstract class AssembleModuleRouteTask : DefaultTask() {

    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

    @get:OutputFile
    abstract val output: RegularFileProperty

    private val moduleRouteClassList = ArrayList<String>()
    private var originInject: ByteArray? = null
    private var jarOutput: JarOutputStream? = null

    @TaskAction
    fun taskAction() {
        jarOutput = JarOutputStream(BufferedOutputStream(FileOutputStream(output.get().asFile)))
        val scanTimeCost = measureTimeMillis {
            scanFile()
        }
        Logger.i("Scan finish, current cost time ${scanTimeCost}ms")

        if (originInject == null) {
            Logger.e("Can not find GoRouter inject point, Do you import [GoRouter-Api]???")
            return
        }
        val injectCodeTimeCost = measureTimeMillis {
            injectCode()
        }
        Logger.i("Inject code finish, current cost time ${injectCodeTimeCost}ms")
    }

    private fun scanFile() {
        allDirectories.get().forEach { directory ->
//            Logger.i("Scan to directory [${directory.asFile.absolutePath}]")
            directory.asFile.walk().forEach { file ->
                if (file.isFile) {
                    if (file.name.endsWith(Constants.MODULE_ROUTE_NAME_SUFFIX)) {
                        Logger.i("Scan to class [${file.name}]  be from directory [${directory.asFile.absolutePath}]")
                        moduleRouteClassList.add(file.name)
                    }
                    val relativePath = directory.asFile.toURI().relativize(file.toURI()).path
//                    Logger.i("Scan the classes in the directory [${relativePath.replace(File.separatorChar, '.')}]")
                    jarOutput!!.putNextEntry(JarEntry(relativePath.replace(File.separatorChar, '/')))
                    file.inputStream().use { inputStream ->
                        inputStream.copyTo(jarOutput!!)
                    }
                    jarOutput!!.closeEntry()
                }
            }
        }

        val tempList = ArrayList<String>()
        allJars.get().forEach { file ->
            tempList.clear()
//            Logger.i("Scan to jar [${file.asFile.absolutePath}]")
            val jarFile = JarFile(file.asFile)
            val enumeration = jarFile.entries()
            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement()
                try {
                    val entryName = jarEntry.name
//                    Logger.i("Scan the classes in the jar [$entryName]")
                    if (jarEntry.isDirectory || jarEntry.name.isEmpty() || !jarEntry.name.endsWith(".class") || jarEntry.name.contains("META-INF/")) {
                        continue
                    }
                    if (Constants.dotToSlash(INJECT_CLASS_NAME) + _CLASS == entryName) {
                        Logger.i("Find the inject class [$entryName]")
                        jarFile.getInputStream(jarEntry).use { inputs ->
                            originInject = inputs.readAllBytes()
                        }
                    } else {
                        val startsWith = entryName.startsWith(Constants.dotToSlash(SCAN_TARGET_INJECT_PACKAGE_NAME))
                        if (startsWith) {
                            val className = entryName.substring(entryName.lastIndexOf("/") + 1)
                            Logger.i("Scan to class [$className] be from jar [${file.asFile.absolutePath}]")
                            if (className.isNotEmpty()) {
                                tempList.add(className)
                            }
                        }
                        jarOutput!!.putNextEntry(JarEntry(jarEntry.name))
                        jarFile.getInputStream(jarEntry).use {
                            it.copyTo(jarOutput!!)
                        }
                        jarOutput!!.closeEntry()
                    }
                } catch (e: Exception) {
                    Logger.w("Merge jar error entry:[${jarEntry.name}], error message:$e")
                }
            }
            moduleRouteClassList.addAll(tempList)
            jarFile.close()
        }
    }

    private fun injectCode() {
        val resultByteArray = AssembleModuleRouteCodeInjector(moduleRouteClassList).execute(ByteArrayInputStream(originInject))
        jarOutput!!.putNextEntry(JarEntry(Constants.dotToSlash(INJECT_CLASS_NAME) + _CLASS))
        ByteArrayInputStream(resultByteArray).use {
            it.copyTo(jarOutput!!)
        }
        jarOutput!!.closeEntry()
        jarOutput!!.close()
    }
}