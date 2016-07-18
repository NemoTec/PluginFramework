
package com.ryg.plugin;

import java.io.Serializable;
import java.util.ArrayList;

import com.ryg.plugin.utils.Configs;

import android.content.Intent;
import android.os.Parcelable;

/**
 * Intent for Plugin
 *
 * @author singwhatiwanna, modify Nemo.
 * 
 */

public class PluginIntent extends Intent {

    private String mPluginPackage;
    private String mPluginClass;

    public PluginIntent() {
        super();
    }

    public PluginIntent(String pluginPackage) {
        super();
        this.mPluginPackage = pluginPackage;
    }

    public PluginIntent(String pluginPackage, String pluginClass) {
        super();
        this.mPluginPackage = pluginPackage;
        this.mPluginClass = pluginClass;
    }

    public PluginIntent(String pluginPackage, Class<?> clazz) {
        super();
        this.mPluginPackage = pluginPackage;
        this.mPluginClass = clazz.getName();
    }

    public String getPluginPackage() {
        return mPluginPackage;
    }

    public void setPluginPackage(String pluginPackage) {
        this.mPluginPackage = pluginPackage;
    }

    public String getPluginClass() {
        return mPluginClass;
    }

    public void setPluginClass(String pluginClass) {
        this.mPluginClass = pluginClass;
    }

    public void setPluginClass(Class<?> clazz) {
        this.mPluginClass = clazz.getName();
    }

    @Override
    public Intent putExtra(String name, Parcelable value) {
        setupExtraClassLoader(value);
        return super.putExtra(name, value);
    }

    @Override
    public Intent putExtra(String name, Serializable value) {
        setupExtraClassLoader(value);
        return super.putExtra(name, value);
    }

    @Override
    public Intent putParcelableArrayListExtra(String name, ArrayList<? extends Parcelable> value) {
        if (value != null && value.size() > 0) {
            setupExtraClassLoader(value.get(0));
        }
        return super.putParcelableArrayListExtra(name, value);
    }

    private void setupExtraClassLoader(Object value) {
        ClassLoader pluginLoader = value.getClass().getClassLoader();
        Configs.sPluginClassloader = pluginLoader;
        setExtrasClassLoader(pluginLoader);
    }

}
