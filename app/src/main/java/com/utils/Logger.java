package com.utils;

import android.util.Log;

import com.roaddo.store.BuildConfig;

public class Logger {
    public static void d(String title, String content) {
        if (BuildConfig.DEBUG) {
            Log.e(title, content);
        }
    }

    public static void e(String title, String content) {
        if (BuildConfig.DEBUG) {
            Log.d(title, content);
        }
    }

    /* split logcat message if more than 4076 into another line */
    public static void splitLog(String TAG, String message) {
        int maxLogSize = 4076;
        for (int i = 0; i <= message.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i + 1) * maxLogSize;
            end = Math.min(end, message.length());
            android.util.Log.d(TAG, message.substring(start, end));
        }
    }
}
