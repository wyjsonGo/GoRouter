package com.wyjson.router.compiler.processor;

import static com.wyjson.router.compiler.utils.Constants.ACTIVITY;
import static com.wyjson.router.compiler.utils.Constants.ANNOTATION_TYPE_INTERCEPTOR;
import static com.wyjson.router.compiler.utils.Constants.ANNOTATION_TYPE_PARAM;
import static com.wyjson.router.compiler.utils.Constants.ANNOTATION_TYPE_ROUTE;
import static com.wyjson.router.compiler.utils.Constants.ANNOTATION_TYPE_SERVICE;
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
import static com.wyjson.router.compiler.utils.Constants.LONG_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.LONG_PRIMITIVE;
import static com.wyjson.router.compiler.utils.Constants.METHOD_NAME_LOAD;
import static com.wyjson.router.compiler.utils.Constants.MODULE_PACKAGE_NAME;
import static com.wyjson.router.compiler.utils.Constants.PARCELABLE_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.PREFIX_OF_LOGGER;
import static com.wyjson.router.compiler.utils.Constants.PROJECT;
import static com.wyjson.router.compiler.utils.Constants.SERIALIZABLE_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.SHORT_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.SHORT_PRIMITIVE;
import static com.wyjson.router.compiler.utils.Constants.STRING_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.WARNING_TIPS;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.wyjson.router.annotation.Interceptor;
import com.wyjson.router.annotation.Param;
import com.wyjson.router.annotation.Route;
import com.wyjson.router.annotation.Service;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

@AutoService(Processor.class)
@SupportedAnnotationTypes({ANNOTATION_TYPE_SERVICE, ANNOTATION_TYPE_INTERCEPTOR, ANNOTATION_TYPE_ROUTE, ANNOTATION_TYPE_PARAM})
public class AssembleRouteProcessor extends BaseProcessor {

    TypeElement mGoRouter;
    TypeMirror serializableType;
    TypeMirror parcelableType;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        logger.info(moduleName + " >>> AssembleRouteProcessor init. <<<");
        mGoRouter = elementUtils.getTypeElement(GOROUTER_PACKAGE_NAME);
        serializableType = elementUtils.getTypeElement(SERIALIZABLE_PACKAGE).asType();
        parcelableType = elementUtils.getTypeElement(PARCELABLE_PACKAGE).asType();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (CollectionUtils.isEmpty(set))
            return false;
        MethodSpec.Builder loadInto = MethodSpec.methodBuilder(METHOD_NAME_LOAD).addModifiers(PUBLIC, STATIC);
        loadInto.addJavadoc("Load the $S route", moduleName);

        addService(roundEnvironment, loadInto);
        addInterceptor(roundEnvironment, loadInto);
        addRoute(roundEnvironment, loadInto);

        String className = generateClassName + PROJECT;
        try {
            JavaFile.builder(MODULE_PACKAGE_NAME,
                    TypeSpec.classBuilder(className)
                            .addModifiers(PUBLIC)
                            .addJavadoc(WARNING_TIPS)
                            .addMethod(loadInto.build())
                            .build()
            ).indent("    ").build().writeTo(mFiler);

            logger.info(moduleName + " >>> AssembleRouteProcessor over. <<<");
        } catch (IOException e) {
            logger.error(moduleName + " Failed to generate [" + className + "] class!");
            logger.error(e);
        }

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

            String type;
            // handle type
            if (types.isSubtype(tm, typeActivity)) {
                type = ".commitActivity($T.class)";
            } else if (types.isSubtype(tm, typeFragment)) {
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
                if (StringUtils.equals(typeStr, BYTE_PACKAGE) || StringUtils.equals(typeStr, BYTE_PRIMITIVE)) {
                    tempParamSB.append(".putByte(");
                } else if (StringUtils.equals(typeStr, SHORT_PACKAGE) || StringUtils.equals(typeStr, SHORT_PRIMITIVE)) {
                    tempParamSB.append(".putShort(");
                } else if (StringUtils.equals(typeStr, INTEGER_PACKAGE) || StringUtils.equals(typeStr, INTEGER_PRIMITIVE)) {
                    tempParamSB.append(".putInt(");
                } else if (StringUtils.equals(typeStr, LONG_PACKAGE) || StringUtils.equals(typeStr, LONG_PRIMITIVE)) {
                    tempParamSB.append(".putLong(");
                } else if (StringUtils.equals(typeStr, FLOAT_PACKAGE) || StringUtils.equals(typeStr, FLOAT_PRIMITIVE)) {
                    tempParamSB.append(".putFloat(");
                } else if (StringUtils.equals(typeStr, DOUBEL_PACKAGE) || StringUtils.equals(typeStr, DOUBEL_PRIMITIVE)) {
                    tempParamSB.append(".putDouble(");
                } else if (StringUtils.equals(typeStr, BOOLEAN_PACKAGE) || StringUtils.equals(typeStr, BOOLEAN_PRIMITIVE)) {
                    tempParamSB.append(".putBoolean(");
                } else if (StringUtils.equals(typeStr, CHAR_PACKAGE) || StringUtils.equals(typeStr, CHAR_PRIMITIVE)) {
                    tempParamSB.append(".putChar(");
                } else if (StringUtils.equals(typeStr, STRING_PACKAGE)) {
                    tempParamSB.append(".putString(");
                } else if (types.isSubtype(typeMirror, parcelableType)) {
                    tempParamSB.append(".putParcelable(");
                } else if (types.isSubtype(typeMirror, serializableType)) {
                    tempParamSB.append(".putSerializable(");
                } else {
                    throw new RuntimeException("@Param(type='" + typeMirror.toString() + "') is marked as an unsupported type");
                }
                if (StringUtils.isEmpty(param.name()) && !param.required()) {
                    tempParamSB.append("\"").append(paramName).append("\"").append(")");
                } else {
                    tempParamSB
                            .append("\"").append(paramName).append("\"")
                            .append(", ");
                    if (!StringUtils.isEmpty(param.name())) {
                        tempParamSB.append("\"").append(param.name()).append("\"");
                    } else {
                        tempParamSB.append("null");
                    }
                    tempParamSB.append(", ")
                            .append(param.required())
                            .append(")");
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
