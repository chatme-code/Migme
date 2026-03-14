package com.projectgoth.util;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * The Android-specific functions
 * Created by freddie on 15/5/15.
 */
public class AndroidUtils {

    /**
     * Check apk installed or not
     * @param context application context
     * @param packageName package name of apk
     * @return true if apk is installed
     */
    public static boolean isApkInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        boolean apkInstalled;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            apkInstalled = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            apkInstalled = false;
        }
        return apkInstalled;
    }
}
