package com.wyjson.module_main.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.wyjson.module_common.entity.UserEntity;
import com.wyjson.module_main.R;
import com.wyjson.module_main.databinding.MainActivityEventBinding;
import com.wyjson.router.GoRouter;
import com.wyjson.router.annotation.Route;
import com.wyjson.router.helper.module_main.group_main.MainActivityGoRouter;
import com.wyjson.router.helper.module_main.group_main.MainEventFragmentGoRouter;

/**
 * 演示路由event使用方法,Activity和Fragment互通,当然你也可以在任何地方通知其他Activity或Fragment
 */
@Route(path = "/main/event/activity", remark = "事件页面")
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
        MainEventFragmentGoRouter.postEvent("Go!");
    }

    public void onClickCustomEvent(View view) {
        // 向EventFragment发送自定义类型
        MainEventFragmentGoRouter.postEvent(new UserEntity(89, "Wyjson"));
    }

    public void onClickIntEvent(View view) {
        // 向MainActivity发送int类型
        MainActivityGoRouter.postEvent(123);
    }

    // 显示EventFragment
    private void addFragment() {
        Fragment fragment = MainEventFragmentGoRouter.go();
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_container, fragment)
                    .commit();
        }
    }

}