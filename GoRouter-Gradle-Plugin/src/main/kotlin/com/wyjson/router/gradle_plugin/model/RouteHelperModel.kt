package com.wyjson.router.gradle_plugin.model

data class RouteHelperModel(
    val services: HashMap<String, ServiceModel> = HashMap(),
    val routes: HashMap<String, List<RouteModel>> = HashMap()
)