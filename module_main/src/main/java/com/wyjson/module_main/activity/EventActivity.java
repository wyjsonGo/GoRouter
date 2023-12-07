package com.wyjson.module_main.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.wyjson.module_common.entity.UserEntity;
import com.wyjson.module_common.route.MainRoute;
import com.wyjson.module_main.R;
import com.wyjson.module_main.databinding.MainActivityEventBinding;
import com.wyjson.router.GoRouter;
import com.wyjson.router.annotation.Route;

/**
 * 演示路由event使用方法,Activity和Fragment互通,当然你也可以在任何地方通知其他Activity或Fragment
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

        // 订阅一下事件
        // 订阅String类型事件(页面处于活跃状态下才会收到)
        GoRouter.getInstance().registerEvent(this, String.class, data -> {
            vb.tvShow.setText("EventActivity->String data:" + data);
        });
        // 订阅String类型事件(无论页面处于何种状态下都会收到)
        GoRouter.getInstance().registerEventForever(this, String.class, data -> {
            // 做一些处理...
        });
        // 订阅自定义类型事件
        GoRouter.getInstance().registerEvent(this, UserEntity.class, data -> {
            vb.tvShow.setText("EventActivity->UserEntity data:" + data.toString());
        });
    }

    public void onClickStringEvent(View view) {
        // 向EventFragment发送String类型
        GoRouter.getInstance().postEvent(MainRoute.EventFragment, "Go!");
    }

    public void onClickCustomEvent(View view) {
        // 向EventFragment发送自定义类型
        GoRouter.getInstance().postEvent(MainRoute.EventFragment, new UserEntity(89, "Wyjson"));
    }

    public void onClickIntEvent(View view) {
        // 向MainActivity发送int类型
        GoRouter.getInstance().postEvent(MainRoute.MainActivity, 123);
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