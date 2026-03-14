/**
 * Copyright (c) 2013 Project Goth
 *
 * ApplicationEx.java.java
 * Created May 30, 2013, 3:47:33 PM
 */

package com.projectgoth.app;

import io.fabric.sdk.android.Fabric;
import java.io.File;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import com.crashlytics.android.Crashlytics;
import com.fiksu.asotracking.FiksuTrackingManager;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger.LogLevel;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.projectgoth.R;
import com.projectgoth.common.Config;
import com.projectgoth.common.Constants;
import com.projectgoth.common.DefaultConfig;
import com.projectgoth.common.DeviceModeHandler;
import com.projectgoth.common.GUIConst;
import com.projectgoth.common.Logger;
import com.projectgoth.common.MimeDataGeneratorImpl;
import com.projectgoth.common.SharedPrefsManager;
import com.projectgoth.common.Theme;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.StatusBarController;
import com.projectgoth.datastore.Session;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.notification.NotificationHandler;
import com.projectgoth.notification.system.NativeNotificationManager;
import com.projectgoth.service.NetworkService;
import com.projectgoth.ui.activity.BaseFragmentActivity;
import com.projectgoth.ui.activity.MainDrawerLayoutActivity;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.CrashlyticsLog;
import com.projectgoth.util.FileUtils;
import com.projectgoth.util.LogUtils;
import com.squareup.leakcanary.LeakCanary;

/**
 * @author cherryv
 * 
 */
public class ApplicationEx extends Application {

    private static final String         TAG                             = AndroidLogger.makeLogTag(ApplicationEx.class);
    public static final String          WIDGET_NAMESPACE                = "http://schemas.android.com/apk/src/com.projectgoth.ui.widget";
    private boolean                     mToPauseWebViewTimerAtFirstTime = false;
    private NetworkService              mNetworkService                 = null;
    private boolean                     mIsNetworkServiceBound          = false;
    private static ApplicationEx        INSTANCE                        = null;
    private BaseFragmentActivity        mCurrentActivity;
    private AppProperties               mAppProperties;
    private NotificationHandler         mNotificationHandler;
    private DeviceModeHandler           mDeviceHandler;
    private SharedPrefsManager          mSharedPrefsManager;
    public static String                sCountryCode                    = "";
    private static Tracker              mTracker                        = null;
    private static int                  RESTART_DELAY                   = 100;
    private static int                  PENDING_INTENT_ID               = 129456;
    private boolean                     mIsInitilized;
    private static boolean              mApplicationVisible;
    private boolean                     mPreviewStatus                  = false;
    private HandlerThread               mUiSlaveHandlerThread;
    private Handler                     mUiSlaveHandler;
    private Handler                     mMainHandler;

    private static final int            CLEAR_CACHE_TIME                = 120 * 1000; //2mins

    private void setInstance(ApplicationEx app) {
        if (INSTANCE != null) {
            Logger.error.logWithTrace(TAG, getClass(), "Instance has already been set!");
            return;
        }
        INSTANCE = app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setInstance(this);

        //must call initApplicationProperties() first
        initApplicationProperties();
        //cannot lazy-initialize, put them below
        if (Config.getInstance().isCrashlyticsEnabled()) {
            Fabric.with(this, new Crashlytics());
        }

        initTheme();
        // Initialize Config first of all to have isDebug set before anything uses it (e.g. logs)
        Config.initialize(this);
        BroadcastHandler.initialize();
        LogUtils.initializeLoggers();
        //init SharedPrefsManager first due to issue AD-1786
        mSharedPrefsManager = new SharedPrefsManager(this);
        initUiSlave();
        initStrictModeInDebug();
        mIsInitilized = false;
        LeakCanary.install(this);
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    private void initStrictModeInDebug() {
        if (Config.isDebug()) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }
    }

    //For operations which have chances to block ui.
    private void initUiSlave() {
        mUiSlaveHandlerThread = new HandlerThread("Ui Slave Handler Thread");
        mUiSlaveHandlerThread.start();
        mUiSlaveHandler = new Handler(mUiSlaveHandlerThread.getLooper());
    }

    public Handler getUiSlaveHandler() {
        if (mUiSlaveHandler == null) {
            initUiSlave();
        }
        return mUiSlaveHandler;
    }

    public static boolean isActivityVisible() {
        return mApplicationVisible;
    }

    public static void activityResumed() {
        mApplicationVisible = true;
    }

    public static void activityPaused() {
        mApplicationVisible = false;
    }

    public void initialize() {
        com.projectgoth.b.data.common.Config.getInstance().setMimeDataGenerator(new MimeDataGeneratorImpl());

        bindNetworkService();

        // init first time of lanuch app
        long startTime = NativeNotificationManager.getInstance().getFirstStartTime();
        if (startTime == 0) {
            long curTime = System.currentTimeMillis();
            NativeNotificationManager.getInstance().setFirstStartTime(curTime);
        }

        // Initialize Google analytics
        if (Config.getInstance().isGoogleAnalyticsEnabled()) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);

            // Set the log level to verbose.
            analytics.getLogger().setLogLevel(LogLevel.VERBOSE);
            mTracker = analytics.newTracker(R.xml.tracker);
        }

        Session.getInstance().init(this);
        Config.getInstance().setRequestedEmoticonDimension(
                getResources().getDimensionPixelSize(R.dimen.emoticon_requestedHeight));

        // Language
        I18n.loadLanguage(I18n.getLanguageID());

        GUIConst.initialize(this);
        initNotificationHandler();

        //Remove initFiksu()

        mIsInitilized = true;
    }

    /**
     * Initialize the application properties configuration file
     */
    private void initApplicationProperties() {
        mAppProperties = new AppProperties(this);
    }

    public void onDestroy() {
        unbindNetworkService();
    }

    @SuppressWarnings("deprecation")
    public static void initializeAppProperties(Activity activity) {

        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Config.getInstance().setScreenScale(metrics.density);
        Config.getInstance().setFontScale(metrics.scaledDensity);

        Display display = activity.getWindowManager().getDefaultDisplay();
        Config.getInstance().setScreenWidth(display.getWidth());
        Config.getInstance().setScreenHeight(display.getHeight());

        //display picture sizes
        int displayPicSizeSmall = ApplicationEx.getDimension(R.dimen.contact_pic_size_small);
        int displayPicSizeNormal = ApplicationEx.getDimension(R.dimen.contact_pic_size_normal);
        if (displayPicSizeNormal > DefaultConfig.MAX_DISPLAY_PIC_SIZE_NORMAL) {
            displayPicSizeNormal = DefaultConfig.MAX_DISPLAY_PIC_SIZE_NORMAL;
        }
        int displayPicSizeLarge = ApplicationEx.getDimension(R.dimen.contact_pic_size_large);
        if (displayPicSizeLarge > DefaultConfig.MAX_DISPLAY_PIC_SIZE_LARGE) {
            displayPicSizeLarge = DefaultConfig.MAX_DISPLAY_PIC_SIZE_LARGE;
        }
        Config.getInstance().setDisplayPicSizeSmall(displayPicSizeSmall);
        Config.getInstance().setDisplayPicSizeNormal(displayPicSizeNormal);
        Config.getInstance().setDisplayPicSizeLarge(displayPicSizeLarge);
    }

    //@formatter:off
    /**
     * Define the ServiceConnection to be used, to bind to the networkService
     */
    private ServiceConnection mNetworkServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                mNetworkService = ((NetworkService.LocalBinder) service).getService();
                BroadcastHandler.NetworkService.sendStarted();
                Logger.debug.log(TAG, "ServiceConnected: ", mNetworkService);
            } catch (ClassCastException e) {
                String errorMsg =  "onServiceConnected has ClassCastException : " + service.toString();
                Logger.error.log(TAG, errorMsg);
                CrashlyticsLog.log(new ClassCastException(), errorMsg);
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            mNetworkService = null;
            BroadcastHandler.NetworkService.sendStopped();
            Logger.debug.log(TAG, "ServiceDisconnected: ", mNetworkService);
        }
    };

    private void initTheme() {
        // Theme
        try {
            AssetManager assetMgr = getAssets();
            String[] assetsIWant = assetMgr.list(Constants.PATH_ASSETS_THEME);
            InputStream is = null;
            if (assetsIWant.length > 0) {
                String filename = Constants.PATH_ASSETS_THEME + File.separator + assetsIWant[0];
                is = assetMgr.open(filename);
            }
            Theme.setTheme(is);
        } catch (Exception e) {
            Logger.error.log(TAG, e);
        }
    }
    //@formatter:on

    /**
     * Returns the application instance
     *
     * @return
     */
    public static ApplicationEx getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the application context
     *
     * @return
     */
    public static Context getContext() {
        return INSTANCE.getApplicationContext();
    }

    /**
     * @return the mCurrentActivity
     */
    public BaseFragmentActivity getCurrentActivity() {
        return mCurrentActivity;
    }

    /**
     * @param mCurrentActivity the mCurrentActivity to set
     */
    public void setCurrentActivity(BaseFragmentActivity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }

    public static synchronized Tracker getTracker() {
        return mTracker;
    }

    private void initNotificationHandler() {
        if (mNotificationHandler == null) {
            mNotificationHandler = new NotificationHandler(this);
        }
    }

    public NotificationHandler getNotificationHandler() {
        if (mNotificationHandler == null) {
            mNotificationHandler = new NotificationHandler(this);
        }
        return mNotificationHandler;
    }

    public DeviceModeHandler getDeviceHandler() {
        if (mDeviceHandler == null) {
            mDeviceHandler = new DeviceModeHandler(this);
        }
        return mDeviceHandler;
    }

    /**
     * Gets the network service started by the Application
     *
     * @return
     */
    public NetworkService getNetworkService() {
        if (mNetworkService == null) {
            Logger.warning.log(TAG, "NetworkService is NULL!");
        }
        return mNetworkService;
    }

    public boolean isLoggedIn() {
        if (mNetworkService == null) {
            // trick things, user may be at login status.
            return false;
        } else {
            return mNetworkService.isLoggedIn();
        }
    }

    public void bindNetworkService() {
        bindService(new Intent(this, NetworkService.class), mNetworkServiceConnection, Context.BIND_AUTO_CREATE);
        mIsNetworkServiceBound = true;
    }

    private void unbindNetworkService() {
        if (mIsNetworkServiceBound) {
            unbindService(mNetworkServiceConnection);
            mIsNetworkServiceBound = false;
        }
    }

    public static int getInlineEmoticonDimension() {
        return getDimension(R.dimen.emoticon);
    }

    public static int getDimension(int dimenId) {
        return INSTANCE.getResources().getDimensionPixelSize(dimenId);
    }

    public DisplayMetrics getDisplayMetrics() {
        return INSTANCE.getResources().getDisplayMetrics();
    }

    public static int getColor(int colorId) {
        return INSTANCE.getResources().getColor(colorId);
    }

    public static InputStream loadAssetFile(String fileName) {
        return FileUtils.loadAssetFile(getInstance(), fileName);
    }

    public void setPreviewStatus(boolean status) {
        this.mPreviewStatus = status;
    }

    public boolean getPreviewStatus() {
        return mPreviewStatus;
    }

    /**
     * it can return null when the client gets disconnected. So need to check
     * the instance returned before using it every time
     */
    @Nullable
    public RequestManager getRequestManager() {
        if (getNetworkService() != null) {
            return getNetworkService().getRequestManager();
        }
        return null;
    }

    /**
     * Called by {@link DeviceModeHandler} whenever the application is detected
     * to switch from background to foreground mode.
     * <p/>
     * Functionalities specific to application state should be handled here.
     */
    public void onApplicationEnterForeground() {
        if (Config.isDebug()) {
            Tools.showToast(this, "migme App is now on foreground");
        }

        mMainHandler.removeCallbacks(mClearCache);
    }

    /**
     * Called by {@link DeviceModeHandler} whenever the application is detected
     * to switch from foreground to background mode.
     * <p/>
     * Functionalities specific to application state should be handled here.
     */
    public void onApplicationEnterBackground() {
        if (Config.isDebug()) {
            Tools.showToast(this, "migme App is now on background");
        }

        StatusBarController.getInstance().setActivity(null);
        mMainHandler.postDelayed(mClearCache, CLEAR_CACHE_TIME);
    }

    public boolean isApplicationInBackground() {
        return getDeviceHandler().isInBackground();
    }

    public boolean isPhoneLocked() {
        return getDeviceHandler().isPhoneLocked();
    }

    /**
     * @return the appProperties
     */
    public AppProperties getAppProperties() {
        return mAppProperties;
    }

    private void initFiksu() {
        // Fiksu (for non-debug and Android 2.3 or higher)
        if (Config.getInstance().isFiksuEnabled() &&
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext()) == ConnectionResult.SUCCESS) {
            try {
                FiksuTrackingManager.initialize(this);
            } catch (SecurityException e) {
                StringBuilder builder = new StringBuilder();
                builder.append("FiksuTrackingManager initialization SecurityException\n");
                builder.append(String.format("check rooted device, build tag -> %s \n", android.os.Build.TAGS));
                Logger.error.log(TAG, builder.toString(), e);
            }
        }
    }


    public SharedPrefsManager getSharedPrefsManager() {
        return mSharedPrefsManager;
    }

    public void restartApp() {
        Intent mStartActivity = new Intent(getContext(), MainDrawerLayoutActivity.class);
        mStartActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent mPendingIntent = PendingIntent.getActivity(getContext(), PENDING_INTENT_ID, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + RESTART_DELAY, mPendingIntent);
        System.exit(0);
    }

    public boolean isIsInitilized() {
        return mIsInitilized;
    }

    public void setFirstTimeRunApp(boolean isFirstTime) {
        this.mToPauseWebViewTimerAtFirstTime = isFirstTime;
    }

    public boolean isFirstTimeRunApp() {
        return mToPauseWebViewTimerAtFirstTime;
    }

    private Runnable mClearCache = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "clear cache in background");
            mCurrentActivity = null;
            //TODO: we may clear other cache here if clear image cache doesn't help decrease OOM
            ImageHandler.getInstance().clearImageCache();
        }
    };
}
