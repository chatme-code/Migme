/**
 * Mig33 Pte. Ltd.
 *
 * Copyright (c) 2012 mig33. All rights reserved.
 */

package com.projectgoth.common;

import com.projectgoth.blackhole.enums.ClientType;
import com.projectgoth.common.ConnectionDetail.Type;

/**
 * DefaultConfig.java
 * 
 * @author warrenbalcos on Jun 5, 2013
 * 
 */
public class DefaultConfig {

    /**
     * Major build id. need to set this when starting new major version
     * 
     * This is decided by product management
     */
    public static final String                VERSION_MAJOR                 = "5";

    /**
     * Minor build id. need to set this for minor releases
     * 
     * This is decided by product management
     */
    public static final String                VERSION_MINOR                 = "01";

    /**
     * Default patch number. set when releasing patches
     */
    public static final String                BUILD_PATCH                   = "016";

    /**
     * Default Server Connection
     */
    public static final ConnectionDetail.Type DEFAULT_CONNECTION            = Type.PROD;

    /**
     * Client platform for android
     */
    public static final String                PLATFORM                      = "android";

    public static final boolean               ENABLE_TRAFFIC_STATS          = false;

    public static final boolean               ENABLE_THIRDPARTY_IM          = false;

    public final static boolean               ENABLE_FEEDS_DOWNLOAD         = false;
    
    public final static boolean               ENABLE_EMOTICON_DATA_AS_JSON  = true;

    public final static boolean               ENABLE_MY_GIFTS               = true;

    public final static String                LANGUAGE                      = "en-US";

    public final static ClientType            CLIENT_TYPE                   = ClientType.ANDROID;

    public static final float                 SCREEN_SCALE                  = 1;

    public static final float                 FONT_SCALE                    = 1;

    public static final int                   REQUESTED_EMOTICON_DIMENSION  = 24;
    
    public static final boolean               ENABLE_LOCATION_IN_POST       = true;

    public static final int                   MAX_DISPLAY_PIC_SIZE_NORMAL   = 96;
    public static final int                   MAX_DISPLAY_PIC_SIZE_LARGE    = 112;

    /**
     * CHAT SYNC DEFAULTS
     */
    public static final int                   GET_SYNC_MESSAGES_LIMIT       = 20; 
    public static final int                   GET_SYNC_GROUP_CHAT_MSG_LIMIT = 20;
    public static final int                   MESSAGE_GAP_REQUEST_LIMIT     = 3;
    public static final int                   MSG_REQ_LIMIT_FOR_NEW_CHAT    = 1;

    /**
     * Database version number
     */
    public static final int                   VERSION_WITH_STICKER_SUPPORT      = 33;
    public static final int                   VERSION_WITH_JSON_IMPLEMENTAION   = 34;
    public static final int                   VERSION_WITH_FAILED_MESSAGE_TABLE = 35;


}
