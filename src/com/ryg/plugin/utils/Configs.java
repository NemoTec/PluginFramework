
package com.ryg.plugin.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

/**
 * persistence data
 * @author yuanzeyao
 */
public class Configs {
    public static ClassLoader sPluginClassloader = Constants.class.getClassLoader();

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void setSoLastModifiedTime(Context cxt, String soName, long time) {
        SharedPreferences prefs = cxt.getSharedPreferences(Constants.PREFERENCE_NAME,
                Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        prefs.edit().putLong(soName, time).apply();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static long getSoLastModifiedTime(Context cxt, String soName) {
        SharedPreferences prefs = cxt.getSharedPreferences(Constants.PREFERENCE_NAME,
                Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        return prefs.getLong(soName, 0);
    }

}
