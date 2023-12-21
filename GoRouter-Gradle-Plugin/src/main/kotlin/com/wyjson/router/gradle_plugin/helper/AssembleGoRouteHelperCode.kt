package com.wyjson.router.gradle_plugin.helper

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeVariableName
import com.wyjson.router.gradle_plugin.model.ParamModel
import com.wyjson.router.gradle_plugin.model.RouteHelperModel
import com.wyjson.router.gradle_plugin.model.RouteModel
import com.wyjson.router.gradle_plugin.utils.Constants
import com.wyjson.router.gradle_plugin.utils.Constants.CARD_CLSS_NAME
import com.wyjson.router.gradle_plugin.utils.Constants.CARD_META_CLSS_NAME
import com.wyjson.router.gradle_plugin.utils.Constants.CONTEXT
import com.wyjson.router.gradle_plugin.utils.Constants.FIELD_CARD
import com.wyjson.router.gradle_plugin.utils.Constants.FRAGMENT
import com.wyjson.router.gradle_plugin.utils.Constants.GOROUTER_CLASS_NAME
import com.wyjson.router.gradle_plugin.utils.Constants.GOROUTER_HELPER_PACKAGE_NAME
import com.wyjson.router.gradle_plugin.utils.Constants.I_DEGRADE_SERVICE
import com.wyjson.router.gradle_plugin.utils.Constants.I_JSON_SERVICE
import com.wyjson.router.gradle_plugin.utils.Constants.I_PRETREATMENT_SERVICE
import com.wyjson.router.gradle_plugin.utils.Constants.PROJECT
import com.wyjson.router.gradle_plugin.utils.Constants.WARNING_TIPS
import com.wyjson.router.gradle_plugin.utils.Logger
import org.gradle.configurationcache.extensions.capitalized
import java.io.File
import javax.lang.model.element.Modifier

class AssembleGoRouteHelperCode(private val model: RouteHelperModel) {

    private val TAG = "RHCode"
    private val GoRouter = ClassName.bestGuess(GOROUTER_CLASS_NAME)
    private val Card = ClassName.bestGuess(CARD_CLSS_NAME)
    private val CardMeta = ClassName.bestGuess(CARD_META_CLSS_NAME)
    private val Context = ClassName.bestGuess(CONTEXT)
    private val Fragment = ClassName.bestGuess(FRAGMENT)
    private val Nullable = ClassName.bestGuess(Constants.Nullable)
    private val Deprecated = ClassName.bestGuess(Constants.Deprecated)
    private val clearDirList = ArrayList<String>()

    fun generateJavaFile(packageFile: File) {
        clearDirList.clear()
        addService(packageFile)
        addRoute(packageFile)
    }

    private fun addService(packageFile: File) {
        for (service in model.services) {
            if (service.value.prototype == I_DEGRADE_SERVICE
                || service.value.prototype == I_PRETREATMENT_SERVICE
                || service.value.prototype == I_JSON_SERVICE
            ) {
                continue
            }
            val methods = LinkedHashSet<MethodSpec>()

            var className = service.key
            if (service.value.alias?.isNotEmpty() == true) {
                className = service.key.split("$")[0] + "For" + service.value.alias!!.capitalized()
            }
            className += PROJECT;

            val serviceClassName = ClassName.bestGuess(service.value.prototype)
            val itemMethod = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addAnnotation(Nullable)
                .returns(TypeVariableName.get(serviceClassName.simpleName()))

            if (service.value.alias?.isNotEmpty() == true) {
                itemMethod.addStatement(
                    "return \$T.getInstance().getService(\$T.class, \$S)",
                    GoRouter,
                    serviceClassName,
                    service.value.alias
                )
            } else {
                itemMethod.addStatement(
                    "return \$T.getInstance().getService(\$T.class)",
                    GoRouter,
                    serviceClassName
                )
            }
            methods.add(itemMethod.build())

            val classBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc(WARNING_TIPS)
                .addMethods(methods)

            if (service.value.remark?.isNotEmpty() == true) {
                classBuilder.addJavadoc("\n${service.value.remark}")
                classBuilder.addJavadoc("\n{@link \$N}", service.value.className)
            } else {
                classBuilder.addJavadoc("\n{@link \$N}", service.value.className)
            }

            val moduleName = projectNameToPackageName(service.value.moduleName)
            clearModuleDir(packageFile, moduleName)
            val java = JavaFile.builder("${GOROUTER_HELPER_PACKAGE_NAME}.${moduleName}.service", classBuilder.build()).indent("    ").build().toString()
            val outputFile = File(packageFile, "/${moduleName}/service/${className}.java")
            outputFile.parentFile.mkdirs()
            outputFile.writeText(java, Charsets.UTF_8)
        }
    }

    private fun addRoute(packageFile: File) {
        for (route in model.routes) {
            for (routeModel in route.value) {
                if (routeModel.ignoreHelper == true)
                    continue
                var className = try {
                    extractClassNameByPath(routeModel.path)
                } catch (e: Exception) {
                    Logger.e(TAG, e.message!!)
                    return
                }
                className += PROJECT

                val methods = LinkedHashSet<MethodSpec>()
                val typeSpecs = LinkedHashSet<TypeSpec>()

                val getPathMethod = MethodSpec.methodBuilder("getPath")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(String::class.java)
                    .addStatement("return \$S", routeModel.path)

                val getCardMetaMethod = MethodSpec.methodBuilder("getCardMeta")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(CardMeta)
                    .addStatement(
                        "return \$T.getInstance().build(\$N()).getCardMeta()",
                        GoRouter,
                        getPathMethod.build().name
                    )

                val postEventMethod = MethodSpec.methodBuilder("postEvent")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addTypeVariable(TypeVariableName.get("T"))
                    .addParameter(TypeVariableName.get("T"), "value")
                    .addStatement(
                        "\$T.getInstance().postEvent(\$N(), value)",
                        GoRouter,
                        getPathMethod.build().name
                    )

                val buildMethod = MethodSpec.methodBuilder("build")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(Card)

                val goMethod = MethodSpec.methodBuilder("go")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(Context, "context")

                methods.add(getPathMethod.build())
                methods.add(getCardMetaMethod.build())
                methods.add(postEventMethod.build())

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
                        val createMethod = MethodSpec.methodBuilder("create")
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        val goParamCodeString = handleGoParamCodeString(goParamCode)
                        val allParamMethodParamCode = CodeBlock.builder()
                        handleBuilderInnerClass(routeModel,  getPathMethod, createMethod, buildMethod, paramCode, goParamCodeString, typeSpecs, methods)

                        for (param in routeModel.paramsType) {
                            if (param.required)
                                continue
                            handleParam(param, buildMethod, goMethod, paramCode, goParamCode, allParamMethodParamCode)
                        }
                        toCodeEndFormAllParam(createMethod, buildMethod, goMethod, routeModel, goParamCode, goParamCodeString, allParamMethodParamCode, methods)
                    }
                } else {
                    toCodeEnd(getPathMethod,buildMethod, goMethod, routeModel, CodeBlock.builder(), CodeBlock.builder(), methods)
                }

                val classBuilder = TypeSpec.classBuilder(className)
                    .addModifiers(Modifier.PUBLIC)
                    .addJavadoc(WARNING_TIPS)
                    .addMethods(methods)
                    .addTypes(typeSpecs)

                if (routeModel.remark?.isNotEmpty() == true) {
                    classBuilder.addJavadoc("\n${routeModel.remark}")
                    classBuilder.addJavadoc("\n{@link \$N}", routeModel.pathClass)
                } else {
                    classBuilder.addJavadoc("\n{@link \$N}", routeModel.pathClass)
                }

                if (routeModel.deprecated == true) {
                    classBuilder.addAnnotation(Deprecated)
                }

                val moduleName = projectNameToPackageName(routeModel.moduleName)
                clearModuleDir(packageFile, moduleName)
                val groupName = route.key.lowercase().replace(".", "").replace("-", "")
                val java = JavaFile.builder("${GOROUTER_HELPER_PACKAGE_NAME}.${moduleName}.group_${groupName}", classBuilder.build()).indent("    ").build().toString()
                val outputFile = File(packageFile, "/${moduleName}/group_${groupName}/${className}.java")
                outputFile.parentFile.mkdirs()
                outputFile.writeText(java, Charsets.UTF_8)
            }
        }
    }

    private fun projectNameToPackageName(projectName: String): String {
        if (projectName.isEmpty()) {
            return projectName
        }
        var str = projectName
        // 去除开头字母是0-9和_的情况
        str = str.replace("^[0-9_]+".toRegex(), "")
        str = str.replace("-", "_")
        // 首字母小写
        str = str.substring(0, 1).lowercase() + str.substring(1)
        // 处理大写
        val len = str.length
        val sb = StringBuilder(len)
        for (i in 0 until len) {
            val c = str[i]
            if (Character.isUpperCase(c)) {
                if (str[i - 1] != '_') {
                    sb.append("_")
                }
                sb.append(str[i].lowercaseChar())
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    private fun extractClassNameByPath(path: String): String {
        var methodName = ""
        val replace = path.replace(".", "").replace("-", "")
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
        routeModel: RouteModel,
        getPathMethod: MethodSpec.Builder,
        createMethod: MethodSpec.Builder,
        buildMethod: MethodSpec.Builder,
        paramCode: CodeBlock.Builder,
        goParamCodeString: String,
        typeSpecs: LinkedHashSet<TypeSpec>,
        methods: LinkedHashSet<MethodSpec>
    ) {
        val builderInnerClassMethods = LinkedHashSet<MethodSpec>()
        val builderInnerClass = TypeSpec.classBuilder("Builder")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)

        builderInnerClass.addField(Card, FIELD_CARD, Modifier.PRIVATE, Modifier.FINAL)
        builderInnerClass.addMethod(
            MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameters(buildMethod.parameters)
                .addStatement(
                    "\$N = \$T.getInstance().build(\$N())\$L",
                    FIELD_CARD,
                    GoRouter,
                    getPathMethod.build().name,
                    paramCode.build()
                )
                .build()
        )

        for (param in routeModel.paramsType!!) {
            if (param.required)
                continue
            val type = param.type.replace("java.lang.", "")
            val name = param.name
            val itemSetMethod = MethodSpec.methodBuilder("set${param.name.capitalized()}")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeVariableName.get(builderInnerClass.build().name))
                .addParameter(TypeVariableName.get(type), name)
            if (param.remark?.isNotEmpty() == true) {
                itemSetMethod.addJavadoc(param.remark)
            }
            // $L withXXX
            itemSetMethod.addStatement("\$N.\$L(\$S, \$N)", FIELD_CARD, param.intentType, name, name)
            itemSetMethod.addStatement("return this")
            builderInnerClassMethods.add(itemSetMethod.build())
        }

        val buildCardMethod = MethodSpec.methodBuilder("build")
            .addModifiers(Modifier.PUBLIC)
            .returns(Card)
            .addStatement("return \$N", FIELD_CARD)
        builderInnerClassMethods.add(buildCardMethod.build())

        builderInnerClass.addMethods(builderInnerClassMethods)
        typeSpecs.add(builderInnerClass.build())

        createMethod.returns(TypeVariableName.get(builderInnerClass.build().name))
        createMethod.addParameters(buildMethod.parameters)
        createMethod.addStatement("return new \$L(\$L)", builderInnerClass.build().name, goParamCodeString)
        methods.add(createMethod.build())
    }

    private fun handleGoParamCodeString(goParamCode: CodeBlock.Builder): String {
        var goParamCodeString = goParamCode.build().toString()
        if (goParamCodeString.isNotEmpty()) {
            goParamCodeString = goParamCodeString.substring(0, goParamCodeString.length - 2)
        }
        return goParamCodeString
    }

    private fun handleParam(
        param: ParamModel,
        buildMethod: MethodSpec.Builder,
        goMethod: MethodSpec.Builder,
        paramCode: CodeBlock.Builder,
        goParamCode: CodeBlock.Builder,
        allParamMethodParamCode: CodeBlock.Builder
    ) {
        val type = param.type.replace("java.lang.", "")
        val name = param.name
        if (param.remark?.isNotEmpty() == true) {
            paramCode.add("\n// \$N", param.remark)
        }
        // $L withXXX
        paramCode.add("\n.\$L(\$S, \$N)", param.intentType, name, name)
        buildMethod.addParameter(TypeVariableName.get(type), name)

        goParamCode.add("\$N, ", name)
        allParamMethodParamCode.add(".set\$N(\$N)", name.capitalized(), name)
        goMethod.addParameter(TypeVariableName.get(type), name)
    }

    private fun toCodeEnd(
        getPathMethod: MethodSpec.Builder,
        buildMethod: MethodSpec.Builder,
        goMethod: MethodSpec.Builder,
        routeModel: RouteModel,
        paramCode: CodeBlock.Builder,
        goParamCode: CodeBlock.Builder,
        methods: LinkedHashSet<MethodSpec>
    ) {
        val newBuildMethod = buildMethod.build().toBuilder()
        newBuildMethod.addStatement(
            "return \$T.getInstance().build(\$N())\$L",
            GoRouter,
            getPathMethod.build().name,
            paramCode.build()
        )
        methods.add(newBuildMethod.build())

        val newGoMethod = goMethod.build().toBuilder()
        val goParamCodeString = handleGoParamCodeString(goParamCode)
        if (routeModel.type == "Activity") {
            newGoMethod.addStatement("\$N(\$L).go(context)", buildMethod.build().name, goParamCodeString)
        } else {
            newGoMethod.returns(Fragment)
            newGoMethod.addStatement("return (Fragment) \$N(\$L).go(context)", buildMethod.build().name, goParamCodeString)
        }
        methods.add(newGoMethod.build())
    }

    private fun toCodeEndFormAllParam(
        createMethod: MethodSpec.Builder,
        buildMethod: MethodSpec.Builder,
        goMethod: MethodSpec.Builder,
        routeModel: RouteModel,
        goParamCode: CodeBlock.Builder,
        oldGoParamCodeString: String,
        allParamMethodParamCode: CodeBlock.Builder,
        methods: LinkedHashSet<MethodSpec>
    ) {
        val newBuildMethod = buildMethod.build().toBuilder()
        newBuildMethod.addStatement(
            "return \$N(\$L)\$L.build()",
            createMethod.build().name,
            oldGoParamCodeString,
            allParamMethodParamCode.build()
        )

        methods.add(newBuildMethod.build())

        val newGoMethod = goMethod.build().toBuilder()
        val goParamCodeString = handleGoParamCodeString(goParamCode)
        if (routeModel.type == "Activity") {
            newGoMethod.addStatement("\$N(\$L).go(context)", buildMethod.build().name, goParamCodeString)
        } else {
            newGoMethod.returns(Fragment)
            newGoMethod.addStatement("return (Fragment) \$N(\$L).go(context)", buildMethod.build().name, goParamCodeString)
        }
        methods.add(newGoMethod.build())
    }

    /**
     * 清除一下模块包下的文件
     */
    private fun clearModuleDir(packageFile: File, moduleName: String) {
        if (!clearDirList.contains(moduleName)) {
            val moduleFile = File(packageFile, "/${moduleName}")
            if (moduleFile.exists()) {
                moduleFile.deleteRecursively()
            }
            clearDirList.add(moduleName)
        }
    }
}