
package com.projectgoth.app;

import android.content.Context;
import android.text.TextUtils;

import com.projectgoth.common.Config;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Version;
import com.projectgoth.datastore.SystemDatastore;
import com.projectgoth.util.FileUtils;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Properties;

/**
 * AppProperties
 * 
 * @author warrenbalcos
 */
public class AppProperties {

    public static final String  TAG                   = "AppProperties";

    public static final String  APP_PROPERTIES_FILE   = "app.properties";

    private static final String BUILD_ID_KEY          = "build.id";

    private static final String DEBUG_ID_KEY          = "debug.id";

    private static final String CONN_SETTINGS_KEY     = "connection.settings";

    private static final String CRASHLYTICS_KEY       = "crashlytics";
    
    private static final String GOOGLE_ANALYTICS_KEY  = "ga";
    
    private static final String FIKSU_KEY             = "fiksu";

    public static final String  ON                    = "on";
    public static final String  OFF                   = "off";

    // -------------------------------------------------
    // Default configurations
    // -------------------------------------------------

    /**
     * Tracking ID for VAS partners, or builds in general. This is default value
     * in case properties file is not found. 001 is tracking id for wap site.
     * 002 is tracking id for android market.
     * 
     * WARNING: This value will be overridden with value from
     * <code>assets/app.properties</code>. Please change the VAS tracking id on
     * the <code>assets/app.properties</code>.
     */
    public static final String  BUILD_VAS_TRACKING_ID = "001";

    public static final String  DEBUG_ID              = "0";
    public static final String  CONNECTION_SELECTOR   = OFF;
    public static final String  CRASHLYTICS           = OFF;
    public static final String  GOOGLE_ANALYTICS      = OFF;
    public static final String  DEFAULT_FIKSU         = OFF;

    // -------------------------------------------------

    private Context             context;

    WeakReference<Properties>   assetProperties;

    public AppProperties(Context context) {
        this.context = context;
        init();
    }

    /**
     * Initialize the application properties configuration file
     * 
     * @param context
     */
    public void init() {
        // Application properties needs to be persisted on initial installation
        if (!FileUtils.doesFileExist(context, AppProperties.APP_PROPERTIES_FILE)) {
            persistToFile(context);
        }
        initializeConfigurations();
    }

    /**
     * Initialize application configuration settings
     */
    private void initializeConfigurations() {
        Logger.debug.log(TAG, "initializing configurations");

        String debugId = getProperty(DEBUG_ID_KEY, DEBUG_ID);
        String crashlytics = getProperty(CRASHLYTICS_KEY, CRASHLYTICS);
        String googleAnalytics = getProperty(GOOGLE_ANALYTICS_KEY, GOOGLE_ANALYTICS);
        String connSettings = getProperty(CONN_SETTINGS_KEY, CONNECTION_SELECTOR);
        String fiksu = getProperty(FIKSU_KEY, DEFAULT_FIKSU);

        Version.setDebugId(debugId);
        Config.getInstance().setConnectionSelectorEnabled(ON.equalsIgnoreCase(connSettings));
        Config.getInstance().setCrashlyticsEnabled(ON.equalsIgnoreCase(crashlytics));
        Config.getInstance().setGoogleAnalyticsEnabled(ON.equalsIgnoreCase(googleAnalytics));
        Config.getInstance().setFiksuEnabled(ON.equalsIgnoreCase(fiksu));

        // Initialize VAS build ID, this needs to be persisted
        Logger.debug.log(TAG, "initializing VAS id");
        String buildId = getProperty(BUILD_ID_KEY, BUILD_VAS_TRACKING_ID, true);
        Version.init(buildId);
    }

    /**
     * Fetches the value in the properties configurations.
     * 
     * @param key
     * @param defVal
     * @return
     */
    private String getProperty(String key, String defVal) {
        return getProperty(key, defVal, false);
    }

    /**
     * Fetches the value in the properties configurations. Set persist flag in
     * order to persist the data even if the app is installed over
     * 
     * @param key
     * @param defVal
     * @param persist
     * 
     * @return
     */
    private String getProperty(String key, String defVal, boolean persist) {
        if (persist) {
            return getPersistedProperty(key, defVal);
        } else {
            return getPropertyFromAssets(key, defVal);
        }
    }

    /**
     * Asset property comes with the build and is not persisted on the phone. If
     * you want to persist
     * 
     * @param key
     * @param defVal
     * @return
     */
    private String getPropertyFromAssets(String key, String defVal) {
        String value = null;
        if (!TextUtils.isEmpty(key)) {
            if (assetProperties == null) {
                assetProperties = new WeakReference<Properties>(loadFromAssets(context));
            }
            if (assetProperties.get() != null) {
                value = assetProperties.get().getProperty(key);
                Logger.debug.log(TAG, "[Asset] Property: ", key, " value: ", value, " loaded.");
            }
            if (value == null) {
                value = defVal;
                Logger.error.log(TAG, "[Asset] Failed to load key: ", key, " using default value: ", defVal);
            }
        }
        return value;
    }

    /**
     * Fetches the value in the {@link SystemDatastore}, if the value does not
     * exist, it is extracted from the properties file and saved back into the
     * {@link SystemDatastore}
     * 
     * @param key
     * @param defVal
     * @return
     */
    private String getPersistedProperty(String key, String defVal) {
        String value = null;
        if (!TextUtils.isEmpty(key)) {
            value = SystemDatastore.getInstance().getStringData(key);
            if (value == null) {
                Properties properties = loadFromFile(context);
                if (properties != null) {
                    value = properties.getProperty(key);
                    SystemDatastore.getInstance().saveData(key, value);
                }
                if (value == null) {
                    value = defVal;
                    Logger.error.log(TAG, "[File] Failed to load key: ", key, " using default value: ", defVal);
                }
            }
            Logger.debug.log(TAG, "[File] Property: ", key, " value: ", value, " loaded.");
        }
        return value;
    }

    /**
     * Loads the {@value #APP_PROPERTIES_FILE} from asset file
     * 
     * @param context
     */
    private Properties loadFromAssets(Context context) {
        Logger.debug.log(TAG, "[Asset] loading properties: " + APP_PROPERTIES_FILE);

        Properties properties = null;
        InputStream is = null;
        try {
            is = FileUtils.loadAssetFile(context, APP_PROPERTIES_FILE);
            properties = new Properties();
            properties.load(is);
        } catch (IOException e) {
            Logger.error.log(TAG, e, "[Asset] Failed to load application properties", APP_PROPERTIES_FILE);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                is = null;
            }
        }
        return properties;
    }

    /**
     * Loads the {@value #APP_PROPERTIES_FILE} from persisted file
     * 
     * @param context
     */
    private Properties loadFromFile(Context context) {
        Logger.debug.log(TAG, "[File] loading properties: " + APP_PROPERTIES_FILE);
        Properties properties = null;
        InputStream is = null;
        try {
            is = FileUtils.loadFile(context, APP_PROPERTIES_FILE);
            properties = new Properties();
            properties.load(is);
        } catch (IOException e) {
            properties = null;
            Logger.error.log(TAG, e, "[File] Failed to load application properties", APP_PROPERTIES_FILE);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                is = null;
            }
        }
        return properties;
    }

    /**
     * Persist the Application Properties file {@value #APP_PROPERTIES_FILE} to
     * the phone's file system
     * 
     * @param context
     */
    private void persistToFile(Context context) {
        Logger.debug.log(TAG, "persisting asset file: " + APP_PROPERTIES_FILE);
        InputStream appPropAsset = FileUtils.loadAssetFile(context, APP_PROPERTIES_FILE);
        FileUtils.saveToFile(context, appPropAsset, APP_PROPERTIES_FILE);
    }

}
