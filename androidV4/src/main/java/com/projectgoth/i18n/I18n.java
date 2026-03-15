package com.projectgoth.i18n;

import android.content.res.AssetManager;
import com.google.gson.Gson;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.SystemDatastore;
import com.projectgoth.events.EventMessageProvider;
import com.projectgoth.localization.Language;
import com.projectgoth.localization.LanguageList;
import com.projectgoth.util.AndroidLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class I18n extends com.projectgoth.localization.I18n {
    
    private static final String LOG_TAG = AndroidLogger.makeLogTag(I18n.class);
    
    private static final String LANGUAGE_LIST = "languageList";

    public static final String USER_LANGUAGE = "userLanguage";

    public static String DEFAULT_ERROR_MESSAGE;
    
    // Static initializer
    {
        updateDefaultTranslations();
    }

    public static void setLanguage(Language language) {
        com.projectgoth.localization.I18n.setLanguage(language);
        updateDefaultTranslations();
        
        Session.getInstance().setLanguageId();
        // Re-initialize the EventMessageProvider, so that all event messages that are
        // pre-stored also get updated with the new language.
        EventMessageProvider.getInstance().initialize();
    }
    
    public static String tr(String key) {
        String translated = com.projectgoth.localization.I18n.tr(key);
        if (translated != null) {
            return translated;
        }
        Logger.debug.log(LOG_TAG, "No direct translation found for: ", key);
        
        //Pattern matching for server messages
        translated = PatternTranslation.tryTranslate(key);
        if (translated != null) {
            return translated;
        }
        Logger.debug.log(LOG_TAG, "No pattern translation found for: ", key);

        Logger.warning.log(LOG_TAG, "No translation found for: ", key);

        return key;
    }

	public static String getLanguageID() {
        String userLanguage = SystemDatastore.getInstance().getStringData(I18n.USER_LANGUAGE);
        return userLanguage != null ? userLanguage : getLanguageID(Constants.DEFAULT_LANGUAGE);
	}
	
	private static void updateDefaultTranslations() {
	    // IMPORTANT: leave the "I18n." part. It is necessary when parsing the code for localized strings
        DEFAULT_ERROR_MESSAGE = I18n.tr("Oops, something’s not right. Try again.");
	}
	
    public static void loadLanguage(String languageId) {
        try {
            String langFile = getLanguageFullPath(languageId);
            InputStream is = ApplicationEx.loadAssetFile(langFile);
            
            // Try default language
            if (is == null && !langFile.endsWith(Constants.DEFAULT_LANGUAGE)) { 
                Logger.warning.log(LOG_TAG, "Could not load language ", languageId, ". Trying to load default language.");
                is = ApplicationEx.loadAssetFile( getLanguageFullPath(Constants.DEFAULT_LANGUAGE));
            }

            // In case neither the specific language nor the default could be loaded, try with the first file in the folder
            if (is == null) {
                AssetManager assetMgr = ApplicationEx.getInstance().getAssets();
                String[] langFolder = assetMgr.list(Constants.PATH_ASSETS_LANGUAGE);

                Logger.error.log(LOG_TAG, "Could not load default language! Loading ", langFolder[0]);
                is = ApplicationEx.loadAssetFile(getLanguageFullPath(langFolder[0]));
            }

            if (is != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                Language language = new Gson().fromJson(br, Language.class);
                br.close();
                //initialized a pattern list and clean the language's hashmap
                language = PatternTranslation.initializePattern(language);
                setLanguage(language);
            } else {
                Logger.error.log(LOG_TAG, "Could not load any language!");
            }
        } catch (Exception e) {
            Logger.error.log(LOG_TAG, e);
        }
    }
    
    public static LanguageList getLanguageList() {
        LanguageList languageList = null;
        try {
            String langFile = getLanguageFullPath(LANGUAGE_LIST);
            InputStream is = ApplicationEx.loadAssetFile(langFile);

            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            languageList = new Gson().fromJson(br, LanguageList.class);
            br.close();
        } catch (Exception e) {
            Logger.error.log(LOG_TAG, e);
        }
        return languageList;
    }
    
    public static String getLanguageFullPath(String fileName) {
        return Constants.PATH_ASSETS_LANGUAGE + File.separator + fileName;
    }

}
