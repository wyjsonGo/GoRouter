package com.wyjson.router.gradle_plugin.helper.model

data class RouteHelperModel(
    val services: HashMap<String, ServiceModel> = HashMap(),
    val routes: HashMap<String, List<RouteModel>> = HashMap()
)