package com.wyjson.router.gradle_plugin.model

data class RouteHelperModel(
    val readme: String = "Do not delete this file, which is important to the GoRouter helper function!!!",
    val services: HashMap<String, ServiceModel> = HashMap(),
    val routes: HashMap<String, List<RouteModel>> = HashMap()
)