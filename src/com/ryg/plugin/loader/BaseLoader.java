
package com.ryg.plugin.loader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ryg.plugin.internal.IAttachable;
import com.ryg.plugin.internal.PluginManager;
import com.ryg.plugin.internal.PluginPackage;
import com.ryg.plugin.utils.Configs;
import com.ryg.plugin.utils.Constants;
import com.ryg.plugin.utils.LOG;

import java.lang.reflect.Constructor;

/**
 * 插件加载器基类,负责构建、加载插件组件，并且与代理组件建立关联关系
 * @author mrsimple, modify Nemo.
 * @param <P> 组件代理类型,例如ProxyActivity,ProxyService等
 * @param <T> Plugin类型，例如{@see Plugin},{@see ServicePlugin}
 */
@SuppressWarnings("unchecked")
public abstract class BaseLoader<P extends Context, T> {
	private static final String TAG = BaseLoader.class.getSimpleName();
    
    /**
     * 组件类型,例如Service,Activity等
     */
    protected P mProxyComponent;

    /**
     * 插件组件,例如BasePluginActivity的子类、BasePluginService子类等
     */
    protected T mPlugin;

    // 插件的包名
    protected String mPackageName;
    // 插件的Service的类名
    protected String mPluginClazz;
    // 插件管理器
    protected PluginManager mPluginManager;
    // 插件PluginPackage
    protected PluginPackage mPluginPackage;
    

    /**
     * 创建组件,并且调用组件的onCreate方法
     * 
     * @param intent
     */
    public void onCreate(Intent intent) {
        try {
            LOG.d(TAG, "BaseLoader onCreate().");
            //LOG.d(TAG, "BaseLoader onCreate() Throwable: ", new Throwable());
            
            // 1、初始化插件包名、类名等属性
            init(intent);
            // 2、反射构造插件对象
            LOG.d(TAG, "1015 BaseLoader mPlugin -= " + mPlugin);
            mPlugin = createPlugin(mPluginPackage.classLoader);
            LOG.d(TAG, "1015 BaseLoader mPlugin ---= " + mPlugin);
            // 3、调用插件代理对象的attach方法，将插件注入到代理对象中
            attachPluginToProxy();

			/*MODIFY, Nemo@PluginFramework, 2015-10-14: Modify for .*/
            // 4、调用插件的attach、onCreate方法启动插件
            //callOnCreate();
			//#else
			attachProxyToBasePlugin();
			//#endif

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void init(Intent intent) {
        // set the extra's class loader
        intent.setExtrasClassLoader(Configs.sPluginClassloader);
        // 插件的包名
        LOG.d(TAG, "1015 BaseLoader getStringExtra -");
        mPackageName = intent.getStringExtra(Constants.EXTRA_PACKAGE);
        LOG.d(TAG, "1015 BaseLoader getStringExtra ---");
        // 插件的Service的类名
        mPluginClazz = intent.getStringExtra(Constants.EXTRA_CLASS);
        mPluginManager = PluginManager.getInstance(mProxyComponent);
        mPluginPackage = mPluginManager.getPackage(mPackageName);
    }

    protected T createPlugin(ClassLoader classLoader)
            throws Exception {
        LOG.d(TAG, "clazz=" + mPluginClazz + " packageName=" + mPackageName);
        Class<?> localClass = classLoader.loadClass(mPluginClazz);
        Constructor<?> localConstructor = localClass.getConstructor(new Class[] {});
        return (T) localConstructor.newInstance(new Object[] {});
    }

    /**
     * 调用代理对象的attach
     */
    private void attachPluginToProxy() {
        LOG.d(TAG, "1013 attachPluginToProxy() -" );
        ((IAttachable<T>) mProxyComponent).attach(mPlugin, mPluginPackage);
    }
	
	/**
     * 调用插件的attach
     */
    private void attachProxyToBasePlugin() {
        LOG.d(TAG, "1014 attachProxyToBasePlugin() ---" );
        ((IAttachable<P>) mPlugin).attach(mProxyComponent, mPluginPackage);
    }

    /**
     * 调用插件的attach和onCreate函数，启动插件
     */
	/*
    protected void callOnCreate() {
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.FROM, Constants.FROM_EXTERNAL);
        // 调用插件的onCreate
        callPluginOnCreate(bundle);
    }
	*/
	

    /**
     * 调用插件的onCreate方法
     * 
     * @param bundle 额外的数据
     */
    protected abstract void callPluginOnCreate();

}
