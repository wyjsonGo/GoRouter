package com.wyjson.module_main.activity;

import androidx.fragment.app.FragmentActivity;

import com.wyjson.router.annotation.Route;
import com.wyjson.router.callback.impl.GoCallbackImpl;
import com.wyjson.router.helper.module_main.group_main.MainActivityGoRouter;
import com.wyjson.router.model.Card;

@Route(path = "/main/splash/activity", remark = "欢迎页")
public class SplashActivity extends FragmentActivity {

    @Override
    protected void onStart() {
        super.onStart();
        goMainActivity();
    }

    private void goMainActivity() {
        MainActivityGoRouter.build().go(this, new GoCallbackImpl() {
            @Override
            public void onArrival(Card card) {
                finish();
            }
        });
    }

}
