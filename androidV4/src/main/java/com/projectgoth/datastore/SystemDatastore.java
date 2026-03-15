/**
 * Mig33 Pte. Ltd.
 *
 * Copyright (c) 2012 mig33. All rights reserved.
 */

package com.projectgoth.datastore;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.MenuConfig;
import com.projectgoth.common.Constants;
import com.projectgoth.common.DefaultConfig;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Tools;
import com.projectgoth.util.AndroidLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * SystemDatastore.java
 * 
 * @author warrenbalcos on Jun 4, 2013
 * 
 */
public class SystemDatastore extends BaseDatastore {

    private static final String          LOG_TAG                      = AndroidLogger.makeLogTag(SystemDatastore.class);

    public static final String           PREFERENCES_NAME             = "Mig33SystemPreferences";

    public static final String           ALERT_PREFERENCES_NAME       = "Mig33AlertPreferences";

    public static final String           NUE_PREFERENCES_NAME         = "Mig33NUEPreferences";

    public static final String           POSTDRAFT_PREFERENCES_NAME   = "Mig33POSTDRAFTPreferences";

    public static final String           MENTIONS_PREFERENCES_NAME    = "MigMentionsPreferences";

    private static final String          HOME_MENU_CONFIG             = "HOME_MENU_CONFIG";

    private static final String          LANGUAGE                     = "LANGUAGE";

    private static final String          APP_MENU_VERSION             = "APP_MENU_VERSION";

    private SharedPreferences            preferences;
    private SharedPreferences            alertReadPreferences;
    private SharedPreferences            nuePreferences;
    private SharedPreferences            postDraftPreferences;
    private SharedPreferences            mentionsPreferences;

    // TODO: add dummy data for these...
    // language setting, emoticon dimension, emoticon list,
    // sticker pack list, sticker list, gift list, default urls, contact list
    // version, chat list version, menu list, etc.

    /**
     * The contact list version that is set when receiving the
     * CONTACT_LIST_VERSION packet.
     */
    private Integer              contactListVersion      = null;

    /**
     * The time stamp that is set when receiving the last CONTACT_LIST_VERSION
     * packet.
     */
    private Long                 contactListTimestamp    = null;

    /**
     * The account balance of the logged in user. This data is received from the
     * server on post-login. It is received as a string whose formatting is not
     * determined.
     */
    private String               accountBalance          = null;

    /**
     * auto complete mentions
     */
    private ArrayList<String>    mentions = null;

    private SystemDatastore() {
        super();

        initPreferences();
    }

    private static class SystemDatastoreHolder {
        static final SystemDatastore sINSTANCE = new SystemDatastore();
    }

    public static SystemDatastore getInstance() {
        return SystemDatastoreHolder.sINSTANCE;
    }
    
    @Override
    protected void initData() {
        // Nothing to do here...
    }    
    
    @Override
    public void clearData() {
        clearData(LANGUAGE);
        clearData(HOME_MENU_CONFIG);
        //need to clear otherwise if I switch my account with a new account which has
        //not set display picture yet, it still displays the display picture of the 
        //previous account
        clearData(Session.PROFILE_PIC_GUID_KEY);
        clearData(Session.AVATAR_GUID_KEY);
        //we need to clear contact list version here, otherwise when switching to another account,
        //it will compare the contact list version from loginOk packet with the contact list version
        //of the previous account persistently stored 
        contactListVersion = null;
        clearData(Session.CONTACTLIST_VERSION);
        contactListTimestamp = null;
        clearData(Session.CONTACTLIST_TIMESTAMP);
        accountBalance = null;
        clearData(Session.ACCOUNT_BALANCE);

        clearDraftPostData();

        mentions = null;
        clearMentionsData();
    }

    private void initPreferences() {
        final Context appCtx = ApplicationEx.getContext();
        if (appCtx != null) {
            preferences = appCtx.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
            alertReadPreferences =  appCtx.getSharedPreferences(ALERT_PREFERENCES_NAME, Context.MODE_PRIVATE);
            nuePreferences = appCtx.getSharedPreferences(NUE_PREFERENCES_NAME, Context.MODE_PRIVATE);
            postDraftPreferences = appCtx.getSharedPreferences(POSTDRAFT_PREFERENCES_NAME, Context.MODE_PRIVATE);
            mentionsPreferences = appCtx.getSharedPreferences(MENTIONS_PREFERENCES_NAME, Context.MODE_PRIVATE);
        }
    }

    /**
     * Saves a string value, if data is null it is cleared from persistence
     * 
     * @param key
     * @param data
     */
    public void saveData(String key, String data) {
        Logger.debug.log(LOG_TAG, "Saving string data for key: ", key);

        if (key == null) {
            return;
        }

        if (data == null) {
            clearData(key);
            return;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, data);
        Logger.debug.log(LOG_TAG, "data: ", data);
        editor.commit();
    }

    public void saveData(String key, Boolean data) {
        Logger.debug.log(LOG_TAG, "Saving boolean data for key: ", key);
        
        if (key == null) {
            return;
        }

        if (data == null) {
            clearData(key);
            return;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, data);
        Logger.debug.log(LOG_TAG, "data: ", data);

        editor.commit();
    }

    public void saveAlertReadData(String key, Boolean isRead) {
        Logger.debug.log(LOG_TAG, "Saving boolean data for alert key: ", key);

        if (key == null) {
            return;
        }

        SharedPreferences.Editor editor = alertReadPreferences.edit();
        editor.putBoolean(key, isRead);
        Logger.debug.log(LOG_TAG, "data: ", isRead);

        editor.commit();
    }

    public void saveNUEData(String key, Boolean isRead) {
        Logger.debug.log(LOG_TAG, "Saving boolean data for NUE key: ", key);

        if (key == null) {
            return;
        }

        SharedPreferences.Editor editor = nuePreferences.edit();
        editor.putBoolean(key, isRead);
        Logger.debug.log(LOG_TAG, "data: ", isRead);

        editor.commit();
    }

    public boolean getAlertReadData(String key) {
        boolean result = false;
        try {
            result = alertReadPreferences.getBoolean(key, result);
        } catch (Exception e) {
            Logger.error.log(LOG_TAG, e);
        }

        return result;
    }

    public boolean getNUEData(String key) {
        boolean result = false;
        try {
            result = nuePreferences.getBoolean(key, result);
        } catch (Exception e) {
            Logger.error.log(LOG_TAG, e);
        }

        return result;
    }

    public String getPostDraftData(String key) {
        String result = "";
        try {
            result = postDraftPreferences.getString(key, result);
        } catch (Exception e) {
            Logger.error.log(LOG_TAG, e);
        }

        return result;
    }

    public void savePostDraftData(String key, String data) {
        Logger.debug.log(LOG_TAG, "Saving string data for key: ", key);

        if (key == null) {
            return;
        }

        SharedPreferences.Editor editor = postDraftPreferences.edit();
        editor.putString(key, data);
        Logger.debug.log(LOG_TAG, "data: ", data);
        editor.commit();
    }

    public void saveData(String key, Float data) {
        Logger.debug.log(LOG_TAG, "Saving float data for key: ", key);

        if (key == null) {
            return;
        }

        if (data == null) {
            clearData(key);
            return;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(key, data);
        Logger.debug.log(LOG_TAG, "data: ", data);

        editor.commit();
    }

    public void saveData(String key, Integer data) {
        Logger.debug.log(LOG_TAG, "Saving integer data for key: ", key);
        
        if (key == null) {
            return;
        }

        if (data == null) {
            clearData(key);
            return;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, data);
        Logger.debug.log(LOG_TAG, "data: ", data);

        editor.commit();
    }

    public void saveData(String key, Long data) {
        Logger.debug.log(LOG_TAG, "Saving Long data for key: ", key);

        if (key == null) {
            return;
        }

        if (data == null) {
            clearData(key);
            return;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, data);
        Logger.debug.log(LOG_TAG, "data: ", data);

        editor.commit();
    }

    public void saveData(String key, Object data) {
        if (key == null) {
            Logger.error.log(LOG_TAG, "Can't save data for null key!");
            return;
        }

        if (data == null) {
            clearData(key);
            return;
        }

        Logger.debug.log(LOG_TAG, "Saving data for key: ", key);
        saveData(key, new Gson().toJson(data));
    }
    
    /**
     * Returns stored {@link Integer} data, defaults to -1
     * 
     * @param key
     * @return
     */
    public Integer getIntegerData(String key) {
        int result = -1;
        try {
            result = preferences.getInt(key, result);
        } catch (Exception e) {
        }
        
        return result;
    }

    /**
     * Returns stored boolean data, defaults to false
     * 
     * @param key
     * @return
     */
    public boolean getBooleanData(String key) {
        boolean result = false;
        try {
            result = preferences.getBoolean(key, result);
        } catch (Exception e) {
        }
        
        return result;
    }

    /**
     * Returns stored {@link String} data
     * 
     * @param key
     * @return
     */
    public String getStringData(String key) {
        String result = null;
        try {
            result = preferences.getString(key, result);
        } catch (Exception e) {
        }

        return result;
    }

    /**
     * Returns stored {@link Long} data, defaults to -1
     * 
     * @param key
     * @return
     */
    public Long getLongData(String key) {
        long result = -1;
        try {
            result = preferences.getLong(key, result);
        } catch (Exception e) {
        }

        return result;
    }

    /**
     * Returns stored {@link Float} data, defaults to -1
     * 
     * @param key
     * @return
     */
    public Float getFloatData(String key) {
        float result = -1;
        try {
            result = preferences.getFloat(key, result);
        } catch (Exception e) {
        }

        return result;
    }

    /**
     * Delete the stored data
     * 
     * @param key
     */
    public void clearData(String key) {
        Logger.debug.log(LOG_TAG, "Clearing data for: ", key);
        
        if (key == null) {
            return;
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        editor.commit();
    }

    public void clearDraftPostData() {
        SharedPreferences.Editor editor = postDraftPreferences.edit();
        editor.clear();
        editor.commit();
    }

    public void clearMentionsData() {
        SharedPreferences.Editor editor = mentionsPreferences.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * @return the language (default is "en-US")
     */
    public String getLanguage() {
        String data = getStringData(LANGUAGE);
        if (data == null) {
            data = DefaultConfig.LANGUAGE;
        }
        return data;
    }

    /**
     * @param language
     *            the language to set
     */
    public void setLanguage(String language) {
        saveData(LANGUAGE, language);
    }

    public MenuConfig getHomeMenuConfig() {
        String data = getStringData(HOME_MENU_CONFIG);
        if (data == null) {
            data = loadMenuConfigFile();
        }
        if (data != null) {
            return new Gson().fromJson(data, MenuConfig.class);
        }
        return null;
    }

    private String loadMenuConfigFile() {
        String buff = null;
        final Context appCtx = ApplicationEx.getContext();
        
        if (appCtx != null) {
            try {            
                String asset = "default_home_menu.json";
                String fileName = Constants.PATH_ASSETS_CONFIG + File.separator + asset;
                buff = Tools.readFromfile(fileName, appCtx);
                Logger.debug.log("MenuConfig", "buff: ", buff);
                if (null != buff) {
                    saveData(HOME_MENU_CONFIG, buff);
                }
            } catch (Exception e) {
                Logger.error.log(LOG_TAG, e);
            }
        }
        return buff;
    }

    /**
     * @return the appMenuVersion
     */
    public int getAppMenuVersion() {
        int data = getIntegerData(APP_MENU_VERSION);
        if (data < 0) {
            data = 0;
        }
        return data;
    }

    /**
     * @param appMenuVersion
     *            the appMenuVersion to set
     */
    public void setAppMenuVersion(int appMenuVersion) {
        saveData(APP_MENU_VERSION, appMenuVersion);
    }

    public boolean isFirstTimeLog(String events){
        if(!getBooleanData(events)) {
            saveData(events,true);
            return true;
        }
        return false;
    }

    public Integer getContactListVersion() {
        if (contactListVersion == null) {
            final Integer data = getIntegerData(Session.CONTACTLIST_VERSION);
            contactListVersion = data;
        }

        return contactListVersion;
    }

    public void setContactListVersion(final Integer version) {
        contactListVersion = version;
        saveData(Session.CONTACTLIST_VERSION, contactListVersion);
    }

    public Long getContactListTimestamp() {
        if (contactListTimestamp == null) {
            final Long data = getLongData(Session.CONTACTLIST_TIMESTAMP);
            contactListTimestamp = data;
        }

        return contactListTimestamp;
    }

    public void setContactListTimestamp(final Long timestamp) {
        contactListTimestamp = timestamp;
        saveData(Session.CONTACTLIST_TIMESTAMP, contactListTimestamp);
    }

    public String getAccountBalance() {
        if (accountBalance == null) {
            final String data = getStringData(Session.ACCOUNT_BALANCE);
            accountBalance = data;
        }

        return accountBalance;
    }

    public void setAccountBalance(final String balance) {
        accountBalance = balance;
        saveData(Session.ACCOUNT_BALANCE, accountBalance);
    }

    public String getMentionsData(String key) {
        String result = Constants.BLANKSTR;
        try {
            result = mentionsPreferences.getString(key, result);
        } catch (Exception e) {
            Logger.error.log(LOG_TAG, e);
        }

        return result;

    }

    public void saveMentionsData(String key, String mentions) {
        if (key == null) {
            return;
        }

        SharedPreferences.Editor editor = mentionsPreferences.edit();
        editor.putString(key, mentions);

        editor.commit();
    }

    public ArrayList<String> getMentions(String userId) {
        if (mentions == null) {
            String mentionsData = getMentionsData(userId);
            mentions = new ArrayList<>(Arrays.asList(mentionsData.split(",")));

            //fetch it from server for the first time.
            UserDatastore.getInstance().requestGetMentionAutoCompleteList(userId);
        }

        return mentions;
    }

    public void setMentions(String userId, ArrayList<String> mentions) {
        if (mentions == null) {
            return;
        }

        this.mentions = mentions;
        StringBuilder mentionsData = new StringBuilder();
        for (int i = 0; i < mentions.size(); i++ ) {
            mentionsData.append(mentions.get(i));
            if (i != mentions.size()-1) {
                mentionsData.append(",");
            }
        }
        saveMentionsData(userId, mentionsData.toString());
    }


    public void addMention(String mention) {
        if (mentions != null) {
            mentions.add(0, mention);
        }
    }
}