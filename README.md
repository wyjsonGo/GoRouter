# [GoRouter](https://github.com/wyjsonGo/GoRouter)

[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/wyjsonGo/GoRouter/blob/main/LICENSE)
[![Release Version](https://jitpack.io/v/wyjsonGo/GoRouter.svg)](https://jitpack.io/#wyjsonGo/GoRouter)

> 一个用于帮助 Android App 进行组件化改造的框架 —— 支持模块间的路由、通信、解耦

## 简介

之前一直在用阿里开源的[ARouter](https://github.com/alibaba/ARouter)项目，因为ARouter多年未更新，ARouter开始有些不太适合了，所以重新开发了这款Android路由框架，同样的API，更多的功能，迁移请参见文末6-7。

## GoRouter和ARouter功能差异对比

| 功能                    | ARouter  | GoRouter  | 描述                                                  |
| ---------------------- | -------- | --------- | ---------------------------------------------------- |
| 路由注册方式             | 注解      | 注解、java | GoRouter不仅提供了注解，还能使用java方式注册，参见5-6 |
| 服务                    | 一对多    | 一对一     | ARouter可以为一个服务接口注册多个实现类(没啥用)，本库一个服务接口对应一个实现方法(调用更方便) |
| 动态注册拦截器            | 不支持    | 支持      | ARouter只能动态注册路由，不能动态注册拦截器 |
| 重写跳转URL服务          | 支持      | 不支持     | 可以在`IPretreatmentService`里实现相同功能 |
| 获取元数据               | 不支持    | 支持       | 有些场景需要判断某个页面当前是否存在等需求，就需要获取页面class等信息，参见5-1 |
| withObject()           | 支持      | 不支持     | ARouter实现原理是转JSON后使用`withString()`方法传递 |
| inject(T)              | 单一      | 更多       | ARouter不能在`onNewIntent()`方法里使用，GoRouter提供了更多使用场景 |
| 按组分类、按需初始化       | 支持      | 支持       | ARouter不允许多个module中存在相同的分组，GoRouter允许 |
| 模块Application生命周期   | 不支持    | 支持       | 主动分发到业务模块，让模块无侵入的获取Application生命周期，参见6-1 |
| 路由页面Event            | 不支持    | 支持       | 页面事件解耦,提供更多、更方便的API，参见5-10 |

***

## 一、功能介绍

1.  **支持直接解析标准URL进行跳转，并自动注入参数到目标页面中**
2.  **支持多模块工程使用**
3.  **支持添加多个拦截器，自定义拦截顺序**
4.  **支持依赖注入，可单独作为依赖注入框架使用**
5.  **支持InstantRun**
6.  **支持MultiDex**(Google方案)
7.  映射关系按组分类、多级管理，按需初始化
8.  支持用户指定全局降级与局部降级策略
9.  页面、拦截器、服务等组件均自动注册到框架
10. 支持多种方式配置转场动画
11. 支持获取Fragment
12. **支持第三方 App 加固**
13. **支持生成路由文档**
14. 支持增量编译
15. 支持动态注册路由、路由组、服务和拦截器
16. 支持模块Application生命周期
16. 支持路由页面Event

## 二、典型应用

1.  从外部URL映射到内部页面，以及参数传递与解析
2.  跨模块页面跳转，模块间解耦
3.  拦截跳转过程，处理登陆、埋点等逻辑
4.  跨模块API调用，通过控制反转来做组件解耦

## 三、基础功能

##### 1.  添加依赖和配置

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

[![Release Version](https://jitpack.io/v/wyjsonGo/GoRouter.svg)](https://jitpack.io/#wyjsonGo/GoRouter)

```groovy
dependencies {
    // x.x.x 替换为jitpack最新版本
    implementation 'com.github.wyjsonGo.GoRouter:GoRouter-Api:x.x.x'
}
```

##### 2.  在module项目下添加注解处理器依赖和配置 (如只使用java方式注册,可忽略此步骤)

```groovy
android {
    defaultConfig {
        ...
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [GOROUTER_MODULE_NAME: project.getName()]
            }
        }
    }
}
```

[![Release Version](https://jitpack.io/v/wyjsonGo/GoRouter.svg)](https://jitpack.io/#wyjsonGo/GoRouter)

```groovy
dependencies {
    // x.x.x 替换为jitpack最新版本
    annotationProcessor 'com.github.wyjsonGo.GoRouter:GoRouter-Compiler:x.x.x'
}
```

module_user模块Demo示例[module_user/build.gradle](https://github.com/wyjsonGo/GoRouter/blob/master/module_user/build.gradle)

##### 3.  添加路由

```java
// 在支持路由的页面上添加注解
// 这里的路径需要注意的是至少需要有两级，/xx/xx
@Route(path = "/test/activity")
public class TestActivity extend Activity {
    ...
}
```

##### 4.  初始化SDK

```java
if (BuildConfig.DEBUG) {
    GoRouter.openDebug(); // 开启调试，查看路由详细加载和跳转过程日志
}
GoRouter.autoLoadRouteModule(this); // 尽可能早，推荐在Application中初始化
```

##### 5.  发起路由操作

```java
// 1. 应用内简单的跳转(通过URL跳转在'进阶用法'中)
GoRouter.getInstance().build("/test/activity").go(this);

// 2. 跳转并携带参数
GoRouter.getInstance().build("/test/fragment")
            .withInt("age", 35)
            .withString("name", "Wyjson")
            .go(this);
```

##### 6.  使用Gradle插件实现路由表的自动加载，支持Gradle8.0+ (可选)

```groovy
// 项目根目录下的settings.gradle
pluginManagement {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

[![Release Version](https://jitpack.io/v/wyjsonGo/GoRouter.svg)](https://jitpack.io/#wyjsonGo/GoRouter)

```groovy
// 项目根目录下的build.gradle
buildscript {
    dependencies {
        // x.x.x 替换为jitpack最新版本
        classpath 'com.github.wyjsonGo.GoRouter:GoRouter-Gradle-Plugin:x.x.x'
    }
}
```

```groovy
// app目录下的build.gradle
plugins {
    ...
    id 'com.wyjson.gorouter'
}
```
*   支持Gradle8.0+，Gradle8.0以下参见5-7。
*   开发阶段构建加速参见5-8(最好在开发阶段开启,节省build时间)。
*   可选使用，通过GoRouter提供的注册插件进行路由表的自动加载，默认通过扫描dex的方式进行加载(在运行时注册,节省打包时间)，通过gradle插件进行自动注册可以缩短运行时初始化时间(在打包时注册,节省运行时间)，解决应用加固导致无法直接访问dex文件。

## 四、进阶用法

##### 1.  通过URL跳转

```java
// 新建一个Activity用于监听Scheme事件,之后直接把url传递给GoRouter即可
public class SchemeFilterActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri uri = getIntent().getData();
        GoRouter.getInstance().build(uri).go(this);
        finish();
    }
}
```

AndroidManifest.xml

```xml
<activity android:name=".activity.SchemeFilterActivity">
    <!-- Scheme -->
    <intent-filter>
        <data
        android:host="m.wyjson.com"
        android:scheme="gorouter"/>

        <action android:name="android.intent.action.VIEW"/>

        <category android:name="android.intent.category.DEFAULT"/>
        <category android:name="android.intent.category.BROWSABLE"/>
    </intent-filter>
</activity>
```

##### 2.  解析参数

```java
// 为每一个参数声明一个字段，并使用 @Param 标注
// URL中不能传递Parcelable类型数据，通过GoRouter api可以传递Parcelable对象
// 支持父类字段自动注入
@Route(path = "/param/activity")
public class ParamActivity extends BaseParamActivity {

    @Param
    int age = 18;

    // 可以自定义参数name
    @Param(name = "nickname", remark = "昵称", required = true)
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       GoRouter.getInstance().inject(this);
       // GoRouter会自动对字段进行赋值，无需主动获取
       Log.d("param", "base:" + base + "age:" + age + ",name:" + name);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        GoRouter.getInstance().inject(this, intent);
    }
}

public class BaseParamActivity extends Activity {
    @Param(remark = "我是一个父类字段")
    protected int base;
}
```

##### 3.  声明拦截器(拦截跳转过程，面向切面编程)

```java
// 比较经典的应用就是在跳转过程中处理登陆事件，这样就不需要在目标页重复做登陆检查
// 拦截器会在跳转之间执行，多个拦截器会按序号从小到大顺序依次执行
@Interceptor(ordinal = 1, remark = "测试拦截器")
public class TestInterceptor implements IInterceptor {
    @Override
    public void process(Card card, InterceptorCallback callback) {
        ...
        callback.onContinue(card); // 处理完成，交还控制权
        // callback.onInterrupt(card, new RouterException("我觉得有点异常")); // 觉得有问题，中断路由流程

        // 以上两种至少需要调用其中一种，否则不会继续路由
    }

    @Override
    public void init() {
        // 拦截器的初始化，会在sdk初始化的时候调用该方法，仅会调用一次
    }
}
```

##### 4.  处理跳转结果

```java
// 使用两个参数的go方法，可以获取单次跳转的结果
GoRouter.getInstance().build("/test/activity").go(this, new GoCallback() {
    @Override
    public void onFound(Card card) {

    }

    @Override
    public void onLost(Card card) {

    }

    @Override
    public void onArrival(Card card) {

    }

    @Override
    public void onInterrupt(Card card,@NonNull Throwable exception) {

    }
});
```

##### 5.  自定义全局降级策略

```java
// 实现IDegradeService接口
@Service(remark = "全局降级策略")
public class DegradeServiceImpl implements IDegradeService {
   @Override
   public void onLost(Context context, Card card) {
       // do something.
   }

   @Override
   public void init() {

   }
}
```

##### 6.  为目标页面声明更多信息

```java
// 我们经常需要在目标页面中配置一些属性，比方说"是否需要登陆"之类的
// 可以通过@Route注解的tag属性进行扩展，这个属性是一个 int值，换句话说，单个int有4字节，也就是32位，可以配置32个开关
// 剩下的可以自行发挥，通过字节操作可以标识32个开关，通过开关标记目标页面的一些属性，在拦截器中可以拿到这个标记进行业务逻辑判断
@Route(path = "/user/info/activity", tag = LOGIN | AUTHENTICATION)
```

tag使用示例[UserInfoActivity.java](https://github.com/wyjsonGo/GoRouter/blob/master/module_user/src/main/java/com/wyjson/module_user/activity/UserInfoActivity.java)，
tag处理示例[SignInInterceptor.java](https://github.com/wyjsonGo/GoRouter/blob/master/module_user/src/main/java/com/wyjson/module_user/route/interceptor/SignInInterceptor.java)

##### 7.  通过依赖注入解耦:服务管理(一) 暴露服务

```java
// 声明接口,其他组件通过接口来调用服务
public interface HelloService extends IService {
    String sayHello(String name);
}

// 实现接口
@Service(remark = "服务描述")
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {
       return "hello, " + name;
    }

    @Override
    public void init() {

    }
}
```

##### 8.  通过依赖注入解耦:服务管理(二) 发现服务

```java
HelloService helloService = GoRouter.getInstance().getService(HelloService.class);
if (helloService != null) {
    helloService.sayHello("Wyjson");
}
```

##### 9.  跳转前预处理

```java
// 比如跳转登录页面，只要简单的调用go就可以了，一些必须的参数和标识可以放到预处理里来做。
// 或者一些埋点的处理
// 实现IPretreatmentService接口
@Service(remark = "预处理服务")
public class PretreatmentServiceImpl implements IPretreatmentService {

   @Override
   public void init() {

   }

   @Override
   public boolean onPretreatment(Context context, Card card) {
       // 登录页面预处理
       if ("/user/sign_in/activity".equals(card.getPath())) {
           card.withFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
       }
       return true; // 跳转前预处理，如果需要自行处理跳转，该方法返回 false 即可
   }
}
```

## 五、更多功能

##### 1.  根据路径获取元数据

```java
// 有些场景需要判断某个页面当前是否存在等需求,就需要获取页面class等信息，可以使用此方法getCardMete()
CardMeta cardMeta = GoRouter.getInstance().build("/user/info/activity").getCardMeta();
if (cardMeta != null) {
    cardMeta.getPathClass();
}
```

##### 2.  详细的API说明

```java
// 标准的路由请求
GoRouter.getInstance().build("/main/activity").go(this);

// 通过Uri直接解析(外部、h5等调用native页面携带参数可以使用此方式)
Uri uri = Uri.parse("/new/param/activity?age=9&name=jack&base=123");
GoRouter.getInstance().build(uri).go(this);

// 构建标准的路由请求，startActivityForResult()
// go的第一个参数必须是Activity，第二个参数则是RequestCode
// 当然也支持registerForActivityResult()方法
GoRouter.getInstance().build("/main/activity").go(this, 5);

// 直接传递Bundle
Bundle params = new Bundle();
GoRouter.getInstance()
    .build("/main/activity")
    .with(params)
    .go(this);

// 指定Flag
GoRouter.getInstance()
    .build("/main/activity")
    .withFlags()
    .go(this);

// 获取Fragment
Fragment fragment = (Fragment) GoRouter.getInstance().build("/test/fragment").go(this);

// 序列化对象传递
GoRouter.getInstance().build("/main/activity")
    .withSerializable("user",new User())
    .go(this);

// 觉得接口不够多，可以直接拿出Bundle赋值
GoRouter.getInstance()
        .build("/main/activity")
        .getExtras();

// 转场动画(常规方式)
GoRouter.getInstance()
    .build("/test/activity")
    .withTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom)
    .go(this);

// 转场动画(API16+)
ActivityOptionsCompat compat = ActivityOptionsCompat.
    makeScaleUpAnimation(v, v.getWidth() / 2, v.getHeight() / 2, 0, 0);

// ps. makeSceneTransitionAnimation 使用共享元素的时候，需要在go方法中传入当前Activity

GoRouter.getInstance()
    .build("/test/activity")
    .withActivityOptionsCompat(compat)
    .go(this);

// 使用绿色通道(跳过所有的拦截器)
GoRouter.getInstance().build("/main/activity").greenChannel().go(this);

// 使用自己的日志工具打印日志
GoRouter.setLogger();

// 使用自己提供的线程池
GoRouter.setExecutor();

// 开启openDebug()后,打印日志的时候打印线程堆栈
GoRouter.printStackTrace();
```

##### 3.  获取原始的URI

```java
String uriStr = GoRouter.getInstance().getRawURI(this);
```

##### 4.  获取当前页面路径

```java
String path = GoRouter.getInstance().getCurrentPath(this);
```

##### 5.  获取路由注册模式

```java
// true: Gradle插件进行自动注册(在打包时注册,节省运行时间)
// false: 扫描dex的方式(在运行时注册,节省打包时间)
GoRouter.getInstance().isRouteRegisterMode();
```

##### 6. java方式动态注册路由信息

适用于插件化架构的App以及需要动态注册路由、路由组、服务和拦截器的场景。目标页面、服务和拦截器可以不标注`@Route`、`@Service`、`@Interceptor`注解。

```java
// 动态注册服务(重复添加相同服务会被覆盖(更新))
GoRouter.getInstance().addService(UserServiceImpl.class);

// 注册拦截器(重复添加相同序号会catch)
GoRouter.getInstance().addInterceptor(1, TestInterceptor.class);

// 动态注册拦截器(重复添加相同序号会覆盖(更新))
GoRouter.getInstance().setInterceptor(1, TestInterceptor.class);

// 动态注册路由分组,按需加载路由(注意：同一批次仅允许相同group的路由信息注册)
GoRouter.getInstance().addRouterGroup("show", new IRouteModuleGroup() {
    @Override
    public void load() {
        GoRouter.getInstance().build("/show/list/activity").commitActivity(ShowListActivity.class);
        GoRouter.getInstance().build("/show/info/fragment").commitFragment(ShowInfoFragment.class);
        ...
    }
});

// 当然,你也可以直接注册Activity、Fragment路由
// 动态注册Activity
GoRouter.getInstance().build("/user/info/activity").putTag(3).commitActivity(UserInfoActivity.class);
// 动态注册Fragment
GoRouter.getInstance().build("/new/param/fragment").putInt("age").putString("name").commitFragment(ParamFragment.class);
```

##### 7.  自定义模块路由加载

如不使用gradle插件[3-6]进行自动注册，也不想走默认扫描dex的方式，可以不调用`GoRouter.autoLoadRouteModule(this)`方法，但需要自行调用模块生成的路由加载类。
模块项目里至少使用一条注解`@Route`、`@Service`、`@Interceptor`，就会生成对应路由表的加载类。路由表加载类命名规则会根据`GOROUTER_MODULE_NAME`设置的模块名称转换成大写驼峰命名+`$$Route.java`，所有模块生成的路由表加载类都会放到`com.wyjson.router.module.route`包下。
例如模块名称`module_user`会生成`ModuleUser$$Route.java`

```java
// 可在任意地方调用模块路由加载类
new ModuleUser$$Route().load();
```

##### 8.  Gradle插件自定义执行的任务

由于在开发阶段需要经常build项目，每次运行都走gradle插件自动注册后，会导致gradle自带任务`dexBuilderDebug`(转换class文件为dex文件)会很耗时,所以在开发阶段最好忽略buildType等于debug的情况，debug的情况就会走默认扫描dex方式注册，节省开发build时间。

```groovy
// app目录下的build.gradle
plugins {
    ...
    id 'com.wyjson.gorouter'
}
// 不写下面的配置，默认android.buildTypes任务全部执行自动注册。
GoRouter {
    // 允许执行自动注册任务的集合
    buildTypes "release", "sandbox", "more"
}
```

##### 9.  生成路由文档

使用gradle命令一键生成路由文档

```sh
./gradlew generateRouteDoc
# 或使用快速生成
./gradlew quickGenerateRouteDoc
```
当然你也可以使用图形页面执行任务

<img src="https://github.com/wyjsonGo/GoRouter/blob/master/screenshot/gradle_task_generate_router_doc.png" width="40%" />

两条任务的区别是:

*   执行`generateRouteDoc`任务会先自动触发`build`任务生成各个模块子路由文档,在触发生成最终的路由文档。
*   执行`quickGenerateRouteDoc`任务会直接去获取子模块路由文档，生成最终的路由文档(如果你已经运行过项目,可以使用此任务快速得到结果)。

生成的路由文档会保存到项目下的`/app/项目名-route-doc.json`,Demo示例[/app/GoRouter-route-doc.json](https://github.com/wyjsonGo/GoRouter/blob/master/app/GoRouter-route-doc.json)

##### 10.  路由页面Event

在之前跨模块页面事件通知的流程是，使用EventBus库，在module_common模块里定义event类，页面注册订阅者接收事件，实现事件处理并注解标识，页面销毁解除注册。
这一套流程下来步骤很多，会出现很多event类，而且这些类只在一个页面订阅，还要去module_common模块里定义，发布基础数据类型，会导致所有订阅者都会收到，也无法检测页面生命周期状态。
显然EventBus适合任意处发布多处订阅的场景，而我们需要任意处发布一处订阅的场景，这样就可以订阅基础数据类型了，自定义类型也不需要再包裹一层新的Event类发布出去。

典型场景

1.  消息Fragment通知主页Activity的tab刷新未读消息数。
2.  代替startActivityForResult()，获取返回来的数据。
3.  Activity显示其他模块Dialog，实时显示Dialog里选择的数据。

使用

```java
// 在Activity/Fragment上订阅String类型事件
GoRouter.getInstance().registerEvent(this, String.class, new Observer<String>() {
    @Override
    public void onChanged(String value) {
        // do something.
    }
});

// 在任意地方向MainActivity发送String类型数据
GoRouter.getInstance().postEvent("/main/activity", "Go!");
```

更多用法

```java
// 订阅自定义类型事件
GoRouter.getInstance().registerEvent(this, UserEntity.class, new Observer<UserEntity>() {
    @Override
    public void onChanged(UserEntity data) {
        // do something.
    }
});

// 向UserFragment发送自定义类型数据
GoRouter.getInstance().postEvent("/user/fragment", new UserEntity(89, "Wyjson"));

// 手动解除String类型全部订阅
GoRouter.getInstance().unRegisterEvent(this, String.class);
// 手动解除String类型指定observer订阅
GoRouter.getInstance().unRegisterEvent(this, String.class, observer);
```

*   `registerEvent()`和`registerEventForever()`支持在Activity、Fragment上使用。
*   `registerEvent()`方法只有页面处于活跃状态下才会收到，`registerEventForever()`方法无论页面处于何种状态下都会收到。
*   `postEvent()`方法支持主线程和子线程调用。多次调用的情况下，在页面生命周期处于`onPause`下，`registerEvent()`方法只会收到最后一次数据，`registerEventForever()`会全部收到。在页面生命周期处于`onResume`下，两个方法会收到全部数据。
*   `unRegisterEvent()`方法无需主动调用，框架会在页面销毁时自动解除订阅，除非你想立刻取消订阅。
*   页面可以订阅多个相同类型的事件和多个不同类型的事件。
*   路由页面Event功能内部使用`LiveData`。

Demo示例[EventActivity.java](https://github.com/wyjsonGo/GoRouter/blob/master/module_main/src/main/java/com/wyjson/module_main/activity/EventActivity.java)

## 六、模块Application生命周期

Application生命周期主动分发到组件，让模块无侵入的获得Application生命周期

##### 1.  在模块中添加`@ApplicationModule`注解，实现`IApplicationModule`接口

```java
@ApplicationModule
public class UserApplication implements IApplicationModule {

    @Override
    public void onCreate(Application app) {
        // do something.
    }

    /**
     * 优化启动速度,一些不着急的初始化可以放在这里做,子线程
     */
    @Override
    public void onLoadAsync(Application app) {
        // do something.
    }
}
```

Demo示例[CommonApplication.java](https://github.com/wyjsonGo/GoRouter/blob/master/module_common/src/main/java/com/wyjson/module_common/CommonApplication.java)

##### 2.  在主Application添加分发

```java
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 调用callAMOnCreate()方法，触发模块Application的onCreate()、onLoadAsync()方法。
        GoRouter.callAMOnCreate(this);
    }
```

Demo示例[MyApplication.java](https://github.com/wyjsonGo/GoRouter/blob/master/app/src/main/java/com/wyjson/go_router/MyApplication.java)

##### 3.  进阶用法

*   指定模块Application优先级，可以重写`setPriority()`方法，它将按照从大到小的执行顺序执行。
*   `IApplicationModule`接口不仅提供了`onCreate`、`onLoadAsync`方法，还提供了`onTerminate`、`onConfigurationChanged`、`onLowMemory`、`onTrimMemory`方法，如需监听记得在主Application中添加他们的分发方法`GoRouter.callAMOnCreate()`、`GoRouter.callAMOnTerminate()`、`GoRouter.callAMOnConfigurationChanged(newConfig)`、`GoRouter.callAMOnLowMemory()`、`GoRouter.callAMOnTrimMemory(level)`。

## 七、其他

##### 1.  路由中的分组概念

*   SDK中针对所有的路径`/test/1`、`/test/2`进行分组，分组只有在分组中的某一个路径第一次被访问的时候，该分组才会被初始化。分组使用路径中第一段字符串(/*/)作为分组，这里的路径需要注意的是至少需要有两级`/xx/xx`。
*   GRouter允许一个module中存在多个分组，也允许多个module中存在相同的分组，但是最好不要在多个module中存在相同的分组，因为在注册路由组时发现存在相同的分组，会立即注册老的路由组里的全部路由，然后更新新的路由组信息。

##### 2.  拦截器和服务的异同

*   拦截器和服务所需要实现的接口不同，但是结构类似，都存在`init()`方法，但是两者的调用时机不同。
*   拦截器因为其特殊性，只对Activity路由有效，拦截器会在GoRouter首次调用的时候初始化。
*   服务没有该限制，某一服务可能在App整个生命周期中都不会用到，所以服务只有被调用的时候才会触发初始化操作。

##### 3.  使用java方式注册服务

*   实现相同服务（HelloService）的实现类（HelloServiceImpl）调用`addService()`方法会被覆盖(更新)，全局唯一。

##### 4.  使用java方式注册拦截器

*   `addInterceptor(ordinal,interceptor)`重复添加相同序号级会catch。
*   `setInterceptor(ordinal,interceptor)`重复添加相同序号会覆盖(更新)。

##### 5.  混淆

*   框架已经做了混淆处理，开发者无需关心。需要注意的是，如果不使用`@Param`注解方式，使用java方式注册，不要忘记参数加上java自带`@Keep`注解，否则使用`inject()`方法自动注入会失败。

##### 6.  开启调试,查看日志可以检查使用java方式注册的路由是否有重复提交的情况

```log
route path[/xx/xx] duplicate commit!!!
```

```log
route pathClass[class xx.xx] duplicate commit!!!
```
GoRouter日志tag为`GoRouter`，GoRouter-Compiler日志tag为`GoRouter::Compiler`，GoRouter-Gradle-Plugin日志tag为`GoRouter::Gradle-Plugin`。

##### 7.  ARouter迁移指南

| ARouter             | GoRouter             |
| ------------------- | -------------------- |
| ARouter             | GoRouter             |
| navigation()        | go()                 |
| NavigationCallback  | GoCallback           |
| IProvider           | IService             |
| DegradeService      | IDegradeService      |
| PretreatmentService | IPretreatmentService |
| Postcard            | Card                 |
| @Route              | @Route               |
| @Route              | @Service             |
| @Route              | @Interceptor         |
| @Autowired          | @Param               |
