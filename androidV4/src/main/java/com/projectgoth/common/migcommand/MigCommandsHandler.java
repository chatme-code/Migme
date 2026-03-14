/**
 * Copyright (c) 2013 Project Goth
 *
 * MigCommandsHandler.java
 * Created Aug 4, 2014, 3:47:00 PM
 */

package com.projectgoth.common.migcommand;

import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

import com.projectgoth.common.CaseInsensitiveHashMap;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.WebURL;
import com.projectgoth.datastore.Session;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.util.AndroidLogger;

//@formatter:off
/**
 * Handles external URL commands that can command the client to do specific actions.
 * Mig commands are in the format "mig33:<commandname>(<param1>, <param2>, ...).
 * 
 * @author angelorohit
 */
//@formatter:on
public class MigCommandsHandler {

    private static final String                      LOG_TAG     = AndroidLogger.makeLogTag(MigCommandsHandler.class);
    private static final MigCommandsHandler          INSTANCE    = new MigCommandsHandler();
    
    private static final Pattern migCmdPattern = Pattern.compile(Constants.LINK_MIG33 + "([\\w\\d]*)\\(*'*([^\\)']*)'*\\)*");

    private String                                   pendingCommandUrl;

    /**
     * A cache of supported Mig commands. The key is the name of the Mig command.
     * @see {@link SupportedMigCommands}.
     */
    private CaseInsensitiveHashMap<MigCommandAction> migCommands = new CaseInsensitiveHashMap<MigCommandAction>();

    private MigCommandsHandler() {
        init();
    }

    public static synchronized MigCommandsHandler getInstance() {
        return INSTANCE;
    }

    //@formatter:off
    private void init() {
        // Generate a list of supported mig commands.
        migCommands.put(SupportedMigCommands.SHOW_MIGSTORE, new MigCommandAction.ShowMigStore());
        migCommands.put(SupportedMigCommands.JOIN_CHATROOM, new MigCommandAction.JoinChatroom());
        migCommands.put(SupportedMigCommands.SEARCH_TOPIC, new MigCommandAction.SearchTopic());
        migCommands.put(SupportedMigCommands.START_PRIVATE_CHAT, new MigCommandAction.StartPrivateChat());
        migCommands.put(SupportedMigCommands.JOIN_LINKED_CHATROOM, new MigCommandAction.JoinLinkedChatroom());
        migCommands.put(SupportedMigCommands.SHOW_GROUP, new MigCommandAction.ShowGroup());
        migCommands.put(SupportedMigCommands.SHOW_CHATROOM_LIST, new MigCommandAction.ShowChatroomList());
        migCommands.put(SupportedMigCommands.GOTO_LOGIN, new MigCommandAction.GotoLogin());
        migCommands.put(SupportedMigCommands.OPEN_URL, new MigCommandAction.OpenUrl());
        migCommands.put(SupportedMigCommands.SHOW_PROFILE, new MigCommandAction.ShowProfile());
        migCommands.put(SupportedMigCommands.SHOW_MY_GROUPS, new MigCommandAction.ShowBrowser(WebURL.URL_MY_GROUPS));
        migCommands.put(SupportedMigCommands.SHOW_GROUP_LIST, new MigCommandAction.ShowBrowser(WebURL.URL_GROUPS_LIST));
        migCommands.put(SupportedMigCommands.SHOW_MIGWORLD, new MigCommandAction.ShowBrowser(WebURL.URL_MIGWORLD));
        migCommands.put(SupportedMigCommands.SHOW_HOTTOPICS, new MigCommandAction.ShowHotTopics());
        migCommands.put(SupportedMigCommands.SHOW_RECOMMENDEDUSERS, new MigCommandAction.ShowRecommendedUsers());
        migCommands.put(SupportedMigCommands.DO_LOGOUT, new MigCommandAction.DoLogout());
        migCommands.put(SupportedMigCommands.DO_SSOLOGIN, new MigCommandAction.DoSSOLogin());
        migCommands.put(SupportedMigCommands.DO_SSOLOGIN_FIKSU, new MigCommandAction.DoSSOLoginFiksu());
        migCommands.put(SupportedMigCommands.SYNC_PHONE_ADDRESSBOOK, new MigCommandAction.SyncPhoneAddressbook());
        migCommands.put(SupportedMigCommands.SHOW_POST, new MigCommandAction.ShowPost());
        migCommands.put(SupportedMigCommands.SHOW_SHAREBOX, new MigCommandAction.ShowSharebox());
        migCommands.put(SupportedMigCommands.SHOW_SHARETO, new MigCommandAction.ShowShareTo());
        migCommands.put(SupportedMigCommands.SHOW_SHARETO_FB, new MigCommandAction.ShowShareToFB());
        migCommands.put(SupportedMigCommands.SHOW_SHARETO_TW, new MigCommandAction.ShowShareToTW());
        migCommands.put(SupportedMigCommands.SHOW_SHARETO_MIGME, new MigCommandAction.ShowShareToMigme());
        migCommands.put(SupportedMigCommands.SEND_GIFT, new MigCommandAction.SendGift());
        migCommands.put(SupportedMigCommands.SHOW_FOLLOWERS, new MigCommandAction.ShowFollowers());
        migCommands.put(SupportedMigCommands.SHOW_BADGES, new MigCommandAction.ShowBadges());
        migCommands.put(SupportedMigCommands.SHOW_INVITE_FRIENDS, new MigCommandAction.ShowInviteFriends());
        migCommands.put(SupportedMigCommands.GO_GAMEPAGE, new MigCommandAction.GoGamePage());
    }
    
    //@formatter:on
    public boolean handleCommandForUrl(final String url) {
        if (!performActionForUrl(url)) {
            Logger.warning.log(LOG_TAG, "Could not perform an action for: " + url);
            return false;
        }
        return true;
    }

    /**
     * Parses a given command url and performs any supported action for it.
     * 
     * @param commandUrl
     *            A String that represents the command url sent by an external
     *            source.
     * @return true if an action was performed for the command url and false
     *         otherwise.
     */
    private boolean performActionForUrl(final String commandUrl) {
        // The command url can be formatted as mig33:<commandname>(<param1>, <param2>, ...)
        Matcher m = migCmdPattern.matcher(commandUrl);

        if (m.find()) {
            // Note: Group count is not the same as size (accessing
            // m.groupCount() is valid).
            // Group index 0 is the original string that was matched.
            // Group index 1 is the command that was matched.
            // Group index 2 is the parameters that should be comma separated.
            String strCommand = null;
            String[] params = null;
            if (m.groupCount() > 0) {
                strCommand = m.group(1);
            }

            if (m.groupCount() > 1) {
                String strRawParams = m.group(2);

                // Split by regex (combination of comma and spaces).
                if (!TextUtils.isEmpty(strRawParams)) {
                    try {
                        strRawParams = URLDecoder.decode(strRawParams, Constants.DEFAULT_ENCODING);
                        
                        // The regex below will strip away spurious commas followed by one or more spaces in parameters.
                        // It will preserve leading or trailing spaces in parameters.
                        // So, for example: "  test,  , ,,  string  " => ["  test", "  string  "] 
                        params = TextUtils.split(strRawParams, ",(?:\\s*,)*");
                    } catch (Exception ex) {
                        Logger.warning.log(LOG_TAG, ex);
                    }
                }
            }

            return performActionForCommand(commandUrl, strCommand, params);
        }

        return false;
    }

    /**
     * Performs any supported action for the given command.
     * 
     * @param command
     *            Any one of {@link SupportedMigCommands}
     * @param params
     *            An array containing one or more String parameters that help
     *            define the action for which the command is to be performed.
     * @return true if an action was performed and false otherwise.
     */
    private boolean performActionForCommand(final String commandUrl, 
    							final String command, final String[] params) {
        if (!TextUtils.isEmpty(command)) {

            final MigCommandAction migCommandAction = migCommands.get(command);
            
            if (migCommandAction != null) {
            	// Check whether the service is active and if an invalid
            	// session check needs to be done for this mig command action.
            	if (migCommandAction.shouldDoInvalidSessionCheck() &&
        			!Session.getInstance().isServiceActive()) {
                    // If the action is not performed, 
            		// store the URL for processing later on.
                    pendingCommandUrl = commandUrl;
                    ActionHandler.getInstance().showLogin(null);
                    return true;
                }
                
                clearPendingCommandUrl();
                return migCommandAction.doAction(params);
            } else {
                Logger.warning.log(LOG_TAG, command,  
                		" is not a supported Mig Command.");
            }
        }

        return false;
    }

    /**
     * Checks if there is a command URL that needs to be handled
     * after logging in. 
     */
    public void processPendingCommandUrl() {
        if (!TextUtils.isEmpty(pendingCommandUrl)) {
            handleCommandForUrl(pendingCommandUrl);
        }
    }

    /**
     * Clears the pending command URL after it is handled 
     * or if the NUE is displayed
     * 
     */
    public void clearPendingCommandUrl() {
        pendingCommandUrl = null;
    }

}
