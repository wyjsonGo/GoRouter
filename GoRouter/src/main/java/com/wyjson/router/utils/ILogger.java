package com.wyjson.router.utils;

public interface ILogger {

    boolean isShowLog = false;
    boolean isShowStackTrace = false;
    String defaultTag = "GoRouter";

    boolean isShowLog();

    String getDefaultTag();

    void showLog(boolean isShowLog);

    void showStackTrace(boolean isShowStackTrace);

    void debug(String tag, String message);

    void info(String tag, String message);

    void warning(String tag, String message);

    void error(String tag, String message);

    void error(String tag, String message, Throwable e);
}
