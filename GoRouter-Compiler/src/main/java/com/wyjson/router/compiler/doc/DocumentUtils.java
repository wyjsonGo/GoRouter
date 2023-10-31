package com.wyjson.router.compiler.doc;

import static com.wyjson.router.compiler.utils.Constants.DOCS_PACKAGE_NAME;

import com.google.gson.GsonBuilder;
import com.wyjson.router.annotation.Interceptor;
import com.wyjson.router.annotation.Route;
import com.wyjson.router.annotation.Service;
import com.wyjson.router.compiler.doc.model.DocumentModel;
import com.wyjson.router.compiler.doc.model.InterceptorModel;
import com.wyjson.router.compiler.doc.model.ParamModel;
import com.wyjson.router.compiler.doc.model.RouteModel;
import com.wyjson.router.compiler.doc.model.ServiceModel;
import com.wyjson.router.compiler.utils.Logger;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
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
                    moduleName + "-gorouter-doc.json"
            ).openWriter();
        } catch (IOException e) {
            logger.error(moduleName + " Failed to create the document because " + e.getMessage());
        }
    }

    public static void generate(String moduleName, Logger logger) {
        if (!isDocEnable)
            return;
        try {
            docWriter.append(new GsonBuilder().setPrettyPrinting().create().toJson(documentModel));
            docWriter.flush();
            docWriter.close();
        } catch (IOException e) {
            logger.error(moduleName + " Failed to generate the document because " + e.getMessage());
        }
    }

    public static void addService(String moduleName, Logger logger, Element element, Service service) {
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

    public static void addInterceptor(String moduleName, Logger logger, Element element, Interceptor interceptor) {
        if (!isDocEnable)
            return;
        try {
            documentModel.getInterceptors().add(new InterceptorModel(interceptor.priority(), element.toString(), interceptor.remark()));
        } catch (Exception e) {
            logger.error(moduleName + " Failed to add interceptor [" + element.toString() + "] document, " + e.getMessage());
        }
    }

    public static void addRoute(String moduleName, Logger logger, Element element, Route route, String typeDoc, List<ParamModel> paramModels) {
        if (!isDocEnable)
            return;
        try {
            RouteModel routeModel = new RouteModel();
            routeModel.setPath(route.path());
            routeModel.setType(typeDoc);
            routeModel.setPathClass(element.toString());
            routeModel.setRemark(route.remark());
            if (route.tag() != 0) {
                routeModel.setTag(route.tag());
            }
            if (!paramModels.isEmpty()) {
                routeModel.setParamsType(paramModels);
            }
            documentModel.getRoutes().add(routeModel);
        } catch (Exception e) {
            logger.error(moduleName + " Failed to add route [" + element.toString() + "] document, " + e.getMessage());
        }
    }

}
