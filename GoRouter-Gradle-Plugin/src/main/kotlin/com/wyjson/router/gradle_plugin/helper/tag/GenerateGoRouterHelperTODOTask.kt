package com.wyjson.router.gradle_plugin.helper.tag

import com.wyjson.router.gradle_plugin.utils.Constants
import com.wyjson.router.gradle_plugin.utils.Constants.GOROUTER_HELPER_CLASS_NAME
import com.wyjson.router.gradle_plugin.utils.Logger
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateGoRouterHelperTODOTask : DefaultTask() {

    init {
        group = Constants.PROJECT
    }

    @get:OutputDirectory
    abstract val outputFolder: DirectoryProperty

    private val TAG = "RH(TODO)"

    private val catalog: String = "main"

    @TaskAction
    fun taskAction() {
        Logger.i(TAG, "Generate GoRouterHelper(TODO) task start.")
        val className = GOROUTER_HELPER_CLASS_NAME
        val dir = project.buildDir
        val path = "/generated/source/gorouter/${catalog}/com/wyjson/router/${className}.java"
        val outputFile = File(dir, path)
        outputFile.parentFile.mkdirs()
        outputFile.writeText(
            """
           package com.wyjson.router;
    
           import android.content.Context;
           import androidx.annotation.Nullable;
           import androidx.fragment.app.Fragment;
           import com.wyjson.module_common.route.service.user.PayService;
           import com.wyjson.module_common.route.service.user.UserService;
           import com.wyjson.router.model.Card;
           import java.lang.String;
    
           /**
            * TODO
            */
           public class GoRouterHelper {
               /**
                * 微信Pay服务
                * {@link com.wyjson.module_user.route.service.WechatPayServiceImpl}
                */
               @Nullable
               public static PayService getPayServiceForWechatPay() {
                   return GoRouter.getInstance().getService(PayService.class, "WechatPay");
               }
    
               /**
                * 用户服务
                * {@link com.wyjson.module_user.route.service.UserServiceImpl}
                */
               @Nullable
               public static UserService getUserService() {
                   return GoRouter.getInstance().getService(UserService.class);
               }
    
               /**
                * AliPay服务
                * {@link com.wyjson.module_user.route.service.AliPayServiceImpl}
                */
               @Nullable
               public static PayService getPayServiceForAlipay() {
                   return GoRouter.getInstance().getService(PayService.class, "Alipay");
               }
    
               /**
                * Kotlin模块用户服务
                * {@link com.wyjson.module_kotlin.route.service.KotlinUserServiceImpl}
                */
               @Nullable
               public static UserService getUserServiceForKotlinUserService() {
                   return GoRouter.getInstance().getService(UserService.class, "KotlinUserService");
               }
    
               /**
                * path 参数页面
                * {@link com.wyjson.module_user.activity.ParamActivity}
                */
               public static String getNewParamActivityPath() {
                   return "/new/param/activity";
               }
    
               /**
                * build 参数页面
                * {@link com.wyjson.module_user.activity.ParamActivity}
                */
               public static Card buildNewParamActivity(String nickname,
                       com.wyjson.module_common.model.TestModel test) {
                   return GoRouter.getInstance().build(getNewParamActivityPath())
                           // 昵称
                           .withString("nickname", nickname)
                           // 自定义类型
                           .withObject("test", test);
               }
    
               /**
                * go 参数页面
                * {@link com.wyjson.module_user.activity.ParamActivity}
                */
               public static void goNewParamActivity(Context context, String nickname,
                       com.wyjson.module_common.model.TestModel test) {
                   buildNewParamActivity(nickname, test).go(context);
               }
    
               /**
                * get 参数页面
                * {@link com.wyjson.module_user.activity.ParamActivity}
                */
               public static NewParamActivityBuilder getNewParamActivity(String nickname,
                       com.wyjson.module_common.model.TestModel test) {
                   return new NewParamActivityBuilder(nickname, test);
               }
    
               /**
                * build 参数页面
                * {@link com.wyjson.module_user.activity.ParamActivity}
                */
               public static Card buildNewParamActivity(String nickname,
                       com.wyjson.module_common.model.TestModel test, int base, int age) {
                   return getNewParamActivity(nickname, test).setBase(base).setAge(age).build();
               }
    
               /**
                * go 参数页面
                * {@link com.wyjson.module_user.activity.ParamActivity}
                */
               public static void goNewParamActivity(Context context, String nickname,
                       com.wyjson.module_common.model.TestModel test, int base, int age) {
                   buildNewParamActivity(nickname, test, base, age).go(context);
               }
    
               /**
                * path 参数片段
                * {@link com.wyjson.module_user.fragment.ParamFragment}
                */
               public static String getNewParamFragmentPath() {
                   return "/new/param/fragment";
               }
    
               /**
                * build 参数片段
                * {@link com.wyjson.module_user.fragment.ParamFragment}
                */
               public static Card buildNewParamFragment() {
                   return GoRouter.getInstance().build(getNewParamFragmentPath());
               }
    
               /**
                * go 参数片段
                * {@link com.wyjson.module_user.fragment.ParamFragment}
                */
               public static Fragment goNewParamFragment(Context context) {
                   return (Fragment) buildNewParamFragment().go(context);
               }
    
               /**
                * get 参数片段
                * {@link com.wyjson.module_user.fragment.ParamFragment}
                */
               public static NewParamFragmentBuilder getNewParamFragment() {
                   return new NewParamFragmentBuilder();
               }
    
               /**
                * build 参数片段
                * {@link com.wyjson.module_user.fragment.ParamFragment}
                */
               public static Card buildNewParamFragment(int age, String name) {
                   return getNewParamFragment().setAge(age).setName(name).build();
               }
    
               /**
                * go 参数片段
                * {@link com.wyjson.module_user.fragment.ParamFragment}
                */
               public static Fragment goNewParamFragment(Context context, int age, String name) {
                   return (Fragment) buildNewParamFragment(age, name).go(context);
               }
    
               /**
                * path 这是一个kotlin页面，本库支持kapt
                * {@link com.wyjson.module_kotlin.activity.KotlinActivity}
                */
               public static String getKotlinActivityPath() {
                   return "/kotlin/activity";
               }
    
               /**
                * build 这是一个kotlin页面，本库支持kapt
                * {@link com.wyjson.module_kotlin.activity.KotlinActivity}
                */
               public static Card buildKotlinActivity(String nickname) {
                   return GoRouter.getInstance().build(getKotlinActivityPath())
                           // 昵称
                           .withString("nickname", nickname);
               }
    
               /**
                * go 这是一个kotlin页面，本库支持kapt
                * {@link com.wyjson.module_kotlin.activity.KotlinActivity}
                */
               public static void goKotlinActivity(Context context, String nickname) {
                   buildKotlinActivity(nickname).go(context);
               }
    
               /**
                * get 这是一个kotlin页面，本库支持kapt
                * {@link com.wyjson.module_kotlin.activity.KotlinActivity}
                */
               public static KotlinActivityBuilder getKotlinActivity(String nickname) {
                   return new KotlinActivityBuilder(nickname);
               }
    
               /**
                * build 这是一个kotlin页面，本库支持kapt
                * {@link com.wyjson.module_kotlin.activity.KotlinActivity}
                */
               public static Card buildKotlinActivity(String nickname, int age) {
                   return getKotlinActivity(nickname).setAge(age).build();
               }
    
               /**
                * go 这是一个kotlin页面，本库支持kapt
                * {@link com.wyjson.module_kotlin.activity.KotlinActivity}
                */
               public static void goKotlinActivity(Context context, String nickname, int age) {
                   buildKotlinActivity(nickname, age).go(context);
               }
    
               /**
                * path 主页
                * {@link com.wyjson.module_main.activity.MainActivity}
                */
               public static String getMainActivityPath() {
                   return "/main/activity";
               }
    
               /**
                * build 主页
                * {@link com.wyjson.module_main.activity.MainActivity}
                */
               public static Card buildMainActivity() {
                   return GoRouter.getInstance().build(getMainActivityPath());
               }
    
               /**
                * go 主页
                * {@link com.wyjson.module_main.activity.MainActivity}
                */
               public static void goMainActivity(Context context) {
                   buildMainActivity().go(context);
               }
    
               /**
                * path 事件页面
                * {@link com.wyjson.module_main.activity.EventActivity}
                */
               public static String getMainEventActivityPath() {
                   return "/main/event/activity";
               }
    
               /**
                * build 事件页面
                * {@link com.wyjson.module_main.activity.EventActivity}
                */
               public static Card buildMainEventActivity() {
                   return GoRouter.getInstance().build(getMainEventActivityPath());
               }
    
               /**
                * go 事件页面
                * {@link com.wyjson.module_main.activity.EventActivity}
                */
               public static void goMainEventActivity(Context context) {
                   buildMainEventActivity().go(context);
               }
    
               /**
                * path 事件片段
                * {@link com.wyjson.module_main.fragment.EventFragment}
                */
               public static String getMainEventFragmentPath() {
                   return "/main/event/fragment";
               }
    
               /**
                * build 事件片段
                * {@link com.wyjson.module_main.fragment.EventFragment}
                */
               public static Card buildMainEventFragment() {
                   return GoRouter.getInstance().build(getMainEventFragmentPath());
               }
    
               /**
                * go 事件片段
                * {@link com.wyjson.module_main.fragment.EventFragment}
                */
               public static Fragment goMainEventFragment(Context context) {
                   return (Fragment) buildMainEventFragment().go(context);
               }
    
               /**
                * path 欢迎页
                * {@link com.wyjson.module_main.activity.SplashActivity}
                */
               public static String getMainSplashActivityPath() {
                   return "/main/splash/activity";
               }
    
               /**
                * build 欢迎页
                * {@link com.wyjson.module_main.activity.SplashActivity}
                */
               public static Card buildMainSplashActivity() {
                   return GoRouter.getInstance().build(getMainSplashActivityPath());
               }
    
               /**
                * go 欢迎页
                * {@link com.wyjson.module_main.activity.SplashActivity}
                */
               public static void goMainSplashActivity(Context context) {
                   buildMainSplashActivity().go(context);
               }
    
               /**
                * path 卡片片段
                * {@link com.wyjson.module_user.fragment.CardFragment}
                */
               public static String getUserCardFragmentPath() {
                   return "/user/card/fragment";
               }
    
               /**
                * build 卡片片段
                * {@link com.wyjson.module_user.fragment.CardFragment}
                */
               public static Card buildUserCardFragment() {
                   return GoRouter.getInstance().build(getUserCardFragmentPath());
               }
    
               /**
                * go 卡片片段
                * {@link com.wyjson.module_user.fragment.CardFragment}
                */
               public static Fragment goUserCardFragment(Context context) {
                   return (Fragment) buildUserCardFragment().go(context);
               }
    
               /**
                * path 用户信息页面
                * {@link com.wyjson.module_user.activity.UserInfoActivity}
                */
               public static String getUserInfoActivityPath() {
                   return "/user/info/activity";
               }
    
               /**
                * build 用户信息页面
                * {@link com.wyjson.module_user.activity.UserInfoActivity}
                */
               public static Card buildUserInfoActivity() {
                   return GoRouter.getInstance().build(getUserInfoActivityPath());
               }
    
               /**
                * go 用户信息页面
                * {@link com.wyjson.module_user.activity.UserInfoActivity}
                */
               public static void goUserInfoActivity(Context context) {
                   buildUserInfoActivity().go(context);
               }
    
               /**
                * path 登录页面
                * {@link com.wyjson.module_user.activity.SignInActivity}
                */
               public static String getUserSignInActivityPath() {
                   return "/user/sign_in/activity";
               }
    
               /**
                * build 登录页面
                * {@link com.wyjson.module_user.activity.SignInActivity}
                */
               public static Card buildUserSignInActivity() {
                   return GoRouter.getInstance().build(getUserSignInActivityPath());
               }
    
               /**
                * go 登录页面
                * {@link com.wyjson.module_user.activity.SignInActivity}
                */
               public static void goUserSignInActivity(Context context) {
                   buildUserSignInActivity().go(context);
               }
    
               /**
                * 参数页面 Builder
                * {@link com.wyjson.module_user.activity.ParamActivity}
                */
               public static class NewParamActivityBuilder {
                   private final Card mCard;
    
                   public NewParamActivityBuilder(String nickname,
                           com.wyjson.module_common.model.TestModel test) {
                       mCard = GoRouter.getInstance().build(getNewParamActivityPath())
                               // 昵称
                               .withString("nickname", nickname)
                               // 自定义类型
                               .withObject("test", test);
                   }
    
                   /**
                    * 我是一个父类字段
                    */
                   public NewParamActivityBuilder setBase(int base) {
                       mCard.withInt("base", base);
                       return this;
                   }
    
                   public NewParamActivityBuilder setAge(int age) {
                       mCard.withInt("age", age);
                       return this;
                   }
    
                   public Card build() {
                       return mCard;
                   }
               }
    
               /**
                * 参数片段 Builder
                * {@link com.wyjson.module_user.fragment.ParamFragment}
                */
               public static class NewParamFragmentBuilder {
                   private final Card mCard;
    
                   public NewParamFragmentBuilder() {
                       mCard = GoRouter.getInstance().build(getNewParamFragmentPath());
                   }
    
                   public NewParamFragmentBuilder setAge(int age) {
                       mCard.withInt("age", age);
                       return this;
                   }
    
                   public NewParamFragmentBuilder setName(String name) {
                       mCard.withString("name", name);
                       return this;
                   }
    
                   public Card build() {
                       return mCard;
                   }
               }
    
               /**
                * 这是一个kotlin页面，本库支持kapt Builder
                * {@link com.wyjson.module_kotlin.activity.KotlinActivity}
                */
               public static class KotlinActivityBuilder {
                   private final Card mCard;
    
                   public KotlinActivityBuilder(String nickname) {
                       mCard = GoRouter.getInstance().build(getKotlinActivityPath())
                               // 昵称
                               .withString("nickname", nickname);
                   }
    
                   public KotlinActivityBuilder setAge(int age) {
                       mCard.withInt("age", age);
                       return this;
                   }
    
                   public Card build() {
                       return mCard;
                   }
               }
           }

        """.trimIndent(), Charsets.UTF_8
        )
        Logger.i(TAG, "Generate GoRouterHelper(TODO) task end. ${dir}${path}")
    }


}