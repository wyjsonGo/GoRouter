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
import static com.wyjson.router.compiler.utils.Constants.I_ROUTE_MODULE_GROUP_METHOD_NAME_LOAD;
import static com.wyjson.router.compiler.utils.Constants.I_ROUTE_MODULE_GROUP_PACKAGE_NAME;
import static com.wyjson.router.compiler.utils.Constants.I_ROUTE_MODULE_PACKAGE_NAME;
import static com.wyjson.router.compiler.utils.Constants.LOGISTICS_CENTER_METHOD_NAME_GET_ROUTE_GROUPS;
import static com.wyjson.router.compiler.utils.Constants.LOGISTICS_CENTER_PACKAGE_NAME;
import static com.wyjson.router.compiler.utils.Constants.LONG_PACKAGE;
import static com.wyjson.router.compiler.utils.Constants.LONG_PRIMITIVE;
import static com.wyjson.router.compiler.utils.Constants.METHOD_NAME_LOAD;
import static com.wyjson.router.compiler.utils.Constants.METHOD_NAME_LOAD_ROUTE_FOR_x_GROUP;
import static com.wyjson.router.compiler.utils.Constants.METHOD_NAME_LOAD_ROUTE_GROUP;
import static com.wyjson.router.compiler.utils.Constants.MODULE_PACKAGE_NAME;
import static com.wyjson.router.compiler.utils.Constants.PARAM_NAME_ROUTE_GROUPS;
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
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.wyjson.router.annotation.Interceptor;
import com.wyjson.router.annotation.Param;
import com.wyjson.router.annotation.Route;
import com.wyjson.router.annotation.Service;
import com.wyjson.router.compiler.doc.DocumentUtils;
import com.wyjson.router.compiler.doc.model.RouteModel;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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

    TypeElement mLogisticsCenter;
    TypeElement mIRouteModuleGroup;
    TypeMirror serializableType;
    TypeMirror parcelableType;
    TypeMirror activityType;
    TypeMirror fragmentType;
    private final Map<String, Set<Element>> routeGroupMap = new HashMap<>();

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
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (CollectionUtils.isEmpty(set))
            return false;

        DocumentUtils.createDoc(mFiler, moduleName, logger, isGenerateDoc);

        String className = generateClassName + SEPARATOR + PROJECT;

        MethodSpec.Builder loadIntoMethod = MethodSpec.methodBuilder(METHOD_NAME_LOAD)
                .addModifiers(PUBLIC)
                .addAnnotation(Override.class);
        loadIntoMethod.addJavadoc("load the $S route", moduleName);

        addService(roundEnvironment, loadIntoMethod);
        addInterceptor(roundEnvironment, loadIntoMethod);
        LinkedHashSet<MethodSpec> routeGroupMethods = addRouteGroup(roundEnvironment, loadIntoMethod);

        try {
            JavaFile.builder(MODULE_PACKAGE_NAME,
                            TypeSpec.classBuilder(className)
                                    .addModifiers(PUBLIC)
                                    .addSuperinterface(ClassName.get(mIRouteModule))
                                    .addJavadoc(WARNING_TIPS)
                                    .addMethod(loadIntoMethod.build())
                                    .addMethods(routeGroupMethods)
                                    .build())
                    .indent("    ")
                    .build()
                    .writeTo(mFiler);
            logger.info(moduleName + " >>> GenerateModuleRouteProcessor over. <<<");
        } catch (IOException e) {
            logger.error(moduleName + " Failed to generate [" + className + "] class!");
            logger.error(e);
        }

        DocumentUtils.generateDoc(moduleName, logger);
        return true;
    }

    private void addService(RoundEnvironment roundEnvironment, MethodSpec.Builder loadIntoMethod) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Service.class);
        if (CollectionUtils.isEmpty(elements))
            return;
        logger.info(moduleName + " >>> Found Service, size is " + elements.size() + " <<<");

        loadIntoMethod.addCode("// add Service\n");
        for (Element element : elements) {
            Service service = element.getAnnotation(Service.class);
            loadIntoMethod.addStatement("$T.getInstance().addService($T.class)", mGoRouter, element);
            DocumentUtils.addServiceDoc(moduleName, logger, element, service);
        }
    }

    private void addInterceptor(RoundEnvironment roundEnvironment, MethodSpec.Builder loadIntoMethod) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Interceptor.class);
        if (CollectionUtils.isEmpty(elements))
            return;
        logger.info(moduleName + " >>> Found Interceptor, size is " + elements.size() + " <<<");

        loadIntoMethod.addCode("// add Interceptor\n");
        for (Element element : elements) {
            Interceptor interceptor = element.getAnnotation(Interceptor.class);
            loadIntoMethod.addStatement("$T.getInstance().addInterceptor(" + interceptor.priority() + ", $T.class)", mGoRouter, element);
            DocumentUtils.addInterceptorDoc(moduleName, logger, element, interceptor);
        }
    }

    private LinkedHashSet<MethodSpec> addRouteGroup(RoundEnvironment roundEnvironment, MethodSpec.Builder loadIntoMethod) {
        LinkedHashSet<MethodSpec> routeGroupMethods = new LinkedHashSet<>();

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Route.class);
        if (CollectionUtils.isEmpty(elements))
            return routeGroupMethods;
        logger.info(moduleName + " >>> Found Route, size is " + elements.size() + " <<<");

        mLogisticsCenter = elementUtils.getTypeElement(LOGISTICS_CENTER_PACKAGE_NAME);
        mIRouteModuleGroup = elementUtils.getTypeElement(I_ROUTE_MODULE_GROUP_PACKAGE_NAME);
        serializableType = elementUtils.getTypeElement(SERIALIZABLE_PACKAGE).asType();
        parcelableType = elementUtils.getTypeElement(PARCELABLE_PACKAGE).asType();
        activityType = elementUtils.getTypeElement(ACTIVITY).asType();
        fragmentType = elementUtils.getTypeElement(FRAGMENT).asType();

        ParameterSpec routeGroupsParamSpec = ParameterSpec.builder(
                ParameterizedTypeName.get(
                        ClassName.get(Map.class),
                        ClassName.get(String.class),
                        ClassName.get(mIRouteModuleGroup)
                ),
                PARAM_NAME_ROUTE_GROUPS
        ).build();

        MethodSpec.Builder loadRouteGroupMethod = MethodSpec
                .methodBuilder(METHOD_NAME_LOAD_ROUTE_GROUP)
                .addModifiers(PRIVATE)
                .addParameter(routeGroupsParamSpec);
        loadRouteGroupMethod.addJavadoc("load route group");

        saveRouteGroup(elements);

        for (Map.Entry<String, Set<Element>> entry : routeGroupMap.entrySet()) {
            List<RouteModel> routeModelDocList = new ArrayList<>();
            String groupName = entry.getKey();
            // 首字母大写
            String groupNameToUpperCase = groupName.substring(0, 1).toUpperCase() + groupName.substring(1);
            String methodName = String.format(METHOD_NAME_LOAD_ROUTE_FOR_x_GROUP, groupNameToUpperCase);
            MethodSpec.Builder loadRouteForXGroupMethod = MethodSpec.methodBuilder(methodName).addModifiers(PRIVATE);
            loadRouteForXGroupMethod.addJavadoc("\"" + groupName + "\" route group");
            for (Element element : entry.getValue()) {
                addRoute(element, loadRouteForXGroupMethod, routeModelDocList);
            }
            MethodSpec loadRouteForXGroupMethodBuild = loadRouteForXGroupMethod.build();
            routeGroupMethods.add(loadRouteForXGroupMethodBuild);

            // 把每个路由分组方法汇总到loadRouteGroup方法中
            CodeBlock.Builder iRouteModuleGroupCode = CodeBlock.builder();
            iRouteModuleGroupCode.beginControlFlow("new $N()", ClassName.get(mIRouteModuleGroup).simpleName());
            iRouteModuleGroupCode.indent().add("@Override\n");
            iRouteModuleGroupCode.beginControlFlow("public void $N()", I_ROUTE_MODULE_GROUP_METHOD_NAME_LOAD);
            iRouteModuleGroupCode.indent().addStatement("$N()", loadRouteForXGroupMethodBuild.name);
            iRouteModuleGroupCode.unindent().endControlFlow();
            iRouteModuleGroupCode.add("}");

            CodeBlock.Builder routeGroupPutCode = CodeBlock.builder();
            routeGroupPutCode.add("$N.put($S, $N)", routeGroupsParamSpec, groupName, iRouteModuleGroupCode.build().toString());

            loadRouteGroupMethod.addStatement(routeGroupPutCode.build());
            DocumentUtils.addRouteGroupDoc(moduleName, logger, groupName, routeModelDocList);
        }

        loadIntoMethod.addCode("// call load route group\n");

        loadIntoMethod.addStatement("$N($T.$N())", loadRouteGroupMethod.build(), mLogisticsCenter, LOGISTICS_CENTER_METHOD_NAME_GET_ROUTE_GROUPS);
        routeGroupMethods.add(loadRouteGroupMethod.build());
        return routeGroupMethods;
    }

    private void saveRouteGroup(Set<? extends Element> elements) {
        routeGroupMap.clear();
        for (Element element : elements) {
            Route route = element.getAnnotation(Route.class);
            String group = extractRouteGroup(route.path());
            Set<Element> routeSet = routeGroupMap.get(group);
            if (CollectionUtils.isEmpty(routeSet)) {
                Set<Element> sortRouteSet = new TreeSet<>(new Comparator<Element>() {
                    @Override
                    public int compare(Element r1, Element r2) {
                        String r1Path = r1.getAnnotation(Route.class).path();
                        String r2Path = r2.getAnnotation(Route.class).path();
                        return r1Path.compareTo(r2Path);
                    }
                });
                sortRouteSet.add(element);
                routeGroupMap.put(group, sortRouteSet);
            } else {
                routeSet.add(element);
            }
        }
    }

    private String extractRouteGroup(String path) {
        if (StringUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new RuntimeException(PREFIX_OF_LOGGER + moduleName + " Extract the path[" + path + "] group failed, Extract the default group failed, the path must be start with '/' and contain more than 2 '/'!");
        }
        try {
            String group = path.substring(1, path.indexOf("/", 1));
            if (StringUtils.isEmpty(group)) {
                throw new RuntimeException("Extract the default group failed! There's nothing between 2 '/'!");
            }
            return group;
        } catch (Exception e) {
            throw new RuntimeException(PREFIX_OF_LOGGER + moduleName + " Extract the path[" + path + "] group failed, Extract the default group failed, the path must be start with '/' and contain more than 2 '/'! " + e.getMessage());
        }
    }

    private void addRoute(Element element, MethodSpec.Builder loadRouteGroupMethod, List<RouteModel> routeModelDocList) {
        Route route = element.getAnnotation(Route.class);
        TypeMirror tm = element.asType();

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
        if (types.isSubtype(tm, activityType)) {
            typeDoc = "Activity";
            typeCode.add(".commitActivity($T.class)", element);
        } else if (types.isSubtype(tm, fragmentType)) {
            typeDoc = "Fragment";
            typeCode.add(".commitFragment($T.class)", element);
        } else {
            throw new RuntimeException(PREFIX_OF_LOGGER + moduleName + " The @Route(path='" + route.path() + "') is marked on unsupported class, look at [" + tm.toString() + "].");
        }
        unifyCode.add(typeCode.build());

        loadRouteGroupMethod.addStatement(unifyCode.build());
        DocumentUtils.addRouteDoc(moduleName, logger, element, routeModelDocList, route, typeDoc);
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
                    case BYTE_PACKAGE:
                    case BYTE_PRIMITIVE:
                        paramType = "putByte";
                        break;
                    case SHORT_PACKAGE:
                    case SHORT_PRIMITIVE:
                        paramType = "putShort";
                        break;
                    case INTEGER_PACKAGE:
                    case INTEGER_PRIMITIVE:
                        paramType = "putInt";
                        break;
                    case LONG_PACKAGE:
                    case LONG_PRIMITIVE:
                        paramType = "putLong";
                        break;
                    case FLOAT_PACKAGE:
                    case FLOAT_PRIMITIVE:
                        paramType = "putFloat";
                        break;
                    case DOUBEL_PACKAGE:
                    case DOUBEL_PRIMITIVE:
                        paramType = "putDouble";
                        break;
                    case BOOLEAN_PACKAGE:
                    case BOOLEAN_PRIMITIVE:
                        paramType = "putBoolean";
                        break;
                    case CHAR_PACKAGE:
                    case CHAR_PRIMITIVE:
                        paramType = "putChar";
                        break;
                    case STRING_PACKAGE:
                        paramType = "putString";
                        break;
                    default:
                        if (types.isSubtype(typeMirror, parcelableType)) {
                            paramType = "putParcelable";
                        } else if (types.isSubtype(typeMirror, serializableType)) {
                            paramType = "putSerializable";
                        } else {
                            throw new RuntimeException("@Param(type='" + typeMirror + "') is marked as an unsupported type");
                        }
                }

                if (StringUtils.isEmpty(param.name()) && !param.required()) {
                    tempParamCode.add(".$N($S)", paramType, key);
                } else {
                    if (!StringUtils.isEmpty(param.name())) {
                        tempParamCode.add(".$N($S, $S, $L)", paramType, key, param.name(), param.required());
                    } else {
                        tempParamCode.add(".$N($S, null, $L)", paramType, key, param.required());
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
