package com.projectgoth.util;

import android.util.Log;

import com.projectgoth.BuildConfig;
import com.projectgoth.common.Config;
import com.projectgoth.common.LevelLogger.EmptyLogger;
import com.projectgoth.common.Logger;
import com.projectgoth.util.AndroidLoggers.DebugLogger;
import com.projectgoth.util.AndroidLoggers.ErrorLogger;
import com.projectgoth.util.AndroidLoggers.InfoLogger;
import com.projectgoth.util.AndroidLoggers.WarningLogger;

public class LogUtils {

    private static final boolean IS_LOGGING_ENABLED     = BuildConfig.DEBUG && Config.isDebug();
    private static final int     LOG_LEVEL              = Log.DEBUG;
    
    // PRE-DEFINE LOG TAGS
    public static final String TAG_MAIN_UI              = "MainUI";
    public static final String TAG_IMAGE_FETCHER        = "ImageFetcher";
    public static final String TAG_FUSION_IMAGE_FETCHER = "FusionImageFetcher";
    public static final String TAG_NOTIFICATION         = "Notification";

    // Initialize Logger
    public static void initializeLoggers() {
        Logger.debug   = (IS_LOGGING_ENABLED && LOG_LEVEL <= Log.DEBUG)? new DebugLogger() : new EmptyLogger();
        Logger.info    = (IS_LOGGING_ENABLED && LOG_LEVEL <= Log.INFO)? new InfoLogger() : new EmptyLogger();
        Logger.warning = (IS_LOGGING_ENABLED && LOG_LEVEL <= Log.WARN)? new WarningLogger() : new EmptyLogger();
        Logger.error   = (IS_LOGGING_ENABLED && LOG_LEVEL <= Log.ERROR)? new ErrorLogger() : new EmptyLogger();
    }
    
    private LogUtils() {}
    
}
