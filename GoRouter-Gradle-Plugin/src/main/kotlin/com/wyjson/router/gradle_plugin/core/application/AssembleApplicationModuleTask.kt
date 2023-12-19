package com.wyjson.router.gradle_plugin.core.application

import com.wyjson.router.gradle_plugin.utils.Constants
import com.wyjson.router.gradle_plugin.utils.Constants.APPLICATION_MODULE_NAME_SUFFIX
import com.wyjson.router.gradle_plugin.utils.Constants.APPLICATION_MODULE_INJECT_CLASS_NAME
import com.wyjson.router.gradle_plugin.utils.Constants.APPLICATION_MODULE_SCAN_TARGET_INJECT_PACKAGE_NAME
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
import java.util.zip.ZipException
import kotlin.system.measureTimeMillis

abstract class AssembleApplicationModuleTask : DefaultTask() {

    init {
        group = Constants.PROJECT_OTHER
    }

    private val TAG = "AM"

    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

    @get:OutputFile
    abstract val output: RegularFileProperty

    private val applicationModuleClassList = ArrayList<String>()
    private var originInject: ByteArray? = null
    private var jarOutput: JarOutputStream? = null

    @TaskAction
    fun taskAction() {
        jarOutput = JarOutputStream(BufferedOutputStream(FileOutputStream(output.get().asFile)))
        val scanTimeCost = measureTimeMillis {
            scanFile()
        }
        Logger.i(TAG, "Scan finish, current cost time ${scanTimeCost}ms")

        if (originInject == null) {
            Logger.e(TAG, "Can not find GoRouter inject point, Do you import [GoRouter-Api]???")
            return
        }
        val injectCodeTimeCost = measureTimeMillis {
            injectCode()
        }
        Logger.i(TAG, "Inject code finish, current cost time ${injectCodeTimeCost}ms")
    }

    private fun scanFile() {
        allDirectories.get().forEach { directory ->
//            Logger.i("Scan to directory [${directory.asFile.absolutePath}]")
            directory.asFile.walk().forEach { file ->
                if (file.isFile) {
                    if (file.name.endsWith(APPLICATION_MODULE_NAME_SUFFIX + _CLASS)) {
                        Logger.i(TAG, "Scan to class [${file.name}] be from directory [${directory.asFile.absolutePath}]")
                        applicationModuleClassList.add(file.name)
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
                    if (jarEntry.isDirectory || jarEntry.name.isEmpty()) {
                        continue
                    }
                    if (Constants.dotToSlash(APPLICATION_MODULE_INJECT_CLASS_NAME) + _CLASS == entryName) {
                        Logger.i(TAG, "Find the inject class [$entryName]")
                        jarFile.getInputStream(jarEntry).use { inputs ->
                            originInject = inputs.readAllBytes()
                        }
                    } else {
                        val startsWith = entryName.startsWith(Constants.dotToSlash(APPLICATION_MODULE_SCAN_TARGET_INJECT_PACKAGE_NAME))
                        val endsWith = entryName.endsWith(Constants.dotToSlash(APPLICATION_MODULE_NAME_SUFFIX) + _CLASS)
                        if (startsWith && endsWith) {
                            val className = entryName.substring(entryName.lastIndexOf("/") + 1)
                            Logger.i(TAG, "Scan to class [$className] be from jar [${file.asFile.absolutePath}]")
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
                    if (!(e is ZipException && e.message?.startsWith("duplicate entry:") == true)) {
                        Logger.w(TAG, "Merge jar error entry:[${jarEntry.name}], error message:$e")
                    }
                }
            }
            applicationModuleClassList.addAll(tempList)
            jarFile.close()
        }
    }

    private fun injectCode() {
        val resultByteArray = AssembleApplicationModuleCodeInjector(applicationModuleClassList).execute(ByteArrayInputStream(originInject))
        jarOutput!!.putNextEntry(JarEntry(Constants.dotToSlash(APPLICATION_MODULE_INJECT_CLASS_NAME) + _CLASS))
        ByteArrayInputStream(resultByteArray).use {
            it.copyTo(jarOutput!!)
        }
        jarOutput!!.closeEntry()
        jarOutput!!.close()
    }
}