package com.wyjson.module_main.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.wyjson.module_common.route.UserRoute;
import com.wyjson.module_common.route.service.user.UserService;
import com.wyjson.module_main.R;
import com.wyjson.module_main.databinding.MainActivityMainBinding;
import com.wyjson.router.core.GoRouter;

public class MainActivity extends FragmentActivity {

    MainActivityMainBinding vb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = MainActivityMainBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());
    }

    public void onClickSignInActivity(View view) {
        GoRouter.getInstance().build(UserRoute.SignInActivity).go(this);
    }

    public void onClickParamActivity(View view) {
        GoRouter.getInstance().build(UserRoute.ParamActivity)
                .withInt("age", 78)
                .withString("name", "Wyjson")
                .go(this);
    }

    public void onClickCardFragment(View view) {
        Fragment cardFragment = (Fragment) GoRouter.getInstance().build(UserRoute.CardFragment).go(this);
        if (cardFragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, cardFragment);
            transaction.commit();
        }
    }

    public void onClickParamFragment(View view) {
        Fragment cardFragment = (Fragment) GoRouter.getInstance().build(UserRoute.ParamFragment)
                .withInt("age", 78)
                .withString("name", "Wyjson")
                .go(this);
        if (cardFragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, cardFragment);
            transaction.commit();
        }
    }

    public void onClickUserInfoActivity(View view) {
        GoRouter.getInstance().build(UserRoute.UserInfoActivity).go(this);
    }

    public void onClickUserService(View view) {
        UserService userService = GoRouter.getInstance().getService(UserService.class);
        if (userService != null) {
            Toast.makeText(this, "userId:" + userService.getUserId(), Toast.LENGTH_SHORT).show();
        }
    }

}