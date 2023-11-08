package com.wyjson.module_main.activity;

import android.util.ArrayMap;

import com.wyjson.router.load.IRouteModuleGroup;

import java.util.Map;

public class RouteGroupTest {

    public void load() {
        //...
        loadRouteGroup();
    }

    Map<String, IRouteModuleGroup> routeGroups = new ArrayMap<>();

    private void loadRouteGroup() {
        routeGroups.put("my", new IRouteModuleGroup() {
            @Override
            public void load() {
                // loadRouteForMyGroup()
            }
        });
        routeGroups.put("user", new IRouteModuleGroup() {
            @Override
            public void load() {
                // loadRouteForUserGroup()
            }
        });
    }

    private void loadRouteForMyGroup() {
        // commit()
    }

    private void loadRouteForUserGroup() {
        // commit()
    }
}
