
# #   ########## 基本指令 # start ##########
# 设置混淆的压缩比率 0 ~ 7
-optimizationpasses 5
# 混淆时不使用大小写混合，混淆后的类名为小写
-dontusemixedcaseclassnames
# 指定不去忽略非公共库的类
-dontskipnonpubliclibraryclasses
# 指定不去忽略非公共库的成员
-dontskipnonpubliclibraryclassmembers
# 混淆时不做预校验
-dontpreverify
# 混淆时不记录日志
-verbose
# 忽略警告
-ignorewarnings
# 保留注解不混淆
-keepattributes *Annotation*,InnerClasses
# 避免混淆泛型
-keepattributes Signature
# 保留代码行号，方便异常信息的追踪
-keepattributes SourceFile,LineNumberTable
# 保留代码行号，隐藏原始源文件名
-renamesourcefileattribute SourceFile
# 混淆采用的算法
-optimizations !code/simplification/cast,!field/*,!class/merging/*
# #   ########## 基本指令 # end ##########


# #   ########## 不需混淆的Android类 # start ##########
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
# #   ########## 不需混淆的Android类 # end ##########


# #   ########## androidx # start ##########
-dontwarn androidx.**
-keep class androidx.** {*;}
-keep interface androidx.** {*;}
-keep public class * extends androidx.**
-keep class com.google.android.material.** {*;}
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
# #   ########## androidx # end ##########


# #   ########## android support # start ##########
-keep class android.support.** {*;}
-dontwarn android.support.**
-keep interface android.support.** { *; }
# #   ########## android support # end ##########

















