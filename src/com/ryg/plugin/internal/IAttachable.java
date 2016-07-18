
package com.ryg.plugin.internal;

/**
 * 
 * 这是一个通用的attach接口，所有的组件都使用该接口即可
 * @author singwhatiwanna, renamed Nemo.
 * 
 */
public interface IAttachable<T> {
    /**
     * when the proxy impl launch
     * the plugin activity , dl will call this method to attach the proxy
     * activity and pluginManager to the plugin activity. the proxy activity
     * will load the plugin's resource, so the proxy activity is a resource
     * delegate for plugin activity.
     * 
     * @param proxyComponent a instance of Plugin, {@see BasePluginActivity}
     *            and {@see BasePluginFragmentActivity}, {@see
     *            BasePluginService}
     * @param pluginManager PluginManager instance, manager the plugins
     */
    public void attach(T proxyComponent, PluginPackage pluginManager);
}
