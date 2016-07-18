
package com.ryg.plugin.loader;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;

import com.ryg.plugin.internal.IPluginService;
import com.ryg.plugin.utils.Constants;

/**
 * 服务代理类
 * @author singwhatiwanna, modify Nemo.
 */
public class ServiceLoader extends BaseLoader<Service, IPluginService> {

    public ServiceLoader(Service service) {
        mProxyComponent = service;
    }
	
	@Override
    public void onCreate(Intent intent) {
        super.onCreate(intent);
        callPluginOnCreate();
    }

    @Override
    protected void callPluginOnCreate() {
		Bundle bundle = new Bundle();
        bundle.putInt(Constants.FROM, Constants.FROM_EXTERNAL);
        if (mPlugin != null)
        	mPlugin.onCreate();
    }

}
