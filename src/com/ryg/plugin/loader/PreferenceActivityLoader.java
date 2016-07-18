
package com.ryg.plugin.loader;

import android.preference.PreferenceActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.os.Build;
import android.os.Bundle;

import com.ryg.plugin.internal.IPluginActivity;
import com.ryg.plugin.utils.Constants;
import com.ryg.plugin.utils.LOG;

/**
 * This is a plugin activity proxy, the proxy will create the plugin activity
 * with reflect, and then call the plugin activity's attach、onCreate method, at
 * this time, the plugin activity is running.
 * @author Nemo.
 */
public class PreferenceActivityLoader extends BaseLoader<PreferenceActivity, IPluginActivity> {
	
	private final String TAG = PreferenceActivityLoader.class.getSimpleName();
    private AssetManager mAssetManager;
    private Resources mResources;
    private Theme mTheme;

    private ActivityInfo mActivityInfo;

    public PreferenceActivityLoader(PreferenceActivity activity) {
        mProxyComponent = activity;
    }

    private void initializeActivityInfo() {
        PackageInfo packageInfo = mPluginPackage.packageInfo;
        if ((packageInfo.activities != null) && (packageInfo.activities.length > 0)) {
            if (mPluginClazz == null) {
                mPluginClazz = packageInfo.activities[0].name;
            }

            // Finals 修复主题BUG
            int defaultTheme = packageInfo.applicationInfo.theme;
            for (ActivityInfo a : packageInfo.activities) {
                if (a.name.equals(mPluginClazz)) {
                    mActivityInfo = a;
                    // Finals ADD 修复主题没有配置的时候插件异常
                    if (mActivityInfo.theme == 0) {
                        if (defaultTheme != 0) {
                            mActivityInfo.theme = defaultTheme;
                        } else {
                            if (Build.VERSION.SDK_INT >= 14) {
                                mActivityInfo.theme = android.R.style.Theme_DeviceDefault;
                            } else {
                                mActivityInfo.theme = android.R.style.Theme;
                            }
                        }
                    }
                }
            }

        }
    }

    private void handleActivityInfo() {
        LOG.d(TAG, "handleActivityInfo, theme=" + mActivityInfo.theme);
        if (mActivityInfo.theme > 0) {
            mProxyComponent.setTheme(mActivityInfo.theme);
        }

        Theme superTheme = mProxyComponent.getTheme();
        mTheme = mResources.newTheme();
        mTheme.setTo(superTheme);
        // Finals适配三星以及部分加载XML出现异常BUG
        try {
            mTheme.applyStyle(mActivityInfo.theme, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // TODO: handle mActivityInfo.launchMode here in the future.
    }
	
	//@Override
    //public void onCreate(Intent intent) {
    //   super.onCreate(intent);
    //}

    @Override
    protected void init(Intent intent) {
        super.init(intent);
        mAssetManager = mPluginPackage.assetManager;
        mResources = mPluginPackage.resources;
        initializeActivityInfo();
        handleActivityInfo();
    }

    public ClassLoader getClassLoader() {
        return mPluginPackage.classLoader;
    }

    public AssetManager getAssets() {
        return mAssetManager;
    }

    public Resources getResources() {
        return mResources;
    }

    public Theme getTheme() {
        return mTheme;
    }

    public IPluginActivity getRemoteActivity() {
        return mPlugin;
    }

    @Override
    public void callPluginOnCreate() {
		Bundle bundle = new Bundle();
        bundle.putInt(Constants.FROM, Constants.FROM_EXTERNAL);
        mPlugin.onCreate(bundle);
    }
}
