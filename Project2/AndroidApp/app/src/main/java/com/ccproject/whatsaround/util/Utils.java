package com.ccproject.whatsaround.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by lei on 5/2/2018.
 */

public class Utils {
    public static final String PREF_FILE = "whats_prefs";

    /**
     * Check if the application has all the permissions.
     * @param context
     * @param permissions
     * @return true if the app is granted all the permissions, false otherwise
     */
    public static boolean checkPermissions(Context context, String[] permissions){
        for(String permission : permissions){
            if(!checkPermission(context, permission)){
                return false;
            }
        }
        return true;
    }

    public static boolean checkPermission(Context context, String permission){
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermissions(Activity activity, String[] permissions, int requestCoe){
        ActivityCompat.requestPermissions(activity, permissions, requestCoe);
    }

    public static boolean needRequestPermission(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static void saveSharedPrefDouble(Context context, String key, double value){
        SharedPreferences sf = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        sf.edit().putLong(key, Double.doubleToLongBits(value)).apply();
    }

    public static double getSharedPrefDouble(Context context, String key, double defValue){
        SharedPreferences sf = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        long value = sf.getLong(key, Double.doubleToLongBits(defValue));
        return Double.longBitsToDouble(value);
    }

    public static boolean sharedPrefContainsKey(Context context, String key){
        SharedPreferences sf = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        return sf.contains(key);
    }

}
