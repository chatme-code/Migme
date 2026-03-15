/**
 * Copyright (c) 2013 Project Goth
 *
 * WebURL.java.java
 * Created Jun 24, 2013, 3:41:44 PM
 */

package com.projectgoth.common;

/**
 * @author cherryv
 * 
 */
public class WebURL {
    
    public static final String URL_PARAM_SHOW_NO_HEADER       = "&show_header=0";

    public static final String URL_REGISTRATION               = "/sites/touch/registration/register";
    public static final String URL_FORGOT_PASSWORD            = "/sites/touch/forgot_password/details";

    public static final String URL_SOCIAL_SPACE               = "/sites/touch/socialspace/home";
    public static final String URL_MIGWORLD                   = "/discover/migworld";
    public static final String URL_MIGSTORE                   = "/sites/touch/store/home_touch";
    public static final String URL_LEADERBOARD                = "/sites/touch/leaderboard/home";
    public static final String URL_ACCOUNT_SETTINGS           = "/sites/touch/account/home";
    public static final String URL_MERCHANT                   = "/sites/touch/merchant/dashboard";

    public static final String URL_CREATE_ROOM                = "/sites/touch/chatroom/create";
    public static final String URL_CHATROOM_SEARCH            = "/sites/touch/chatroom/search_submit?search_keywords=%s&search=%s";
    public static final String URL_CHATROOM_INFO              = "/sites/touch/chatroom/setup?roomName=%s";

    public static final String URL_SEARCH_GROUP               = "/sites/touch/group/search?name=%s";

    public static final String URL_INVITEFRIEND               = "/sites/touch/invite/refer_friend";
    public static final String URL_INVITE_VIA_FACEBOOK        = "/sites/touch/invite_api/redirect_invite_facebook";
    public static final String URL_INVITE_VIA_EMAIL           = "/sites/touch/invite/refer_friend_email";
    public static final String URL_INVITE_VIA_EMAIL_SUBMIT    = "/sites/touch/invite/refer_friend_email_submit?";

    public static final String URL_BUY_EMOTICON               = "/sites/touch/store/show_with_category_touch?ty=3&catid=3";
    public static final String URL_BUY_STICKERS               = "/sites/touch/store/show_with_category_touch?ty=6&catid=6";
    public static final String URL_BUY_AVATAR                 = "/sites/touch/store/show_with_category_touch?ty=2&catid=2";
    public static final String URL_RECHARGE_CREDITS           = "/sites/touch/account/recharge_credit";

    public static final String URL_SEND_FROM_PHOTO            = "/sites/touch/photo/home/sendfromphoto";
    public static final String URL_SEND_GIFT                  = "/sites/touch/store/show_with_category_touch?ty=1&catid=1&username=%s";
    public static final String URL_TRANSFER_CREDITS           = "/sites/touch/account/transfer_credit?recipient_username=%s";

    public static final String URL_REPORT_USER                = "/sites/touch/report/report_abuse?type=2&offender=%s";
    public static final String URL_REPORT_GROUPCHAT           = "/sites/touch/report/report_abuse?type=4&group_chatid=%s";
    public static final String URL_REPORT_CHATROOM            = "/sites/touch/report/report_abuse?type=3&chatroom_name=%s";

    public static final String URL_MY_GROUPS                  = "/sites/touch/group/list_joined_groups?username=%s";
    public static final String URL_GIFTS_RECEIVED             = "/sites/touch/profile/gifts_received?username=%s";
    public static final String URL_GROUPS_LIST                = "/sites/touch/group/list_my_groups?username=%s";
    public static final String URL_FOOTPRINTS                 = "/sites/touch/profile/footprints?username=%s";
    public static final String URL_USER_OWNED_CHATROOMS       = "/sites/touch/chatroom/user_owned_chatrooms?username=%s";
    public static final String URL_GAMES_PLAYED               = "/sites/touch/profile/list_games?username=%s";
    public static final String URL_PHOTOS                     = "/sites/touch/photo/home?username=%s";

    public static final String URL_GAMES_LIST                 = "/sites/touch/bot/bot_list?e=100";

    public static final String URL_THIRD_PARTY_SITES_SETTINGS = "/sites/touch/settings/updates_posts";

    public static final String URL_GROUPS_INVITE              = "/sites/touch/group/invite_contact?cid=%s";
    public static final String URL_GROUPS_CHATROOMS           = "/sites/touch/group/list_chatrooms?cid=%s&e=10";
    public static final String URL_GROUPS_MEMBERS             = "/sites/touch/group/members?cid=%s&e=10";
    public static final String URL_GROUPS_REPORT_ABUSE        = "/sites/touch/report/report_abuse?ty=5&gn=%s&cid=%s";
    public static final String URL_GROUPS_SETTINGS            = "/sites/touch/group/setting?cid=%s";
    public static final String URL_GROUPS_JOIN_REQUESTS       = "/sites/touch/group/join_requests?cid=%s";
    public static final String URL_GROUPS_SEARCH              = "/sites/touch/group/search";

    // game client id
    public static final String URL_GROUPS_PLAY                = "/sites/touch/opensocial/start_app?appid=%s";
    public static final String URL_BUYCREDIT                  = "/sites/touch/account/recharge_credit";
    public static final String URL_EDITPROFILE                = "/sites/touch/profile/edit";

    public static final String URL_SETTINGS_ABOUTMIG33        = "/sites/touch/settings/about";
    public static final String URL_SETTINGS_PRIVACY           = "/sites/touch/settings/privacy";
    public static final String URL_SETTINGS_ACCOUNT           = "/sites/touch/settings/account";
    public static final String URL_SETTINGS_SERVICES          = "/sites/touch/settings/services";

    public static final String URL_PHOTO_VIEW                 = "/sites/touch/photo/view_photo?imgid=%s";
    public static final String URL_PHOTO_RECEIVE_CONFIRMATION = "/sites/touch/photo/received?original=1&nid=%s&sender=%s";

    public static final String URL_MY_AVATAR                  = "/sites/touch/avatar/home";

    public static final String URL_IM_SETUP                   = "/sites/touch/im/setup_im?&im=%s";

    public static final String URL_GOOGLE_PLAY_PHOTOS         = "https://play.google.com/store/apps/details?id=com.google.android.apps.photos";
}
