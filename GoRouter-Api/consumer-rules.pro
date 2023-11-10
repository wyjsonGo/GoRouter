
# #   ########## GoRouter # start ##########
# 需要自动注入的参数不能混淆
-keepclassmembers class * {
    @com.wyjson.router.annotation.Param <fields>;
}
# 自动注册模块路由加载类不混淆
-keep class * implements com.wyjson.router.module.interfaces.IRouteModule
# #   ########## GoRouter # end ##########


# #   ########## delete GoRouter logger # start ##########
# 1.使用混淆优化文件 proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
# 2.不开启 -dontoptimize
# 3.满足以上两点下面的配置才会生效
-assumenosideeffects class com.wyjson.router.logger.DefaultLogger {
    public *** debug(...);
    public *** info(...);
    public *** warning(...);
    public *** error(...);
}
# #   ########## GoRouter # end ##########