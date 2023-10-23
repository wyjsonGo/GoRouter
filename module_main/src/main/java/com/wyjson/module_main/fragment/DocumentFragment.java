package com.wyjson.module_main.fragment;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wyjson.module_common.route.enums.RouteTag;
import com.wyjson.module_main.databinding.MainFragmentDocumentBinding;
import com.wyjson.router.core.GoRouter;

import org.json.JSONException;
import org.json.JSONObject;

public class DocumentFragment extends Fragment {

    protected MainFragmentDocumentBinding vb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        vb = MainFragmentDocumentBinding.inflate(inflater, container, false);
        return vb.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String jsonStr = GoRouter.generateDocument(tag -> RouteTag.getExistList(tag).toString());
        try {
            vb.tvContent.setText(new JSONObject(jsonStr).toString(4).replace("\\", ""));
            Log.i("GoRouterDemo", vb.tvContent.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        vb.tvContent.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

}
