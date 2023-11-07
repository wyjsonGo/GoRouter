package com.wyjson.module_main.activity;

import androidx.fragment.app.FragmentActivity;

import com.wyjson.module_common.route.MainRoute;
import com.wyjson.router.annotation.Route;
import com.wyjson.router.callback.GoCallbackImpl;
import com.wyjson.router.card.Card;
import com.wyjson.router.core.GoRouter;

@Route(path = MainRoute.SplashActivity, remark = "欢迎页")
public class SplashActivity extends FragmentActivity {

    @Override
    protected void onStart() {
        super.onStart();
        goMainActivity();
    }

    private void goMainActivity() {
        GoRouter.getInstance().build(MainRoute.MainActivity).go(this, new GoCallbackImpl() {
            @Override
            public void onArrival(Card card) {
                finish();
            }
        });
    }

}
