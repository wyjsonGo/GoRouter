package com.wyjson.router.gradle_plugin.helper

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeVariableName
import com.wyjson.router.gradle_plugin.helper.model.ParamModel
import com.wyjson.router.gradle_plugin.helper.model.RouteHelperModel
import com.wyjson.router.gradle_plugin.helper.model.RouteModel
import com.wyjson.router.gradle_plugin.utils.Constants.CARD
import com.wyjson.router.gradle_plugin.utils.Constants.CONTEXT
import com.wyjson.router.gradle_plugin.utils.Constants.FRAGMENT
import com.wyjson.router.gradle_plugin.utils.Constants.I_DEGRADE_SERVICE
import com.wyjson.router.gradle_plugin.utils.Constants.I_JSON_SERVICE
import com.wyjson.router.gradle_plugin.utils.Constants.I_PRETREATMENT_SERVICE
import com.wyjson.router.gradle_plugin.utils.Constants.NULLABLE
import com.wyjson.router.gradle_plugin.utils.Constants.PACKAGE_NAME
import com.wyjson.router.gradle_plugin.utils.Constants.WARNING_TIPS
import javax.lang.model.element.Modifier

class AssembleGoRouteHelperCode(private val routeHelperModel: RouteHelperModel) {

    fun toJavaCode(className: String): String {
        val methods = LinkedHashSet<MethodSpec>()
        addService(methods)
        addRoute(methods)
        return JavaFile.builder(
            PACKAGE_NAME,
            TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc(WARNING_TIPS)
                .addMethods(methods)
                .build()
        ).indent("    ").build().toString()
    }

    private fun addService(methods: LinkedHashSet<MethodSpec>) {
        for (service in routeHelperModel.services) {
            if (service.value.prototype == I_DEGRADE_SERVICE
                || service.value.prototype == I_PRETREATMENT_SERVICE
                || service.value.prototype == I_JSON_SERVICE
            ) {
                continue
            }
            val serviceClassName = ClassName.bestGuess(service.value.prototype)
            val itemMethod = MethodSpec.methodBuilder("get${service.key}".replace("$", "For"))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addAnnotation(ClassName.bestGuess(NULLABLE))
                .returns(TypeVariableName.get(serviceClassName.simpleName()))
            if (service.value.remark?.isNotEmpty() == true) {
                itemMethod.addJavadoc(service.value.remark)
                itemMethod.addJavadoc("\n{@link \$N}", service.value.className)
            } else {
                itemMethod.addJavadoc("{@link \$N}", service.value.className)
            }
            if (service.value.alias?.isNotEmpty() == true) {
                itemMethod.addStatement(
                    "return GoRouter.getInstance().getService(\$T.class, \$S)",
                    serviceClassName,
                    service.value.alias
                )
            } else {
                itemMethod.addStatement(
                    "return GoRouter.getInstance().getService(\$T.class)",
                    serviceClassName
                )
            }
            methods.add(itemMethod.build())
        }
    }

    private fun addRoute(methods: LinkedHashSet<MethodSpec>) {
        for (route in routeHelperModel.routes) {
            for (routeModel in route.value) {
                val routeClassName = ClassName.bestGuess(routeModel.pathClass)

                val getPathMethod = MethodSpec.methodBuilder("get${routeClassName.simpleName()}Path")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(String::class.java)
                    .addStatement("return \$S", routeModel.path)

                val buildMethod = MethodSpec.methodBuilder("build${routeClassName.simpleName()}")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(ClassName.bestGuess(CARD))

                val goMethod = MethodSpec.methodBuilder("go${routeClassName.simpleName()}")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(ClassName.bestGuess(CONTEXT), "context")

                if (routeModel.remark?.isNotEmpty() == true) {
                    getPathMethod.addJavadoc("path:\$N", routeModel.remark)
                    getPathMethod.addJavadoc("\n{@link \$N}", routeModel.pathClass)

                    buildMethod.addJavadoc("build:\$N", routeModel.remark)
                    buildMethod.addJavadoc("\n{@link \$N}", routeModel.pathClass)

                    goMethod.addJavadoc("go:\$N", routeModel.remark)
                    goMethod.addJavadoc("\n{@link \$N}", routeModel.pathClass)
                } else {
                    getPathMethod.addJavadoc("{@link \$N}", routeModel.pathClass)
                    buildMethod.addJavadoc("{@link \$N}", routeModel.pathClass)
                    goMethod.addJavadoc("{@link \$N}", routeModel.pathClass)
                }
                methods.add(getPathMethod.build())
                if (routeModel.paramsType != null) {
                    val paramCode = CodeBlock.builder()
                    val goParamCode = CodeBlock.builder()
                    var requiredCount = 0
                    for (param in routeModel.paramsType) {
                        if (!param.required)
                            continue
                        requiredCount++
                        handleParam(param, buildMethod, goMethod, paramCode, goParamCode)
                    }
                    toCodeEnd(getPathMethod, buildMethod, goMethod, routeModel, paramCode, goParamCode, methods)

                    if (requiredCount != routeModel.paramsType.size) {
                        for (param in routeModel.paramsType) {
                            if (param.required)
                                continue
                            handleParam(param, buildMethod, goMethod, paramCode, goParamCode)
                        }
                        toCodeEnd(getPathMethod, buildMethod, goMethod, routeModel, paramCode, goParamCode, methods)
                    }
                } else {
                    toCodeEnd(getPathMethod,buildMethod, goMethod, routeModel, CodeBlock.builder(), CodeBlock.builder(), methods)
                }
            }
        }
    }

    private fun toCodeEnd(getPathMethod: MethodSpec.Builder, buildMethod: MethodSpec.Builder, goMethod: MethodSpec.Builder, routeModel: RouteModel, paramCode: CodeBlock.Builder, goParamCode: CodeBlock.Builder, methods: LinkedHashSet<MethodSpec>) {
        val newBuildMethod = buildMethod.build().toBuilder()
        newBuildMethod.addStatement(
            "return GoRouter.getInstance().build(\$N())\$L",
            getPathMethod.build().name,
            paramCode.build()
        )
        methods.add(newBuildMethod.build())

        val newGoMethod = goMethod.build().toBuilder()
        var goParamCodeString = goParamCode.build().toString()
        if (goParamCodeString.isNotEmpty()) {
            goParamCodeString = goParamCodeString.substring(0, goParamCodeString.length - 2)
        }
        if (routeModel.type == "Activity") {
            newGoMethod.addStatement("\$N(\$L).go(context)", buildMethod.build().name, goParamCodeString)
        } else {
            newGoMethod.returns(ClassName.bestGuess(FRAGMENT))
            newGoMethod.addStatement("return (Fragment) \$N(\$L).go(context)", buildMethod.build().name, goParamCodeString)
        }
        methods.add(newGoMethod.build())
    }

    private fun handleParam(param: ParamModel, buildMethod: MethodSpec.Builder, goMethod: MethodSpec.Builder, paramCode: CodeBlock.Builder, goParamCode: CodeBlock.Builder) {
        val type = param.type.replace("java.lang.", "")
        val name = param.name
        if (param.remark?.isNotEmpty() == true) {
            paramCode.add("\n// \$N", param.remark)
        }
        paramCode.add("\n.with\$L(\$S, \$N)", param.intentType, name, name)
        buildMethod.addParameter(TypeVariableName.get(type), name)

        goParamCode.add("\$N, ", name)
        goMethod.addParameter(TypeVariableName.get(type), name)
    }
}