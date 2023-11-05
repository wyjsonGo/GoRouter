package com.wyjson.router.logger;

import android.text.TextUtils;
import android.util.Log;

public class DefaultLogger implements ILogger {

    private static boolean isShowLog = false;
    private static boolean isShowStackTrace = false;
    private String defaultTag = "GoRouter";

    @Override
    public String getDefaultTag() {
        return defaultTag;
    }

    public void showLog(boolean showLog) {
        isShowLog = showLog;
    }

    public void showStackTrace(boolean showStackTrace) {
        isShowStackTrace = showStackTrace;
    }

    public DefaultLogger() {
    }

    public DefaultLogger(String defaultTag) {
        this.defaultTag = defaultTag;
    }

    @Override
    public void debug(String tag, String message) {
        if (isShowLog) {
            if (isShowStackTrace) {
                StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
                message = message + getExtInfo(stackTraceElement);
            }
            Log.d(TextUtils.isEmpty(tag) ? getDefaultTag() : tag, message);
        }
    }

    @Override
    public void info(String tag, String message) {
        if (isShowLog) {
            if (isShowStackTrace) {
                StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
                message = message + getExtInfo(stackTraceElement);
            }
            Log.i(TextUtils.isEmpty(tag) ? getDefaultTag() : tag, message);
        }
    }

    @Override
    public void warning(String tag, String message) {
        if (isShowLog) {
            if (isShowStackTrace) {
                StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
                message = message + getExtInfo(stackTraceElement);
            }
            Log.w(TextUtils.isEmpty(tag) ? getDefaultTag() : tag, message);
        }
    }

    @Override
    public void error(String tag, String message) {
        if (isShowLog) {
            if (isShowStackTrace) {
                StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
                message = message + getExtInfo(stackTraceElement);
            }
            Log.e(TextUtils.isEmpty(tag) ? getDefaultTag() : tag, message);
        }
    }

    @Override
    public void error(String tag, String message, Throwable e) {
        if (isShowLog) {
            Log.e(TextUtils.isEmpty(tag) ? getDefaultTag() : tag, message, e);
        }
    }

    private static String getExtInfo(StackTraceElement stackTraceElement) {
        String separator = " & ";
        StringBuilder sb = new StringBuilder(" <-> [");
        if (isShowStackTrace) {
            String threadName = Thread.currentThread().getName();
            String fileName = stackTraceElement.getFileName();
            String className = stackTraceElement.getClassName();
            String methodName = stackTraceElement.getMethodName();
            long threadID = Thread.currentThread().getId();
            int lineNumber = stackTraceElement.getLineNumber();

            sb.append("ThreadId=").append(threadID).append(separator);
            sb.append("ThreadName=").append(threadName).append(separator);
            sb.append("FileName=").append(fileName).append(separator);
            sb.append("ClassName=").append(className).append(separator);
            sb.append("MethodName=").append(methodName).append(separator);
            sb.append("LineNumber=").append(lineNumber);
        }
        sb.append("]");
        return sb.toString();
    }
}