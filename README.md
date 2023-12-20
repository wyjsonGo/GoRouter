# [GoRouter](https://github.com/wyjsonGo/GoRouter)

[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/wyjsonGo/GoRouter/blob/main/LICENSE)
[![Release Version](https://jitpack.io/v/wyjsonGo/GoRouter.svg)](https://jitpack.io/#wyjsonGo/GoRouter)

> 一个用于帮助 Android App 进行组件化改造的框架 —— 支持模块间的路由、通信、解耦

## 简介

之前一直在用阿里开源的[ARouter](https://github.com/alibaba/ARouter)项目，因为ARouter多年未更新，ARouter开始有些不太适合了，所以重新开发了这款Android路由框架，同样的API，更多的功能，迁移请参见文末8-10。

## GoRouter和ARouter功能差异对比

| 功能                      | ARouter  | GoRouter  | 描述                                                  |
| ------------------------ | -------- | --------- | ---------------------------------------------------- |
| 路由注册方式               | 注解      | 注解、java | GoRouter不仅提供了注解，还能使用java方式注册，参见5-6 |
| 动态注册拦截器              | 不支持    | 支持      | ARouter只能动态注册路由，不能动态注册拦截器 |
| 重写跳转URL服务            | 支持      | 不支持     | 可以在`IPretreatmentService`里实现相同功能 |
| 获取元数据                 | 不支持    | 支持       | 有些场景需要判断某个页面当前是否存在等需求，就需要获取页面class等信息，参见5-1 |
| inject(T)                | 单一      | 更多       | ARouter不能在`onNewIntent()`方法里使用，也不能检查`required`，GoRouter提供了更多使用场景，性能更好，参见4-2 |
| 按组分类、按需初始化         | 支持      | 支持       | ARouter不允许多个module中存在相同的分组，GoRouter允许 |
| 生成路由文档                | 支持     | 支持       | ARouter会生成多个模块文档,GoRouter提供Gradle任务一键生成项目总文档,更加丰富，参见5-9 |
| **模块Application生命周期** | 不支持    | 支持       | 主动分发到业务模块，让模块无侵入的获取Application生命周期，参见6-1 |
| **路由页面事件**            | 不支持    | 支持       | 页面事件解耦，提供更多、更方便的API，参见5-10 |
| **自动生成路由帮助类**       | 不支持    | 支持       | 调用更方便，不需要知道目标页面参数名和参数类型、是否必传等元素，通过帮助类提供的方法，可轻松实现路由跳转和服务获取，参见7-1 |

***

## 一、功能介绍

1.  支持直接解析标准URL进行跳转，并自动注入参数到目标页面中
2.  支持多模块工程使用
3.  支持添加多个拦截器，自定义拦截顺序
4.  支持依赖注入，可单独作为依赖注入框架使用
5.  支持InstantRun
6.  支持MultiDex(Google方案)
7.  映射关系按组分类、多级管理，按需初始化
8.  支持用户指定全局降级与局部降级策略
9.  页面、拦截器、服务等组件均自动注册到框架
10. 支持多种方式配置转场动画
11. 支持获取Fragment
12. 完全支持Kotlin以及混编(参见8-1)
13. 支持第三方 App 加固
14. 支持一键生成路由文档
15. 支持增量编译
16. 支持动态注册路由、路由组、服务和拦截器
17. **支持模块Application生命周期**
18. **支持路由页面事件**
19. **自动生成路由帮助类**

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

dependencies {
    api 'com.github.wyjsonGo.GoRouter:GoRouter-Api:2.4.0'
}
// Kotlin配置参见8-1
```

##### 2.  在module项目下添加注解处理器依赖和配置

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

dependencies {
    annotationProcessor 'com.github.wyjsonGo.GoRouter:GoRouter-Compiler:2.4.0'
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

经典方式:

```java
// 1. 应用内简单的跳转(通过URL跳转在'进阶用法'中)
GoRouter.getInstance().build("/test/activity").go(this);

// 2. 跳转并携带参数
GoRouter.getInstance().build("/test/fragment")
            .withString("name", "Wyjson")
            .withObject("test", new TestModel(123, "Jack"))
            .withInt("age", 35)
            .go(this);
```

Helper方式:(开启Helper功能，参见7-1)

```java
// 1. 应用内简单的跳转
TestActivityGoRouter.go(this);

// 2. 跳转并携带参数
TestFragmentGoRouter.go(this, "Wyjson", testModel, 35);
```

##### 6.  使用Gradle插件实现路由表的自动加载，支持Gradle8.0+

```groovy
// 项目根目录下的settings.gradle
pluginManagement {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

```groovy
// 项目根目录下的build.gradle
buildscript {
    dependencies {
        classpath 'com.github.wyjsonGo.GoRouter:GoRouter-Gradle-Plugin:2.4.0'
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
// 新建一个Activity用于监听Scheme事件,之后直接把url传递给GoRouter即可(消息栏和外部所有URL跳转都可以统一用这个Activity接收后分发跳转)
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

在带有`@Route`注解的Activity、Fragment页面使用`@Param`注解，会自动生成`类名+$$Param.java`注入类，可调用`inject()`和`injectCheck()`方法自动注入参数。

```java
// 为每一个参数声明一个字段(不能是private)，并使用 @Param 标注
// URL中不能传递Parcelable类型数据，通过GoRouter api可以传递Parcelable对象
@Route(path = "/param/activity")
public class ParamActivity extends BaseParamActivity {

    @Param
    int age = 18;

    // 可以自定义参数name
    @Param(name = "nickname", remark = "昵称", required = true)
    String name;

    /**
     * 使用 withObject 传递 List 和 Map 的实现了 Serializable 接口的实现类(ArrayList/HashMap)的时候，
     * 接收该对象的地方不能标注具体的实现类类型应仅标注为 List 或 Map，
     * 否则会影响序列化中类型的判断, 其他类似情况需要同样处理
     */
    @Param(name = "test", remark = "自定义类型", required = true)
    TestModel testModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);

       // inject()方法会自动对字段进行赋值
       ParamActivity$$Param.inject(this);

       // 或使用

       // injectCheck()方法会自动对字段进行赋值，
       // 并检查标记@Param(required = true)的字段，
       // 检查不通过会抛出ParamException()类型的异常,
       // 可通过e.getParamName()获取参数名自行处理。
       try {
           ParamActivity$$Param.injectCheck(this);
       } catch (ParamException e) {
           Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
           finish();
           return;
       }

       Log.d("param", "base:" + base + "age:" + age + ",name:" + name);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // inject()方法参数支持intent、bundle
        ParamActivity$$Param.inject(this, intent);
    }
}

// 支持父类字段自动注入
public class BaseParamActivity extends Activity {
    @Param(remark = "我是一个父类字段")
    protected int base;
}
```

如果使用了`withObject()`方法，需要实现JSON服务

```java
// 实现IJsonService接口
@Service(remark = "json服务")
public class JsonServiceImpl implements IJsonService {

    @Override
    public void init() {

    }

    @Override
    public String toJson(Object instance) {
        // 这里演示使用了gson,也可以使用其他json转换工具
        return new Gson().toJson(instance);
    }

    @Override
    public <T> T parseObject(String input, Type typeOfT) {
        return new Gson().fromJson(input, typeOfT);
    }
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
// 可以通过@Route注解的tag属性进行扩展，这个属性是一个 int值，换句话说，单个int有4字节，可以配置31个开关
// 剩下的可以自行发挥，通过字节操作可以标识31个开关，通过开关标记目标页面的一些属性，在拦截器中可以拿到这个标记进行业务逻辑判断
@Route(path = "/user/info/activity", tag = LOGIN | AUTHENTICATION)
```

tag使用示例[UserInfoActivity.java](https://github.com/wyjsonGo/GoRouter/blob/master/module_user/src/main/java/com/wyjson/module_user/activity/UserInfoActivity.java)，
tag处理示例[SignInInterceptor.java](https://github.com/wyjsonGo/GoRouter/blob/master/module_user/src/main/java/com/wyjson/module_user/route/interceptor/SignInInterceptor.java)

##### 7.  通过依赖注入解耦:服务管理

```java
// 暴露服务
// 声明接口并继承IService接口,其他组件通过接口来调用服务
public interface HelloService extends IService {
    String sayHello(String name);
}

// 实现接口
@Service(remark = "服务描述")
public class HelloServiceImpl implements HelloService {

    @Override
    public void init() {

    }

    @Override
    public String sayHello(String name) {
       return "hello, " + name;
    }
}

// 发现服务
HelloService helloService = GoRouter.getInstance().getService(HelloService.class);
if (helloService != null) {
    helloService.sayHello("Wyjson");
}
```

进阶用法，如需多个实现服务可指定`@Service(alias = "xxx")`

```java
// 暴露支付服务
public interface PayService extends IService {
    String getPayType();
}

// 实现阿里支付接口
@Service(alias = "Alipay", remark = "AliPay服务")
public class AliPayServiceImpl implements PayService {
    @Override
    public void init() {

    }

    @Override
    public String getPayType() {
        return "AliPay";
    }
}

// 实现微信支付接口
@Service(alias = "WechatPay", remark = "微信Pay服务")
public class WechatPayServiceImpl implements PayService {
    @Override
    public void init() {

    }

    @Override
    public String getPayType() {
        return "WechatPay";
    }
}

// 发现阿里支付服务
PayService alipayService = GoRouter.getInstance().getService(PayService.class, "Alipay");
if (alipayService != null) {
    alipayService.getPayType();
}
// 发现微信支付服务
PayService wechatPayService = GoRouter.getInstance().getService(PayService.class, "WechatPay");
if (wechatPayService != null) {
    wechatPayService.getPayType();
}
```

##### 8.  跳转前预处理

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

// 自定义对象传递
GoRouter.getInstance().build("/main/activity")
    .withObject("test", new TestModel(123, "Jack"))
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
// 动态注册服务
GoRouter.getInstance().addService(UserServiceImpl.class);
// 动态注册服务多个实现
GoRouter.getInstance().addService(WechatPayServiceImpl.class, "WechatPay");
GoRouter.getInstance().addService(AliPayServiceImpl.class, "Alipay");

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

// 自动注入参数
GoRouter.getInstance().inject(this);
```

##### 7.  自定义模块路由加载

如不使用Gradle插件[3-6]进行自动注册，也不想走默认扫描dex的方式，可以不调用`GoRouter.autoLoadRouteModule(this)`方法，但需要自行调用模块生成的路由加载类。
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
    runAutoRegisterBuildTypes "release", "sandbox", "more"
}
```

##### 9.  生成路由文档

使用gradle命令一键生成路由文档

```sh
./gradlew generateRouteDocDebug
```
当然你也可以使用图形页面执行任务

<img src="https://github.com/wyjsonGo/GoRouter/blob/master/screenshot/gradle_task_generate_router_doc.png" width="40%" />

生成的路由文档会保存到项目下的`/app/项目名-变体名-route-doc.json`,Demo示例[/app/GoRouter-release-route-doc.json](https://github.com/wyjsonGo/GoRouter/blob/master/app/GoRouter-release-route-doc.json)

##### 10.  路由页面事件

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
*   路由页面事件功能内部使用`LiveData`。

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


## 七、自动生成路由帮助类

开启此功能可以大幅提升开发效率，避免错误调用的情况。不需要知道目标页面参数名和参数类型、是否必传等元素，通过帮助类提供的方法，可轻松实现路由跳转和服务获取。

##### 1.  添加配置

```groovy
// app目录下的build.gradle添加配置
plugins {
    ...
    id 'com.wyjson.gorouter'
}
GoRouter {
    ...
    // 指定根模块项目名称,开启自动生成路由帮助类功能
    helperToRootModuleName "module_common"
}
```

指定`helperToRootModuleName`配置参数后会开启自动生成路由帮助类功能，这个参数需要指定一个根模块项目名(指定的这个模块项目只要保证其他业务模块都能调用到，并且页面传递参数用到的实体类也能引用到就可以，推荐指向`module_common`模块或者新建一个模块单独存放这些生成的路由帮助类)。

##### 2.  使用

```java
// 经典:访问/main/activity
GoRouter.getInstance().build("/main/activity").go(this);
// helper:访问/main/activity(框架会根据path生成对应类名GoRouter.java)
MainActivityGoRouter.go(this);

// 经典:带参数访问
GoRouter.getInstance().build("/new/param/activity")
        .withString("nickname", "Wyjson")
        .withObject("test", testModel)
        .withInt("age", 78)
        .withInt("base", 7758)
        .go(this);
// helper:带参数访问
// 必传参数
NewParamActivityGoRouter.go(this, "Wyjson", testModel);
// 所有参数(必传参数和非必传参数一起)
NewParamActivityGoRouter.go(this, "Wyjson", testModel, base, 78);
// 非必传参数可以链式调用,解决了经典方式需要知道类型和参数名的问题
NewParamActivityGoRouter.get("Wyjson", testModel)// 必传参数
        .setAge(78)// 非必传参数
        .setBase(7758)// 非必传参数
        .build()
        .go(this);

// 经典:访问Fragment
Fragment fragment = (Fragment) GoRouter.getInstance().build("/user/card/fragment").go(this);
// helper:访问Fragment
Fragment fragment = UserCardFragmentGoRouter.go(this);

// 经典:获取元数据
CardMeta cardMeta = GoRouter.getInstance().build("/user/info/activity").getCardMeta();
// helper:获取元数据
CardMeta cardMeta = UserInfoActivityGoRouter.getCardMeta()

// 经典:获取User服务
UserService service = GoRouter.getInstance().getService(UserService.class);
// helper:获取User服务
UserService service = UserServiceGoRouter.get()

// 经典:获取带别名的服务
PayService service = GoRouter.getInstance().getService(PayService.class, "Alipay");
// helper:获取带别名的服务
PayService service = PayServiceForAlipayGoRouter.get();

// helper:获取path
String path = MainActivityGoRouter.getPath()

// 也可以通过helper类get/set其他属性
UserSignInActivityGoRouter.build()
        .withAction(...)
        .withFlags(...)
        .go(this, ...);
```

*   `@Route(deprecated = true)`和打开了openDebug()的情况下，框架跳转到该页将Toast提示其他开发人员和测试人员，并且生成的帮助类也会被自动标记为`@Deprecated`,代码上也会提示过时,提醒开发人员更新跳转代码，这在多人开发时会很有用。`@Service`不会有这个问题，service直接在暴漏的服务接口上标记`@Deprecated`，其他模块调用者就能看到过时标记。
*   `@Route(ignoreHelper = true)`的情况下，框架不会为这个页面生成帮助类，适用于仅本模块调用的页面。

##### 3.  设计思路

此功能所生成的帮助类会存放到`com.wyjson.router.helper`包下，按照`模块.路由分组.帮助类.java`分别存放，请勿手动更改这些类。
最早设计的是整个项目生成一个`GoRouterHelper.java`类，调用上比现在的每个页面生成一个对应的帮助类会更统一，
但是这样会导致多人开发每个人生成的`GoRouterHelper.java`类代码合并冲突，所以想到把`GoRouterHelper.java`存放到根项目`build`目录，存放到`build`目录确实可以解决代码冲突问题，
但是新的问题也来了，在首次开启生成帮助类的功能，项目里还没有调用这个`GoRouterHelper.java`类的时候是不会有问题的，如果项目里已经使用了这个帮助类，`build`目录被清除或者重新clone代码，
这时项目就没法运行了，因为缺少这个帮助类。这里不得不说一下帮助类生成流程和Android`buildConfig`任务的执行顺序，正常项目clone下来，idea提示报错缺少`buildConfig.java`类，
你去`build`项目，Gradle会执行`buildConfig`任务，生成缺少的`buildConfig.java`类，再去生成common模块aar。路由帮助类在根项目里还不知道上层其他模块里有什么页面和服务的时候，
它的执行顺序是，先`build`出来common模块，在去`build`其他页面业务模块，最后在app项目汇总，这时知道了所有页面和服务去生成帮助类到根模块项目。说回刚才的话题，项目里使用了帮助类，
而帮助类又存放到`build`里，此时要是缺少了`build`目录，会导致项目无法运行，其实有其他方案解决这个问题，但都不是很好，
所以最后我把这些帮助类存放到了`根项目/src/main/java/com/wyjson/router/helper/...`目录下，按照页面和服务生成对应的帮助类，解决了多人开发代码冲突的问题。
虽然这些类不在`build`目录里，也不用担心无用的类，框架在每次生成新的代码的时候会自动删除无用的类，和`build`目录机制一样。
如果其他业务模块你不是通过本地引用，而是通过aar等方式引入，那框架只会更新本地引用模块项目的帮助类目录，不会更改和删除aar模块在根项目里的帮助类，
这样做到了你所开发模块帮助类的更新而不影响其他团队生成的帮助类。

## 八、其他

##### 1.  Kotlin项目中的配置方式

```groovy
plugins {
    ...
    id 'kotlin-kapt'
}

kapt {
    arguments {
        arg("GOROUTER_MODULE_NAME", project.getName())
    }
}

dependencies {
    kapt 'com.github.wyjsonGo.GoRouter:GoRouter-Compiler:2.4.0'
}
```

module_kotlin模块Demo示例[module_kotlin/build.gradle](https://github.com/wyjsonGo/GoRouter/blob/master/module_kotlin/build.gradle)

##### 2.  Kotlin类中的字段无法注入如何解决？

首先，Kotlin中的字段是可以自动注入的，但是注入代码为了减少反射，使用的字段赋值的方式来注入的，Kotlin默认会生成set/get方法，并把属性设置为private 所以只要保证Kotlin中字段可见性不是private即可，简单解决可以在字段上添加 @JvmField。Demo示例[KotlinActivity.java](https://github.com/wyjsonGo/GoRouter/blob/master/module_kotlin/src/main/java/com/wyjson/module_kotlin/activity/KotlinActivity.java)

##### 3.  路由中的分组概念

*   SDK中针对所有的路径`/test/1`、`/test/2`进行分组，分组只有在分组中的某一个路径第一次被访问的时候，该分组才会被初始化。分组使用路径中第一段字符串(/*/)作为分组，这里的路径需要注意的是至少需要有两级`/xx/xx`。
*   GRouter允许一个module中存在多个分组，也允许多个module中存在相同的分组，但是最好不要在多个module中存在相同的分组，因为在注册路由组时发现存在相同的分组，会立即注册老的路由组里的全部路由，然后更新新的路由组信息。

##### 4.  拦截器和服务的异同

*   拦截器和服务所需要实现的接口不同，但是结构类似，都存在`init()`方法，但是两者的调用时机不同。
*   拦截器因为其特殊性，只对Activity路由有效，拦截器会在GoRouter首次调用的时候初始化。
*   服务没有该限制，某一服务可能在App整个生命周期中都不会用到，所以服务只有被调用的时候才会触发初始化操作。

##### 5.  使用java方式注册服务

*   实现相同服务（HelloService）的实现类（HelloServiceImpl）多次调用`addService()`方法会被覆盖(更新)，全局唯一，使用`getService(service)`方法获取。
*   实现相同服务（PayService）的多个实现类（AliPayServiceImpl、WechatPayServiceImpl）调用`addService(service, alias)`方法注册，使用`getService(service, alias)`方法获取。

##### 6.  使用java方式注册拦截器

*   `addInterceptor(ordinal, interceptor)`重复添加相同序号级会catch。
*   `setInterceptor(ordinal, interceptor)`重复添加相同序号会覆盖(更新)。

##### 7.  混淆

框架已经做了混淆处理，开发者无需关心。需要注意的是，如果不使用`@Param`注解方式，使用java方式注册，不要忘记参数加上java自带`@Keep`注解，否则使用`GoRouter.getInstance().inject(this)`方法自动注入会失败。

##### 8.  `inject()`工作原理

*   2.3.2版本之前，`GoRouter.getInstance().inject(this)`方法会先通过`this`参数拿到`bundle`对象，再去获取当前页面的`path`，通过`path`拿到`CardMeta`数据，利用java反射进行数据的绑定。
*   2.3.2版本起，自动生成了参数注入类，内部代码是原生写法，性能更好。

##### 9.  开启调试,查看日志可以检查使用java方式注册的路由是否有重复提交的情况

```log
route path[/xx/xx] duplicate commit!!!
```

```log
route pathClass[class xx.xx] duplicate commit!!!
```
GoRouter日志tag为`GoRouter`，GoRouter-Compiler日志tag为`GoRouter::Compiler`，GoRouter-Gradle-Plugin日志tag为`GoRouter::Gradle-Plugin`。

##### 10.  ARouter迁移指南

| ARouter              | GoRouter             |
| -------------------- | -------------------- |
| ARouter              | GoRouter             |
| navigation()         | go()                 |
| NavigationCallback   | GoCallback           |
| IProvider            | IService             |
| DegradeService       | IDegradeService      |
| PretreatmentService  | IPretreatmentService |
| SerializationService | IJsonService         |
| Postcard             | Card                 |
| @Route               | @Route               |
| @Route               | @Service             |
| @Route               | @Interceptor         |
| @Autowired           | @Param               |
