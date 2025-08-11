# [GoRouter](https://github.com/wyjsonGo/GoRouter)

[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/wyjsonGo/GoRouter/blob/main/LICENSE)
[![Release Version](https://jitpack.io/v/wyjsonGo/GoRouter.svg)](https://jitpack.io/#wyjsonGo/GoRouter)

## 1、添加依赖和配置Kotlin DSL (`.kts`)语法

##### 1.  `libs.versions.toml`

```toml
[versions]
gorouter = "2.5.6"

[libraries]
gorouter-api = { group = "com.github.wyjsonGo.GoRouter", name = "GoRouter-Api", version.ref = "gorouter" }
gorouter-compiler = { group = "com.github.wyjsonGo.GoRouter", name = "GoRouter-Compiler", version.ref = "gorouter" }

[plugins]
gorouter-plugin = { id = "com.github.wyjsonGo.GoRouter", version.ref = "gorouter" }
```

##### 2.  `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        ...
        maven { url = uri("https://jitpack.io") }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.github.wyjsonGo.GoRouter")) {
                useModule("com.github.wyjsonGo.GoRouter:GoRouter-Gradle-Plugin:${requested.version}")
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven { url = uri("https://jitpack.io") }
    }
}
```

##### 3.  项目根目录下`build.gradle.kts`

```kotlin
plugins {
	...
    alias(libs.plugins.gorouter.plugin) apply false
}

```

##### 4.  底层`module_base`项目目录下`build.gradle.kts`

```kotlin
dependencies {
    api(libs.gorouter.api)
    annotationProcessor(libs.gorouter.compiler)
}
```

##### 5.  app目录下`build.gradle.kts`

```kotlin
plugins {
    ...
    id("com.wyjson.gorouter")
}

GoRouter {
    runAutoRegisterBuildTypes = arrayOf("release")
    helperToRootModuleName = "module_common"
}
```

##### 6.  在module项目下添加注解处理器依赖和配置

```kotlin
android {
    defaultConfig {
        ...
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("GOROUTER_MODULE_NAME" to project.name)
            }
        }
    }
}

dependencies {
    annotationProcessor(libs.gorouter.compiler)
}
```
