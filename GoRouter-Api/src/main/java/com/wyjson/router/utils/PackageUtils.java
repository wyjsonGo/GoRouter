package com.wyjson.router.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.wyjson.router.GoRouter;

public class PackageUtils {

    private static final String LAST_VERSION_NAME = "LAST_VERSION_NAME";
    private static final String LAST_VERSION_CODE = "LAST_VERSION_CODE";
    private static String NEW_VERSION_NAME;
    private static int NEW_VERSION_CODE;

    public static boolean isNewVersion(Context context, String spName) {
        PackageInfo packageInfo = getPackageInfo(context);
        if (null != packageInfo) {
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;

            SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
            if (!versionName.equals(sp.getString(LAST_VERSION_NAME, null)) || versionCode != sp.getInt(LAST_VERSION_CODE, -1)) {
                // new version
                NEW_VERSION_NAME = versionName;
                NEW_VERSION_CODE = versionCode;

                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static void updateVersion(Context context, String spName) {
        if (!android.text.TextUtils.isEmpty(NEW_VERSION_NAME) && NEW_VERSION_CODE != 0) {
            SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
            sp.edit().putString(LAST_VERSION_NAME, NEW_VERSION_NAME).putInt(LAST_VERSION_CODE, NEW_VERSION_CODE).apply();
        }
    }

    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
        } catch (Exception ex) {
            GoRouter.logger.error(null, "Get package info error.");
        }
        return packageInfo;
    }
}
