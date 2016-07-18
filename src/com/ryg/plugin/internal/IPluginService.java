package com.ryg.plugin.internal;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;

/**
 * IPluginService
 * 插件框架中所有的Service都继承自IPluginActivity
 * @author singwhatiwanna, modify Nemo.
 * 
 */

public interface IPluginService extends IAttachable<Service> {

    public void onCreate(); 

    public void onStart(Intent intent, int startId); 
    
    public int onStartCommand(Intent intent, int flags, int startId);
    
    public void onDestroy();
    
    public void onConfigurationChanged(Configuration newConfig); 
    
    public void onLowMemory();
    
    public void onTrimMemory(int level);
    
    public IBinder onBind(Intent intent);
    
    public boolean onUnbind(Intent intent);
    
    public void onRebind(Intent intent);
    
    public void onTaskRemoved(Intent rootIntent); 
    
}
