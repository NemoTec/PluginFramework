
package com.ryg.plugin.utils;

import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 反射方法封装
 * @author 
 */
public class ReflectionUtil {
    public static String TAG;

    static {
        ReflectionUtil.TAG = ReflectionUtil.class.getSimpleName();
    }

    public static Class getClass(String className) {
        Class c = null;
        try {
            c = Class.forName(className);
        }catch(java.lang.ClassNotFoundException e) {
            Log.d(TAG, Log.getStackTraceString(e)); 
        }
        return c;
    }

    public static Field getField(Class c, String fieldName) {
        Field f = null;
        try {
            f = c.getDeclaredField(fieldName);
            if ( f == null) 
                return f;
            f.setAccessible(true);
        }catch(Exception e) {
            Log.d(TAG, Log.getStackTraceString(e)); 
        }
        return f;
    }

    public static Field getField(String className, String fieldName) {
        Field f = null;
        Class c = null;

        try {
            c = ReflectionUtil.getClass(className);
            if ( c == null)
                return f;
            f = ReflectionUtil.getField(c, fieldName);
        }catch(Exception e) {
            Log.d(TAG, Log.getStackTraceString(e)); 
        }
        return f;
    }

    public static Field getField(Object obj, String fieldName) {
        Field f = null;
        try {
            f = ReflectionUtil.getField(obj.getClass(), fieldName);
        }catch(Exception e) {
            Log.d(TAG, Log.getStackTraceString(e)); 
        }
        return f;
    }

    public static Object getObject(Field f , Object o) {
        Object obj  = null;

        try {
            obj = f.get(o);
        }catch(Exception e ) {
            Log.d(TAG, Log.getStackTraceString(e)); 
        }
        return obj;
    }

    public static int getInt(Object o, String fieldName, int val) {
        Field f = null;

        f = ReflectionUtil.getField(o, fieldName);
        if ( null == f)
            return val;
        Object obj = ReflectionUtil.getObject(f, o);
        if ( null == obj)
            return val;
        try {
            val = ((Integer)obj).intValue();
        }catch(Exception e) {
            Log.d(TAG, Log.getStackTraceString(e)); 
        }
        return val;
    }

    public static int getStaticInt(Class c, String fieldName, int val) {
        Field f = null;

        f = ReflectionUtil.getField(c, fieldName);
        if ( null == f)
            return val;
        Object obj = ReflectionUtil.getObject(f, null);//获取静态字段，所以参数为null
        if ( null == obj)
            return val;
        try {
            val = ((Integer)obj).intValue();
        }catch(Exception e) {
            Log.d(TAG, Log.getStackTraceString(e)); 
        }
        return val;
    }

    public static int getStaticInt(String className, String fieldName, int val) {
        Field f = null;

        f = ReflectionUtil.getField(className, fieldName);
        if ( null == f )
            return val;
        Object obj = ReflectionUtil.getObject(f, null);//获取静态字段，所以参数为null
        if ( null == obj)
            return val;
        try {
            val = ((Integer)obj).intValue();
        }catch(Exception e) {
            Log.d(TAG, Log.getStackTraceString(e)); 
        }
        return val;
    }
    
    public static Object invokeAdaptiveNonStaticMethod(Object instanceObj, String methodName, Object[] params) {
        Method invokeMethod = null;
        Object value        = null;

        if ( methodName == null)
            return null;
        try {
            Method[] methods = instanceObj.getClass().getDeclaredMethods();
            for(int i = 0; i < methods.length; i++) {
                if ( methodName.equals(methods[i].getName()) ) {
                    invokeMethod = methods[i];
                    break;
                }
            }
            //已经找到了method
            int pTypeCount = 0;
            pTypeCount = invokeMethod.getParameterTypes().length; 
            if ( pTypeCount != params.length ) {
                int min = pTypeCount > params.length ? params.length : pTypeCount;
                Object[] new_params = new Object[min];
                for ( int i = 0; i < min; i++ ) {
                    new_params[i] = params[i];
                }
                invokeMethod.setAccessible(true);
                value = invokeMethod.invoke(instanceObj, new_params);
            } else {
                invokeMethod.setAccessible(true);
                value = invokeMethod.invoke(instanceObj, params);
            }
        }catch(Exception e) {
            Log.d(TAG, Log.getStackTraceString(e)); 
        }
        return value;
    }

    public static Object invokeAdaptiveStaticMethod(Class c , String methodName, Object[] params) {
        Method invokeMethod = null;
        Object value        = null;

        if ( methodName == null)
            return null;
        try {
            Method[] methods = c.getDeclaredMethods();
            for(int i = 0; i < methods.length; i++) {
                if ( methodName.equals(methods[i].getName()) ) {
                    invokeMethod = methods[i];
                    break;
                }
            }
            //已经找到了method
            int pTypeCount = 0;
            pTypeCount = invokeMethod.getParameterTypes().length; 
            if ( pTypeCount != params.length ) {
                int min = pTypeCount > params.length ? params.length : pTypeCount;
                Object[] new_params = new Object[min];
                for ( int i = 0; i < min; i++ ) {
                    new_params[i] = params[i];
                }
                invokeMethod.setAccessible(true);
                value = invokeMethod.invoke(null, new_params);
            } else {
                invokeMethod.setAccessible(true);
                value = invokeMethod.invoke(null, params);
            }
        }catch(Exception e) {
            Log.d(TAG, Log.getStackTraceString(e)); 
        }
        return value;
    }

    public static Object invokeNonStaticMethod(Object instance, Class[] paramTypes, String methodName, Object[] params) {
        Object value = null;
        try {
            Method method = instance.getClass().getDeclaredMethod(methodName, paramTypes);
            if(method == null) {
                return value;
            }
            method.setAccessible(true);
            value = method.invoke(instance, params);
        }
        catch(Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
        return value;
    }

    public static Object invokeStaticMethod(Class c, Class[] param_types, String methodName, Object[] params) {
        Object value= null;
        try {
            Method method = c.getDeclaredMethod(methodName, param_types);
            if( method== null) {
                return value;
            }
            method.setAccessible(true);
            value = method.invoke(null, params);
        }
        catch(Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }

        return value;
    }

    public static Object invokeStaticMethod(String className, Class[] param_types, String methodName, Object[] params) {
        Class c   = null;
        Object value = null;
        try {
            c = Class.forName(className);
        }
        catch(Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
        if(c != null) {
            value = ReflectionUtil.invokeStaticMethod(c, param_types, methodName, params);
        }
        return value;
    }

}

