# GoRouter

[![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)

> 一个用于帮助 Android App 进行组件化改造的框架 —— 支持模块间的路由、通信、解耦

---

#### 一、功能介绍
1. **支持直接解析标准URL进行跳转，并自动注入参数到目标页面中**
2. **支持多模块工程使用**
3. **支持添加多个拦截器，自定义拦截顺序**
4. **支持依赖注入，可单独作为依赖注入框架使用**
5. **支持InstantRun**
6. **支持MultiDex**(Google方案)
7. 支持用户指定全局降级与局部降级策略
8. 页面、拦截器、服务等组件均自动注册到框架
9. 支持多种方式配置转场动画
10. 支持获取Fragment
11. 完全支持Kotlin以及混编
12. **支持第三方 App 加固**
13. 支持增量编译
14. 支持动态注册路由信息和拦截器

#### 二、典型应用
1. 从外部URL映射到内部页面，以及参数传递与解析
2. 跨模块页面跳转，模块间解耦
3. 拦截跳转过程，处理登陆、埋点等逻辑
4. 跨模块API调用，通过控制反转来做组件解耦

#### 三、基础功能
1. 添加依赖和配置

	```groovy
	allprojects {
	    repositories {
	        ...
	        maven { url 'https://jitpack.io' }
	    }
	}
	```
 
	[![Download](https://jitpack.io/v/wyjsonGo/GoRouter.svg)](https://jitpack.io/#wyjsonGo/GoRouter)

	
	```groovy
	dependencies {
		// x.x.x 替换为jitpack最新版本
	    implementation 'com.github.wyjsonGo.GoRouter:GoRouter:x.x.x'
	}
	```

2. 添加Activity/Fragment

	``` java
	// 调用时机可以在模块的Application或插件模块加载时
	// 这里的路径需要注意的是至少需要有两级，/xx/activity，路径必须以RouteType[/activity][/fragment]结尾
	GoRouter.getInstance().build("/test/activity").commit(TestActivity.class);
	GoRouter.getInstance().build("/test/fragment").commit(TestFragment.class);
	```
	
	可以引用枚举类[RouteType.java](https://github.com/wyjsonGo/GoRouter/blob/master/GoRouter/src/main/java/com/wyjson/router/enums/RouteType.java)来拼接，例如:
	
	``` java
	"/user/info" + RouteType.ACTIVITY.getType()；
	```
3. 开启Log

	``` java
    if (BuildConfig.DEBUG) {
        GoRouter.openLog(); // 开启日志，最好放到Application里开启
        // GoRouter.printStackTrace(); // 打印日志的时候打印线程堆栈
    }
	```

4. 发起路由操作

    ``` java
    // 1. 应用内简单的跳转(通过URL跳转在'进阶用法'中)
    GoRouter.getInstance().build("/test/activity").go(this);

    // 2. 跳转并携带参数
    GoRouter.getInstance().build("/test/fragment")
                .withInt("age", 35)
                .withString("name", "Wyjson")
                .go(this);
    ```

#### 四、进阶用法
1. 通过URL跳转

    ``` java
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
    
    ``` xml
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

2. 解析参数

    ``` java
    // 为每一个参数声明一个字段，并使用 @Keep 标注
    // URL中不能传递Parcelable类型数据，通过GoRouter api可以传递Parcelable对象
    public class TestActivity extends Activity {
        @Keep
        private String name;
        @Keep
        int age = 18;
        
        @Override
        protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        //inject(T)方法支持Activity, Fragment, Intent, Bundle
	        GoRouter.getInstance().inject(this);
	
	        // GoRouter会自动对字段进行赋值，无需主动获取
	        Log.d("param", name + age);
        }
    }

	 // 注册
    GoRouter.getInstance().build("/test/activity")
            .putString("name")
            .putInt("age")
            .commit(TestActivity.class);
   ```

3. 声明拦截器(拦截跳转过程，面向切面编程)

    ``` java
    // 比较经典的应用就是在跳转过程中处理登陆事件，这样就不需要在目标页重复做登陆检查
    // 拦截器会在跳转之间执行，多个拦截器会按优先级顺序依次执行
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

	 // 注册
    GoRouter.getInstance().addInterceptor(1, TestInterceptor.class);
    ```

4. 处理跳转结果

    ``` java
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
        public void onInterrupt(Card card, Throwable exception) {

        }
    });
    ```

5. 自定义全局降级策略

    ``` java
    // 实现DegradeService接口
    public class DegradeServiceImpl implements DegradeService {
	    @Override
	    public void onLost(Context context, Card card) {
		// do something.
	    }
	
	    @Override
	    public void init() {
	
	    }
    }
    
    // 注册
    GoRouter.getInstance().addService(DegradeServiceImpl.class);
    ```

6. 为目标页面声明更多信息

    ``` java
    // 我们经常需要在目标页面中配置一些属性，比方说"是否需要登陆"之类的
    // 可以通过putTag()进行扩展，这个属性是一个 int值，换句话说，单个int有4字节，也就是32位，可以配置32个开关
    // 剩下的可以自行发挥，通过字节操作可以标识32个开关，通过开关标记目标页面的一些属性，在拦截器中可以拿到这个标记进行业务逻辑判断
    GoRouter.getInstance().build("/user/info/activity")
	    .putTag(RouteTag.LOGIN.getValue() | RouteTag.AUTHENTICATION.getValue())
	    .commit(UserInfoActivity.class);
    ```
	Demo里[RouteTag.java](https://github.com/wyjsonGo/GoRouter/blob/master/module_common/src/main/java/com/wyjson/module_common/route/enums/RouteTag.java)已经实现了一个例子

7. 通过依赖注入解耦:服务管理(一) 暴露服务

    ``` java
    // 声明接口,其他组件通过接口来调用服务
    public interface HelloService extends IService {
        String sayHello(String name);
    }

    // 实现接口
    public class HelloServiceImpl implements HelloService {

        @Override
        public String sayHello(String name) {
        	return "hello, " + name;
        }

        @Override
        public void init() {

        }
    }
    
    // 注册
    GoRouter.getInstance().addService(UserServiceImpl.class);
    ```

8. 通过依赖注入解耦:服务管理(二) 发现服务

    ``` java
    HelloService helloService = GoRouter.getInstance().getService(HelloService.class);
    if (helloService != null) {
        helloService.sayHello("Wyjson");
    }
    ```

9. 跳转前预处理

    ``` java
    // 比如跳转登录页面，只要简单的调用go就可以了，一些必须的参数和标识可以放到预处理里来做。
    // 或者一些埋点的处理
    // 实现PretreatmentService接口
    public class PretreatmentServiceImpl implements PretreatmentService {

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

	// 注册
	GoRouter.getInstance().addService(PretreatmentServiceImpl.class);
    ```

10. 动态注册路由信息

	适用于部分插件化架构的App以及需要动态注册路由信息和拦截器的场景，可以通过 GoRouter 提供的接口实现动态注册
路由信息和拦截器。

    ``` java
    GoRouter.getInstance().addInterceptor(1, TestInterceptor.class);
    GoRouter.getInstance().addService(UserServiceImpl.class);
    GoRouter.getInstance().build("/user/info/activity").commit(UserInfoActivity.class);    
    // 适用于动态插件加载时使用
    GoRouter.getInstance().setInterceptor(2, Test2Interceptor.class);
    ```

#### 五、更多功能

1. 获取路径元数据

    ``` java
    // 有些场景需要判断通话页面当前是否存在,就需要获取页面class等信息，可以使用此方法getCardMete()
    CardMeta cardMeta = GoRouter.getInstance().build("/user/info/activity").getCardMeta();
    if (cardMeta != null) {
        cardMeta.getPathClass();
    }
   ```

2. 详细的API说明

    ``` java
    // 构建标准的路由请求
    GoRouter.getInstance().build("/home/main/activity").go(this);

    // 构建标准的路由请求，通过Uri直接解析
    Uri uri;
    GoRouter.getInstance().build(uri).go(this);

    // 构建标准的路由请求，startActivityForResult
    // go的第一个参数必须是Activity，第二个参数则是RequestCode
    GoRouter.getInstance().build("/home/main/activity").go(this, 5);

    // 直接传递Bundle
    Bundle params = new Bundle();
    GoRouter.getInstance()
        .build("/home/main/activity")
        .with(params)
        .go(this);

    // 指定Flag
    GoRouter.getInstance()
        .build("/home/main/activity")
        .withFlags();
        .go(this);

    // 获取Fragment
    Fragment fragment = (Fragment) GoRouter.getInstance().build("/test/fragment").go(this);
                        
    // 序列化对象传递
    GoRouter.getInstance().build("/home/main/activity")
        .withSerializable("user",new User())
        .go(this);

    // 觉得接口不够多，可以直接拿出Bundle赋值
    GoRouter.getInstance()
            .build("/home/main/activity")
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
        .withOptionsCompat(compat)
        .go(this);
            
    // 使用绿色通道(跳过所有的拦截器)
    GoRouter.getInstance().build("/home/main/activity").greenChannel().go(this);

    // 使用自己的日志工具打印日志
    GoRouter.setLogger();

    // 使用自己提供的线程池
    GoRouter.setExecutor();
    ```

3. 获取原始的URI

    ``` java
    String uriStr = getIntent().getStringExtra(GoRouter.ROUTER_RAW_URI);
    ```
4. 获取参数名

    ``` java
    String[] params = getIntent().getStringArrayExtra(GoRouter.ROUTER_PARAM_INJECT);
    ```

#### 六、其他

1. 拦截器和服务的异同

	- 拦截器和服务所需要实现的接口不同，但是结构类似，都存在 init() 方法，但是两者的调用时机不同。
	- 拦截器因为其特殊性，只对Activity路由有效，拦截器会在GoRouter首次调用的时候初始化。
	- 服务没有该限制，某一服务可能在App整个生命周期中都不会用到，所以服务只有被调用的时候才会触发初始化操作。

2. 实现相同服务（HelloService）的实现类（HelloServiceImpl）调用addService方法会被覆盖(更新)，全局唯一。
3. 拦截器addInterceptor(priority,interceptor)相同优先级添加会catch，setInterceptor(priority,interceptor)相同优先级添加会覆盖(更新)。
4. 开启混淆后框架不受影响。注意，如果使用GoRouter.getInstance().inject(this)自动注入参数方法，不要忘记参数加上@Keep注解，否则自动注入会失败。
