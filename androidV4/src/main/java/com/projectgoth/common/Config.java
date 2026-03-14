/**
 * Mig33 Pte. Ltd.
 *
 * Copyright (c) 2012 mig33. All rights reserved.
 */

package com.projectgoth.common;

import android.app.AlarmManager;
import android.app.Application;
import android.content.pm.ApplicationInfo;
import com.projectgoth.BuildConfig;
import com.projectgoth.common.ConnectionDetail.Type;
import com.projectgoth.datastore.SystemDatastore;
import com.projectgoth.fusion.packet.UrlHandler;

/**
 * Config.java
 * 
 * @author warrenbalcos on Jun 5, 2013
 * 
 */
public class Config {

    private static final String CONN_DETAIL_TYPE_KEY    = "CONN_DETAIL_TYPE_KEY";

    private static final String CONN_DETAIL_GATEWAY_KEY = "CONN_DETAIL_GATEWAY_KEY";

    private static final String CONN_DETAIL_PORT_KEY    = "CONN_DETAIL_PORT_KEY";

    private static boolean      debug;

    private boolean             isTrafficStatsEnabled;
    
    private boolean             isImEnabled;
    
    private boolean             isConnectionSelectorEnabled;
    
    private boolean             isLocationInPostEnabled;

    private boolean             enableFeedsDownload;

    private int                 screenWidth;

    private int                 screenHeight;

    private float               screenScale;

    private float               fontScale;
    
    private int                 softKeyboardHeight;
    
    private int                 requestedEmoticonDimension;

    private int                 displayPicSizeSmall;
    private int                 displayPicSizeNormal;
    private int                 displayPicSizeLarge;
    
    private long                serviceAlarmDelay;

    private boolean             isChatSyncEnabled;
    
    private boolean             isFiksuEnabled = false;
    
    private boolean             isCrashlyticsEnabled;
    
    private boolean 			isGoogleAnalyticsEnabled = false;
    
    private boolean             isMyGiftsEnabled;

    private int                 syncMessagesRequestLimit;
    private int                 syncGroupChatMsgRequestLimit;
    private int                 messageGapRequestLimit;
    private int                 msgReqLimitForNewChat;
    
    private boolean             enableEmoticonJsonData;
    
    private ConnectionDetail    connectionDetail;

    private static final Config config                  = new Config();

    private Config() {
        loadDefaultConfig();
    }

    public static synchronized Config getInstance() {
        return config;
    }

    public void loadDefaultConfig() {
        setTrafficStatsEnabled(DefaultConfig.ENABLE_TRAFFIC_STATS);
        setEnableFeedsDownload(DefaultConfig.ENABLE_FEEDS_DOWNLOAD);
        setScreenScale(DefaultConfig.SCREEN_SCALE);
        setFontScale(DefaultConfig.FONT_SCALE);
        setRequestedEmoticonDimension(DefaultConfig.REQUESTED_EMOTICON_DIMENSION);
        setServiceAlarmDelay(AlarmManager.INTERVAL_FIFTEEN_MINUTES);
        setImEnabled(DefaultConfig.ENABLE_THIRDPARTY_IM);
        setLocationInPostEnabled(DefaultConfig.ENABLE_LOCATION_IN_POST);

        setChatSyncMessagesRequestLimit(DefaultConfig.GET_SYNC_MESSAGES_LIMIT);
        setChatSyncGroupChatMsgRequestLimit(DefaultConfig.GET_SYNC_GROUP_CHAT_MSG_LIMIT);
        setMessageGapRequestLimit(DefaultConfig.MESSAGE_GAP_REQUEST_LIMIT);
        setMsgReqForNewChatLimit(DefaultConfig.MSG_REQ_LIMIT_FOR_NEW_CHAT);
        setEnableEmoticonJsonData(DefaultConfig.ENABLE_EMOTICON_DATA_AS_JSON);
        setMyGiftsEnabled(DefaultConfig.ENABLE_MY_GIFTS);
    }

    public static void initialize(Application app) {
        debug = (0 != (app.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
    }

    /**
     * @return the debug
     */
    public static boolean isDebug() {
        return debug;
    }

    /**
     * @return the isTrafficStatsEnabled
     */
    public boolean isTrafficStatsEnabled() {
        return isTrafficStatsEnabled;
    }

    /**
     * @param isTrafficStatsEnabled
     *            the isTrafficStatsEnabled to set
     */
    public void setTrafficStatsEnabled(boolean isTrafficStatsEnabled) {
        this.isTrafficStatsEnabled = isTrafficStatsEnabled;
    }

    /**
     * @return the enableFeedsDownload
     */
    public boolean getEnableFeedsDownload() {
        return enableFeedsDownload;
    }

    /**
     * @param enableFeedsDownload
     *            the enableFeedsDownload to set
     */
    public void setEnableFeedsDownload(boolean enableFeedsDownload) {
        this.enableFeedsDownload = enableFeedsDownload;
    }

    /**
     * @return the fontScale
     */
    public float getFontScale() {
        return fontScale;
    }

    /**
     * @param fontScale
     *            the fontScale to set
     */
    public void setFontScale(float fontScale) {
        this.fontScale = fontScale;
    }

    /**
     * @return the screenScale
     */
    public float getScreenScale() {
        return screenScale;
    }

    /**
     * @param screenScale
     *            the screenScale to set
     */
    public void setScreenScale(float screenScale) {
        this.screenScale = screenScale;
    }

    /**
     * @return the screenWidth
     */
    public int getScreenWidth() {
        return screenWidth;
    }

    /**
     * @param screenWidth
     *            the screenWidth to set
     */
    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    /**
     * @return the screenHeight
     */
    public int getScreenHeight() {
        return screenHeight;
    }

    /**
     * @param screenHeight
     *            the screenHeight to set
     */
    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    
    public int getDisplayPicSizeSmall() {
        return displayPicSizeSmall;
    }

    
    public void setDisplayPicSizeSmall(int displayPicSizeSmall) {
        this.displayPicSizeSmall = displayPicSizeSmall;
    }

    
    public int getDisplayPicSizeNormal() {
        return displayPicSizeNormal;
    }

    
    public void setDisplayPicSizeNormal(int displayPicSizeNormal) {
        this.displayPicSizeNormal = displayPicSizeNormal;
    }

    
    public int getDisplayPicSizeLarge() {
        return displayPicSizeLarge;
    }

    
    public void setDisplayPicSizeLarge(int displayPicSizeLarge) {
        this.displayPicSizeLarge = displayPicSizeLarge;
    }
    
    /**
     * @return the requestedEmoticonDimension
     */
    public int getRequestedEmoticonDimension() {
        return requestedEmoticonDimension;
    }

    /**
     * @param requestedEmoticonDimension
     *            the requestedEmoticonDimension to set
     */
    public void setRequestedEmoticonDimension(int requestedEmoticonDimension) {
        this.requestedEmoticonDimension = requestedEmoticonDimension;
    }

    /**
     * @return the serviceAlarmDelay
     */
    public long getServiceAlarmDelay() {
        return serviceAlarmDelay;
    }

    /**
     * @param serviceAlarmDelay
     *            the serviceAlarmDelay to set
     */
    public void setServiceAlarmDelay(long serviceAlarmDelay) {
        this.serviceAlarmDelay = serviceAlarmDelay;
    }

    /**
     * @return the isImEnabled
     */
    public boolean isImEnabled() {
        return isImEnabled;
    }

    /**
     * @param isImEnabled
     *            the isImEnabled to set
     */
    public void setImEnabled(boolean isImEnabled) {
        this.isImEnabled = isImEnabled;
    }
    
    /**
     */
    public boolean isConnectionSelectorEnabled() {
        return this.isConnectionSelectorEnabled;
    }
    
    /**
     */
    public void setConnectionSelectorEnabled(boolean isConnectionSelectorEnabled) {
        this.isConnectionSelectorEnabled = isConnectionSelectorEnabled;
    }
    
    public boolean isLocationInPostEnabled() {
        return this.isLocationInPostEnabled;
    }
    
    public void setLocationInPostEnabled(boolean isEnabled) {
        this.isLocationInPostEnabled = isEnabled;
    }

    /**
     * @return the isChatSyncEnabled
     */
    public boolean isChatSyncEnabled() {
        return isChatSyncEnabled;
    }

    /**
     * @param isChatSyncEnabled
     *            the isChatSyncEnabled to set
     */
    public void setChatSyncEnabled(boolean isChatSyncEnabled) {
        this.isChatSyncEnabled = isChatSyncEnabled;
    }
    
    /**
     * @return the isFiksuEnabled
     */
    @SuppressWarnings("unused")
    public boolean isFiksuEnabled() {
        //disable fiksu seems we don't use it now
        return false;
    }

    /**
     * @param isFiksuEnabled
     *            the isFiksuEnabled to set
     */
    public void setFiksuEnabled(boolean isFiksuEnabled) {
        this.isFiksuEnabled = isFiksuEnabled;
    }    
    
    /**
     * @return the isCrashlyticsEnabled
     */
    public boolean isCrashlyticsEnabled() {
        return isCrashlyticsEnabled;
    }

    /**
     * @param isCrashlyticsEnabled the isCrashlyticsEnabled to set
     */
    public void setCrashlyticsEnabled(boolean isCrashlyticsEnabled) {
        this.isCrashlyticsEnabled = isCrashlyticsEnabled;
    }
    
    public boolean isGoogleAnalyticsEnabled() {
    	return isGoogleAnalyticsEnabled;
    }
    
    public void setGoogleAnalyticsEnabled(boolean isGoogleAnalyticsEnabled) {
    	this.isGoogleAnalyticsEnabled = isGoogleAnalyticsEnabled;
    }

    public int getChatSyncMessagesRequestLimit() {
        return syncMessagesRequestLimit;
    }

    
    public void setChatSyncMessagesRequestLimit(int syncMessagesRequestLimit) {
        this.syncMessagesRequestLimit = syncMessagesRequestLimit;
    }

    
    public int getChatSyncGroupChatMsgRequestLimit() {
        return syncGroupChatMsgRequestLimit;
    }

    
    public void setChatSyncGroupChatMsgRequestLimit(int syncGroupChatMsgRequestLimit) {
        this.syncGroupChatMsgRequestLimit = syncGroupChatMsgRequestLimit;
    }

    
    public int getMessageGapRequestLimit() {
        return messageGapRequestLimit;
    }

    
    public void setMessageGapRequestLimit(int messageGapRequestLimit) {
        this.messageGapRequestLimit = messageGapRequestLimit;
    }
    
    public int getMsgReqLimitForNewChat() {
        return msgReqLimitForNewChat;
    }

    public void setMsgReqForNewChatLimit(int msgReqLimitForNewChat) {
        this.msgReqLimitForNewChat = msgReqLimitForNewChat;
    }
    
    /**
     * @return the connectionDetail
     */
    public ConnectionDetail getConnectionDetail() {
        if (connectionDetail == null) {
            SystemDatastore sys = SystemDatastore.getInstance();
            Type type = Type.fromValue(sys.getStringData(CONN_DETAIL_TYPE_KEY));
            connectionDetail = new ConnectionDetail(type);
            if (type == Type.CUSTOM) {
                connectionDetail.setGateway(sys.getStringData(CONN_DETAIL_GATEWAY_KEY));
                connectionDetail.setPort(sys.getIntegerData(CONN_DETAIL_PORT_KEY));
            }
        }
        return connectionDetail;
    }

    /**
     * @param connectionDetail
     *            the connectionDetail to set
     */
    public void setConnectionDetail(ConnectionDetail connectionDetail) {
        if (connectionDetail != null) {
            SystemDatastore sys = SystemDatastore.getInstance();
            sys.saveData(CONN_DETAIL_TYPE_KEY, connectionDetail.getType().name());
            
            if (connectionDetail.getType() == Type.CUSTOM) {
                sys.saveData(CONN_DETAIL_GATEWAY_KEY, connectionDetail.getGateway());
                sys.saveData(CONN_DETAIL_PORT_KEY, connectionDetail.getPort());
            }
            
            UrlHandler.getInstance().clearCachedUrls();
            this.connectionDetail = connectionDetail;
        }
    }
    
    public int getSoftKeyboardHeight() {
        return softKeyboardHeight;
    }

    public void setSoftKeyboardHeight(int softKeyboardHeight) {
        this.softKeyboardHeight = softKeyboardHeight;
    }

    /**
     * @return the enableEmoticonJsonData
     */
    public boolean isEnableEmoticonJsonData() {
        return enableEmoticonJsonData;
    }

    /**
     * @param enableEmoticonJsonData the enableEmoticonJsonData to set
     */
    public void setEnableEmoticonJsonData(boolean enableEmoticonJsonData) {
        this.enableEmoticonJsonData = enableEmoticonJsonData;
    }

    
    /**
     * @return the isMyGiftsEnabled
     */
    public boolean isMyGiftsEnabled() {
        return isMyGiftsEnabled;
    }

    
    /**
     * @param isMyGiftsEnabled the isMyGiftsEnabled to set
     */
    public void setMyGiftsEnabled(boolean isMyGiftsEnabled) {
        this.isMyGiftsEnabled = isMyGiftsEnabled;
    }

}
