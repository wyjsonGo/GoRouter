package com.wyjson.router.gradle_plugin.doc.model

data class DocumentModel(
    val services: HashMap<String, ServiceModel> = HashMap(),
    val interceptors: ArrayList<InterceptorModel> = ArrayList(),
    val routes: HashMap<String, List<RouteModel>> = HashMap()
)