package com.wyjson.router.compiler.doc;

import static com.wyjson.router.compiler.utils.Constants.DOCS_PACKAGE_NAME;
import static com.wyjson.router.compiler.utils.Constants.DOCUMENT_FILE_NAME;

import com.google.gson.Gson;
import com.wyjson.router.annotation.Interceptor;
import com.wyjson.router.annotation.Param;
import com.wyjson.router.annotation.Route;
import com.wyjson.router.annotation.Service;
import com.wyjson.router.compiler.doc.model.DocumentModel;
import com.wyjson.router.compiler.doc.model.InterceptorModel;
import com.wyjson.router.compiler.doc.model.ParamModel;
import com.wyjson.router.compiler.doc.model.RouteModel;
import com.wyjson.router.compiler.doc.model.ServiceModel;
import com.wyjson.router.compiler.utils.Logger;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.StandardLocation;

public class DocumentUtils {

    private static Writer docWriter;
    private static DocumentModel documentModel;
    private static boolean isDocEnable;

    public static void createDoc(Filer mFiler, String moduleName, Logger logger, boolean isEnable) {
        isDocEnable = isEnable;
        if (!isDocEnable)
            return;
        try {
            documentModel = new DocumentModel();
            docWriter = mFiler.createResource(
                    StandardLocation.SOURCE_OUTPUT,
                    DOCS_PACKAGE_NAME,
                    moduleName + "-" + DOCUMENT_FILE_NAME
            ).openWriter();
        } catch (IOException e) {
            logger.error(moduleName + " Failed to create the document because " + e.getMessage());
        }
    }

    public static void generateDoc(String moduleName, Logger logger) {
        if (!isDocEnable)
            return;
        try {
            docWriter.append(new Gson().toJson(documentModel));
            docWriter.flush();
            docWriter.close();
        } catch (IOException e) {
            logger.error(moduleName + " Failed to generate the document because " + e.getMessage());
        }
    }

    public static void addServiceDoc(String moduleName, Logger logger, Element element, Service service) {
        if (!isDocEnable)
            return;
        try {
            String className = ((TypeElement) element).getInterfaces().get(0).toString();
            String serviceName = className.substring(className.lastIndexOf(".") + 1);
            documentModel.getServices().put(serviceName, new ServiceModel(className, element.toString(), service.remark()));
        } catch (Exception e) {
            logger.error(moduleName + " Failed to add service [" + element.toString() + "] document, " + e.getMessage());
        }
    }

    public static void addInterceptorDoc(String moduleName, Logger logger, Element element, Interceptor interceptor) {
        if (!isDocEnable)
            return;
        try {
            documentModel.getInterceptors().add(new InterceptorModel(interceptor.ordinal(), element.toString(), interceptor.remark()));
        } catch (Exception e) {
            logger.error(moduleName + " Failed to add interceptor [" + element.toString() + "] document, " + e.getMessage());
        }
    }

    public static void addRouteGroupDoc(String moduleName, Logger logger, String group, List<RouteModel> routeModelList) {
        if (!isDocEnable)
            return;
        try {
            documentModel.getRoutes().put(group, routeModelList);
        } catch (Exception e) {
            logger.error(moduleName + " Failed to add route group [" + group + "] document, " + e.getMessage());
        }
    }

    public static void addRouteDoc(String moduleName, Logger logger, Element element, List<RouteModel> routeModelList, Route route, String typeDoc) {
        if (!isDocEnable)
            return;
        try {
            RouteModel routeModel = new RouteModel();
            routeModel.setPath(route.path());
            routeModel.setType(typeDoc);
            routeModel.setPathClass(element.toString());
            if (!StringUtils.isEmpty(route.remark())) {
                routeModel.setRemark(route.remark());
            }
            if (route.tag() != 0) {
                routeModel.setTag(route.tag());
            }
            addParamCode(moduleName, logger, element, routeModel);
            routeModelList.add(routeModel);
        } catch (Exception e) {
            logger.error(moduleName + " Failed to add route [" + element.toString() + "] document, " + e.getMessage());
        }
    }

    private static void addParamCode(String moduleName, Logger logger, Element element, RouteModel routeModel) {
        List<ParamModel> tempParamModels = new ArrayList<>();
        for (Element field : element.getEnclosedElements()) {
            if (field.getKind().isField() && field.getAnnotation(Param.class) != null) {
                Param param = field.getAnnotation(Param.class);
                String paramName = field.getSimpleName().toString();
                TypeMirror typeMirror = field.asType();
                String typeStr = typeMirror.toString();

                ParamModel paramModel = new ParamModel();
                if (!StringUtils.isEmpty(param.remark())) {
                    paramModel.setRemark(param.remark());
                }
                paramModel.setRequired(param.required());

                if (typeStr.contains(".")) {
                    paramModel.setType(typeStr.substring(typeStr.lastIndexOf(".") + 1));
                } else {
                    paramModel.setType(typeStr);
                }

                if (StringUtils.isEmpty(param.name()) && !param.required()) {
                    paramModel.setName(paramName);
                } else {
                    if (!StringUtils.isEmpty(param.name())) {
                        paramModel.setName(param.name());
                    } else {
                        paramModel.setName(paramName);
                    }
                }
                tempParamModels.add(paramModel);
            }
        }

        // The parent class parameter is processed before the subclass parameter
        if (!tempParamModels.isEmpty()) {
            tempParamModels.addAll(routeModel.getParamsType());
            routeModel.setParamsType(tempParamModels);
        }

        // if has parent?
        TypeMirror parent = ((TypeElement) element).getSuperclass();
        if (parent instanceof DeclaredType) {
            Element parentElement = ((DeclaredType) parent).asElement();
            if (parentElement instanceof TypeElement && !((TypeElement) parentElement).getQualifiedName().toString().startsWith("android")) {
                addParamCode(moduleName, logger, parentElement, routeModel);
            }
        }
    }

}
