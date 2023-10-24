package com.wyjson.module_user.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wyjson.module_common.route.UserRoute;
import com.wyjson.module_user.databinding.UserFragmentParamBinding;
import com.wyjson.router.annotation.Route;
import com.wyjson.router.core.GoRouter;

//@Route(path = UserRoute.ParamFragment, name = "参数碎片")
public class ParamFragment extends Fragment {

    protected UserFragmentParamBinding vb;

    @Keep
    private int age = 18;
    @Keep
    private String name;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        vb = UserFragmentParamBinding.inflate(inflater, container, false);
        return vb.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GoRouter.getInstance().inject(this);
        vb.tvTitle.setText("age:" + age + ",name:" + name);
    }

}
