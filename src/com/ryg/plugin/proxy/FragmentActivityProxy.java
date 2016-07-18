
package com.ryg.plugin.proxy;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager.LayoutParams;

import com.ryg.plugin.internal.IPluginActivity;
import com.ryg.plugin.internal.IProxyRealCallback;
import com.ryg.plugin.internal.IAttachable;
import com.ryg.plugin.internal.PluginPackage;
import com.ryg.plugin.loader.ActivityLoader;
import com.ryg.plugin.utils.ReflectionUtil;
import com.ryg.plugin.utils.LOG;
/**
 * 插件FragmentActivity,宿主APK的manifest中使用需要注册此FragmentActivityProxy
 * @author singwhatiwanna, modify Nemo.
 */

public class FragmentActivityProxy extends FragmentActivity implements IAttachable<IPluginActivity>, IProxyRealCallback {
    private static final String TAG = FragmentActivityProxy.class.getSimpleName();

    protected IPluginActivity mRemoteActivity;
    private ActivityLoader impl = new ActivityLoader(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        invokeApplicationIcon(android.R.drawable.sym_def_app_icon);
        
        super.onCreate(savedInstanceState);
        impl.onCreate(getIntent());
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
        mRemoteActivity.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        mRemoteActivity.onStart();
        super.onStart();
    }

    @Override
    protected void onRestart() {
        mRemoteActivity.onRestart();
        super.onRestart();
    }

    @Override
    protected void onResume() {
        mRemoteActivity.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mRemoteActivity.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mRemoteActivity.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mRemoteActivity.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mRemoteActivity.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mRemoteActivity.onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        mRemoteActivity.onNewIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    public void onBackPressed() {
        mRemoteActivity.onBackPressed();
        //super.onBackPressed();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return mRemoteActivity.onTouchEvent(event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        super.onKeyUp(keyCode, event);
        return mRemoteActivity.onKeyUp(keyCode, event);
    }

    @Override
    public void onWindowAttributesChanged(LayoutParams params) {
        mRemoteActivity.onWindowAttributesChanged(params);
        super.onWindowAttributesChanged(params);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        mRemoteActivity.onWindowFocusChanged(hasFocus);
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mRemoteActivity.onCreateOptionsMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mRemoteActivity.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
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
