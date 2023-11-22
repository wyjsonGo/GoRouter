package com.wyjson.module_common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

public class ToastUtils {

    private static Toast mToast;

    public static void makeText(Context context, CharSequence text) {
        makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("ShowToast")
    private static Toast makeText(Context context, CharSequence text, int duration) {
        if (mToast != null)
            mToast.cancel();
        try {
            mToast = Toast.makeText(context, null, duration);
            mToast.setText(text);
        } catch (NullPointerException e) {
            e.printStackTrace();
            mToast = Toast.makeText(context, text, duration);
        }
        return mToast;
    }

}
