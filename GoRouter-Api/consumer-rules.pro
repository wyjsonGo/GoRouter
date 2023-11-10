
# #   ########## GoRouter # start ##########
# 需要自动注入的参数不能混淆
-keepclassmembers class * {
    @com.wyjson.router.annotation.Param <fields>;
}
# 自动注册模块路由加载类不混淆
-keep class * implements com.wyjson.router.module.interfaces.IRouteModule
# #   ########## GoRouter # end ##########