package com.ryg.plugin.internal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager.LayoutParams;
import android.preference.Preference;
import android.preference.PreferenceScreen;

/**
 * IPluginActivity
 * 插件框架中所有的Activity都继承自IPluginActivity
 * @author singwhatiwanna, modify Nemo.
 * 
 */

public interface IPluginActivity extends IAttachable<Activity>{

    public void onCreate(Bundle savedInstanceState);
    public void onStart();
    public void onRestart();
    public void onActivityResult(int requestCode, int resultCode, Intent data);
    public void onResume();
    public void onPause();
    public void onStop();
    public void onDestroy();
    public void onSaveInstanceState(Bundle outState);
    public void onNewIntent(Intent intent);
    public void onRestoreInstanceState(Bundle savedInstanceState);
    public boolean onTouchEvent(MotionEvent event);
    public boolean onKeyUp(int keyCode, KeyEvent event);
    public void onWindowAttributesChanged(LayoutParams params);
    public void onWindowFocusChanged(boolean hasFocus);
    public void onBackPressed();
    public boolean onCreateOptionsMenu(Menu menu);
    public boolean onOptionsItemSelected(MenuItem item);
    
    //Add for PreferenceActivity Proxy.
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference);
}
