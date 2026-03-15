/**
 * Copyright (c) migme 2014
 *
 * SharedPrefsManager.java
 * Created Aug 20, 2014, 9:55:01 AM
 */
package com.projectgoth.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.datastore.Session;

/**
 * Manages shared preference saving and loading throughout the app.
 * @author angelorohit
 */
public class SharedPrefsManager {
    private SharedPreferences mSharedPrefs = null;
    private SharedPreferences.Editor mEditor;

    /**
     * Per the design guidelines, we show the navigation drawer on 
     * launch until the user manually expands it.
     */
    private final static String PREFS_USER_LEARNED_DRAWER = "PREFS_USER_LEARNED_DRAWER";
    private final static String PREFS_LAST_SHOWN_PINNED_MESSAGE_ID = "PREFS_LAST_SHOWN_PINNED_MESSAGE_ID";
    private final static String PREFS_SHOW_PINNED_MESSAGE = "_PREFS_SHOW_PINNED_MESSAGE";
    private final static String PREFS_GLOBAL_PREFIX = "PREFS_GLOBAL_PREFIX";
    private final static String PREFS_LOGIN_IS_LOGGED_IN = "PREFS_LOGIN_IS_LOGGED_IN";


    public SharedPrefsManager(final Context context) {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        //Calling edit() creates an editor instance each time. Only create once here.
        mEditor = mSharedPrefs.edit();
    }

    public void setUserLearnedDrawer(final boolean state) {
        mEditor.putBoolean(PREFS_USER_LEARNED_DRAWER, state).apply();
    }

    public boolean didUserLearnDrawer() {
        return mSharedPrefs.getBoolean(PREFS_USER_LEARNED_DRAWER, false);
    }

    /**
     * Record whether user wants to hide pinned message or not.
     *
     * @param key the conversationId
     * @param shouldShowPinnedMessage set to false if user wants to hide pinned message
     */
    public void setShouldShowPinnedMessage(String key, boolean shouldShowPinnedMessage) {
        mEditor.putBoolean(key + PREFS_SHOW_PINNED_MESSAGE, shouldShowPinnedMessage).apply();
    }

    public boolean getShouldShowPinnedMessage(String key) {
        return mSharedPrefs.getBoolean(key + PREFS_SHOW_PINNED_MESSAGE, true);
    }

    public void setLastShownPinnedMessageId(String messageId) {
        mEditor.putString(PREFS_LAST_SHOWN_PINNED_MESSAGE_ID, messageId).apply();
    }

    public String getLastShownPinnedMessageId() {
        return mSharedPrefs.getString(PREFS_LAST_SHOWN_PINNED_MESSAGE_ID, "");
    }

    public static SharedPreferences getGlobalSharedPreference() {
        String prefsName;
        if (Session.getInstance().getUsername() != null) {
            prefsName = PREFS_GLOBAL_PREFIX + Session.getInstance().getUsername();
        } else {
            prefsName = PREFS_GLOBAL_PREFIX;
        }

        SharedPreferences sharedPreferences = ApplicationEx.getInstance().getSharedPreferences(
                prefsName, Context.MODE_PRIVATE);
        return sharedPreferences;
    }

}
