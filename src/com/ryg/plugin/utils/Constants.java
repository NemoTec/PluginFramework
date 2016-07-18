
package com.ryg.plugin.utils;
/**
 * 常量定义
 * @author singwhatiwanna, modify Nemo.
 */
public class Constants {
	public static final String FROM = "extra.from";
    public static final int FROM_INTERNAL = 0;
    public static final int FROM_EXTERNAL = 1;

    public static final String EXTRA_DEX_PATH = "extra.dex.path";
    public static final String EXTRA_CLASS    = "extra.pluginclass";
    public static final String EXTRA_PACKAGE  = "extra.pluginpackage";

    public static final int ACTIVITY_TYPE_UNKNOWN 	= -1;
    public static final int ACTIVITY_TYPE_NORMAL 	= 1;
    public static final int ACTIVITY_TYPE_FRAGMENT 	= 2;
    public static final int ACTIVITY_TYPE_ACTIONBAR = 3;
    public static final int ACTIVITY_TYPE_PREFERENCE_ACTIVITY = 4;
    

    public static final String PROXY_ACTIVITY_VIEW_ACTION 			 =
            "com.ryg.plugin.proxy.activity.VIEW";
    public static final String PROXY_FRAGMENT_ACTIVITY_VIEW_ACTION 	 =
            "com.ryg.plugin.proxy.fragmentactivity.VIEW";
    public static final String PROXY_PREFERENCE_ACTIVITY_VIEW_ACTION =
            "com.ryg.plugin.proxy.preferenceactivity.VIEW";            

    public static final String PROCESSOR_ARCH_ARMEABI = "armeabi";
    public static final String PROCESSOR_ARCH_ARM64   = "arm64-v8a";
    public static final String PROCESSOR_ARCH_X86     = "x86";
    public static final String PROCESSOR_ARCH_MIPS    = "mips";
    
    public static final String PROCESSOR_LABEL_ARMEABI = "arm";
    public static final String PROCESSOR_LABEL_ARM64   = "aarch64";
    public static final String PROCESSOR_LABEL_X86     = "x86";
    public static final String PROCESSOR_LABEL_MIPS    = "mips";
    
    public static final String PREFERENCE_NAME = "dynamic_load_configs";
    
    public final static String INTENT_PLUGIN_PACKAGE = "dl_plugin_package";
    public final static String INTENT_PLUGIN_CLASS 	 = "dl_plugin_class";

	// data/data/HostPackageName下各插件相关存放目录 
	public static final String PATH_PLUGIN_RAW  = "p_raw";
	public static final String PATH_PLUGIN_DEX  = "p_dex";
	public static final String PATH_PLUGIN_LIB  = "p_lib";
	public static final String PATH_PLUGIN_ICON = "p_icon";
	public static final String PATH_PLUGIN_CONF = "p_conf";
}
