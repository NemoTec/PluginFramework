package com.ryg.plugin.utils;

import android.util.Log;
/**
 * 日志封装
 * @author singwhatiwanna, modify Nemo.
 */
public class LOG {
    private static final String TAG = "PluginFramework";
    private static final String DOT = ".";
    
    //Android Log level is "v, d, i, w, e", and the default level is "i" by using isLoggable() to check,
    //so if we do not run "adb shell setprop log.tag.PluginFramework DEBUG", DEBUG will be false.
    private static boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG/*Log.INFO*/);

    /*remove first.
    public static void v(String tag, String msg) {
        if (DEBUG) {
            Log.v(TAG + DOT + tag, msg);
        }
    }
    */
    
    public static void d(String tag, String msg) {
        if (DEBUG) {
	        Log.d(TAG + DOT + tag, msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (DEBUG) {
	        Log.d(TAG + DOT + tag, msg, tr);
        }
    }

    /*cause somebody may use "i" as "d", so remove first.
    public static void i(String tag, String msg) {
	    Log.i(TAG + DOT + tag, msg);
    }

    public static void i(String tag, String msg, Throwable tr) { 
	    Log.i(TAG + DOT + tag, msg, tr);
    }
    */

    public static void w(String tag, String msg) {
	    Log.w(TAG + DOT + tag, msg);
    }

    public static void w(String tag, String msg, Throwable tr) {
	    Log.w(TAG + DOT + tag, msg, tr);
    }

    public static void e(String tag, String msg) {
	    Log.e(TAG + DOT + tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
	    Log.e(TAG + DOT + tag, msg, tr);
    } 
    
}
