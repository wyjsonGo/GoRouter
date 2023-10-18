package com.wyjson.router.interfaces;

import android.net.Uri;

/**
 * 预处理路径
 */
public interface PathReplaceService extends IService {

    /**
     * 对于正常路径
     *
     * @param path raw path
     */
    String forString(String path);

    /**
     * 对于uri类型
     *
     * @param uri raw uri
     */
    Uri forUri(Uri uri);
}
