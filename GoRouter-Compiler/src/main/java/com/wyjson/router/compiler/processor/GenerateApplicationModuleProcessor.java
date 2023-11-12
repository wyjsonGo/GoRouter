package com.wyjson.router.compiler.processor;

import static com.wyjson.router.compiler.utils.Constants.APPLICATION;
import static com.wyjson.router.compiler.utils.Constants.APPLICATION_MODULE_GENERATE_CLASS_NAME_SUFFIX;
import static com.wyjson.router.compiler.utils.Constants.APPLICATION_MODULE_PACKAGE_NAME;
import static com.wyjson.router.compiler.utils.Constants.CONFIGURATION;
import static com.wyjson.router.compiler.utils.Constants.CONTEXT;
import static com.wyjson.router.compiler.utils.Constants.FIELD_MAM;
import static com.wyjson.router.compiler.utils.Constants.I_APPLICATION_MODULE_PACKAGE_NAME;
import static com.wyjson.router.compiler.utils.Constants.METHOD_NAME_ON_CONFIGURATION_CHANGED;
import static com.wyjson.router.compiler.utils.Constants.METHOD_NAME_ON_CREATE;
import static com.wyjson.router.compiler.utils.Constants.METHOD_NAME_ON_LOAD_ASYNC;
import static com.wyjson.router.compiler.utils.Constants.METHOD_NAME_ON_LOW_MEMORY;
import static com.wyjson.router.compiler.utils.Constants.METHOD_NAME_ON_TERMINATE;
import static com.wyjson.router.compiler.utils.Constants.METHOD_NAME_ON_TRIM_MEMORY;
import static com.wyjson.router.compiler.utils.Constants.METHOD_NAME_SET_PRIORITY;
import static com.wyjson.router.compiler.utils.Constants.NONNULL;
import static com.wyjson.router.compiler.utils.Constants.PREFIX_OF_LOGGER;
import static com.wyjson.router.compiler.utils.Constants.WARNING_TIPS;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.wyjson.router.annotation.ApplicationModule;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

@AutoService(Processor.class)
public class GenerateApplicationModuleProcessor extends BaseProcessor {

    TypeMirror mApplication;
    TypeMirror mContext;
    TypeMirror mConfiguration;
    TypeElement mNONNULL;
    TypeElement mIApplicationModule;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new LinkedHashSet<>();
        set.add(ApplicationModule.class.getCanonicalName());
        return set;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        logger.info(moduleName + " >>> GenerateApplicationModuleProcessor init. <<<");
        mApplication = elementUtils.getTypeElement(APPLICATION).asType();
        mContext = elementUtils.getTypeElement(CONTEXT).asType();
        mConfiguration = elementUtils.getTypeElement(CONFIGURATION).asType();
        mNONNULL = elementUtils.getTypeElement(NONNULL);
        mIApplicationModule = elementUtils.getTypeElement(I_APPLICATION_MODULE_PACKAGE_NAME);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (CollectionUtils.isEmpty(set))
            return false;

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ApplicationModule.class);
        if (CollectionUtils.isEmpty(elements))
            return false;

        logger.info(moduleName + " >>> Found ApplicationModule, size is " + elements.size() + " <<<");

        for (Element element : elements) {
            verify(element);
            String applicationModuleClassName = ((TypeElement) element).getQualifiedName().toString();
            String className = String.format(APPLICATION_MODULE_GENERATE_CLASS_NAME_SUFFIX, generateClassName, element.getSimpleName().toString());
            TypeSpec.Builder thisClass = TypeSpec.classBuilder(className)
                    .addModifiers(PUBLIC)
                    .addSuperinterface(ClassName.get(mIApplicationModule))
                    .addJavadoc(WARNING_TIPS);

            setConstructorMethod(thisClass, element);
            setSetPriorityMethod(thisClass, element);
            setOnCreateMethod(thisClass, element);
            setOnLoadAsyncMethod(thisClass, element);
            setOnTerminateMethod(thisClass, element);
            setOnConfigurationChangedMethod(thisClass, element);
            setOnLowMemoryMethod(thisClass, element);
            setOnTrimMemoryMethod(thisClass, element);

            try {
                JavaFile.builder(APPLICATION_MODULE_PACKAGE_NAME, thisClass.build()).indent("    ").build().writeTo(mFiler);
                logger.info(moduleName + " class[" + applicationModuleClassName + "], Success to generate [" + className + "] class.");
            } catch (IOException e) {
                logger.error(moduleName + "class[" + applicationModuleClassName + "], Failed to generate [" + className + "] class!");
                logger.error(e);
            }
        }

        logger.info(moduleName + " >>> GenerateApplicationModuleProcessor over. <<<");
        return true;
    }

    private void setConstructorMethod(TypeSpec.Builder thisClass, Element element) {
        thisClass.addField(ClassName.get(mIApplicationModule), FIELD_MAM, PRIVATE, FINAL)
                .addMethod(MethodSpec.constructorBuilder().addModifiers(PUBLIC).addStatement("this.$N = new $T()", FIELD_MAM, element).build());
    }

    private void setSetPriorityMethod(TypeSpec.Builder thisClass, Element element) {
        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_SET_PRIORITY)
                .addModifiers(PUBLIC)
                .returns(int.class)
                .addAnnotation(Override.class)
                .addStatement("return this.$N.$N()", FIELD_MAM, METHOD_NAME_SET_PRIORITY);
        thisClass.addMethod(method.build());
    }

    private void setOnCreateMethod(TypeSpec.Builder thisClass, Element element) {
        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_ON_CREATE)
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(mApplication), "app")
                .addStatement("this.$N.$N($N)", FIELD_MAM, METHOD_NAME_ON_CREATE, "app");
        thisClass.addMethod(method.build());
    }

    private void setOnLoadAsyncMethod(TypeSpec.Builder thisClass, Element element) {
        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_ON_LOAD_ASYNC)
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(mApplication), "app")
                .addStatement("this.$N.$N($N)", FIELD_MAM, METHOD_NAME_ON_LOAD_ASYNC, "app");
        thisClass.addMethod(method.build());
    }

    private void setOnTerminateMethod(TypeSpec.Builder thisClass, Element element) {
        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_ON_TERMINATE)
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addStatement("this.$N.$N()", FIELD_MAM, METHOD_NAME_ON_TERMINATE);
        thisClass.addMethod(method.build());
    }

    private void setOnConfigurationChangedMethod(TypeSpec.Builder thisClass, Element element) {
        ParameterSpec newConfigParamSpec = ParameterSpec
                .builder(TypeName.get(mConfiguration), "newConfig")
                .addAnnotation(ClassName.get(mNONNULL))
                .build();
        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_ON_CONFIGURATION_CHANGED)
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
//                .addParameter(TypeName.get(mConfiguration), "newConfig")
                .addParameter(newConfigParamSpec)
                .addStatement("this.$N.$N($N)", FIELD_MAM, METHOD_NAME_ON_CONFIGURATION_CHANGED, "newConfig");
        thisClass.addMethod(method.build());
    }

    private void setOnLowMemoryMethod(TypeSpec.Builder thisClass, Element element) {
        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_ON_LOW_MEMORY)
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addStatement("this.$N.$N()", FIELD_MAM, METHOD_NAME_ON_LOW_MEMORY);
        thisClass.addMethod(method.build());
    }

    private void setOnTrimMemoryMethod(TypeSpec.Builder thisClass, Element element) {
        MethodSpec.Builder method = MethodSpec.methodBuilder(METHOD_NAME_ON_TRIM_MEMORY)
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(int.class, "level")
                .addStatement("this.$N.$N($N)", FIELD_MAM, METHOD_NAME_ON_TRIM_MEMORY, "level");
        thisClass.addMethod(method.build());
    }

    private void verify(Element element) {
        // 检查注解只能在class类上使用,不能在接口等其他地方使用.
        if (!element.getKind().isClass()) {
            throw new RuntimeException(PREFIX_OF_LOGGER + moduleName + " '@ApplicationModule' Annotation can only be used in class.");
        }
        TypeElement typeElement = (TypeElement) element;

        // 检查使用该注解的类，必须同时要实现IApplicationModule接口
        List<? extends TypeMirror> mirrorList = typeElement.getInterfaces();
        boolean isExistIApplicationModule = false;
        if (!mirrorList.isEmpty()) {
            for (TypeMirror mirror : mirrorList) {
                if (StringUtils.equals(I_APPLICATION_MODULE_PACKAGE_NAME, mirror.toString())) {
                    isExistIApplicationModule = true;
                    break;
                }
            }
        }
        if (!isExistIApplicationModule) {
            String applicationModuleClassName = typeElement.getQualifiedName().toString();
            throw new RuntimeException(PREFIX_OF_LOGGER + moduleName + " class[" + applicationModuleClassName + "] must implements interface[" + I_APPLICATION_MODULE_PACKAGE_NAME + "]");
        }
    }

}
