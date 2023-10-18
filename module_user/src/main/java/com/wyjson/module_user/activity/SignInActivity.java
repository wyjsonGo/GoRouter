package com.wyjson.module_user.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.wyjson.module_user.databinding.UserActivitySignInBinding;

public class SignInActivity extends FragmentActivity {

    UserActivitySignInBinding vb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = UserActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());
    }
}