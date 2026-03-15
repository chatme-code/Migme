package com.projectgoth.util;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

/**
 * Created by shiyukun on 12/1/15.
 */
public class CrashlyticsLog {

    private static final String TAG = "CrashlyticsLog";

    public static void log(Throwable e, String message){
        try {
            Crashlytics.log(message);
            Crashlytics.logException(e);
        } catch (IllegalStateException exception) {
            Log.w(TAG, "Fabric is not initialized, not output the log");
        }
    }
}
