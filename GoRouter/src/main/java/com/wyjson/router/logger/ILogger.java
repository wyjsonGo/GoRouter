package com.wyjson.router.logger;

public interface ILogger {

    String getDefaultTag();

    void showLog(boolean isShowLog);

    void showStackTrace(boolean isShowStackTrace);

    void debug(String tag, String message);

    void info(String tag, String message);

    void warning(String tag, String message);

    void error(String tag, String message);

    void error(String tag, String message, Throwable e);
}
