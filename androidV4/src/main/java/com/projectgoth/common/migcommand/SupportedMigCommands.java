/**
 * Copyright (c) 2013 Project Goth
 *
 * SupportedBrowserCommands.java
 * Created Aug 5, 2014, 12:18:26 PM
 */

package com.projectgoth.common.migcommand;

/**
 * Contains all supported browser commands. Refer to the doc on Browser command
 * availability to see what each of these commands mean and what parameters they
 * support.
 * 
 * @author angelorohit
 * @see {@link MigCommandsHandler}
 */
public abstract class SupportedMigCommands {

    /**
     * Show MigStore page.
     * 
     * Example: mig33:migStore()
     */
    public static final String SHOW_MIGSTORE          = "migStore";

    /**
     * Join public chatroom.
     * 
     * Param: chatroom name
     * 
     * Example: mig33:joinChatroom('Lobby 123')
     */
    public static final String JOIN_CHATROOM          = "joinChatroom";

    /**
     * Show post page related to topics
     * 
     * Param: hashtag aka topic name
     * 
     * Example: mig33:searchTopic('mig33')
     */
    public static final String SEARCH_TOPIC           = "searchTopic";

    /**
     * Initiate new private chat.
     * 
     * Param: username
     * 
     * Example: mig33:privateChat('dangerbot')
     */
    public static final String START_PRIVATE_CHAT     = "privateChat";

    /**
     * NOT YET IMPLEMENTED! Initiate group chat.
     * 
     * Param: chatroomname
     * 
     * Example: mig33:groupChat('dangerbot')
     */
    public static final String START_GROUP_CHAT       = "groupChat";

    /**
     * Initiate public chatroom that is linked to a group.
     * 
     * Param: chatroom name, group id
     * 
     * Example: mig33:joinGroupChatroom('My Group,123')
     */
    public static final String JOIN_LINKED_CHATROOM   = "joinGroupChatroom";

    /**
     * Show the group screen.
     * 
     * Param: group id
     * 
     * Example: mig33:showGroup('<group id>')
     */
    public static final String SHOW_GROUP             = "showGroup";

    /**
     * Show chatroom list.
     * 
     * Example: mig33:chat()
     */
    public static final String SHOW_CHATROOM_LIST     = "chat";

    /**
     * NOT YET IMPLEMENTED! Launch authentication screen.
     * 
     * Example: mig33:auth()
     */
    public static final String AUTHENTICATE           = "auth";

    /**
     * NOT YET IMPLEMENTED! Simply close the browser screen.
     * 
     * Example: mig33:closeBrowser()
     */
    public static final String CLOSE_BROWSER          = "closeBrowser";

    /**
     * NOT YET IMPLEMENTED! Show update status screen.
     * 
     * Example: mig33:updateStatus()
     */
    public static final String SHOW_UPDATE_STATUS     = "updateStatus";

    /**
     * Show login screen. Only valid if the user hasn't logged in yet.
     * 
     * Examples: mig33:login() mig33:login('dangerbot')
     */
    public static final String GOTO_LOGIN             = "login";

    /**
     * NOT YET IMPLEMENTED! Show settings screen.
     * 
     * Example: mig33:settings()
     */
    public static final String SHOW_SETTINGS          = "settings";

    /**
     * NOT YET IMPLEMENTED! Display chatroom's users.
     * 
     * Example: mig33:showChatroomUsers('Lobby 123')
     */
    public static final String SHOW_CHATROOM_USERS    = "showChatroomUsers";

    /**
     * NOT YET IMPLEMENTED! Show phone's contact list.
     * 
     * Example: mig33:showPhoneBook()
     */
    public static final String SHOW_PHONEBOOK         = "showPhoneBook";

    /**
     * NOT YET IMPLEMENTED! Show IM manager screen.
     * 
     * Example: mig33:showIMManager()
     */
    public static final String SHOW_IM_MANAGER        = "showIMManager";

    /**
     * NOT YET IMPLEMENTED! Show friend list screen
     * 
     * Example: mig33:showFriends()
     */
    public static final String SHOW_FRIENDS           = "showFriends";
    
    /**
     * NOT YET IMPLEMENTED! Show friend list.
     * 
     * Example: mig33:friend()
     */
    public static final String SHOW_FRIEND_LIST       = "friend";

    /**
     * NOT YET IMPLEMENTED! Launch native browser.
     * 
     * Param: url to be launched.
     * 
     * Example: mig33:invokeNativeBrowser('http://mig33.com')
     * mig33:invokeNativeBrowser('market://details?id=com.projectgoth')
     */
    public static final String SHOW_NATIVE_BROWSER    = "invokeNativeBrowser";

    // @formatter:off
    /**
     * Open browser on a new window.
     * 
     * Examples: mig33:url('My Profile,http://www.mig33.com') 
     * mig33:url('My Profile,http://www.mig33.com,file://drawable/icon_home_invitefriend')
     * mig33:url('My Profile,http://www.mig33.com,http://www.mig33.com/icon.png')
     */
    public static final String OPEN_URL               = "url";

    // @formatter:on
    /**
     * Open profile page mig33:profile('<username>')
     * 
     * Example: mig33:profile('rondev')
     */
    public static final String SHOW_PROFILE           = "profile";

    /**
     * NOT YET IMPLEMENTED! Open mentions page
     * 
     * Example: mig33:mentions()
     */
    public static final String SHOW_MENTIONS          = "mentions";

    /**
     * NOT YET IMPLEMENTED! Open watch list page
     * 
     * Example: mig33:watchlist()
     */
    public static final String SHOW_WATCHLIST         = "watchlist";

    /**
     * Show my groups screen.
     * 
     * Example: mig33:mygroups()
     */
    public static final String SHOW_MY_GROUPS         = "mygroups";

    /**
     * Show all groups screen.
     * 
     * Example: mig33:groups()
     */
    public static final String SHOW_GROUP_LIST        = "groupList";

    /**
     * Show migWorld screen.
     * 
     * Example: mig33:migWorld()
     */
    public static final String SHOW_MIGWORLD          = "migWorld";

    /**
     * Show Hot Topic screen.
     * 
     * Example: mig33:hotTopics()
     */
    public static final String SHOW_HOTTOPICS         = "hotTopics";

    /**
     * NOT YET IMPLEMENTED! Show recommendations screen.
     * 
     * Example: mig33:recommendations()
     */
    public static final String SHOW_RECOMMENDATIONS   = "recommendations";

    /**
     * Show recommendations screen for users.
     * 
     * Example: mig33:recommendedUsers()
     */
    public static final String SHOW_RECOMMENDEDUSERS  = "recommendedUsers";

    /**
     * NOT YET IMPLEMENTED! Show help screen.
     * 
     * Example: mig33:help()
     */
    public static final String SHOW_HELP              = "help";

    /**
     * Trigger a logout action
     * 
     * Example: mig33:logout()
     */
    public static final String DO_LOGOUT              = "logout";

    // @formatter:off
    /**
     * Initiates an SSO Login. Since 3.12.237
     * 
     * Format: mig33:ssologin('<password type>, <username>, <access token>')
     * 
     * Examples: 
     * mig33:ssologin('14') 
     * mig33:ssologin('14, 100004513570896, AAAFFcC5...')
     */
    public static final String DO_SSOLOGIN            = "ssologin";
    
    /**
     * Initiates an SSO Login and sends Fiksu event. Since 4.03.002
     * 
     * Format: mig33:ssologinFiksu('<password type>, <username>, <access token>')
     * 
     * Examples: 
     * mig33:ssologinFiksu('14') 
     * mig33:ssologinFiksu('14, 100004513570896, AAAFFcC5...')
     */
    public static final String DO_SSOLOGIN_FIKSU      = "ssologinFiksu";
    

    // @formatter:on
    /**
     * Initiates syncing of device address book.
     * 
     * Example: mig33:syncPhoneAddressBook()
     */
    public static final String SYNC_PHONE_ADDRESSBOOK = "syncPhoneAddressBook";

    /**
     * Shows the post page.
     * 
     * Parameters:- postId - The id of the post whose native page is to be
     * shown. postType - 0 for regular post, 1 for group post.
     * 
     * Example: mig33:showPost('195596930-1393385164952', '1') if the post is a
     * group post.
     */
    public static final String SHOW_POST              = "showPost";

    /**
     * Shows the share box.
     * 
     * Parameters:- content - The content that the share box is automatically
     * populated with. If not present, then the share box is shown without any
     * preset content.
     * 
     * Example: mig33:share('http://www.google.com')
     */
    public static final String SHOW_SHAREBOX          = "share";

    /**
     * Shows the share list to share url to external, post or chat.
     *
     * Parameter:- string - The string should be shared
     *
     * Example: mig33:shareTo('some words http://www.google.com')
     */
    public static final String SHOW_SHARETO          = "shareTo";


    /**
     * Share url to facebook directly.
     *
     * Parameter:- string - The string should be shared
     *
     * Example: mig33:shareTo('some words http://www.google.com')
     */
    public static final String SHOW_SHARETO_FB          = "shareToFB";

    /**
     * Share url to twitter directly.
     *
     * Parameter:- string - The string should be shared
     *
     * Example: mig33:shareTo('some words http://www.google.com')
     */
    public static final String SHOW_SHARETO_TW          = "shareToTW";

    /**
     * Share url to migme chat.
     *
     * Parameter:- string - The string should be shared
     *
     * Example: mig33:shareTo('some words http://www.google.com')
     */
    public static final String SHOW_SHARETO_MIGME       = "shareToMigme";

    /**
     * Shows the migstore gift page.
     * 
     * -> mig33:sendGift() -> opens mig store (since v4.03.003)
     * -> mig33:sendGift('recipient') -> opens mig store with initial recipient
     * -> mig33:sendGift('recipient, giftId') -> (since v4.03.003)
     * 
     * Parameters:
     * - username - The name of the user to whom a gift must be sent.
     * - giftId - The id of the gift item 
     * 
     * Example: mig33:sendGift('angelorohit').
     */
    public static final String SEND_GIFT              = "sendGift";

    /**
     * Shows the fans page in the discover pager.
     * 
     * Parameters:- none
     * 
     * Example: mig33:showFollowers()
     */
    public static final String SHOW_FOLLOWERS         = "showFollowers";

    /**
     * Shows the badges page of the currently logged in user in the profile
     * pager.
     * 
     * Parameters:- none
     * 
     * Example: mig33:showBadges()
     */
    public static final String SHOW_BADGES            = "showBadges";

    /**
     * Shows the invite friends page.
     * 
     * Parameters:- none
     * 
     * Example: mig33:showInviteFriends()
     */
    public static final String SHOW_INVITE_FRIENDS    = "showInviteFriends";

    /**
     * go to the game page.
     *
     * Parameters:- none
     *
     * Example: mig33:GoGamePage()
     */
    public static final String GO_GAMEPAGE    = "goGamePage";
}
