package com.ryg.plugin.proxy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ryg.plugin.internal.PluginManager;

import com.ryg.plugin.utils.LOG;
/**
 * BroadcastReceiverProxy暂未使用
 * @author Nemo
 */

public class BroadcastReceiverProxy extends BroadcastReceiver {
    private final String TAG = BroadcastReceiverProxy.class.getSimpleName();
	

    @Override
    public void onReceive(Context context, Intent intent) {
		if (intent == null) {
			return;
		}
        String action = intent.getAction();
		LOG.d(TAG, "1112--- receive intent = " + action);
        
		PluginManager.getInstance(context.getApplicationContext()).fetchStaticRegisteredIntentReceiver(intent);
    }
	
}