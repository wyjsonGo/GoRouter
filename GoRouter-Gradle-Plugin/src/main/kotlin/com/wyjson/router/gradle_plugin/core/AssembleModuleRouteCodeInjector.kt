package com.wyjson.router.gradle_plugin.core

import com.wyjson.router.gradle_plugin.utils.Constants
import com.wyjson.router.gradle_plugin.utils.Constants.INJECT_CLASS_NAME
import com.wyjson.router.gradle_plugin.utils.Constants.INJECT_METHOD_NAME
import com.wyjson.router.gradle_plugin.utils.Constants.INJECT_TARGET_METHOD_NAME
import com.wyjson.router.gradle_plugin.utils.Constants.SCAN_TARGET_INJECT_PACKAGE_NAME
import com.wyjson.router.gradle_plugin.utils.Constants._CLASS
import com.wyjson.router.gradle_plugin.utils.Logger
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter
import java.io.InputStream

class AssembleModuleRouteCodeInjector(val moduleRouteClassList: List<String>) {

    fun execute(inputStream: InputStream): ByteArray {
        Logger.i("Start execute ASM method")
        val classReader = ClassReader(inputStream)
        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
        val classVisitor = AssembleModuleRouteClassVisitor(classWriter)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }

    inner class AssembleModuleRouteClassVisitor(private val mClassVisitor: ClassVisitor)
        : ClassVisitor(Opcodes.ASM9, mClassVisitor) {

        override fun visitMethod(
                access: Int,
                name: String?,
                desc: String?,
                signature: String?,
                exception: Array<out String>?
        ): MethodVisitor {
            var mv = mClassVisitor.visitMethod(access, name, desc, signature, exception)
            if (INJECT_METHOD_NAME == name) {
                mv = AssembleModuleRouteMethodAdapter(mv, access, name, desc)
            }
            return mv
        }
    }

    inner class AssembleModuleRouteMethodAdapter(mv: MethodVisitor, access: Int, name: String, desc: String?)
        : AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {

        override fun visitInsn(opcode: Int) {
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                if (moduleRouteClassList.isNotEmpty()) {
                    val injectClassName = INJECT_CLASS_NAME
                    Logger.i("Start inject the [${injectClassName}.${INJECT_METHOD_NAME}]")
                } else {
                    Logger.w("No need for an inject!")
                }
                moduleRouteClassList.forEach { moduleRouterClassName ->
                    val name = Constants.slashToDot(SCAN_TARGET_INJECT_PACKAGE_NAME) + "." + moduleRouterClassName.replace(_CLASS, "")
                    Logger.i("inject the [${INJECT_TARGET_METHOD_NAME}(\"${name}\")]")
                    mv.visitLdcInsn(name)
                    mv.visitMethodInsn(
                            INVOKESTATIC,
                            Constants.dotToSlash(INJECT_CLASS_NAME),
                            INJECT_TARGET_METHOD_NAME,
                            "(Ljava/lang/String;)V",
                            false
                    )
                }
            }
            super.visitInsn(opcode)
        }

        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
            super.visitMaxs(maxStack + 4, maxLocals)
        }

        override fun onMethodExit(opcode: Int) {
            super.onMethodExit(opcode)
            Logger.i("End of method inject")
        }
    }
}