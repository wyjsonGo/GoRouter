package com.wyjson.module_common.route.service;

import android.net.Uri;

import com.wyjson.router.interfaces.PathReplaceService;

public class PathReplaceServiceImpl implements PathReplaceService {

    @Override
    public String forString(String path) {
        // TODO: 按照一定的规则处理之后返回处理后的结果...
        return path;
    }

    @Override
    public Uri forUri(Uri uri) {
        // TODO: 按照一定的规则处理之后返回处理后的结果...
        return uri;
    }

    @Override
    public void init() {

    }
}