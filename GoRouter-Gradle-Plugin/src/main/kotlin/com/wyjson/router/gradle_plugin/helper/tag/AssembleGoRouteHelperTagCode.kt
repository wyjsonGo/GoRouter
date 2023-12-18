package com.wyjson.router.gradle_plugin.helper.tag

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeVariableName
import com.wyjson.router.gradle_plugin.model.ParamModel
import com.wyjson.router.gradle_plugin.model.RouteHelperModel
import com.wyjson.router.gradle_plugin.model.RouteModel
import com.wyjson.router.gradle_plugin.utils.Constants.CARD
import com.wyjson.router.gradle_plugin.utils.Constants.CONTEXT
import com.wyjson.router.gradle_plugin.utils.Constants.FRAGMENT
import com.wyjson.router.gradle_plugin.utils.Constants.I_DEGRADE_SERVICE
import com.wyjson.router.gradle_plugin.utils.Constants.I_JSON_SERVICE
import com.wyjson.router.gradle_plugin.utils.Constants.I_PRETREATMENT_SERVICE
import com.wyjson.router.gradle_plugin.utils.Constants.NULLABLE
import com.wyjson.router.gradle_plugin.utils.Constants.PACKAGE_NAME
import com.wyjson.router.gradle_plugin.utils.Constants.WARNING_TIPS
import com.wyjson.router.gradle_plugin.utils.Logger
import org.gradle.configurationcache.extensions.capitalized
import javax.lang.model.element.Modifier

class AssembleGoRouteHelperTagCode(private val model: RouteHelperModel) {

    private val TAG = "RH(Tag)Code"

    fun toJavaCode(className: String): String {
        val methods = LinkedHashSet<MethodSpec>()
        val typeSpecs = LinkedHashSet<TypeSpec>()
        addService(methods)
        addRoute(methods, typeSpecs)
        return JavaFile.builder(
            PACKAGE_NAME,
            TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc(WARNING_TIPS)
                .addMethods(methods)
                .addTypes(typeSpecs)
                .build()
        ).indent("    ").build().toString()
    }

    private fun addService(methods: LinkedHashSet<MethodSpec>) {
        for (service in model.services) {
            if (service.value.prototype == I_DEGRADE_SERVICE
                || service.value.prototype == I_PRETREATMENT_SERVICE
                || service.value.prototype == I_JSON_SERVICE
            ) {
                continue
            }
            val serviceClassName = ClassName.bestGuess(service.value.prototype)
            var key = service.key
            if (service.value.alias?.isNotEmpty() == true) {
                key = service.key.split("$")[0] + "For" + service.value.alias!!.capitalized()
            }
            val itemMethod = MethodSpec.methodBuilder("get$key")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addAnnotation(ClassName.bestGuess(NULLABLE))
                .returns(serviceClassName)
                .addStatement("return null")
            methods.add(itemMethod.build())
        }
    }

    private fun addRoute(methods: LinkedHashSet<MethodSpec>, typeSpecs: LinkedHashSet<TypeSpec>) {
        for (route in model.routes) {
            for (routeModel in route.value) {
                val methodName = try {
                    extractMethodName(routeModel.path)
                } catch (e: Exception) {
                    Logger.e(TAG, e.message!!)
                    return
                }

                val getPathMethod = MethodSpec.methodBuilder("get${methodName}Path")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(String::class.java)
                    .addStatement("return null")

                val buildMethod = MethodSpec.methodBuilder("build$methodName")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(ClassName.bestGuess(CARD))

                val goMethod = MethodSpec.methodBuilder("go$methodName")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(ClassName.bestGuess(CONTEXT), "context")

                methods.add(getPathMethod.build())
                if (routeModel.paramsType != null) {
                    val paramCode = CodeBlock.builder()
                    val goParamCode = CodeBlock.builder()
                    var requiredCount = 0
                    for (param in routeModel.paramsType) {
                        if (!param.required)
                            continue
                        requiredCount++
                        handleParam(param, buildMethod, goMethod, paramCode, goParamCode, CodeBlock.builder())
                    }
                    toCodeEnd(getPathMethod, buildMethod, goMethod, routeModel, paramCode, goParamCode, methods)

                    if (requiredCount != routeModel.paramsType.size) {
                        val getMethod = MethodSpec.methodBuilder("get$methodName")
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        val goParamCodeString = handleGoParamCodeString(goParamCode)
                        val allParamMethodParamCode = CodeBlock.builder()
                        handleBuilderInnerClass(methodName, routeModel,  getPathMethod, getMethod, buildMethod, paramCode, goParamCodeString, typeSpecs, methods)

                        for (param in routeModel.paramsType) {
                            if (param.required)
                                continue
                            handleParam(param, buildMethod, goMethod, paramCode, goParamCode, allParamMethodParamCode)
                        }
                        toCodeEndFormAllParam(getMethod, buildMethod, goMethod, routeModel, goParamCode, goParamCodeString, allParamMethodParamCode, methods)
                    }
                } else {
                    toCodeEnd(getPathMethod,buildMethod, goMethod, routeModel, CodeBlock.builder(), CodeBlock.builder(), methods)
                }
            }
        }
    }

    private fun extractMethodName(path: String): String {
        var methodName = ""
        val replace = path.replace(".", "")
            .replace("~", "")
            .replace("!", "")
            .replace("@", "")
            .replace("#", "")
            .replace("$", "")
            .replace("%", "")
            .replace("^", "")
            .replace("&", "")
            .replace("*", "")
            .replace("(", "")
            .replace(")", "")
            .replace("-", "")
            .replace("+", "")
            .replace("=", "")
        for (item in replace.split("/")) {
            if (item.contains("_")){
                for (_item in item.split("_")) {
                    methodName += _item.capitalized()
                }
            } else {
                methodName += item.capitalized()
            }
        }
        if (methodName.isEmpty()) {
            throw RuntimeException("Failed to extract method name,path[${path}]")
        }
        return methodName
    }

    private fun handleBuilderInnerClass(
        methodName: String,
        routeModel: RouteModel,
        getPathMethod: MethodSpec.Builder,
        getMethod: MethodSpec.Builder,
        buildMethod: MethodSpec.Builder,
        paramCode: CodeBlock.Builder,
        goParamCodeString: String,
        typeSpecs: LinkedHashSet<TypeSpec>,
        methods: LinkedHashSet<MethodSpec>
    ) {
        val builderInnerClassMethods = LinkedHashSet<MethodSpec>()
        val builderInnerClass = TypeSpec.classBuilder("${methodName}Builder")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)

        builderInnerClass.addMethod(
            MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameters(buildMethod.parameters)
                .build()
        )

        for (param in routeModel.paramsType!!) {
            if (param.required)
                continue
            val type = "Object"
            val name = param.name
            val itemSetMethod = MethodSpec.methodBuilder("set${param.name.capitalized()}")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeVariableName.get(builderInnerClass.build().name))
                .addParameter(TypeVariableName.get(type), name)
            itemSetMethod.addStatement("return this")
            builderInnerClassMethods.add(itemSetMethod.build())
        }

        val buildCardMethod = MethodSpec.methodBuilder("build")
            .addModifiers(Modifier.PUBLIC)
            .returns(ClassName.bestGuess(CARD))
            .addStatement("return null")
        builderInnerClassMethods.add(buildCardMethod.build())

        builderInnerClass.addMethods(builderInnerClassMethods)
        typeSpecs.add(builderInnerClass.build())

        getMethod.returns(TypeVariableName.get(builderInnerClass.build().name))
        getMethod.addParameters(buildMethod.parameters)
        getMethod.addStatement("return null")
        methods.add(getMethod.build())
    }

    private fun handleGoParamCodeString(goParamCode: CodeBlock.Builder): String {
        var goParamCodeString = goParamCode.build().toString()
        if (goParamCodeString.isNotEmpty()) {
            goParamCodeString = goParamCodeString.substring(0, goParamCodeString.length - 2)
        }
        return goParamCodeString
    }

    private fun handleParam(param: ParamModel, buildMethod: MethodSpec.Builder, goMethod: MethodSpec.Builder, paramCode: CodeBlock.Builder, goParamCode: CodeBlock.Builder, allParamMethodParamCode: CodeBlock.Builder) {
        val type = "Object"
        val name = param.name
        buildMethod.addParameter(TypeVariableName.get(type), name)

        goParamCode.add("\$N, ", name)
        goMethod.addParameter(TypeVariableName.get(type), name)
    }

    private fun toCodeEnd(getPathMethod: MethodSpec.Builder, buildMethod: MethodSpec.Builder, goMethod: MethodSpec.Builder, routeModel: RouteModel, paramCode: CodeBlock.Builder, goParamCode: CodeBlock.Builder, methods: LinkedHashSet<MethodSpec>) {
        val newBuildMethod = buildMethod.build().toBuilder()
        newBuildMethod.addStatement("return null")
        methods.add(newBuildMethod.build())

        val newGoMethod = goMethod.build().toBuilder()
        if (routeModel.type != "Activity") {
            newGoMethod.returns(ClassName.bestGuess(FRAGMENT))
            newGoMethod.addStatement("return null")
        }
        methods.add(newGoMethod.build())
    }

    private fun toCodeEndFormAllParam(
        getMethod: MethodSpec.Builder,
        buildMethod: MethodSpec.Builder,
        goMethod: MethodSpec.Builder,
        routeModel: RouteModel,
        goParamCode: CodeBlock.Builder,
        oldGoParamCodeString: String,
        allParamMethodParamCode: CodeBlock.Builder,
        methods: LinkedHashSet<MethodSpec>
    ) {
        val newBuildMethod = buildMethod.build().toBuilder()
        newBuildMethod.addStatement("return null")

        methods.add(newBuildMethod.build())

        val newGoMethod = goMethod.build().toBuilder()
        if (routeModel.type != "Activity") {
            newGoMethod.returns(ClassName.bestGuess(FRAGMENT))
            newGoMethod.addStatement("return null")
        }
        methods.add(newGoMethod.build())
    }
}