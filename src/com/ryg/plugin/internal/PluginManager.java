
package com.ryg.plugin.internal;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageUserState;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;

import com.ryg.plugin.BasePluginActivity;
import com.ryg.plugin.BasePluginFragmentActivity;
import com.ryg.plugin.PluginIntent;
import com.ryg.plugin.BasePluginPreferenceActivity;
import com.ryg.plugin.BasePluginService;
import com.ryg.plugin.proxy.ActivityProxy;
import com.ryg.plugin.proxy.FragmentActivityProxy;
import com.ryg.plugin.proxy.PreferenceActivityProxy;
import com.ryg.plugin.proxy.ServiceProxy;
import com.ryg.plugin.utils.Constants;
import com.ryg.plugin.utils.LOG;
import com.ryg.plugin.utils.SoLibManager;
import com.ryg.plugin.utils.Utils;

import dalvik.system.DexClassLoader;

/**
 * PluginManager
 * PluginManager 用来管理插件APK以及资源的加载，插件Activity、Service等的启动
 * @author singwhatiwanna, modify Nemo.
 */

public class PluginManager {

    private static final String TAG = PluginManager.class.getSimpleName();

    /**
     * return value of {@link #startPluginActivity(Activity, PluginIntent)} start
     * success
     */
    public static final int START_RESULT_SUCCESS = 0;

    /**
     * return value of {@link #startPluginActivity(Activity, PluginIntent)} package
     * not found
     */
    public static final int START_RESULT_NO_PKG = 1;

    /**
     * return value of {@link #startPluginActivity(Activity, PluginIntent)} class
     * not found
     */
    public static final int START_RESULT_NO_CLASS = 2;

    /**
     * return value of {@link #startPluginActivity(Activity, PluginIntent)} class
     * type error
     */
    public static final int START_RESULT_TYPE_ERROR = 3;

    private static PluginManager sInstance;
    private Context mContext;
    private final HashMap<String, PluginPackage> mPackagesHolder = new HashMap<String, PluginPackage>();

    private int mFrom = Constants.FROM_INTERNAL;

    private String mNativeLibDir = null;

    private int mResult;

    private BroadcastReceiver mReceiver = new BroadcastReceiver(){
    	@Override
       public void onReceive(Context context, Intent intent) {
        	if(intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
        		updatePluginResources();
        	}
        }
    };
    
    private PluginManager(Context context) {
        mContext = context.getApplicationContext();
        mNativeLibDir = mContext.getDir(Constants.PATH_PLUGIN_LIB, Context.MODE_PRIVATE).getAbsolutePath();
        
        IntentFilter filter =new IntentFilter();
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);        
        mContext.registerReceiver(mReceiver, filter);
    }

    public static PluginManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (PluginManager.class) {
                if (sInstance == null) {
                    sInstance = new PluginManager(context);
                }
            }
        }

        return sInstance;
    }

    public PackageInfo fetchArchivePackageInfo(Context context, String apkFilepath) {
        return Utils.getPackageInfo(context, apkFilepath);
    }

    /**
     * Load a apk. Before start a plugin Activity, we should do this first.<br/>
     * NOTE : will only be called by host apk.
     *
     * @param dexPath
     */
    public PluginPackage loadApk(String dexPath) {
        // when loadApk is called by host apk, we assume that plugin is invoked
        // by host.
        return loadApk(dexPath, true);
    }

    /**
     * @param pluginApkPath
     *            plugin path
     * @param hasSoLib
     *            whether exist so lib in plugin
     * @return
     */
    /*
    private PluginPackage loadApk(final String dexPath, boolean hasSoLib) {
        mFrom = Constants.FROM_EXTERNAL;

        PackageInfo packageInfo = mContext.getPackageManager().getPackageArchiveInfo(dexPath,
                PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES);
        if (packageInfo == null) {
            return null;
        }

        PluginPackage pluginPackage = preparePluginEnv(packageInfo, dexPath);
        if (hasSoLib) {
            copySoLib(dexPath);
        }

        return pluginPackage;
    }
    */

    public PluginPackage loadApk(final String pluginApkPath, boolean hasSoLib) {
        mFrom = Constants.FROM_EXTERNAL;
        PackageInfo packageInfo = null;
        PackageParser.Package parserPkg = null;

        //Copy from PackageManager.getPackageArchiveInfo().
        final PackageParser parser = new PackageParser();
        final File apkFile = new File(pluginApkPath);
        try {
            parserPkg = parser.parseMonolithicPackage(apkFile, 0);
            int flags = PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES;

            //if ((flags & GET_SIGNATURES) != 0) {
            //    parser.collectCertificates(pkg, 0);
            //    parser.collectManifestDigest(pkg);
            //}

            //Normal get apk PackageInfo.
            PackageUserState state = new PackageUserState();
            packageInfo = PackageParser.generatePackageInfo(parserPkg, null, flags, 0, 0, null, state);

        } catch (PackageParser.PackageParserException e) {
            LOG.e(TAG, "loadApkEx() PackageParser.PackageParserException: " + e);
            return null;
        } catch (Exception e) {
            LOG.e(TAG, "loadApkEx() Exception: " + e);
            return null;
        } finally {

        }

        PluginPackage pluginPackage = preparePluginEnv(parserPkg, packageInfo, pluginApkPath);
        if (hasSoLib) {
            copySoLib(pluginApkPath);
        }

        return pluginPackage;
    }

    /**
     * prepare plugin runtime env, has DexClassLoader, Resources, and so on.
     *
     * @param packageInfo
     * @param pluginApkPath
     * @return
     */
    private PluginPackage preparePluginEnv(PackageParser.Package parserPkg,
            PackageInfo packageInfo, String pluginApkPath) {

        PluginPackage pluginPackage = mPackagesHolder.get(packageInfo.packageName);
        if (pluginPackage != null) {
            return pluginPackage;
        }
        DexClassLoader dexClassLoader = createDexClassLoader(pluginApkPath);
        AssetManager assetManager = createAssetManager(pluginApkPath);
        Resources resources = createResources(assetManager);

        LOG.d(TAG, "1113--- pluginApkPath=" + pluginApkPath);
        LOG.d(TAG, "1113--- assetManager=" + assetManager);
        // create pluginPackage
        pluginPackage = new PluginPackage(dexClassLoader, resources, packageInfo,
                parseActivityIntents(parserPkg), parseServiceIntents(parserPkg), parseReceiverIntents(parserPkg));
     
        LOG.d(TAG, "1113--- pluginPackage=" + pluginPackage);    
        mPackagesHolder.put(packageInfo.packageName, pluginPackage);

        return pluginPackage;
    }

    /*ADD, Nemo@PluginFramework, 2015-10-13.*/
    private Map<String, ArrayList<String>> parseActivityIntents(PackageParser.Package parserPkg) {
        LOG.d(TAG, "-----parseActivityIntents()-----");
        Map<String, ArrayList<String>> mapActivityIntents = new HashMap<String, ArrayList<String>>();

        //Parse the Activity-intentFilters.
        ArrayList<PackageParser.Activity> pkgActivities = parserPkg.activities;
        if(pkgActivities != null && pkgActivities.size() > 0) {
            LOG.d(TAG, "----Activitys---- SIZE: " + pkgActivities.size());
            for (PackageParser.Activity parserAct : pkgActivities) {
                LOG.d(TAG, "------ Name: " + parserAct.className);
                ArrayList<String> listIntents = new ArrayList<String>();

                int intentsSize = parserAct.intents.size();
                LOG.d(TAG, "------ intents.size(): " + intentsSize);
                for(int i = 0; i < intentsSize; i++) {
                    int count = parserAct.intents.get(i).countActions();
                    LOG.d(TAG, "------ countActions(): " + count);
                    for(int k = 0; k < count; k++) {
                        listIntents.add(parserAct.intents.get(i).getAction(k));
                        LOG.d(TAG, "------ getAction(k): " + parserAct.intents.get(i).getAction(k));
                    }
                }

                mapActivityIntents.put(parserAct.className, listIntents);
                LOG.d(TAG, "--- end one ---");
            }
        }

        return mapActivityIntents;
    }

    /*ADD, Nemo@PluginFramework, 2015-10-13.*/
    private Map<String, ArrayList<String>> parseServiceIntents(PackageParser.Package parserPkg) {
        LOG.d(TAG, "-----parseServiceIntents()-----");
        Map<String, ArrayList<String>> mapServiceIntents = new HashMap<String, ArrayList<String>>();

        //Parse the Service-intentFilters.
        ArrayList<PackageParser.Service> pkgServices = parserPkg.services;
        if(pkgServices != null && pkgServices.size() > 0) {
            LOG.d(TAG, "-----Services----- SIZE: " + pkgServices.size());
            for (PackageParser.Service parserSer : pkgServices) {
                LOG.d(TAG, "------ Name: " + parserSer.className);
                ArrayList<String> listIntents = new ArrayList<String>();

                int intentsSize = parserSer.intents.size();
                LOG.d(TAG, "------ intents.size(): " + intentsSize);
                for(int i = 0; i < intentsSize; i++) {
                    int count = parserSer.intents.get(i).countActions();
                    LOG.d(TAG, "------ countActions(): " + count);
                    for(int k = 0; k < count; k++) {
                        listIntents.add(parserSer.intents.get(i).getAction(k));
                        LOG.d(TAG, "------ getAction(k): " + parserSer.intents.get(i).getAction(k));
                    }
                }

                mapServiceIntents.put(parserSer.className, listIntents);
                LOG.d(TAG, "--- end one ---");
            }

        }

        return mapServiceIntents;
    }
    
    /*ADD, Nemo@PluginFramework, 2015-10-13.*/
    private Map<String, ArrayList<String>> parseReceiverIntents(PackageParser.Package parserPkg) {
        LOG.d(TAG, "-----parseReceiverIntents()-----");
        Map<String, ArrayList<String>> mapReceiverIntents = new HashMap<String, ArrayList<String>>();

        //Parse the Receiver-intentFilters.
        ArrayList<PackageParser.Activity> pkgReceivers = parserPkg.receivers;
        if(pkgReceivers != null && pkgReceivers.size() > 0) {
            LOG.d(TAG, "----Receivers---- SIZE: " + pkgReceivers.size());
            for (PackageParser.Activity parserAct : pkgReceivers) {
                LOG.d(TAG, "------ Name: " + parserAct.className);
                ArrayList<String> listIntents = new ArrayList<String>();

                int intentsSize = parserAct.intents.size();
                LOG.d(TAG, "------ intents.size(): " + intentsSize);
                for(int i = 0; i < intentsSize; i++) {
                    int count = parserAct.intents.get(i).countActions();
                    LOG.d(TAG, "------ countActions(): " + count);
                    for(int k = 0; k < count; k++) {
                        listIntents.add(parserAct.intents.get(i).getAction(k));
                        LOG.d(TAG, "------ getAction(k): " + parserAct.intents.get(i).getAction(k));
                    }
                }

                mapReceiverIntents.put(parserAct.className, listIntents);
                LOG.d(TAG, "--- end one ---");
            }
        }

        return mapReceiverIntents;
    }

    private String dexOutputPath;
    private DexClassLoader createDexClassLoader(String pluginApkPath) {
        File dexOutputDir = mContext.getDir(Constants.PATH_PLUGIN_DEX, Context.MODE_PRIVATE);
        dexOutputPath = dexOutputDir.getAbsolutePath();
        DexClassLoader loader = new DexClassLoader(pluginApkPath, dexOutputPath, mNativeLibDir, mContext.getClassLoader());
        return loader;
    }

    private AssetManager createAssetManager(String pluginApkPath) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, pluginApkPath);
            return assetManager;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public PluginPackage getPackage(String packageName) {
        return mPackagesHolder.get(packageName);
    }

    private Resources createResources(AssetManager assetManager) {
        Resources superRes = mContext.getResources();
        Resources resources = new Resources(assetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
        return resources;
    }

    /**
     * copy .so file to pluginlib dir.
     *
     * @param dexPath
     * @param hasSoLib
     */
    private void copySoLib(String dexPath) {
        // TODO: copy so lib async will lead to bugs maybe, waiting for
        // resolved later.

        // TODO : use wait and signal is ok ? that means when copying the
        // .so files, the main thread will enter waiting status, when the
        // copy is done, send a signal to the main thread.
        // new Thread(new CopySoRunnable(dexPath)).start();

        SoLibManager.getSoLoader().copyPluginSoLib(mContext, dexPath, mNativeLibDir);
    }

    /**
     * {@link #startPluginActivityForResult(Activity, PluginIntent, int)}
     */
    public int startPluginActivity(Context context, PluginIntent dlIntent) {
        return startPluginActivityForResult(context, dlIntent, -1);
    }

    /**
     * @param context
     * @param dlIntent
     * @param requestCode
     * @return One of below: {@link #START_RESULT_SUCCESS}
     *         {@link #START_RESULT_NO_PKG} {@link #START_RESULT_NO_CLASS}
     *         {@link #START_RESULT_TYPE_ERROR}
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public int startPluginActivityForResult(Context context, PluginIntent dlIntent, int requestCode) {
        if (mFrom == Constants.FROM_INTERNAL) {
            dlIntent.setClassName(context, dlIntent.getPluginClass());
            performStartActivityForResult(context, dlIntent, requestCode);
            return PluginManager.START_RESULT_SUCCESS;
        }

        String packageName = dlIntent.getPluginPackage();
        if (TextUtils.isEmpty(packageName)) {
            throw new NullPointerException("disallow null packageName.");
        }

        PluginPackage pluginPackage = mPackagesHolder.get(packageName);
        if (pluginPackage == null) {
            return START_RESULT_NO_PKG;
        }

        final String className = getPluginActivityFullPath(dlIntent, pluginPackage);
        Class<?> clazz = loadPluginClass(pluginPackage.classLoader, className);
        if (clazz == null) {
            return START_RESULT_NO_CLASS;
        }

        // get the proxy activity class, the proxy activity will launch the
        // plugin activity.
        Class<? extends Activity> activityClass = getProxyActivityClass(clazz);
        if (activityClass == null) {
            return START_RESULT_TYPE_ERROR;
        }

        // put extra data
        dlIntent.putExtra(Constants.EXTRA_CLASS, className);
        dlIntent.putExtra(Constants.EXTRA_PACKAGE, packageName);
        dlIntent.setClass(mContext, activityClass);
        performStartActivityForResult(context, dlIntent, requestCode);
        return START_RESULT_SUCCESS;
    }

    public int startPluginService(final Context context, final PluginIntent dlIntent) {
        if (mFrom == Constants.FROM_INTERNAL) {
            dlIntent.setClassName(context, dlIntent.getPluginClass());
            context.startService(dlIntent);
            return PluginManager.START_RESULT_SUCCESS;
        }

        fetchProxyServiceClass(dlIntent, new OnFetchProxyServiceClass() {
            @Override
            public void onFetch(int result, Class<? extends Service> proxyServiceClass) {
                // TODO Auto-generated method stub
                if (result == START_RESULT_SUCCESS) {
                    LOG.d(TAG, "lanuch Service: " + dlIntent.getPluginClass());
                    dlIntent.setClass(context, proxyServiceClass);
                    // start代理Service
                    context.startService(dlIntent);
                }
                mResult = result;
            }
        });

        return mResult;
    }

    public int bindPluginService(final Context context, final PluginIntent dlIntent, final ServiceConnection conn,
            final int flags) {
        if (mFrom == Constants.FROM_INTERNAL) {
            dlIntent.setClassName(context, dlIntent.getPluginClass());
            context.bindService(dlIntent, conn, flags);
            return PluginManager.START_RESULT_SUCCESS;
        }

        fetchProxyServiceClass(dlIntent, new OnFetchProxyServiceClass() {
            @Override
            public void onFetch(int result, Class<? extends Service> proxyServiceClass) {
                // TODO Auto-generated method stub
                if (result == START_RESULT_SUCCESS) {
			        dlIntent.setClass(context, proxyServiceClass);
                    // Bind代理Service
                    context.bindService(dlIntent, conn, flags);
                }
                mResult = result;
            }
        });

        return mResult;
    }

    public int unBindPluginService(final Context context, PluginIntent dlIntent, final ServiceConnection conn) {
        if (mFrom == Constants.FROM_INTERNAL) {
            context.unbindService(conn);
            return PluginManager.START_RESULT_SUCCESS;
        }

        fetchProxyServiceClass(dlIntent, new OnFetchProxyServiceClass() {
            @Override
            public void onFetch(int result, Class<? extends Service> proxyServiceClass) {
                // TODO Auto-generated method stub
                if (result == START_RESULT_SUCCESS) {
                    // unBind代理Service
                    context.unbindService(conn);
                }
                mResult = result;
            }
        });
        return mResult;

    }

    /**
     * 获取代理ServiceClass
     * @param dlIntent
     * @param fetchProxyServiceClass
     */
    private void fetchProxyServiceClass(PluginIntent dlIntent, OnFetchProxyServiceClass fetchProxyServiceClass) {
        String packageName = dlIntent.getPluginPackage();
        if (TextUtils.isEmpty(packageName)) {
            throw new NullPointerException("disallow null packageName.");
        }
        PluginPackage pluginPackage = mPackagesHolder.get(packageName);
        if (pluginPackage == null) {
            fetchProxyServiceClass.onFetch(START_RESULT_NO_PKG, null);
            return;
        }

        // 获取要启动的Service的全名
        String className = dlIntent.getPluginClass();
        LOG.d(TAG, "load Service: " + className);
        Class<?> clazz = loadPluginClass(pluginPackage.classLoader, className);
        if (clazz == null) {
            fetchProxyServiceClass.onFetch(START_RESULT_NO_CLASS, null);
            return;
        }

        Class<? extends Service> proxyServiceClass = getProxyServiceClass(clazz);
        if (proxyServiceClass == null) {
            fetchProxyServiceClass.onFetch(START_RESULT_TYPE_ERROR, null);
            return;
        }

        // put extra data
        dlIntent.putExtra(Constants.EXTRA_CLASS, className);
        dlIntent.putExtra(Constants.EXTRA_PACKAGE, packageName);
        fetchProxyServiceClass.onFetch(START_RESULT_SUCCESS, proxyServiceClass);
    }

    private Class<?> loadPluginClass(ClassLoader classLoader, String className) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className, true, classLoader);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return clazz;
    }

    private String getPluginActivityFullPath(PluginIntent dlIntent, PluginPackage pluginPackage) {
        String className = dlIntent.getPluginClass();
        className = (className == null ? pluginPackage.defaultActivity : className);
        if (className.startsWith(".")) {
            className = dlIntent.getPluginPackage() + className;
        }
        return className;
    }

    /**
     * get the proxy activity class, the proxy activity will delegate the plugin
     * activity
     *
     * @param clazz
     *            target activity's class
     * @return
     */
    private Class<? extends Activity> getProxyActivityClass(Class<?> clazz) {
        Class<? extends Activity> activityClass = null;
        if (BasePluginActivity.class.isAssignableFrom(clazz)) {
            activityClass = ActivityProxy.class;
        } else if (BasePluginFragmentActivity.class.isAssignableFrom(clazz)) {
            activityClass = FragmentActivityProxy.class;
        }
        
		/*ADD, Nemo@PluginFramework, 2015-10-13: Add for PreferenceActivity Proxy.*/
		else if (BasePluginPreferenceActivity.class.isAssignableFrom(clazz)) {
            activityClass = PreferenceActivityProxy.class;
        }

        return activityClass;
    }

    private Class<? extends Service> getProxyServiceClass(Class<?> clazz) {
        Class<? extends Service> proxyServiceClass = null;
        if (BasePluginService.class.isAssignableFrom(clazz)) {
            proxyServiceClass = ServiceProxy.class;
        }
        // 后续可能还有IntentService，待补充

        return proxyServiceClass;
    }

    private void performStartActivityForResult(Context context, PluginIntent dlIntent, int requestCode) {
        LOG.d(TAG, "launch Activity: " + dlIntent.getPluginClass());
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(dlIntent, requestCode);
        } else {
            context.startActivity(dlIntent);
        }
    }

    private interface OnFetchProxyServiceClass {
        public void onFetch(int result, Class<? extends Service> proxyServiceClass);
    }

	/*ADD, Nemo@PluginFramework, 2015-10-13.*/
	public Class<?> fetchStaticRegisteredIntentReceiver(Intent intent) {
		//Collection<PluginPackage> coPluginPackage = mPackagesHolder.values();
		//Set<String> setPackageName = mPackagesHolder.keySet();
		if (intent == null) {
			LOG.e(TAG, "1112--- fetchStaticRegisteredIntentReceiver() intent == null, return.");
			return null;
		}
        String inAction = intent.getAction();
		LOG.d(TAG, "1112--- fetchStaticRegisteredIntentReceiver() intent = " + inAction);
		
		Class<?> clazz = null;
		Set<Map.Entry<String, PluginPackage>> setAllPackage = mPackagesHolder.entrySet();	
		for(Map.Entry<String, PluginPackage> packageEntry : setAllPackage) {
			String packageName = packageEntry.getKey();
			PluginPackage pluginPkg = packageEntry.getValue();
			LOG.d(TAG, "1112--- --- --- packageName = " + packageName);
			
			Map<String, ArrayList<String>> mapReceiverIntent = pluginPkg.getReceiverIntentMap();//
			Set<Map.Entry<String, ArrayList<String>>> setReceiverIntent = mapReceiverIntent.entrySet();
			for(Map.Entry<String, ArrayList<String>> receiverEntry : setReceiverIntent) {
				String receiverName = receiverEntry.getKey();
				ArrayList<String> listAction = receiverEntry.getValue();
				LOG.d(TAG, "1112--- --- receiverName = " + receiverName);
				
				for(String action : listAction) {
					LOG.d(TAG, "1112--- action = " + action);
					if (inAction.equals(action)) {
						LOG.d(TAG, "1112--- action equals.");
						
						clazz = loadPluginClass(pluginPkg.classLoader, receiverName);
						if (clazz != null) {
							LOG.d(TAG, "1112--- clazz=" + clazz);
							return clazz;
						}
							
					}
				}
			}
		}
		
		return clazz;
	}

	/*ADD, Nemo@PluginFramework, 2015-10-13.*/
	public void updatePluginResources() {
		if (mPackagesHolder.isEmpty()) {
			// DO NOTHING
		} else {
			Iterator<Map.Entry<String, PluginPackage>> iterator = mPackagesHolder.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, PluginPackage> entry = iterator.next();
				PluginPackage pluginPackage = (PluginPackage)entry.getValue();
				if (pluginPackage != null) {
					pluginPackage.updatePluginResources(mContext);
				}
			}
		}
	}
}
