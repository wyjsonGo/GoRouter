package com.wyjson.router.compiler.processor;

import static com.wyjson.router.compiler.utils.Constants.ACTIVITY;
import static com.wyjson.router.compiler.utils.Constants.BOOLEAN_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.BOOLEAN_PRIMITIVE;
import static com.wyjson.router.compiler.utils.Constants.BUNDLE;
import static com.wyjson.router.compiler.utils.Constants.BYTE_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.BYTE_PRIMITIVE;
import static com.wyjson.router.compiler.utils.Constants.CHAR_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.CHAR_PRIMITIVE;
import static com.wyjson.router.compiler.utils.Constants.DOUBEL_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.DOUBEL_PRIMITIVE;
import static com.wyjson.router.compiler.utils.Constants.FLOAT_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.FLOAT_PRIMITIVE;
import static com.wyjson.router.compiler.utils.Constants.FRAGMENT;
import static com.wyjson.router.compiler.utils.Constants.GOROUTER;
import static com.wyjson.router.compiler.utils.Constants.INJECT_CLASS_NAME_SUFFIX;
import static com.wyjson.router.compiler.utils.Constants.INTEGER_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.INTEGER_PRIMITIVE;
import static com.wyjson.router.compiler.utils.Constants.INTENT;
import static com.wyjson.router.compiler.utils.Constants.I_JSON_SERVICE;
import static com.wyjson.router.compiler.utils.Constants.LONG_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.LONG_PRIMITIVE;
import static com.wyjson.router.compiler.utils.Constants.METHOD_NAME_INJECT;
import static com.wyjson.router.compiler.utils.Constants.METHOD_NAME_INJECT_CHECK;
import static com.wyjson.router.compiler.utils.Constants.PARAM_EXCEPTION;
import static com.wyjson.router.compiler.utils.Constants.PARCELABLE_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.PREFIX_OF_LOGGER;
import static com.wyjson.router.compiler.utils.Constants.ROUTER_EXCEPTION;
import static com.wyjson.router.compiler.utils.Constants.SERIALIZABLE_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.SHORT_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.SHORT_PRIMITIVE;
import static com.wyjson.router.compiler.utils.Constants.STRING_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.TYPE_WRAPPER;
import static com.wyjson.router.compiler.utils.Constants.WARNING_TIPS;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.wyjson.router.annotation.Param;
import com.wyjson.router.annotation.Route;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

@AutoService(Processor.class)
public class ParamProcessor extends BaseProcessor {

    private final Map<TypeElement, List<Element>> paramList = new HashMap<>();
    TypeElement mIntent;
    TypeElement mBundle;
    TypeElement mParamException;
    TypeElement mIJsonService;
    TypeElement mGoRouter;
    TypeElement mRouterException;
    TypeElement mTypeWrapper;
    TypeMirror activityType;
    TypeMirror fragmentType;
    TypeMirror serializableType;
    TypeMirror parcelableType;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new LinkedHashSet<>();
        set.add(Param.class.getCanonicalName());
        return set;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        logger.info(moduleName + " >>> ParamProcessor init. <<<");
        mIntent = elementUtils.getTypeElement(INTENT);
        mBundle = elementUtils.getTypeElement(BUNDLE);
        mParamException = elementUtils.getTypeElement(PARAM_EXCEPTION);
        mIJsonService = elementUtils.getTypeElement(I_JSON_SERVICE);
        mGoRouter = elementUtils.getTypeElement(GOROUTER);
        mRouterException = elementUtils.getTypeElement(ROUTER_EXCEPTION);
        mTypeWrapper = elementUtils.getTypeElement(TYPE_WRAPPER);
        activityType = elementUtils.getTypeElement(ACTIVITY).asType();
        fragmentType = elementUtils.getTypeElement(FRAGMENT).asType();
        serializableType = elementUtils.getTypeElement(SERIALIZABLE_PACKAGE).asType();
        parcelableType = elementUtils.getTypeElement(PARCELABLE_PACKAGE).asType();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (CollectionUtils.isEmpty(set))
            return false;

        try {
            categories(roundEnvironment.getElementsAnnotatedWith(Param.class));
        } catch (IllegalAccessException e) {
            logger.error(moduleName + " " + e.getMessage());
        }

        if (MapUtils.isNotEmpty(paramList)) {
            for (Map.Entry<TypeElement, List<Element>> entry : paramList.entrySet()) {

                TypeElement parent = entry.getKey();
                List<Element> childs = entry.getValue();

                String qualifiedName = parent.getQualifiedName().toString();
                String packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
                String className = parent.getSimpleName() + INJECT_CLASS_NAME_SUFFIX;

                LinkedHashSet<MethodSpec> injectMethods = addInjectMethod(parent, childs, false);
                LinkedHashSet<MethodSpec> injectCheckMethods = addInjectMethod(parent, childs, true);

                try {
                    JavaFile.builder(packageName,
                                    TypeSpec.classBuilder(className)
                                            .addModifiers(PUBLIC)
                                            .addJavadoc(WARNING_TIPS)
                                            .addMethods(injectMethods)
                                            .addMethods(injectCheckMethods)
                                            .build())
                            .indent("    ")
                            .build()
                            .writeTo(mFiler);
                } catch (IOException e) {
                    logger.error(moduleName + " Failed to generate [" + className + "] class!");
                    logger.error(e);
                }
            }
        }
        logger.info(moduleName + " >>> ParamProcessor over. <<<");

        return true;
    }

    private LinkedHashSet<MethodSpec> addInjectMethod(TypeElement parent, List<Element> childs, boolean isCheck) {
        LinkedHashSet<MethodSpec> methodList = new LinkedHashSet<>();
        MethodSpec.Builder method = MethodSpec.methodBuilder(isCheck ? METHOD_NAME_INJECT_CHECK : METHOD_NAME_INJECT)
                .addParameter(ClassName.get(parent), "self")
                .addModifiers(PUBLIC, STATIC);

        if (isCheck) {
            method.addException(ClassName.get(mParamException)).addException(ClassName.get(NullPointerException.class));
        }

        if (types.isSubtype(parent.asType(), activityType)) {
            MethodSpec.Builder method1 = method.build().toBuilder();
            method1.addStatement("$L(self, self.getIntent().getExtras())", method.build().name);
            methodList.add(method1.build());

            MethodSpec.Builder method2 = method.build().toBuilder().addParameter(ClassName.get(mIntent), "intent");
            method2.addStatement("$L(self, intent.getExtras())", method.build().name);
            methodList.add(method2.build());
        } else if (types.isSubtype(parent.asType(), fragmentType)) {
            MethodSpec.Builder method1 = method.build().toBuilder();
            method1.addStatement("$L(self, self.getArguments())", method.build().name);
            methodList.add(method1.build());
        } else {
            throw new RuntimeException(PREFIX_OF_LOGGER + moduleName + " @Param can only be used in activity and fragment, Current type [" + parent.asType().toString() + "].");
        }
        method.addParameter(ClassName.get(mBundle), "bundle");

        method.beginControlFlow("if (bundle == null)");
        if (isCheck) {
            method.addStatement("throw new NullPointerException($S)", "The bundle in the intent is empty!");
        } else {
            method.addStatement("return");
        }
        method.endControlFlow();

        boolean isJsonService = false;

        for (Element field : childs) {
            Param param = field.getAnnotation(Param.class);

            method.addCode("// $L\n", param.remark());
            String key = field.getSimpleName().toString();
            String name = !StringUtils.isEmpty(param.name()) ? param.name() : key;

            CodeBlock.Builder itemCode = CodeBlock.builder();
            TypeMirror typeMirror = field.asType();
            String typeStr = typeMirror.toString();
            boolean isSelfHandleRequired = false;
            switch (typeStr) {
                case BYTE_PACKAGE, BYTE_PRIMITIVE ->
                        itemCode.addStatement("self.$N = bundle.getByte($S, self.$N)", key, name, key);
                case SHORT_PACKAGE, SHORT_PRIMITIVE ->
                        itemCode.addStatement("self.$N = bundle.getShort($S, self.$N)", key, name, key);
                case INTEGER_PACKAGE, INTEGER_PRIMITIVE ->
                        itemCode.addStatement("self.$N = bundle.getInt($S, self.$N)", key, name, key);
                case LONG_PACKAGE, LONG_PRIMITIVE ->
                        itemCode.addStatement("self.$N = bundle.getLong($S, self.$N)", key, name, key);
                case FLOAT_PACKAGE, FLOAT_PRIMITIVE ->
                        itemCode.addStatement("self.$N = bundle.getFloat($S, self.$N)", key, name, key);
                case DOUBEL_PACKAGE, DOUBEL_PRIMITIVE ->
                        itemCode.addStatement("self.$N = bundle.getDouble($S, self.$N)", key, name, key);
                case BOOLEAN_PACKAGE, BOOLEAN_PRIMITIVE ->
                        itemCode.addStatement("self.$N = bundle.getBoolean($S, self.$N)", key, name, key);
                case CHAR_PACKAGE, CHAR_PRIMITIVE ->
                        itemCode.addStatement("self.$N = bundle.getChar($S, self.$N)", key, name, key);
                case STRING_PACKAGE ->
                        itemCode.addStatement("self.$N = bundle.getString($S, self.$N)", key, name, key);
                default -> {
                    if (types.isSubtype(typeMirror, parcelableType)) {
                        itemCode.beginControlFlow("if (bundle.containsKey($S))", name);
                        itemCode.addStatement("self.$N = bundle.getParcelable($S)", key, name);

                        isSelfHandleRequired = true;
                        if (param.required() && isCheck) {
                            itemCode.nextControlFlow("else");
                            itemCode.addStatement("throw new ParamException($S)", name);
                            itemCode.endControlFlow();
                        } else {
                            itemCode.endControlFlow();
                        }
                    } else if (types.isSubtype(typeMirror, serializableType)) {
                        itemCode.beginControlFlow("if (bundle.containsKey($S))", name);
                        itemCode.addStatement("self.$N = ($L) bundle.getSerializable($S)", key, typeStr, name);

                        isSelfHandleRequired = true;
                        if (param.required() && isCheck) {
                            itemCode.nextControlFlow("else");
                            itemCode.addStatement("throw new ParamException($S)", name);
                            itemCode.endControlFlow();
                        } else {
                            itemCode.endControlFlow();
                        }
                    } else {
                        if (!isJsonService) {
                            isJsonService = true;
                            method.addStatement("$T jsonService = $T.getInstance().getService($T.class)", mIJsonService, mGoRouter, mIJsonService);
                        }
                        itemCode.beginControlFlow("if (jsonService != null)");
                        itemCode.addStatement("self.$N = jsonService.parseObject(bundle.getString($S), new $L<$L>() {}.getType())", key, name, mTypeWrapper, typeStr);
                        itemCode.nextControlFlow("else");
                        itemCode.addStatement("throw new $T($S)", mRouterException, "To use withObject() method, you need to implement IJsonService");
                        itemCode.endControlFlow();
                    }
                }
            }

            // required=true的情况
            if (param.required() && isCheck && !isSelfHandleRequired) {
                method.beginControlFlow("if (bundle.containsKey($S))", name);
                method.addCode(itemCode.build());
                method.nextControlFlow("else");
                method.addStatement("throw new ParamException($S)", name);
                method.endControlFlow();
            } else {
                method.addCode(itemCode.build());
            }
        }
        methodList.add(method.build());
        return methodList;
    }

    /**
     * Categories field, find his papa.
     *
     * @param elements
     * @throws IllegalAccessException Field need @Param
     */
    private void categories(Set<? extends Element> elements) throws IllegalAccessException {
        if (CollectionUtils.isNotEmpty(elements)) {
            Map<TypeElement, List<Element>> parentParamList = new HashMap<>();
            for (Element element : elements) {
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

                if (element.getModifiers().contains(Modifier.PRIVATE)) {
                    throw new IllegalAccessException("The inject fields CAN NOT BE 'private'!!! please check field [" + element.getSimpleName() + "] in class [" + enclosingElement.getQualifiedName() + "]");
                }

                if (enclosingElement.getAnnotation(Route.class) != null) {
                    if (paramList.containsKey(enclosingElement)) {
                        paramList.get(enclosingElement).add(element);
                    } else {
                        List<Element> childs = new ArrayList<>();
                        childs.add(element);
                        paramList.put(enclosingElement, childs);
                    }
                } else {
                    if (parentParamList.containsKey(enclosingElement)) {
                        parentParamList.get(enclosingElement).add(element);
                    } else {
                        List<Element> childs = new ArrayList<>();
                        childs.add(element);
                        parentParamList.put(enclosingElement, childs);
                    }
                }
            }
            // 处理父类里的参数
            for (Map.Entry<TypeElement, List<Element>> parentEntry : parentParamList.entrySet()) {
                for (Map.Entry<TypeElement, List<Element>> childEntry : paramList.entrySet()) {
                    handleParentParam(parentEntry.getKey(), parentEntry.getValue(), childEntry.getKey(), childEntry.getValue());
                }
            }
            logger.info(moduleName + " categories finished.");
        }
    }

    private static void handleParentParam(TypeElement parentKey, List<Element> parentValue, Element childKey, List<Element> childValue) {
        TypeMirror parent = ((TypeElement) childKey).getSuperclass();
        if (StringUtils.equals(parent.toString(), parentKey.getQualifiedName().toString())) {
            childValue.addAll(parentValue);
        } else {
            if (parent instanceof DeclaredType) {
                Element parentElement = ((DeclaredType) parent).asElement();
                if (parentElement instanceof TypeElement && !((TypeElement) parentElement).getQualifiedName().toString().startsWith("android")) {
                    handleParentParam(parentKey, parentValue, parentElement, childValue);
                }
            }
        }
    }

}
