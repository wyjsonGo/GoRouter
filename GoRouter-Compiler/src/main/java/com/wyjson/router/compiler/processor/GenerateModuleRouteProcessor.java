package com.wyjson.router.compiler.processor;

import static com.wyjson.router.compiler.utils.Constants.ACTIVITY;
import static com.wyjson.router.compiler.utils.Constants.BOOLEAN_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.BOOLEAN_PRIMITIVE;
import static com.wyjson.router.compiler.utils.Constants.BYTE_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.BYTE_PRIMITIVE;
import static com.wyjson.router.compiler.utils.Constants.CHAR_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.CHAR_PRIMITIVE;
import static com.wyjson.router.compiler.utils.Constants.DOUBEL_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.DOUBEL_PRIMITIVE;
import static com.wyjson.router.compiler.utils.Constants.FLOAT_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.FLOAT_PRIMITIVE;
import static com.wyjson.router.compiler.utils.Constants.FRAGMENT;
import static com.wyjson.router.compiler.utils.Constants.GOROUTER_PACKAGE_NAME;
import static com.wyjson.router.compiler.utils.Constants.INTEGER_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.INTEGER_PRIMITIVE;
import static com.wyjson.router.compiler.utils.Constants.I_ROUTE_MODULE_PACKAGE_NAME;
import static com.wyjson.router.compiler.utils.Constants.LONG_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.LONG_PRIMITIVE;
import static com.wyjson.router.compiler.utils.Constants.METHOD_NAME_LOAD;
import static com.wyjson.router.compiler.utils.Constants.METHOD_NAME_LOAD_ROUTE_GROUP;
import static com.wyjson.router.compiler.utils.Constants.MODULE_PACKAGE_NAME;
import static com.wyjson.router.compiler.utils.Constants.PARCELABLE_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.PREFIX_OF_LOGGER;
import static com.wyjson.router.compiler.utils.Constants.PROJECT;
import static com.wyjson.router.compiler.utils.Constants.SEPARATOR;
import static com.wyjson.router.compiler.utils.Constants.SERIALIZABLE_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.SHORT_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.SHORT_PRIMITIVE;
import static com.wyjson.router.compiler.utils.Constants.STRING_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.WARNING_TIPS;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.wyjson.router.annotation.Interceptor;
import com.wyjson.router.annotation.Param;
import com.wyjson.router.annotation.Route;
import com.wyjson.router.annotation.Service;
import com.wyjson.router.compiler.doc.DocumentUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

@AutoService(Processor.class)
public class GenerateModuleRouteProcessor extends BaseProcessor {

    TypeElement mGoRouter;
    TypeElement mIRouteModule;
    TypeMirror serializableType;
    TypeMirror parcelableType;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new LinkedHashSet<>();
        set.add(Service.class.getCanonicalName());
        set.add(Interceptor.class.getCanonicalName());
        set.add(Route.class.getCanonicalName());
        set.add(Param.class.getCanonicalName());
        return set;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        logger.info(moduleName + " >>> GenerateModuleRouteProcessor init. <<<");
        mGoRouter = elementUtils.getTypeElement(GOROUTER_PACKAGE_NAME);
        mIRouteModule = elementUtils.getTypeElement(I_ROUTE_MODULE_PACKAGE_NAME);
        serializableType = elementUtils.getTypeElement(SERIALIZABLE_PACKAGE).asType();
        parcelableType = elementUtils.getTypeElement(PARCELABLE_PACKAGE).asType();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (CollectionUtils.isEmpty(set))
            return false;

        DocumentUtils.createDoc(mFiler, moduleName, logger, isGenerateDoc);

        MethodSpec.Builder loadInto = MethodSpec.methodBuilder(METHOD_NAME_LOAD)
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class);
        loadInto.addJavadoc("load the $S route", moduleName);

        addService(roundEnvironment, loadInto);
        addInterceptor(roundEnvironment, loadInto);
        addRoute(roundEnvironment, loadInto);
        LinkedHashSet<MethodSpec> routeGroupMethods = addRouteGroup(roundEnvironment, loadInto);

        String className = generateClassName + SEPARATOR + PROJECT;
        try {
            JavaFile.builder(MODULE_PACKAGE_NAME,
                    TypeSpec.classBuilder(className)
                            .addModifiers(PUBLIC)
                            .addSuperinterface(ClassName.get(mIRouteModule))
                            .addJavadoc(WARNING_TIPS)
                            .addMethod(loadInto.build())
                            .addMethods(routeGroupMethods)
                            .build()
            ).indent("    ").build().writeTo(mFiler);

            logger.info(moduleName + " >>> GenerateModuleRouteProcessor over. <<<");
        } catch (IOException e) {
            logger.error(moduleName + " Failed to generate [" + className + "] class!");
            logger.error(e);
        }

        DocumentUtils.generateDoc(moduleName, logger);
        return true;
    }

    private void addService(RoundEnvironment roundEnvironment, MethodSpec.Builder loadInto) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Service.class);
        if (CollectionUtils.isEmpty(elements))
            return;
        logger.info(moduleName + " >>> Found Service, size is " + elements.size() + " <<<");

        loadInto.addCode("// add Service\n");
        for (Element element : elements) {
            Service service = element.getAnnotation(Service.class);
            loadInto.addStatement("$T.getInstance().addService($T.class)", mGoRouter, element);
            DocumentUtils.addServiceDoc(moduleName, logger, element, service);
        }
    }

    private void addInterceptor(RoundEnvironment roundEnvironment, MethodSpec.Builder loadInto) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Interceptor.class);
        if (CollectionUtils.isEmpty(elements))
            return;
        logger.info(moduleName + " >>> Found Interceptor, size is " + elements.size() + " <<<");

        loadInto.addCode("// add Interceptor\n");
        for (Element element : elements) {
            Interceptor interceptor = element.getAnnotation(Interceptor.class);
            loadInto.addStatement("$T.getInstance().addInterceptor(" + interceptor.priority() + ", $T.class)", mGoRouter, element);
            DocumentUtils.addInterceptorDoc(moduleName, logger, element, interceptor);
        }
    }

    private LinkedHashSet<MethodSpec> addRouteGroup(RoundEnvironment roundEnvironment, MethodSpec.Builder loadInto) {
        LinkedHashSet<MethodSpec> methodSpecs = new LinkedHashSet<>();
        MethodSpec.Builder loadRouteGroup = MethodSpec.methodBuilder(METHOD_NAME_LOAD_ROUTE_GROUP).addModifiers(PRIVATE);
        loadRouteGroup.addJavadoc("load route group");

        loadInto.addStatement("$N()", loadRouteGroup.build());
        methodSpecs.add(loadRouteGroup.build());
        return methodSpecs;
    }

    private void addRoute(RoundEnvironment roundEnvironment, MethodSpec.Builder loadInto) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Route.class);
        if (CollectionUtils.isEmpty(elements))
            return;
        logger.info(moduleName + " >>> Found Route, size is " + elements.size() + " <<<");

        TypeMirror typeActivity = elementUtils.getTypeElement(ACTIVITY).asType();
        TypeMirror typeFragment = elementUtils.getTypeElement(FRAGMENT).asType();

        loadInto.addCode("// add Route\n");
        for (Element element : elements) {
            Route route = element.getAnnotation(Route.class);
            TypeMirror tm = element.asType();

//            String typeDoc;
//            String type;
//            // handle type
//            if (types.isSubtype(tm, typeActivity)) {
//                typeDoc = "Activity";
//                type = ".commitActivity($T.class)";
//            } else if (types.isSubtype(tm, typeFragment)) {
//                typeDoc = "Fragment";
//                type = ".commitFragment($T.class)";
//            } else {
//                throw new RuntimeException(PREFIX_OF_LOGGER + moduleName + " The @Route(path='" + route.path() + "') is marked on unsupported class, look at [" + tm.toString() + "].");
//            }
//
//            String tag = route.tag() == 0 ? "" : ".putTag(" + route.tag() + ")";
//
//            // Get all fields annotation by @Param
//            String param = "";
//            try {
//                param = handleParam(new StringBuilder(), element);
//            } catch (Exception e) {
//                throw new RuntimeException(PREFIX_OF_LOGGER + moduleName + " The @Route(path='" + route.path() + "') under " + e.getMessage());
//            }
//            loadInto.addStatement("$T.getInstance().build($S)" + tag + param + type, mGoRouter, route.path(), element);
//            DocumentUtils.addRoute(moduleName, logger, element, route, typeDoc);

            CodeBlock.Builder unifyCode = CodeBlock.builder();
            // GoRouter.getInstance().build(xxx)
            CodeBlock.Builder buildCode = CodeBlock.builder().add("$T.getInstance().build($S)", mGoRouter, route.path());
            unifyCode.add(buildCode.build());

            // .putTag(x)
            CodeBlock.Builder tagCode = CodeBlock.builder();
            if (route.tag() != 0) {
                tagCode.add(".putTag($L)", route.tag());
            }
            unifyCode.add(tagCode.build());

            // .putInt(x).putString(x) ...
            // Get all fields annotation by @Param
            CodeBlock.Builder paramCode = CodeBlock.builder();
            try {
                paramCode = handleParam(paramCode, element);
            } catch (Exception e) {
                throw new RuntimeException(PREFIX_OF_LOGGER + moduleName + " The @Route(path='" + route.path() + "') under " + e.getMessage());
            }
            unifyCode.add(paramCode.build());

            // .commitXXX(x)
            String typeDoc;
            CodeBlock.Builder typeCode = CodeBlock.builder();
            if (types.isSubtype(tm, typeActivity)) {
                typeDoc = "Activity";
                typeCode.add(".commitActivity($T.class)", element);
            } else if (types.isSubtype(tm, typeFragment)) {
                typeDoc = "Fragment";
                typeCode.add(".commitFragment($T.class)", element);
            } else {
                throw new RuntimeException(PREFIX_OF_LOGGER + moduleName + " The @Route(path='" + route.path() + "') is marked on unsupported class, look at [" + tm.toString() + "].");
            }
            unifyCode.add(typeCode.build());

            loadInto.addStatement(unifyCode.build());
            DocumentUtils.addRouteDoc(moduleName, logger, element, route, typeDoc);
        }
    }

    private CodeBlock.Builder handleParam(CodeBlock.Builder paramCode, Element element) {
        CodeBlock.Builder tempParamCode = CodeBlock.builder();
        for (Element field : element.getEnclosedElements()) {
            // It must be field, then it has annotation
            if (field.getKind().isField() && field.getAnnotation(Param.class) != null) {
                Param param = field.getAnnotation(Param.class);
                String key = field.getSimpleName().toString();
                TypeMirror typeMirror = field.asType();
                String typeStr = typeMirror.toString();
                String paramType;
                switch (typeStr) {
                    case BYTE_PACKAGE, BYTE_PRIMITIVE -> paramType = "putByte";
                    case SHORT_PACKAGE, SHORT_PRIMITIVE -> paramType = "putShort";
                    case INTEGER_PACKAGE, INTEGER_PRIMITIVE -> paramType = "putInt";
                    case LONG_PACKAGE, LONG_PRIMITIVE -> paramType = "putLong";
                    case FLOAT_PACKAGE, FLOAT_PRIMITIVE -> paramType = "putFloat";
                    case DOUBEL_PACKAGE, DOUBEL_PRIMITIVE -> paramType = "putDouble";
                    case BOOLEAN_PACKAGE, BOOLEAN_PRIMITIVE -> paramType = "putBoolean";
                    case CHAR_PACKAGE, CHAR_PRIMITIVE -> paramType = "putChar";
                    case STRING_PACKAGE -> paramType = "putString";
                    default -> {
                        if (types.isSubtype(typeMirror, parcelableType)) {
                            paramType = "putParcelable";
                        } else if (types.isSubtype(typeMirror, serializableType)) {
                            paramType = "putSerializable";
                        } else {
                            throw new RuntimeException("@Param(type='" + typeMirror + "') is marked as an unsupported type");
                        }
                    }
                }

                if (StringUtils.isEmpty(param.name()) && !param.required()) {
                    tempParamCode.add(".$N($S)", paramType, key);
                } else {
                    if (!StringUtils.isEmpty(param.name())) {
                        tempParamCode.add(".$N($S, $S, $L)", paramType, key, param.name(), param.required());
                    } else {
                        tempParamCode.add(".$N($S, $L, $L)", paramType, key, "null", param.required());
                    }
                }
            }
        }

        // Processing subclass parameters overrides parent class parameters
        if (paramCode.isEmpty()) {
            paramCode.add(tempParamCode.build());
        } else {
            tempParamCode.add(paramCode.build());
            paramCode.clear();
            paramCode.add(tempParamCode.build());
        }

        // if has parent?
        TypeMirror parent = ((TypeElement) element).getSuperclass();
        if (parent instanceof DeclaredType) {
            Element parentElement = ((DeclaredType) parent).asElement();
            if (parentElement instanceof TypeElement && !((TypeElement) parentElement).getQualifiedName().toString().startsWith("android")) {
                return handleParam(paramCode, parentElement);
            }
        }
        return paramCode;
    }

}
