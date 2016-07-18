
package com.ryg.plugin.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;

import dalvik.system.DexClassLoader;

/**
 * PluginPackage
 * A plugin apk. Activities in a same apk share a same AssetManager, Resources
 * and DexClassLoader.
 * @author singwhatiwanna, modify Nemo.
 */
public class PluginPackage {

    public String packageName;
    public String defaultActivity;
    public DexClassLoader classLoader;
    public AssetManager assetManager;
    public Resources resources;
    public PackageInfo packageInfo;

	/*ADD, Nemo@PluginFramework, 2015-10-13.*/
    private final Map<String, ArrayList<String>> mMapActivityIntents = new HashMap<String, ArrayList<String>>();
    private final Map<String, ArrayList<String>> mMapServiceIntents = new HashMap<String, ArrayList<String>>();
    private final Map<String, ArrayList<String>> mMapReceiverIntents = new HashMap<String, ArrayList<String>>();
    

    public PluginPackage(DexClassLoader loader, Resources resources,
            PackageInfo packageInfo) {
        this.packageName = packageInfo.packageName;
        this.classLoader = loader;
        this.assetManager = resources.getAssets();
        this.resources = resources;
        this.packageInfo = packageInfo;

        defaultActivity = parseDefaultActivityName();
    }

    /*ADD, Nemo@PluginFramework, 2015-10-13.*/
    public PluginPackage(DexClassLoader loader, Resources resources,
            PackageInfo packageInfo, Map<String, ArrayList<String>> activities, 
            Map<String, ArrayList<String>> services, Map<String, ArrayList<String>> receivers) {
        this(loader, resources, packageInfo);

        if(activities != null) {
            mMapActivityIntents.putAll(activities);
        }

        if(services != null) {
            mMapServiceIntents.putAll(services);
        }
		
		if(receivers != null) {
            mMapReceiverIntents.putAll(receivers);
        }
    }

    private final String parseDefaultActivityName() {
        if (packageInfo.activities != null && packageInfo.activities.length > 0) {
            return packageInfo.activities[0].name;
        }
        return "";
    }
	
	/*ADD, Nemo@PluginFramework, 2015-10-13.*/
	public Map<String, ArrayList<String>> getActivityIntentMap() {
        return mMapActivityIntents;
    }
	
	public Map<String, ArrayList<String>> getServiceIntentMap() {
        return mMapServiceIntents;
    }
	
	public Map<String, ArrayList<String>> getReceiverIntentMap() {
        return mMapReceiverIntents;
    }
	
	public Resources getPluginResources() {
		return resources;
	}
	
	public void updatePluginResources(Context hostContext) {
		Configuration config = hostContext.getResources().getConfiguration();
		resources.updateConfiguration(config, hostContext.getResources().getDisplayMetrics());
	}
	/* ADD end */
}
