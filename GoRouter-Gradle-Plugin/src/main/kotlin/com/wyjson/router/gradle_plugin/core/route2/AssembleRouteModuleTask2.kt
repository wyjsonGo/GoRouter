package com.wyjson.router.gradle_plugin.core.route2

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.wyjson.router.gradle_plugin.utils.Constants.ROUTE_MODULE_INJECT_CLASS_NAME
import org.objectweb.asm.ClassVisitor

abstract class AssembleRouteModuleTask2 : AsmClassVisitorFactory<InstrumentationParameters.None> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return AssembleRouteModuleClassVisitor2(
            nextClassVisitor,
            classContext.currentClassData.className
        )
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        return (classData.className.contains(ROUTE_MODULE_INJECT_CLASS_NAME))
    }
}