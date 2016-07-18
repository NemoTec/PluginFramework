package com.ryg.plugin;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.IBinder;

import com.ryg.plugin.internal.PluginPackage;
import com.ryg.plugin.internal.IPluginService;
import com.ryg.plugin.utils.Constants;
import com.ryg.plugin.utils.LOG;

/**
 * PluginService.
 *
 * @author singwhatiwanna, modify Nemo.
 * 
 */

public class BasePluginService extends Service implements IPluginService {
    private static final String TAG = BasePluginService.class.getSimpleName();
    
    private Service mProxyService;
    protected PluginPackage mPluginPackage;
    protected Service that = this;
    protected int mFrom = Constants.FROM_INTERNAL;
    
    @Override
    public void attach(Service proxyService, PluginPackage pluginPackage) {
        LOG.d(TAG, "attach");
        mProxyService = proxyService;
        mPluginPackage = pluginPackage;
        that = mProxyService;
        mFrom = Constants.FROM_EXTERNAL;
    }
    
    protected boolean isInternalCall() {
        return mFrom == Constants.FROM_INTERNAL;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        LOG.d(TAG, "onBind");
        return null;
    }

    @Override
    public void onCreate() {
        LOG.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOG.d(TAG, "onStartCommand");
        return 0;
    }

    @Override
    public void onDestroy() {
        LOG.d(TAG, "onDestroy");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LOG.d(TAG, "onConfigurationChanged");
    }

    @Override
    public void onLowMemory() {
        LOG.d(TAG, "onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        LOG.d(TAG, "onTrimMemory");
        
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LOG.d(TAG, "onUnbind");
        return false;
    }

    @Override
    public void onRebind(Intent intent) {
        LOG.d(TAG, "onRebind");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        LOG.d(TAG, "onTaskRemoved");
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        if (mFrom == Constants.FROM_INTERNAL) {
            return super.registerReceiver(receiver, filter);
        } else {
            return mProxyService.registerReceiver(receiver, filter);
        }
    }

    public void unregisterReceiver(BroadcastReceiver receiver) {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.unregisterReceiver(receiver);
        } else {
            mProxyService.unregisterReceiver(receiver);
        }
    }

    public void sendBroadcast(Intent intent) {
        if (mFrom == Constants.FROM_INTERNAL) {
            super.sendBroadcast(intent);
        } else {
            mProxyService.sendBroadcast(intent);
        }
    }

}
