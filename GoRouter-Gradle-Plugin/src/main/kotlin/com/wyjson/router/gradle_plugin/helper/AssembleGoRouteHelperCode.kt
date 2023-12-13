package com.wyjson.router.gradle_plugin.helper

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeVariableName
import com.wyjson.router.gradle_plugin.helper.model.ParamModel
import com.wyjson.router.gradle_plugin.helper.model.RouteHelperModel
import com.wyjson.router.gradle_plugin.utils.Constants.BOOLEAN_PRIMITIVE
import com.wyjson.router.gradle_plugin.utils.Constants.BYTE_PRIMITIVE
import com.wyjson.router.gradle_plugin.utils.Constants.CARD
import com.wyjson.router.gradle_plugin.utils.Constants.CHAR_PRIMITIVE
import com.wyjson.router.gradle_plugin.utils.Constants.DOUBLE_PRIMITIVE
import com.wyjson.router.gradle_plugin.utils.Constants.FLOAT_PRIMITIVE
import com.wyjson.router.gradle_plugin.utils.Constants.FRAGMENT
import com.wyjson.router.gradle_plugin.utils.Constants.INTEGER_PRIMITIVE
import com.wyjson.router.gradle_plugin.utils.Constants.LONG_PRIMITIVE
import com.wyjson.router.gradle_plugin.utils.Constants.NULLABLE
import com.wyjson.router.gradle_plugin.utils.Constants.PACKAGE_NAME
import com.wyjson.router.gradle_plugin.utils.Constants.SHORT_PRIMITIVE
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
                val itemMethod = MethodSpec.methodBuilder("build${routeClassName.simpleName()}")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
                val isRouteActivityType = routeModel.type == "Activity";
                if (isRouteActivityType) {
                    itemMethod.returns(ClassName.bestGuess(CARD))
                } else {
                    itemMethod.returns(ClassName.bestGuess(FRAGMENT))
                }
                if (routeModel.remark?.isNotEmpty() == true) {
                    itemMethod.addJavadoc(routeModel.remark)
                    itemMethod.addJavadoc("\n{@link \$N}", routeModel.pathClass)
                } else {
                    itemMethod.addJavadoc("{@link \$N}", routeModel.pathClass)
                }
                if (routeModel.paramsType != null) {
                    val paramCode = CodeBlock.builder();
                    var requiredCount = 0
                    for (param in routeModel.paramsType) {
                        if (!param.required)
                            continue;
                        requiredCount++
                        handleParam(param, itemMethod, paramCode)
                    }
                    if (requiredCount != 0) {
                        val newItemMethod = itemMethod.build().toBuilder();
                        newItemMethod.addStatement(
                            "return GoRouter.getInstance().build(\$S)\$L",
                            routeModel.path,
                            paramCode.build()
                        )
                        methods.add(newItemMethod.build())
                    }

                    if (requiredCount != routeModel.paramsType.size) {
                        for (param in routeModel.paramsType) {
                            if (param.required)
                                continue;
                            handleParam(param, itemMethod, paramCode)
                        }
                        val newItemMethod = itemMethod.build().toBuilder();
                        newItemMethod.addStatement(
                            "return GoRouter.getInstance().build(\$S)\$L",
                            routeModel.path,
                            paramCode.build()
                        )
                        methods.add(newItemMethod.build())
                    }
                } else {
                    itemMethod.addStatement(
                        "return GoRouter.getInstance().build(\$S)",
                        routeModel.path
                    )
                    methods.add(itemMethod.build())
                }
            }
        }
    }

    private fun handleParam(
        param: ParamModel,
        itemMethod: MethodSpec.Builder,
        paramCode: CodeBlock.Builder
    ) {
        val type = param.type
        val name = param.name
        if (param.remark?.isNotEmpty() == true) {
            paramCode.add("\n// \$N", param.remark)
        }
        paramCode.add("\n.with\$L(\$S, \$N)", param.intentType, name, name)
        if (type.contains(".") && type.contains("<") && type.contains(",")) {
            val type0 = type.substring(0, type.indexOf("<"))
            val typeTemp =
                type.substring(type.indexOf("<")).replace("<", "").replace(">", "").split(",")
            val type1 = typeTemp[0]
            val type2 = typeTemp[1]
            val parameterizedTypeName = ParameterizedTypeName.get(
                ClassName.bestGuess(type0),
                ClassName.bestGuess(type1),
                ClassName.bestGuess(type2)
            )
            itemMethod.addParameter(parameterizedTypeName, name)
        } else if (type.contains(".") && type.contains("<")) {
            val type0 = type.substring(0, type.indexOf("<"))
            val type1 = type.substring(type.indexOf("<")).replace("<", "").replace(">", "")
            val parameterizedTypeName = ParameterizedTypeName.get(
                ClassName.bestGuess(type0),
                ClassName.bestGuess(type1)
            )
            itemMethod.addParameter(parameterizedTypeName, name)
        } else if (type.contains(".")) {
            itemMethod.addParameter(ClassName.bestGuess(type), name)
        } else if (type.contains("[]")) {
            when (type.replace("[]", "")) {
                BYTE_PRIMITIVE -> itemMethod.addParameter(ByteArray::class.java, name)
                SHORT_PRIMITIVE -> itemMethod.addParameter(ShortArray::class.java, name)
                INTEGER_PRIMITIVE -> itemMethod.addParameter(IntArray::class.java, name)
                LONG_PRIMITIVE -> itemMethod.addParameter(LongArray::class.java, name)
                FLOAT_PRIMITIVE -> itemMethod.addParameter(FloatArray::class.java, name)
                DOUBLE_PRIMITIVE -> itemMethod.addParameter(DoubleArray::class.java, name)
                BOOLEAN_PRIMITIVE -> itemMethod.addParameter(BooleanArray::class.java, name)
                CHAR_PRIMITIVE -> itemMethod.addParameter(CharArray::class.java, name)
            }
        } else {
            when (type) {
                BYTE_PRIMITIVE -> itemMethod.addParameter(Byte::class.java, name)
                SHORT_PRIMITIVE -> itemMethod.addParameter(Short::class.java, name)
                INTEGER_PRIMITIVE -> itemMethod.addParameter(Int::class.java, name)
                LONG_PRIMITIVE -> itemMethod.addParameter(Long::class.java, name)
                FLOAT_PRIMITIVE -> itemMethod.addParameter(Float::class.java, name)
                DOUBLE_PRIMITIVE -> itemMethod.addParameter(Double::class.java, name)
                BOOLEAN_PRIMITIVE -> itemMethod.addParameter(Boolean::class.java, name)
                CHAR_PRIMITIVE -> itemMethod.addParameter(Char::class.java, name)
            }
        }
    }
}