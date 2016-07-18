package com.ryg.plugin;

import java.io.File;
import java.lang.reflect.Field;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.app.FragmentManager;
import android.content.res.AssetManager;

import com.ryg.plugin.internal.IPluginActivity;
import com.ryg.plugin.internal.IProxyRealCallback;
import com.ryg.plugin.internal.PluginManager;
import com.ryg.plugin.internal.PluginPackage;
import com.ryg.plugin.utils.Constants;
import com.ryg.plugin.utils.ReflectionUtil;
import com.ryg.plugin.utils.LOG;

/**
 * Modify for PluginManager using the Application context replace the Activity context.
 *
 * @author renyugang, modify Nemo.
 * note: can use that like this.
 * All the time use 'that' represents the Proxy Activity Context.
 * Cause this method is final, so use that.requestWindowFeature(featureId).
 */

public class BasePluginActivity extends Activity implements IPluginActivity {

    private static final String TAG = BasePluginActivity.class.getSimpleName();

    /**
     * 代理activity，可以当作Context来使用，会根据需要来决定是否指向this
     */
    protected Activity mProxyActivity;

    /**
     * 等同于mProxyActivity，可以当作Context来使用，会根据需要来决定是否指向this<br/>
     * 可以当作this来使用
     */
    protected Activity that;
	
	/**
     * this is add for callback that should devide into two steps, such as onBackPressed(),
	 * and is only used for FROM_EXTERNAL.
     */
	protected IProxyRealCallback mProxyRealCallback;
	
    protected PluginManager mPluginManager;
    protected PluginPackage mPluginPackage;

    protected int mFrom = Constants.FROM_INTERNAL;
	

    @Override
    public void attach(Activity proxyActivity, PluginPackage pluginPackage) {
        LOG.d(TAG, "attach: proxyActivity(Activity) = " + proxyActivity);
        mProxyActivity = (Activity) proxyActivity;
        that = mProxyActivity;
        mPluginPackage = pluginPackage;
		
		mProxyRealCallback = (IProxyRealCallback) proxyActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mFrom = savedInstanceState.getInt(Constants.FROM, Constants.FROM_INTERNAL);
        }
        if (mFrom == Constants.FROM_INTERNAL) {
            super.onCreate(savedInstanceState);
            mProxyActivity = this;
            that = mProxyActivity;
        }
		
		//#ifndef /*--- EDIT_NO.1 ---*/
		/*MODIFY, Nemo@PluginFramework, 2015-10-12: Modify for PluginManager using the Application context replace the Activity context.*/
        //mPluginManager = PluginManager.getInstance(that);
		//#else /*  */
        mPluginManager = PluginManager.getInstance(that.getApplicationContext());
		//#endif /*  */
		
        LOG.d(TAG, "onCreate: from= "
                + (mFrom == Constants.FROM_INTERNAL ? "Constants.FROM_INTERNAL"
                        : "FROM_EXTERNAL"));
    }

    @Override
    public void setContentView(View view) {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.setContentView(view);
        } else {
            mProxyActivity.setContentView(view);
        }
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.setContentView(view, params);
        } else {
            mProxyActivity.setContentView(view, params);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.setContentView(layoutResID);
        } else {
            mProxyActivity.setContentView(layoutResID);
        }
    }

    @Override
    public void addContentView(View view, LayoutParams params) {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.addContentView(view, params);
        } else {
            mProxyActivity.addContentView(view, params);
        }
    }

    @Override
    public View findViewById(int id) {
        if (mFrom == Constants.FROM_INTERNAL) {
            return super.findViewById(id);
        } else {
            return mProxyActivity.findViewById(id);
        }
    }

    @Override
    public Intent getIntent() {
        if (mFrom == Constants.FROM_INTERNAL) {
            return super.getIntent();
        } else {
            return mProxyActivity.getIntent();
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        if (mFrom == Constants.FROM_INTERNAL) {
            return super.getClassLoader();
        } else {
            return mProxyActivity.getClassLoader();
        }
    }

    @Override
    public Resources getResources() {
        if (mFrom == Constants.FROM_INTERNAL) {
            return super.getResources();
        } else {
            return mProxyActivity.getResources();
        }
    }

    @Override
    public String getPackageName() {
        if (mFrom == Constants.FROM_INTERNAL) {
            return super.getPackageName();
        } else {
            return mPluginPackage.packageName;
        }
    }

    @Override
    public LayoutInflater getLayoutInflater() {
        if (mFrom == Constants.FROM_INTERNAL) {
            return super.getLayoutInflater();
        } else {
            return mProxyActivity.getLayoutInflater();
        }
    }

    @Override
    public MenuInflater getMenuInflater() {
        if (mFrom == Constants.FROM_INTERNAL) {
            return super.getMenuInflater();
        } else {
            return mProxyActivity.getMenuInflater();
        }
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        if (mFrom == Constants.FROM_INTERNAL) {
            return super.getSharedPreferences(name, mode);
        } else {
            return mProxyActivity.getSharedPreferences(name, mode);
        }
    }

    @Override
    public Context getApplicationContext() {
        if (mFrom == Constants.FROM_INTERNAL) {
            return super.getApplicationContext();
        } else {
            return mProxyActivity.getApplicationContext();
        }
    }

    @Override
    public WindowManager getWindowManager() {
        if (mFrom == Constants.FROM_INTERNAL) {
            return super.getWindowManager();
        } else {
            return mProxyActivity.getWindowManager();
        }
    }

    @Override
    public Window getWindow() {
        if (mFrom == Constants.FROM_INTERNAL) {
            return super.getWindow();
        } else {
            return mProxyActivity.getWindow();
        }
    }

    @Override
    public Object getSystemService(String name) {
        if (mFrom == Constants.FROM_INTERNAL) {
            return super.getSystemService(name);
        } else {
            return mProxyActivity.getSystemService(name);
        }
    }

    @Override
    public void finish() {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.finish();
        } else {
            mProxyActivity.finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.onBackPressed();
        } else {
			//You can also call "mProxyActivity.finish()" here, but it feels not good.
            mProxyRealCallback.onRealBackPressed();
        }
    }

    @Override
    public void onStart() {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.onStart();
        }
    }

    @Override
    public void onRestart() {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.onRestart();
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.onSaveInstanceState(outState);
        }
    }

    public void onNewIntent(Intent intent) {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.onNewIntent(intent);
        }
    }

    @Override
    public void onResume() {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.onPause();
        }
    }

    @Override
    public void onStop() {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.onStop();
        }
    }

    @Override
    public void onDestroy() {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.onDestroy();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (mFrom == Constants.FROM_INTERNAL) {
            return super.onTouchEvent(event);
        }
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mFrom == Constants.FROM_INTERNAL) {
            return super.onKeyUp(keyCode, event);
        }
        return false;
    }

    public void onWindowAttributesChanged(WindowManager.LayoutParams params) {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.onWindowAttributesChanged(params);
        }
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.onWindowFocusChanged(hasFocus);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (mFrom == Constants.FROM_INTERNAL) {
            return super.onCreateOptionsMenu(menu);
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (mFrom == Constants.FROM_INTERNAL) {
            return super.onOptionsItemSelected(item);
        }
        return false;
    }

	public ActionBar getActionBar() {
	    if (mFrom == Constants.FROM_INTERNAL) {
            return super.getActionBar();
        } else {
            return mProxyActivity.getActionBar();
        }
	}

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, 
            Preference preference) {
        return false;
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        if (mFrom == Constants.FROM_INTERNAL) {
            return super.registerReceiver(receiver, filter);
        } else {
            return mProxyActivity.registerReceiver(receiver, filter);
        }
    }

    public void unregisterReceiver(BroadcastReceiver receiver) {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.unregisterReceiver(receiver);
        } else {
            mProxyActivity.unregisterReceiver(receiver);
        }
    }

    public void sendBroadcast(Intent intent) {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.sendBroadcast(intent);
        } else {
            mProxyActivity.sendBroadcast(intent);
        }
    }

    @Override
	public FragmentManager getFragmentManager() {
	    if (mFrom == Constants.FROM_INTERNAL) {
            return super.getFragmentManager();
        } else {
            return mProxyActivity.getFragmentManager();
        }
	}

    /**
     * @param dlIntent
     * @return may be {@link #START_RESULT_SUCCESS},
     *         {@link #START_RESULT_NO_PKG}, {@link #START_RESULT_NO_CLASS},
     *         {@link #START_RESULT_TYPE_ERROR}
     */
    public int startPluginActivity(PluginIntent dlIntent) {
        return startPluginActivityForResult(dlIntent, -1);
    }

    /**
     * @param dlIntent
     * @return may be {@link #START_RESULT_SUCCESS},
     *         {@link #START_RESULT_NO_PKG}, {@link #START_RESULT_NO_CLASS},
     *         {@link #START_RESULT_TYPE_ERROR}
     */
    public int startPluginActivityForResult(PluginIntent dlIntent, int requestCode) {
        if (mFrom == Constants.FROM_EXTERNAL) {
            if (dlIntent.getPluginPackage() == null) {
                dlIntent.setPluginPackage(mPluginPackage.packageName);
            }
        }
        return mPluginManager.startPluginActivityForResult(that, dlIntent, requestCode);
    }

    public int startPluginService(PluginIntent dlIntent) {
        if (mFrom == Constants.FROM_EXTERNAL) {
            if (dlIntent.getPluginPackage() == null) {
                dlIntent.setPluginPackage(mPluginPackage.packageName);
            }
        }
        return mPluginManager.startPluginService(that, dlIntent);
    }

    public int bindPluginService(PluginIntent dlIntent, ServiceConnection conn, int flags) {
        if (mFrom == Constants.FROM_EXTERNAL) {
            if (dlIntent.getPluginPackage() == null) {
                dlIntent.setPluginPackage(mPluginPackage.packageName);
            }
        }
        return mPluginManager.bindPluginService(that, dlIntent, conn, flags);
    }

    public int unBindPluginService(PluginIntent dlIntent, ServiceConnection conn) {
        if (mFrom == Constants.FROM_EXTERNAL) {
            if (dlIntent.getPluginPackage() == null)
                dlIntent.setPluginPackage(mPluginPackage.packageName);
        }
        return mPluginManager.unBindPluginService(that, dlIntent, conn);
    }
	
	/* Nemo@PluginFramework, 2015-12-07: Activity {@link #requestWindowFeature(int)} Proxy.
	 * Cause this method is #final#, so use that.requestWindowFeature(featureId).
	 * ----------------------------------------------------------
	 * public final boolean requestWindowFeature(int featureId);
	 * --->
	 * that.requestWindowFeature(featureId);
	 * ----------------------------------------------------------
	 */
	public void requestPluginWindowFeature(int featureId) {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.requestWindowFeature(featureId);
        } else {
            mProxyActivity.requestWindowFeature(featureId);
        }
    }
	
	/* Nemo@PluginFramework, 2015-12-07: Activity {@link #setResult(int, data)} Proxy.
	 * Cause this method is #final#, so use that.setResult(resultCode, data).
	 * ----------------------------------------------------------
	 * public final void setResult(int resultCode, Intent data);
	 * --->
	 * that.setResult(resultCode, data);
	 * ----------------------------------------------------------
	 */
	public void setPluginResult(int resultCode, Intent data) {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.setResult(resultCode, data);
        } else {
            mProxyActivity.setResult(resultCode, data);
        }
    }
	
	public void invokeApplicationContextAssets(AssetManager toAssets) {
		LOG.d(TAG, "1111--- invokeApplicationContextAssets() START, toAssets = " + toAssets);
		
		try {
		    Class<?> class_mBase = that.getApplicationContext().getClass().getSuperclass().getSuperclass();
		    LOG.d(TAG, "1111--- class_mBase " + class_mBase);
			Field field_mBase = ReflectionUtil.getField(class_mBase, "mBase");
            LOG.d(TAG, "1111--- field_mBase " + field_mBase);
			Object obj_mBase = ReflectionUtil.getObject(field_mBase, that.getApplicationContext());
			LOG.d(TAG, "1111--- obj_mBase " + obj_mBase);
			
			Field field_mResources = ReflectionUtil.getField(obj_mBase, "mResources");
			Object obj_mResources = ReflectionUtil.getObject(field_mResources, obj_mBase);
			LOG.d(TAG, "1111--- obj_mResources = " + obj_mResources);
			
			Field field_mAssets = ReflectionUtil.getField(obj_mResources, "mAssets");
			field_mAssets.set(obj_mResources, toAssets);
			
			Object obj_mAssets = ReflectionUtil.getObject(field_mAssets, obj_mResources);
			LOG.d(TAG, "1111--- invokeApplicationContextAssets() END, obj_mAssets = " + obj_mAssets);
		} catch (Exception e) {
			LOG.d(TAG, "invoke Exception: " + e);
            e.printStackTrace();
        }
	}

    // /**
    // * 直接调用that.startService
    // * that 可能有两种情况
    // * 1.指向this
    // * 2.指向ProxyActivity
    // */
    // public ComponentName startService(Intent service) {
    // return that.startService(service);
    // }
    //
    // @Override
    // public boolean stopService(Intent name) {
    // // TODO Auto-generated method stub
    // return super.stopService(name);
    // }
    //
    // @Override
    // public boolean bindService(Intent service, ServiceConnection conn, int
    // flags) {
    // // TODO Auto-generated method stub
    // return super.bindService(service, conn, flags);
    // }
    //
    // @Override
    // public void unbindService(ServiceConnection conn) {
    // // TODO Auto-generated method stub
    // super.unbindService(conn);
    // }
    
	
	//#ifdef  /*--- QUESTION_NO.1 ---*/
	/*Nemo@PluginFramework, 2015-10-10: Add for either the Activity starts FROM_INTERNAL or FROM_EXTERNAL,
	**we can use the same code and same member 'that' represents the Activity itself.*/
	//----------------------------------------------------------
	//this;
	//--->
	//that;
	//----------------------------------------------------------
	//#endif /*  */

}
