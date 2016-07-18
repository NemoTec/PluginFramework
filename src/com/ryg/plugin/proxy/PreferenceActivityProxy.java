/********************************************************************************
 ** File: - PreferenceActivityProxy.java
 ** Description: 
 **     Add PreferenceActivity Proxy!
 **     
 ** Version: 1.0
 ** Date: 2015-10-13
 ********************************************************************************/
 
package com.ryg.plugin.proxy;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager.LayoutParams;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import com.ryg.plugin.internal.IPluginActivity;
import com.ryg.plugin.internal.IProxyRealCallback;
import com.ryg.plugin.internal.IAttachable;
import com.ryg.plugin.internal.PluginManager;
import com.ryg.plugin.internal.PluginPackage;
import com.ryg.plugin.loader.PreferenceActivityLoader;
import com.ryg.plugin.utils.ReflectionUtil;
import com.ryg.plugin.utils.LOG;
/**
 * 插件PreferenceActivity,宿主APK的manifest中使用需要注册此PreferenceActivityProxy
 * @author Nemo
 */

public class PreferenceActivityProxy extends PreferenceActivity implements IAttachable<IPluginActivity>, IProxyRealCallback {
    private static final String TAG = PreferenceActivityProxy.class.getSimpleName();

    protected IPluginActivity mRemoteActivity;
    private PreferenceActivityLoader impl = new PreferenceActivityLoader(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        impl.onCreate(getIntent());
        
        invokeApplicationIcon(android.R.drawable.sym_def_app_icon);
        
        super.onCreate(savedInstanceState);
        impl.callPluginOnCreate();
    }

    @Override
    public void attach(IPluginActivity remoteActivity, PluginPackage pluginPackage) {
        mRemoteActivity = remoteActivity;
    }

    @Override
    public AssetManager getAssets() {
        return impl.getAssets() == null ? super.getAssets() : impl.getAssets();
    }

    @Override
    public Resources getResources() {
        return impl.getResources() == null ? super.getResources() : impl.getResources();
    }

    @Override
    public Theme getTheme() {
        return impl.getTheme() == null ? super.getTheme() : impl.getTheme();
    }

    @Override
    public ClassLoader getClassLoader() {
        return impl.getClassLoader();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mRemoteActivity != null)
        	mRemoteActivity.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
    	if (mRemoteActivity != null)
    		mRemoteActivity.onStart();
        super.onStart();
    }

    @Override
    protected void onRestart() {
    	if (mRemoteActivity != null)
    		mRemoteActivity.onRestart();
        super.onRestart();
    }

    @Override
    protected void onResume() {
    	if (mRemoteActivity != null)
    		mRemoteActivity.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
    	if (mRemoteActivity != null)
    		mRemoteActivity.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
    	if (mRemoteActivity != null)
    		mRemoteActivity.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
    	if (mRemoteActivity != null)
    		mRemoteActivity.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	if (mRemoteActivity != null)
    		mRemoteActivity.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	if (mRemoteActivity != null)
    		mRemoteActivity.onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
    	if (mRemoteActivity != null)
    		mRemoteActivity.onNewIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    public void onBackPressed() {
    	if (mRemoteActivity != null)
    		mRemoteActivity.onBackPressed();
        //super.onBackPressed();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if (mRemoteActivity != null) {
    		super.onTouchEvent(event);
            return mRemoteActivity.onTouchEvent(event);	
		} else {
			return super.onTouchEvent(event);
		}
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	if (mRemoteActivity != null) {
    		super.onKeyUp(keyCode, event);
            return mRemoteActivity.onKeyUp(keyCode, event);	
		} else {
			return super.onKeyUp(keyCode, event);
		}        
    }

    @Override
    public void onWindowAttributesChanged(LayoutParams params) {
    	if (mRemoteActivity != null)
    		mRemoteActivity.onWindowAttributesChanged(params);
        super.onWindowAttributesChanged(params);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	if (mRemoteActivity != null)
    		mRemoteActivity.onWindowFocusChanged(hasFocus);
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	if (mRemoteActivity != null)
    		mRemoteActivity.onCreateOptionsMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (mRemoteActivity != null)
    		mRemoteActivity.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public ComponentName startService(Intent service) {
        return super.startService(service);
    }
	
	@Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        return mRemoteActivity.onPreferenceTreeClick(preferenceScreen, preference);
    }
	
	//implements IProxyRealCallback.
	@Override
	public void onRealBackPressed() {
        super.onBackPressed();
    }

    private void invokeApplicationIcon(int componentIconId) {
		LOG.d(TAG, "1118--- invokeApplicationIcon() START");
		
		try {
		    LOG.d(TAG, "1114--- super.getResources() = " + super.getResources());
		    LOG.d(TAG, "1114--- impl.getResources() = " + impl.getResources());

		    //ActivityInfo mActivityInfo;
			Field field_mActivityInfo = ReflectionUtil.getField(Activity.class, "mActivityInfo");
			Object obj_mActivityInfo = ReflectionUtil.getObject(field_mActivityInfo, this);
			LOG.d(TAG, "1114--- field_mActivityInfo = " + field_mActivityInfo);
			LOG.d(TAG, "1114--- obj_mActivityInfo = " + obj_mActivityInfo);
			
			
			
			Field field_icon = ReflectionUtil.getField(ActivityInfo.class.getSuperclass().getSuperclass(), "icon");
			LOG.d(TAG, "1114--- field_icon = " + field_icon);
			
			field_icon.set(obj_mActivityInfo, componentIconId);
		
		
		
		} catch (Exception e) {
			LOG.d(TAG, "invoke Exception: " + e);
            e.printStackTrace();
        }
	}

}
