package com.wyjson.module_main.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.wyjson.module_common.route.MainRoute;
import com.wyjson.module_common.route.UserRoute;
import com.wyjson.module_common.route.service.user.UserService;
import com.wyjson.module_main.R;
import com.wyjson.module_main.databinding.MainActivityMainBinding;
import com.wyjson.router.annotation.Route;
import com.wyjson.router.callback.GoCallback;
import com.wyjson.router.card.Card;
import com.wyjson.router.core.GoRouter;

@Route(path = MainRoute.MainActivity, remark = "主页")
public class MainActivity extends FragmentActivity {

    MainActivityMainBinding vb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = MainActivityMainBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());
        showRouteLoadMode();
    }

    public void onClickSignInActivity(View view) {
        GoRouter.getInstance().build(UserRoute.SignInActivity).go(this);
    }

    public void onClickParamActivity(View view) {
        GoRouter.getInstance().build(UserRoute.ParamActivity)
                .withInt("age", 78)
                .withString("nickname", "Wyjson")
                .withInt("base", 7758)// 父类里定义的参数
                .go(this);
    }

    public void onClickCardFragment(View view) {
        Fragment cardFragment = (Fragment) GoRouter.getInstance().build(UserRoute.CardFragment).go(this);
        if (cardFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_container, cardFragment)
                    .commit();
        }
    }

    public void onClickParamFragment(View view) {
        Fragment cardFragment = (Fragment) GoRouter.getInstance().build(UserRoute.ParamFragment)
                .withInt("age", 78)
                .withString("name", "Wyjson")
                .go(this);
        if (cardFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_container, cardFragment)
                    .commit();
        }
    }

    public void onClickUserInfoActivity(View view) {
        GoRouter.getInstance().build(UserRoute.UserInfoActivity).go(this, new GoCallback() {
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
                    Toast.makeText(MainActivity.this, "onInterrupt:" + exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onClickUserService(View view) {
        UserService userService = GoRouter.getInstance().getService(UserService.class);
        if (userService != null) {
            Toast.makeText(this, "userId:" + userService.getUserId(), Toast.LENGTH_SHORT).show();
        }
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

}