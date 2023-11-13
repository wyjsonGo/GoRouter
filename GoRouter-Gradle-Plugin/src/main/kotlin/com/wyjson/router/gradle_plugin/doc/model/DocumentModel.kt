package com.wyjson.router.gradle_plugin.doc.model

data class DocumentModel(
    var services: HashMap<String, ServiceModel> = HashMap(),
    var interceptors: ArrayList<InterceptorModel> = ArrayList(),
    var routes: HashMap<String, List<RouteModel>> = HashMap()
)