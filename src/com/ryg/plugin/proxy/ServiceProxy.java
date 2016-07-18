
package com.ryg.plugin.proxy;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.IBinder;

import com.ryg.plugin.internal.IAttachable;
import com.ryg.plugin.internal.PluginPackage;
import com.ryg.plugin.internal.IPluginService;
import com.ryg.plugin.loader.ServiceLoader;
import com.ryg.plugin.utils.Constants;
import com.ryg.plugin.utils.LOG;

/**
 * 插件Service,宿主APK的manifest中使用需要注册此ServiceProxy
 * @author singwhatiwanna, modify Nemo.
 */

public class ServiceProxy extends Service implements IAttachable<IPluginService> {
    private static final String TAG = ServiceProxy.class.getSimpleName();
    
    private ServiceLoader mImpl = new ServiceLoader(this);
    private IPluginService mRemoteService;


    @Override
    public IBinder onBind(Intent intent) {
        LOG.d(TAG, TAG + " onBind");
        // 判断是否存在插件Service，如果存在，则不进行Service插件的构造工作
        if (mRemoteService == null) {
            mImpl.onCreate(intent);
        } else {
        	String serviceClassName = intent.getStringExtra(Constants.EXTRA_CLASS);
        	if (serviceClassName.equals(mRemoteService.getClass().getName()))  {
        	} else {
        		mRemoteService = null;
        		mImpl.onCreate(intent);
			}
        }
        return mRemoteService.onBind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LOG.d(TAG, "onCreate");
    }

    // @Override
    // public void onStart(Intent intent, int startId) {
    // // TODO Auto-generated method stub
    // super.onStart(intent, startId);
    // LOG.d(TAG, TAG + " onStart");
    //
    // }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOG.d(TAG, "onStartCommand " + this.toString());
        // 判断是否存在插件Service，如果存在，则不进行Service插件的构造工作
        if (mRemoteService == null) {
            mImpl.onCreate(intent);
        } else {
        	String serviceClassName = intent.getStringExtra(Constants.EXTRA_CLASS);
        	if (serviceClassName.equals(mRemoteService.getClass().getName())) {
        	} else {
        		mRemoteService = null;
        		mImpl.onCreate(intent);
			}
        }
        if (mRemoteService == null) {
        	return super.onStartCommand(intent, flags, startId);
		} else {
			super.onStartCommand(intent, flags, startId);
			return mRemoteService.onStartCommand(intent, flags, startId);
		}
    }

    @Override
    public void onDestroy() {
    	if (mRemoteService != null)
    		mRemoteService.onDestroy();
        super.onDestroy();
        LOG.d(TAG, "onDestroy");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	if (mRemoteService != null)
    		mRemoteService.onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
        LOG.d(TAG, "onConfigurationChanged");
    }

    @Override
    public void onLowMemory() {
    	if (mRemoteService != null)
    		mRemoteService.onLowMemory();
        super.onLowMemory();
        LOG.d(TAG, "onLowMemory");
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTrimMemory(int level) {
    	if (mRemoteService != null)
    		mRemoteService.onTrimMemory(level);
        super.onTrimMemory(level);
        LOG.d(TAG, "onTrimMemory");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LOG.d(TAG, "onUnbind");
        super.onUnbind(intent);
        return mRemoteService.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
    	if (mRemoteService != null)
        	mRemoteService.onRebind(intent);
        super.onRebind(intent);
        LOG.d(TAG, "onRebind");
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTaskRemoved(Intent rootIntent) {
    	if (mRemoteService != null)
    		mRemoteService.onTaskRemoved(rootIntent);    	
        super.onTaskRemoved(rootIntent);
        LOG.d(TAG, "onTaskRemoved");
    }

    @Override
    public void attach(IPluginService remoteService, PluginPackage pluginPackage) {
    	if (remoteService != null)
    		mRemoteService = remoteService;
    }
}
