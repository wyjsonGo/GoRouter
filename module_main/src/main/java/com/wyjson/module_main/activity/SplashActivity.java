package com.wyjson.module_main.activity;

import androidx.fragment.app.FragmentActivity;

import com.wyjson.module_common.route.MainRoute;
import com.wyjson.router.callback.GoCallbackImpl;
import com.wyjson.router.core.Card;
import com.wyjson.router.core.GoRouter;

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
