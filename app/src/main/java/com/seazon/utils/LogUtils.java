package com.seazon.utils;

import android.annotation.SuppressLint;
import android.util.Log;

import com.seazon.fo.BuildConfig;
import com.seazon.fo.Core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressLint("SimpleDateFormat")
public class LogUtils {

    private static String TAG = "Fo";
    private static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static boolean isDebugMode() {
        return BuildConfig.DEBUG;
    }

    public static void debug(String content) {
        if (content != null && isDebugMode()) {
            Log.d(TAG, content);
        }
    }

    public static void info(String content) {
        if (content != null && isDebugMode()) {
            Log.i(TAG, content);
        }
    }

    public static void warn(String content) {
        if (content != null && isDebugMode()) {
            Log.w(TAG, content);
        }
    }

    public static void error(String content) {
        error(content, null);
    }

    public static void error(Throwable e) {
        error(null, e);
    }

    public static void error(String content, Throwable e) {
        if (isDebugMode()) {
            Log.e(TAG, content, e);
        }
        if (content != null) {
            appendErrorLog(content);
        }
        if (e != null) {
            appendErrorLog(getStackTrace(e));
        }
    }

    private static String getStackTrace(Throwable e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e2) {
            return "getStackTrace failed, exception:" + e2.getMessage();
        }
    }

    private static void appendLog(String text, String date, String path) {
        try {
            if (path == null) {
                return;
            }

            File logFile = new File(path);

            if (!logFile.getParentFile().exists()) {
                logFile.getParentFile().mkdirs();
            }

            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            // BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append("[" + date + "]" + text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            warn(e.getMessage());
        } catch (Exception e) {
            warn(e.getMessage());
        }
    }

    public static void appendErrorLog(String text) {
        appendLog(text, SDF.format(new Date(System.currentTimeMillis())), Core.FILE_LOG_ERROR);
    }

}
