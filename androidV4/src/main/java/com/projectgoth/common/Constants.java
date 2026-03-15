/**
 * Copyright (c) 2013 Project Goth
 *
 * Constants.java.java
 * Created May 30, 2013, 5:36:14 PM
 */

package com.projectgoth.common;

import com.projectgoth.common.YoutubeUri.ThumbnailQuality;

/**
 * @author cherryv
 * 
 */
public class Constants {

    public static final String           BLANKSTR                                     = "";
    public static final String           SPACESTR                                     = " ";
    public static final String           SLASHSTR                                     = "/";
    public static final String           PLUSSTR                                      = "+";
    public static final String           MINUSSTR                                     = "-";
    public static final String           DOTSTR                                       = ".";
    public static final String           COLON                                        = ":";
    public static final String           ELLIPSIS                                     = "...";
    public static final String           ELLIPSIS_MORE                                = "More";
    public static final String           UNDERSCORE                                   = "_";
    public static final String           NEWLINE                                      = System.getProperty("line.separator");

    public static final String           PROTOCOL_MARK                                = "://";
    public static final String           LINK_HTTP                                    = "http://";
    public static final String           LINK_HTTPS                                   = "https://";
    public static final String           LINK_FILE                                    = "file://";
    public static final String           LINK_MIG33                                   = "mig33:";
    public static final String           LINK_DRAWABLE                                = "file://drawable/";
    
    public static final String           DL_USER                                      = "user";
    public static final String           DL_POST                                      = "post";
    public static final String           DL_MUSIC                                     = "music";
    public static final String           DL_CHATROOM                                  = "chatroom";

    // TODO: place this default setting on the Config file
    public static final String           DEFAULT_ENCODING                             = "UTF-8";

    // default language
    public static final String           DEFAULT_LANGUAGE                             = "en-US";

    public static final String           PATH_ASSETS_TEMPLATE                         = "template";
    public static final String           PATH_ASSETS_THEME                            = "theme";
    public static final String           PATH_ASSETS_LANGUAGE                         = "lang";
    public static final String           PATH_ASSETS_VAS                              = "vas";
    public static final String           PATH_ASSETS_CONFIG                           = "config";

    public static final int              DEFAULT_PHOTO_QUALITY                        = 90;

    // Quality of compression to be used when saving a photo to external
    // storage.
    public static final int              SAVE_PHOTO_QUALITY                           = 100;

    // Format used when naming a photo that is saved to external storage.
    public static final String           PHOTO_DATE_FORMAT                            = "yyyyMMdd_HHmmss";

    /** Default Photo Size... also the max size a photo can be resized */
    public static final int              DEFAULT_PHOTO_SIZE                           = 512;

    public static final int              REQUESTED_DISPLAY_PICTURE_SIZE               = 72;
    public static final int              REQUESTED_MIGLEVEL_ICON_SIZE                 = 42;

    public static final int              FULL_BODY_AVATAR_WIDTH                       = 144;
    public static final int              FULL_BODY_AVATAR_HEIGHT                      = FULL_BODY_AVATAR_WIDTH * 2;

    public static final int              BADGES_SIZE_LARGE                            = 96;
    public static final int              DEFAULT_CHAT_THUMB_SIZE                      = 100;
    public static final long             CHAT_CONV_TIMESTAMP_DISPLAY                  = 60 * 1000;

    public static final int              MAX_COUNT_DISPLAY_FOLLOW                     = 9999;
    public static final int              MAX_COUNT_DISPLAY_REPLIES                    = 999;
    public static final int              MAX_COUNT_DISPLAY_NOTIFICATIONS              = 99;
    public static final int              MAX_COUNT_DISPLAY_PROFILE                    = 999;

    public static final String           FORMAT_DATE_TIME                             = "MMM-dd-yyyy HH:mm";
    public static final String           FORMAT_SHORT_DATE                            = "MMM-dd";
    public static final String           FORMAT_TIME_12                               = "hh:mm aa";
    public static final String           FORMAT_TIME                                  = "HH:mm";

    public static final int              MAX_LINK_LENGTH                              = 20;
    public static final int              MAX_MESSAGE_LENGTH                           = 300;
    public static final int              MAX_LONG_POST_MESSAGE_LENGTH                 = 5000;

    public static final String           URL_REGEX                                    = "\\b(?:https?://|www\\.)(?:(?:[a-z0-9](?:[a-z0-9_-]*[a-z0-9])?\\.)+[a-z]{2,4}|(?:[0-9]+\\.){3}[0-9]+)(?::[0-9]{1,4})?(?:[?/][\\.#=%&a-zA-Z0-9_-]*)*";
    public static final String           HASHTAG_REGEX                                = "#[\\w]+";
    public static final String           MENTIONS_REGEX                               = "@[.\\w-]+";
    public static final String           CHATROOM_LIST_REGEX                          = "[\\+][\\[]([.\\w\\s-]+)[\\]]";
    public static final String           DAY_OF_BIRTH_MISSINGYEAR_REGEX               = "^\\d{2}-\\d{2}$";
    public static final String           YOUTUBE_URL_REGEX                            = "(?:youtube\\.com/(?:[^/]+/.+/|(?:v|e(?:mbed)?)/|.*[?&]v=)|youtu\\.be/)([^\"&?/ ]{11})";

    public static final String           MENTIONS_TAG                                 = "@";
    public static final String           HASH_TAG                                     = "#";

    public static final ThumbnailQuality DEFAULT_YOUTUBE_QUALITY                      = YoutubeUri.ThumbnailQuality.HIGH;

    public static final String           BADGES_PATH                                  = "/badges/%dx%d/%s";

    public static final String           PAINTWARS_EMOTICON_HOTKEY                    = "(paintwars-paintemoticon)";

    public static final int              REQ_PIC_FROM_CAMERA_FOR_CHAT_MSG             = 0;
    public static final int              REQ_PIC_FROM_GALLERY_FOR_CHAT_MSG            = 1;
    public static final int              REQ_PIC_FROM_CAMERA_FOR_POST                 = 2;
    public static final int              REQ_PIC_FROM_GALLERY_FOR_POST                = 3;
    public static final int              REQ_PIC_FROM_CAMERA_FOR_PHOTO_ALBUM          = 4;
    public static final int              REQ_PIC_FROM_GALLERY_FOR_PHOTO_ALBUM         = 5;
    public static final int              REQ_PIC_FROM_CAMERA_FOR_DISPLAY_PIC          = 6;
    public static final int              REQ_PIC_FROM_GALLERY_FOR_DISPLAY_PIC         = 7;
    public static final int              REQ_SHOW_GIFT_CENTER_FROM_CHAT               = 8;
    public static final int              REQ_CROP_IMAGE_FOR_CAMERA_IMAGE              = 9;
    public static final int              REQ_CROP_IMAGE_FOR_GALLERY_IMAGE             = 10;

    public static final int              RESULT_FROM_GIFT_CENTER_SHOW_GROUP           = 101;
    public static final int              RESULT_FROM_GIFT_CENTER_SHOW_STORE           = 102;
    public static final int              RESULT_FROM_GIFT_CENTER_SHOW_RECHARGE        = 103;

    public static final int              ACCUMULATED_FOLLOWERS_STATUS_NOTIFICATION_ID = 99997;
    public static final int              CHAT_STATUS_NOTIFICATION_ID                  = 99998;
    public static final int              MIG_ALERTS_STATUS_NOTIFICATION_ID            = 99999;

    public static final int              NUE_TOOLTIP_DELAY                            = 500;

    public static final float            IMAGE_BUTTON_UNCLICK_ALPHA                   = 0.25f;

}
