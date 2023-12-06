package com.wyjson.router.compiler.utils;

public class Constants {
    // Generate
    public static final String PROJECT = "GoRouter";
    public static final String SEPARATOR = "$$";
    // ModuleName$$Route
    public static final String ROUTE_MODULE_GENERATE_CLASS_NAME_SUFFIX = SEPARATOR + "Route";
    public static final String WARNING_TIPS = "DO NOT EDIT THIS FILE!!! IT WAS GENERATED BY GOROUTER.";
    public static final String DOCUMENT_FILE_NAME = "route-doc.json";

    // System interface
    public static final String ACTIVITY = "android.app.Activity";
    public static final String FRAGMENT = "androidx.fragment.app.Fragment";
    public static final String APPLICATION = "android.app.Application";
    public static final String CONTEXT = "android.content.Context";
    public static final String CONFIGURATION = "android.content.res.Configuration";
    public static final String NONNULL = "androidx.annotation.NonNull";
    public static final String BUNDLE = "android.os.Bundle";
    public static final String INTENT = "android.content.Intent";

    public static final String PACKAGE_NAME = "com.wyjson.router";
    public static final String DOCS = PACKAGE_NAME + ".docs";
    public static final String GOROUTER = PACKAGE_NAME + ".GoRouter";

    public static final String ROUTE_MODULE = PACKAGE_NAME + ".module.route";
    public static final String I_ROUTE_MODULE = PACKAGE_NAME + ".module.interfaces.IRouteModule";
    public static final String METHOD_NAME_LOAD = "load";
    public static final String METHOD_NAME_LOAD_ROUTE_FOR_x_GROUP = "loadRouteFor%sGroup";
    public static final String METHOD_NAME_LOAD_ROUTE_GROUP = "loadRouteGroup";
    public static final String PARAM_NAME_ROUTE_GROUPS = "routeGroups";

    public static final String I_ROUTE_MODULE_GROUP = PACKAGE_NAME + ".module.interfaces.IRouteModuleGroup";
    public static final String I_ROUTE_MODULE_GROUP_METHOD_NAME_LOAD = "load";

    public static final String ROUTE_CENTER = PACKAGE_NAME + ".core.RouteCenter";
    public static final String ROUTE_CENTER_METHOD_NAME_GET_ROUTE_GROUPS = "getRouteGroups";

    public static final String INJECT_CLASS_NAME_SUFFIX = SEPARATOR + "Inject";
    public static final String METHOD_NAME_INJECT_CHECK = "injectCheck";
    public static final String METHOD_NAME_INJECT = "inject";
    public static final String PARAM_EXCEPTION = PACKAGE_NAME + ".exception.ParamException";
    public static final String I_JSON_SERVICE = PACKAGE_NAME + ".interfaces.IJsonService";
    public static final String ROUTER_EXCEPTION = PACKAGE_NAME + ".exception.RouterException";
    public static final String TYPE_WRAPPER = PACKAGE_NAME + ".utils.TypeWrapper";


    // Log
    public static final String PREFIX_OF_LOGGER = PROJECT + "::Compiler ";
    public static final String NO_MODULE_NAME_TIPS = """
            These no module name, at 'build.gradle', like :
            android {
                defaultConfig {
                    ...
                    javaCompileOptions {
                        annotationProcessorOptions {
                            arguments = [GOROUTER_MODULE_NAME: project.getName()]
                        }
                    }
                }
            }
            """;

    // Options of processor
    public static final String KEY_MODULE_NAME = "GOROUTER_MODULE_NAME";

    // Java type
    private static final String LANG = "java.lang";
    public static final String BYTE_PACKAGE = LANG + ".Byte";
    public static final String SHORT_PACKAGE = LANG + ".Short";
    public static final String INTEGER_PACKAGE = LANG + ".Integer";
    public static final String LONG_PACKAGE = LANG + ".Long";
    public static final String FLOAT_PACKAGE = LANG + ".Float";
    public static final String DOUBEL_PACKAGE = LANG + ".Double";
    public static final String BOOLEAN_PACKAGE = LANG + ".Boolean";
    public static final String CHAR_PACKAGE = LANG + ".Character";
    public static final String STRING_PACKAGE = LANG + ".String";
    public static final String SERIALIZABLE_PACKAGE = "java.io.Serializable";
    public static final String PARCELABLE_PACKAGE = "android.os.Parcelable";

    public static final String BYTE_PRIMITIVE = "byte";
    public static final String SHORT_PRIMITIVE = "short";
    public static final String INTEGER_PRIMITIVE = "int";
    public static final String LONG_PRIMITIVE = "long";
    public static final String FLOAT_PRIMITIVE = "float";
    public static final String DOUBEL_PRIMITIVE = "double";
    public static final String BOOLEAN_PRIMITIVE = "boolean";
    public static final String CHAR_PRIMITIVE = "char";


    /**
     * Application Module
     */
    // ModuleName$$className$$AP(ApplicationProxy)
    public static final String APPLICATION_MODULE_GENERATE_CLASS_NAME_SUFFIX = "%s" + SEPARATOR + "%s" + SEPARATOR + "AP";
    public static final String APPLICATION_MODULE_PACKAGE_NAME = PACKAGE_NAME + ".module.application";
    public static final String I_APPLICATION_MODULE_PACKAGE_NAME = PACKAGE_NAME + ".interfaces.IApplicationModule";
    public static final String FIELD_MAM = "mAM";// mApplicationModule
    public static final String METHOD_NAME_SET_PRIORITY = "setPriority";
    public static final String METHOD_NAME_ON_CREATE = "onCreate";
    public static final String METHOD_NAME_ON_LOAD_ASYNC = "onLoadAsync";
    public static final String METHOD_NAME_ON_TERMINATE = "onTerminate";
    public static final String METHOD_NAME_ON_CONFIGURATION_CHANGED = "onConfigurationChanged";
    public static final String METHOD_NAME_ON_LOW_MEMORY = "onLowMemory";
    public static final String METHOD_NAME_ON_TRIM_MEMORY = "onTrimMemory";


}