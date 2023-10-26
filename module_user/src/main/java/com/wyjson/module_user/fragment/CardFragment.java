package com.wyjson.module_user.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wyjson.module_common.route.UserRoute;
import com.wyjson.module_user.databinding.UserFragmentCardBinding;
import com.wyjson.router.annotation.Route;

@Route(path = UserRoute.CardFragment, remark = "卡片碎片")
public class CardFragment extends Fragment {

    protected UserFragmentCardBinding vb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        vb = UserFragmentCardBinding.inflate(inflater, container, false);
        return vb.getRoot();
    }
}
