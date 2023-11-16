package com.wyjson.module_main.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.wyjson.module_common.route.MainRoute;
import com.wyjson.module_main.R;
import com.wyjson.module_main.databinding.MainActivityEventBinding;
import com.wyjson.module_main.event.CustomEvent;
import com.wyjson.router.GoRouter;
import com.wyjson.router.annotation.Route;

/**
 * 演示路由event使用方法,Activity和Fragment互通,当让你也可以在任何地方通知其他Activity或Fragment
 */
@Route(path = MainRoute.EventActivity, remark = "事件页面")
public class EventActivity extends FragmentActivity {

    MainActivityEventBinding vb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = MainActivityEventBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());
        addFragment();

        // 监听一下事件
        // 监听String类型事件,页面处于活跃状态下才会收到
        GoRouter.getInstance().registerEvent(this, String.class, data -> {
            vb.tvShow.setText("EventActivity->String data:" + data);
        });
        // 监听String类型事件,页面无乱处于任何状态下都会收到
        GoRouter.getInstance().registerEventForever(this, String.class, data -> {
            // 做一些处理...
        });
        // 监听自定义类型事件
        GoRouter.getInstance().registerEvent(this, CustomEvent.class, data -> {
            vb.tvShow.setText("EventActivity->CustomEvent data:" + data.toString());
        });
    }

    public void onClickStringEvent(View view) {
        // 向EventFragment发送String类型
        GoRouter.getInstance().postEvent(MainRoute.EventFragment, "Go!");
    }

    public void onClickCustomEvent(View view) {
        // 向EventFragment发送自定义类型
        GoRouter.getInstance().postEvent(MainRoute.EventFragment, new CustomEvent(89, "Wyjson!"));
    }

    // 显示EventFragment
    private void addFragment() {
        Fragment cardFragment = (Fragment) GoRouter.getInstance().build(MainRoute.EventFragment).go(this);
        if (cardFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_container, cardFragment)
                    .commit();
        }
    }

}