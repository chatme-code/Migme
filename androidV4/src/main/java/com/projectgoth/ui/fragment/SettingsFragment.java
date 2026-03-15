/**
 * Copyright (c) 2013 Project Goth
 * SettingsFragment.java
 * Created Sep 4, 2013, 11:35:29 AM
 */

package com.projectgoth.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.blackhole.enums.ImType;
import com.projectgoth.common.Config;
import com.projectgoth.common.SharedPrefsManager;
import com.projectgoth.common.Version;
import com.projectgoth.common.WebURL;
import com.projectgoth.controller.EmoticonsController;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.localization.LanguageList;
import com.projectgoth.localization.LanguageListEntry;
import com.projectgoth.model.SettingsItem;
import com.projectgoth.model.SettingsItem.SettingsViewType;
import com.projectgoth.music.deezer.DeezerPlayerManager;
import com.projectgoth.notification.AlertListener;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.AlertHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.adapter.SettingsAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;


/**
 * @author mapet
 */
public class SettingsFragment extends BaseListFragment implements BaseViewListener<SettingsItem> {

    private SettingsGroupType mGroupType = SettingsGroupType.FIRST_LEVEL;
    private SettingsAdapter mSettingsAdapter;
    private List<SettingsItem> mSettings;

    public static final String PARAM_SETTINGS_GROUP_TYPE = "PARAM_SETTINGS_GROUP_TYPE";
    public static final String SHARED_PREFS_CHAT_NOTIFICATION_SOUND = "SHARED_PREFS_CHAT_NOTIFICATION_SOUND";
    public static final String SHARED_PREFS_CHAT_NOTIFICATION_VIBRATE = "SHARED_PREFS_CHAT_NOTIFICATION_VIBRATE";
    public static final boolean CHAT_NOTIFICATION_SOUND_DEFAULT = true;
    public static final boolean CHAT_NOTIFICATION_VIBRATE_DEFAULT = false;

    public enum SettingsGroupType {

        FIRST_LEVEL(0), SYSTEM(1), APPLICATION(2), LANGUAGE(3), CHAT_NOTIFICATION(4);

        private int type;

        private SettingsGroupType(int type) {
            this.type = type;
        }

        public int getType() {
            return this.type;
        }

        public static SettingsGroupType fromValue(int type) {
            for (SettingsGroupType listType : values()) {
                if (listType.getType() == type) {
                    return listType;
                }
            }
            return FIRST_LEVEL;
        }
    }

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);

        mGroupType = SettingsGroupType.fromValue(args.getInt(PARAM_SETTINGS_GROUP_TYPE,
                SettingsGroupType.FIRST_LEVEL.getType()));


        switch (mGroupType) {
            case FIRST_LEVEL:
                mSettings = getFirstLevelSettings();
                break;
            case SYSTEM:
                mSettings = getSystemSettings();
                break;
            case APPLICATION:
                mSettings = getApplicationSettings();
                break;
            case LANGUAGE:
                mSettings = getLanguageSettings();
                break;
            case CHAT_NOTIFICATION:
                mSettings = getChatNotificationSettings();
                break;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mList.setFooterDividersEnabled(false);
        mMainContainer.setBackgroundColor(ApplicationEx.getContext().getResources().getColor(R.color.gray_background));
        mSettingsAdapter.setSettingsItems(mSettings);
        mSettingsAdapter.setSettingsClickListener(this);
        mSettingsAdapter.notifyDataSetChanged();
    }

    @Override
    protected View createHeaderView() {
        if (mGroupType == SettingsGroupType.CHAT_NOTIFICATION) {
            View wrapper = LayoutInflater.from(ApplicationEx.getContext()).inflate(R.layout.fragment_settings_chat_notification_header, null);
            TextView textView = (TextView) wrapper.findViewById(R.id.title);
            textView.setText(I18n.tr("Notification settings for private and group chats"));
            return wrapper;
        }
        return super.createHeaderView();
    }

    @Override
    protected BaseAdapter createAdapter() {
        mSettingsAdapter = new SettingsAdapter();
        return mSettingsAdapter;
    }

    private List<SettingsItem> getFirstLevelSettings() {
        ArrayList<SettingsItem> firstLevelSettings = new ArrayList<SettingsItem>();

        firstLevelSettings.add(new SettingsItem(I18n.tr("Privacy"), R.id.action_open_browser,
                WebURL.URL_SETTINGS_PRIVACY, GAEvent.Settings_Privacy));
        firstLevelSettings.add(new SettingsItem(I18n.tr("System"), R.id.action_open_settings, SettingsViewType.GROUP,
                SettingsGroupType.SYSTEM, GAEvent.Settings_System));
        firstLevelSettings.add(new SettingsItem(I18n.tr("Account settings"), R.id.action_open_browser,
                WebURL.URL_SETTINGS_ACCOUNT, GAEvent.Settings_AccountSettings));
        firstLevelSettings.add(new SettingsItem(I18n.tr("My account"), R.id.action_open_browser,
                WebURL.URL_ACCOUNT_SETTINGS, GAEvent.Settings_MyAccount));
        firstLevelSettings.add(new SettingsItem(I18n.tr("Chat notification"), R.id.action_open_settings,
                SettingsViewType.GROUP, SettingsGroupType.CHAT_NOTIFICATION, GAEvent.Settings_ChatNotification));
        firstLevelSettings.add(new SettingsItem(I18n.tr("Third party sites"), R.id.action_open_browser,
                WebURL.URL_THIRD_PARTY_SITES_SETTINGS, GAEvent.Settings_ThirdPartySites));
        firstLevelSettings.add(new SettingsItem(I18n.tr("Application"), R.id.action_open_settings,
                SettingsViewType.GROUP, SettingsGroupType.APPLICATION, GAEvent.Settings_Application));
        firstLevelSettings.add(new SettingsItem(I18n.tr("About mig"), R.id.action_open_browser,
                WebURL.URL_SETTINGS_ABOUTMIG33, GAEvent.Settings_AboutMigMe));
        firstLevelSettings.add(new SettingsItem(I18n.tr("Sign out"), R.id.action_logout, "", GAEvent.Settings_Logout));

        return firstLevelSettings;
    }

    private List<SettingsItem> getSystemSettings() {
        ArrayList<SettingsItem> systemSettings = new ArrayList<SettingsItem>();

        systemSettings.add(new SettingsItem(I18n.tr("Clear cached images"), R.id.action_clear_image_cache,
                SettingsViewType.DEFAULT, GAEvent.Settings_ClearImageCache));
        systemSettings.add(new SettingsItem(I18n.tr("Language"), R.id.action_language_list, SettingsViewType.GROUP,
                SettingsGroupType.LANGUAGE, GAEvent.Settings_Language));

        return systemSettings;
    }

    private List<SettingsItem> getApplicationSettings() {
        ArrayList<SettingsItem> applicationSettings = new ArrayList<SettingsItem>();

        applicationSettings.add(new SettingsItem(I18n.tr("Version"), R.id.action_settings_default,
                SettingsViewType.TEXT, getVersion(), GAEvent.Settings_Version));
        applicationSettings.add(new SettingsItem(I18n.tr("Services"), R.id.action_open_browser,
                WebURL.URL_SETTINGS_SERVICES, GAEvent.Settings_Services));

        return applicationSettings;
    }


    private List<SettingsItem> getChatNotificationSettings() {
        ArrayList<SettingsItem> applicationSettings = new ArrayList<SettingsItem>();

        SharedPreferences sharedPreferences = SharedPrefsManager.getGlobalSharedPreference();
        applicationSettings.add(new SettingsItem(I18n.tr("In-app alert sound"), R.id.action_chat_notification_sound,
                SettingsViewType.TOGGLE, sharedPreferences.getBoolean(SHARED_PREFS_CHAT_NOTIFICATION_SOUND,
                CHAT_NOTIFICATION_SOUND_DEFAULT), GAEvent.Settings_Sound));
        applicationSettings.add(new SettingsItem(I18n.tr("Vibrate"), R.id.action_chat_notification_vibrate,
                SettingsViewType.TOGGLE, sharedPreferences.getBoolean(SHARED_PREFS_CHAT_NOTIFICATION_VIBRATE,
                CHAT_NOTIFICATION_VIBRATE_DEFAULT), GAEvent.Settings_Vibrate));

        return applicationSettings;
    }


    @Override
    public void onItemClick(View v, SettingsItem data) {
        SettingsItem settingsItem = data;
        int actionId = settingsItem.getActionId();

        GAEvent event = data.getGAEvent();
        if (event != null) {
            event.send();
        }

        if (v.getId() == R.id.toggle_button) {
            ToggleButton toggleButton = (ToggleButton) v;
            SharedPreferences.Editor editor = SharedPrefsManager.getGlobalSharedPreference().edit();

            switch (actionId) {
                case R.id.action_chat_notification_vibrate:
                    editor.putBoolean(SHARED_PREFS_CHAT_NOTIFICATION_VIBRATE, toggleButton.isChecked());
                    editor.commit();
                    break;
                case R.id.action_chat_notification_sound:
                    editor.putBoolean(SHARED_PREFS_CHAT_NOTIFICATION_SOUND, toggleButton.isChecked());
                    editor.commit();
                    break;
            }
        }

        if (v.getId() == R.id.edit) {
            switch (actionId) {
                case R.id.action_sign_in_off_facebook:
                    displayBrowser(String.format(WebURL.URL_IM_SETUP, ImType.FACEBOOK.getValue()));
                    break;
                case R.id.action_sign_in_off_gtalk:
                    displayBrowser(String.format(WebURL.URL_IM_SETUP, ImType.GTALK.getValue()));
                    break;
                case R.id.action_sign_in_off_msn:
                    displayBrowser(String.format(WebURL.URL_IM_SETUP, ImType.MSN.getValue()));
                    break;
                case R.id.action_sign_in_off_yahoo:
                    displayBrowser(String.format(WebURL.URL_IM_SETUP, ImType.YAHOO.getValue()));
                    break;
            }
        }

        switch (actionId) {
            case R.id.action_open_browser:
                String url = settingsItem.getParam();
                displayBrowser(url);
                break;
            case R.id.action_open_settings:
                ActionHandler.getInstance().displaySettings(getActivity(), settingsItem.getGroupType());
                break;
            case R.id.action_clear_image_cache:
                handleClearImageCacheSettings();
                break;
            case R.id.action_language_list:
                ActionHandler.getInstance().displaySettings(getActivity(), settingsItem.getGroupType());
                break;
            case R.id.action_change_language:
                ActionHandler.getInstance().changeLanguage(getActivity(), settingsItem.getLabel(), settingsItem.getParam());
                break;
            case R.id.action_logout:
                ApplicationEx.getInstance().getNetworkService().logout();
                DeezerPlayerManager.getInstance().stop();
                break;
        }
    }

    @Override
    public void onItemLongClick(View v, SettingsItem data) {
    }

    private String getVersion() {
        String version = Version.getVersionNumberString();
        try {
            version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        version = String.format("%s (%s)", version, Version.getVasTrackingId());

        if (Config.isDebug()) {
            version += Version.getDebugIdText();
        }

        return version;
    }

    private void handleClearImageCacheSettings() {
        String message = I18n.tr("Sure you want to clear cached images?");
        AlertHandler.getInstance().showCustomConfirmationDialog(getActivity(), I18n.tr("Confirmation"), message,
                new AlertListener() {

                    @Override
                    public void onDismiss() {
                    }

                    @Override
                    public void onConfirm() {
                        EmoticonsController.getInstance().clearEmoticonsImageCache();
                        Toast.makeText(getActivity(), I18n.tr("Cached images cleared"), Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    @Override
    protected boolean isPullToRefreshEnabled() {
        return false;
    }

    private ArrayList<SettingsItem> getLanguageSettings() {
        ArrayList<SettingsItem> languageSettings = new ArrayList<SettingsItem>();

        LanguageList languageList = I18n.getLanguageList();
        for (LanguageListEntry entry : languageList) {
            SettingsItem item = new SettingsItem(I18n.tr(entry.name), R.id.action_change_language, SettingsViewType.DEFAULT);
            item.setParam(entry.id);
            languageSettings.add(item);
        }
        return languageSettings;
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = super.getActionBarConfig();
        config.setNavigationButtonState(NavigationButtonState.BACK);
        return config;
    }

    @Override
    protected String getTitle() {
        return I18n.tr("Settings");
    }

    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_setting_white;
    }

    private void displayBrowser(String url) {
        ActionHandler.getInstance()
                .displayBrowser(getActivity(), url, I18n.tr("Settings"), R.drawable.ad_setting_white);
    }
}
