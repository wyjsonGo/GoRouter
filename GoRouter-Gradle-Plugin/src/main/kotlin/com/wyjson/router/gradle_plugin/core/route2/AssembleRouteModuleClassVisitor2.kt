package com.wyjson.router.gradle_plugin.core.route2

import com.wyjson.router.gradle_plugin.utils.Constants
import com.wyjson.router.gradle_plugin.utils.Constants.ROUTE_MODULE_INJECT_CLASS_NAME
import com.wyjson.router.gradle_plugin.utils.Constants.ROUTE_MODULE_INJECT_METHOD_NAME
import com.wyjson.router.gradle_plugin.utils.Constants.ROUTE_MODULE_INJECT_TARGET_METHOD_NAME
import com.wyjson.router.gradle_plugin.utils.Logger
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

class AssembleRouteModuleClassVisitor2(nextVisitor: ClassVisitor, private val className: String) :
    ClassVisitor(Opcodes.ASM9, nextVisitor) {

    private val TAG = "RM2"

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        var mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (ROUTE_MODULE_INJECT_METHOD_NAME == name) {
            mv = AssembleRouteModuleMethodAdapter(mv, access, name, descriptor)
        }
        return mv
    }

    inner class AssembleRouteModuleMethodAdapter(
        mv: MethodVisitor,
        access: Int,
        name: String,
        desc: String?
    ) : AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {

        @Override
        override fun onMethodExit(opcode: Int) {
            Logger.i(TAG, ":::Test className:[${className}],name:[${name}]")

            mv.visitLdcInsn("wyjson")
            mv.visitMethodInsn(
                INVOKESTATIC,
                Constants.dotToSlash(ROUTE_MODULE_INJECT_CLASS_NAME),
                ROUTE_MODULE_INJECT_TARGET_METHOD_NAME,
                "(Ljava/lang/String;)V",
                false
            )
            super.onMethodExit(opcode);
        }
    }
}