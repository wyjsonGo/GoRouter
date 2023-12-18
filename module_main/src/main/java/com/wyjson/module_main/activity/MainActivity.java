package com.wyjson.module_main.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.wyjson.module_common.model.TestModel;
import com.wyjson.module_common.route.service.user.PayService;
import com.wyjson.module_common.route.service.user.UserService;
import com.wyjson.module_common.utils.ToastUtils;
import com.wyjson.module_main.R;
import com.wyjson.module_main.databinding.MainActivityMainBinding;
import com.wyjson.router.GoRouter;
import com.wyjson.router.annotation.Route;
import com.wyjson.router.callback.GoCallback;
import com.wyjson.router.helper.module_kotlin.group_kotlin.KotlinActivityGoRouter;
import com.wyjson.router.helper.module_main.group_main.MainEventActivityGoRouter;
import com.wyjson.router.helper.module_user.group_new.NewParamActivityGoRouter;
import com.wyjson.router.helper.module_user.group_new.NewParamFragmentGoRouter;
import com.wyjson.router.helper.module_user.group_user.UserCardFragmentGoRouter;
import com.wyjson.router.helper.module_user.group_user.UserInfoActivityGoRouter;
import com.wyjson.router.helper.module_user.group_user.UserSignInActivityGoRouter;
import com.wyjson.router.helper.module_user.service.PayServiceForAlipayGoRouter;
import com.wyjson.router.helper.module_user.service.PayServiceForWechatPayGoRouter;
import com.wyjson.router.helper.module_user.service.UserServiceGoRouter;
import com.wyjson.router.model.Card;

@Route(path = "/main/activity", remark = "主页")
public class MainActivity extends FragmentActivity {

    MainActivityMainBinding vb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = MainActivityMainBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());
        showRouteLoadMode();
        registerEvent();
    }

    public void onClickSignInActivity(View view) {
        UserSignInActivityGoRouter.go(this);
    }

    public void onClickParamActivity(View view) {
        // 父类里定义的参数
        int base = 7758;
        /**
         * 使用此方式传递自定义参数需要实现Json服务
         * Demo示例 {@link com.wyjson.module_common.route.service.JsonServiceImpl}
         */
        TestModel testModel = new TestModel(123, "Jack");
        NewParamActivityGoRouter.go(this, "Wyjson", testModel, base, 78);
        // or
//        NewParamActivityGoRouter.go(this, "Wyjson", testModel);
        // or
//        NewParamActivityGoRouter.get("Wyjson", testModel).setAge(78).setBase(base).build().go(this);
    }

    public void onClickCardFragment(View view) {
        Fragment cardFragment = UserCardFragmentGoRouter.go(this);
        if (cardFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_container, cardFragment)
                    .commit();
        }
    }

    public void onClickParamFragment(View view) {
        Fragment cardFragment = NewParamFragmentGoRouter.go(this, 78, "Wyjson");
        if (cardFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_container, cardFragment)
                    .commit();
        }
    }

    public void onClickUserInfoActivity(View view) {
        UserInfoActivityGoRouter.build().go(this, new GoCallback() {
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
            public void onInterrupt(Card card, @NonNull Throwable exception) {
                if (!TextUtils.isEmpty(exception.getMessage())) {
                    ToastUtils.makeText(MainActivity.this, "onInterrupt:" + exception.getMessage());
                }
            }
        });
    }

    public void onClickUserService(View view) {
        UserService userService = UserServiceGoRouter.get();
        if (userService != null) {
            ToastUtils.makeText(MainActivity.this, "userId:" + userService.getUserId());
        }
    }

    public void onClickPayService1(View view) {
        PayService payServiceForAlipay = PayServiceForAlipayGoRouter.get();
        if (payServiceForAlipay != null) {
            ToastUtils.makeText(MainActivity.this, "payType:" + payServiceForAlipay.getPayType());
        }
    }

    public void onClickPayService2(View view) {
        PayService payServiceForWechatPay = PayServiceForWechatPayGoRouter.get();
        if (payServiceForWechatPay != null) {
            ToastUtils.makeText(MainActivity.this, "payType:" + payServiceForWechatPay.getPayType());
        }
    }

    public void onClickEventActivity(View view) {
        MainEventActivityGoRouter.go(this);
    }

    public void onClickKotlinActivity(View view) {
        KotlinActivityGoRouter.go(this, "Wyjson", 78);
    }

    private void showRouteLoadMode() {
        if (GoRouter.getInstance().isRouteRegisterMode()) {
            vb.tvLoadMode.setText("路由注册模式:GoRouter-Gradle-Plugin(在打包时注册,节省运行时间)");
            vb.tvLoadMode.setTextColor(Color.parseColor("#0000ff"));
        } else {
            vb.tvLoadMode.setText("路由注册模式:scan dex file(在运行时注册,节省打包时间)");
            vb.tvLoadMode.setTextColor(Color.parseColor("#ff0000"));
        }
    }

    private void registerEvent() {
        // 订阅一下事件
        // 订阅int类型事件(页面处于活跃状态下才会收到)
        GoRouter.getInstance().registerEvent(this, Integer.class, data -> {
            ToastUtils.makeText(MainActivity.this, "MainActivity->String data:" + data);
        });
    }

}