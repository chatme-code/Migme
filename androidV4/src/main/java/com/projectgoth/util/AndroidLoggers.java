package com.projectgoth.util;

import android.util.Log;

public class AndroidLoggers {

    /**
     * Logger class for debug logs. Uses android.util.Log.d(...);
     * 
     */
    public static class DebugLogger extends AndroidLogger {

        @Override
        public void log(String tag, String msg) {
            Log.d(makeLogTag(tag), msg);
        }

        @Override
        public void log(String tag, String msg, Throwable cause) {
            Log.d(makeLogTag(tag), msg, cause);
        }

    }

    /**
     * Logger class for info logs. Uses android.util.Log.i(...);
     * 
     */
    public static class InfoLogger extends AndroidLogger {

        @Override
        public void log(String tag, String msg) {
            Log.i(makeLogTag(tag), msg);
        }

        @Override
        public void log(String tag, String msg, Throwable cause) {
            Log.i(makeLogTag(tag), msg, cause);
        }
    }

    /**
     * Logger class for warning logs. Uses android.util.Log.w(...);
     * 
     */
    public static class WarningLogger extends AndroidLogger {

        @Override
        public void log(String tag, String msg) {
            Log.w(makeLogTag(tag), msg);
        }

        @Override
        public void log(String tag, String msg, Throwable cause) {
            Log.w(makeLogTag(tag), msg, cause);
        }
    }

    /**
     * Logger class for error logs. Uses android.util.Log.e(...);
     * 
     */
    public static class ErrorLogger extends AndroidLogger {

        @Override
        public void log(String tag, String msg) {
            Log.e(makeLogTag(tag), msg);
        }
        
        /**
         * log method to report to crashlytics.
         * @param String tag
         * @param String msg
         * @param Throwable cause
         */
        @Override
        public void log(String tag, String msg, Throwable cause) {

            Log.e(makeLogTag(tag), msg, cause);
            CrashlyticsLog.log(cause, msg);
        }

        /**
         * log method to report to crashlytics.
         * @param String msg
         * @param Exception e
         */
        @Override
        public void log(String msg, Exception e){
            Log.e("", msg, e);
            CrashlyticsLog.log(e, msg);
        }
    }
}
