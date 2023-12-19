package com.wyjson.router.gradle_plugin.core.route

import com.wyjson.router.gradle_plugin.utils.Constants
import com.wyjson.router.gradle_plugin.utils.Constants.ROUTE_MODULE_INJECT_CLASS_NAME
import com.wyjson.router.gradle_plugin.utils.Constants.ROUTE_MODULE_INJECT_METHOD_NAME
import com.wyjson.router.gradle_plugin.utils.Constants.ROUTE_MODULE_INJECT_TARGET_METHOD_NAME
import com.wyjson.router.gradle_plugin.utils.Constants.ROUTE_MODULE_SCAN_TARGET_INJECT_PACKAGE_NAME
import com.wyjson.router.gradle_plugin.utils.Constants._CLASS
import com.wyjson.router.gradle_plugin.utils.Logger
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter
import java.io.InputStream

class AssembleRouteModuleCodeInjector(val routeModuleClassList: List<String>) {

    private val TAG = "RM"

    fun execute(inputStream: InputStream): ByteArray {
        Logger.i(TAG, "Start execute ASM method")
        val classReader = ClassReader(inputStream)
        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
        val classVisitor = AssembleRouteModuleClassVisitor(classWriter)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }

    inner class AssembleRouteModuleClassVisitor(private val mClassVisitor: ClassVisitor)
        : ClassVisitor(Opcodes.ASM9, mClassVisitor) {

        override fun visitMethod(
                access: Int,
                name: String?,
                desc: String?,
                signature: String?,
                exception: Array<out String>?
        ): MethodVisitor {
            var mv = mClassVisitor.visitMethod(access, name, desc, signature, exception)
            if (ROUTE_MODULE_INJECT_METHOD_NAME == name) {
                mv = AssembleRouteModuleMethodAdapter(mv, access, name, desc)
            }
            return mv
        }
    }

    inner class AssembleRouteModuleMethodAdapter(mv: MethodVisitor, access: Int, name: String, desc: String?)
        : AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {

        override fun onMethodExit(opcode: Int) {
            if (routeModuleClassList.isNotEmpty()) {
                val injectClassName = ROUTE_MODULE_INJECT_CLASS_NAME
                Logger.i(TAG, "Start inject the [${injectClassName}.${ROUTE_MODULE_INJECT_METHOD_NAME}]")
            } else {
                Logger.w(TAG, "No need for an inject!")
            }
            routeModuleClassList.forEach { routeModuleClassName ->
                val name = Constants.slashToDot(ROUTE_MODULE_SCAN_TARGET_INJECT_PACKAGE_NAME) + "." + routeModuleClassName.replace(_CLASS, "")
                Logger.i(TAG, "inject the [${ROUTE_MODULE_INJECT_TARGET_METHOD_NAME}(\"${name}\")]")
                mv.visitLdcInsn(name)
                mv.visitMethodInsn(
                    INVOKESTATIC,
                    Constants.dotToSlash(ROUTE_MODULE_INJECT_CLASS_NAME),
                    ROUTE_MODULE_INJECT_TARGET_METHOD_NAME,
                    "(Ljava/lang/String;)V",
                    false
                )
            }
            super.onMethodExit(opcode)
        }

    }
}