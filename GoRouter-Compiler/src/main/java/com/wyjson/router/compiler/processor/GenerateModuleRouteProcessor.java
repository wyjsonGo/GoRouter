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
import static javax.lang.model.element.Modifier.PUBLIC;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
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
        loadInto.addJavadoc("Load the $S route", moduleName);

        addService(roundEnvironment, loadInto);
        addInterceptor(roundEnvironment, loadInto);
        addRoute(roundEnvironment, loadInto);

        String className = generateClassName + SEPARATOR + PROJECT;
        try {
            JavaFile.builder(MODULE_PACKAGE_NAME,
                    TypeSpec.classBuilder(className)
                            .addModifiers(PUBLIC)
                            .addSuperinterface(ClassName.get(mIRouteModule))
                            .addJavadoc(WARNING_TIPS)
                            .addMethod(loadInto.build())
                            .build()
            ).indent("    ").build().writeTo(mFiler);

            logger.info(moduleName + " >>> GenerateModuleRouteProcessor over. <<<");
        } catch (IOException e) {
            logger.error(moduleName + " Failed to generate [" + className + "] class!");
            logger.error(e);
        }

        DocumentUtils.generate(moduleName, logger);
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
            DocumentUtils.addService(moduleName, logger, element, service);
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
            DocumentUtils.addInterceptor(moduleName, logger, element, interceptor);
        }
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

            String typeDoc;
            String type;
            // handle type
            if (types.isSubtype(tm, typeActivity)) {
                typeDoc = "Activity";
                type = ".commitActivity($T.class)";
            } else if (types.isSubtype(tm, typeFragment)) {
                typeDoc = "Fragment";
                type = ".commitFragment($T.class)";
            } else {
                throw new RuntimeException(PREFIX_OF_LOGGER + moduleName + " The @Route(path='" + route.path() + "') is marked on unsupported class, look at [" + tm.toString() + "].");
            }

            String tag = route.tag() == 0 ? "" : ".putTag(" + route.tag() + ")";

            // Get all fields annotation by @Param
            String param = "";
            try {
                param = handleParam(new StringBuilder(), element);
            } catch (Exception e) {
                throw new RuntimeException(PREFIX_OF_LOGGER + moduleName + " The @Route(path='" + route.path() + "') under " + e.getMessage());
            }
            loadInto.addStatement("$T.getInstance().build($S)" + tag + param + type, mGoRouter, route.path(), element);
            DocumentUtils.addRoute(moduleName, logger, element, route, typeDoc);
        }
    }

    private String handleParam(StringBuilder paramSB, Element element) {
        StringBuilder tempParamSB = new StringBuilder();
        for (Element field : element.getEnclosedElements()) {
            // It must be field, then it has annotation
            if (field.getKind().isField() && field.getAnnotation(Param.class) != null) {
                Param param = field.getAnnotation(Param.class);
                String paramName = field.getSimpleName().toString();
                TypeMirror typeMirror = field.asType();
                String typeStr = typeMirror.toString();
                switch (typeStr) {
                    case BYTE_PACKAGE:
                    case BYTE_PRIMITIVE:
                        tempParamSB.append(".putByte(");
                        break;
                    case SHORT_PACKAGE:
                    case SHORT_PRIMITIVE:
                        tempParamSB.append(".putShort(");
                        break;
                    case INTEGER_PACKAGE:
                    case INTEGER_PRIMITIVE:
                        tempParamSB.append(".putInt(");
                        break;
                    case LONG_PACKAGE:
                    case LONG_PRIMITIVE:
                        tempParamSB.append(".putLong(");
                        break;
                    case FLOAT_PACKAGE:
                    case FLOAT_PRIMITIVE:
                        tempParamSB.append(".putFloat(");
                        break;
                    case DOUBEL_PACKAGE:
                    case DOUBEL_PRIMITIVE:
                        tempParamSB.append(".putDouble(");
                        break;
                    case BOOLEAN_PACKAGE:
                    case BOOLEAN_PRIMITIVE:
                        tempParamSB.append(".putBoolean(");
                        break;
                    case CHAR_PACKAGE:
                    case CHAR_PRIMITIVE:
                        tempParamSB.append(".putChar(");
                        break;
                    case STRING_PACKAGE:
                        tempParamSB.append(".putString(");
                        break;
                    default:
                        if (types.isSubtype(typeMirror, parcelableType)) {
                            tempParamSB.append(".putParcelable(");
                        } else if (types.isSubtype(typeMirror, serializableType)) {
                            tempParamSB.append(".putSerializable(");
                        } else {
                            throw new RuntimeException("@Param(type='" + typeMirror.toString() + "') is marked as an unsupported type");
                        }
                        break;
                }

                if (StringUtils.isEmpty(param.name()) && !param.required()) {
                    tempParamSB.append("\"").append(paramName).append("\"").append(")");
                } else {
                    tempParamSB.append("\"").append(paramName).append("\"").append(", ");
                    if (!StringUtils.isEmpty(param.name())) {
                        tempParamSB.append("\"").append(param.name()).append("\"").append(", ");
                    } else {
                        tempParamSB.append("null").append(", ");
                    }
                    tempParamSB.append(param.required()).append(")");
                }
            }
        }

        // Processing subclass parameters overrides parent class parameters
        if (StringUtils.isEmpty(paramSB)) {
            paramSB.append(tempParamSB);
        } else {
            paramSB.insert(0, tempParamSB);
        }

        // if has parent?
        TypeMirror parent = ((TypeElement) element).getSuperclass();
        if (parent instanceof DeclaredType) {
            Element parentElement = ((DeclaredType) parent).asElement();
            if (parentElement instanceof TypeElement && !((TypeElement) parentElement).getQualifiedName().toString().startsWith("android")) {
                return handleParam(paramSB, parentElement);
            }
        }
        return paramSB.toString();
    }

}
