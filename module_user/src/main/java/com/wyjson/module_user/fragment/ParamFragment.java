package com.wyjson.module_user.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wyjson.module_common.route.UserRoute;
import com.wyjson.module_user.databinding.UserFragmentParamBinding;
import com.wyjson.router.annotation.Param;
import com.wyjson.router.annotation.Route;

@Route(path = UserRoute.ParamFragment, remark = "参数片段")
public class ParamFragment extends Fragment {

    UserFragmentParamBinding vb;

    @Param
    int age = 18;
    @Param
    String name;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        vb = UserFragmentParamBinding.inflate(inflater, container, false);
        return vb.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ParamFragment$$Param.inject(this);
        vb.tvTitle.setText("age:" + age + ",name:" + name);
    }

}
