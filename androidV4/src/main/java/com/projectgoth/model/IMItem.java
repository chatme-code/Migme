/**
 * Copyright (c) 2013 Project Goth
 *
 * IMItem.java
 * Created Nov 13, 2013, 11:41:12 AM
 */

package com.projectgoth.model;

import android.graphics.Bitmap;
import com.projectgoth.blackhole.enums.ImDetailType;
import com.projectgoth.blackhole.enums.ImType;
import com.projectgoth.blackhole.enums.MessageType;
import com.projectgoth.blackhole.fusion.packet.FusionPktImAvailable;


/**
 * @author sarmadsangi
 *
 */
public class IMItem {
    
    private boolean      groupChatEnabled;
    private String       name;
    private ImType       type;
    private MessageType  messagetype;
    private ImDetailType detail;
    
    private Bitmap       bmpOnline;
    private Bitmap       bmpAway;
    private Bitmap       bmpBusy;
    private Bitmap       bmpOffline;

    public IMItem(String name, ImType type, MessageType messagetype, ImDetailType detail, boolean isgroupsenabled) {
        this.name = name;
        this.type = type;
        this.messagetype = messagetype;
        this.detail = detail;
        this.groupChatEnabled = isgroupsenabled;
    }
    
    public IMItem(FusionPktImAvailable imAvailable) {
        this.name = imAvailable.getName();
        this.type = imAvailable.getImType();
        this.messagetype = imAvailable.getMessageType();
        this.detail = imAvailable.getImDetail();
        this.groupChatEnabled = imAvailable.getSupportsGroupChat();
    }

    public int describeContents() {
        return 0;
    }

    public boolean isIMLoggedIn(){
        return detail == ImDetailType.CONNECTED;
    }

    
    /**
     * @return the groupChatEnabled
     */
    public boolean getGroupChatEnabled() {
        return groupChatEnabled;
    }

    
    /**
     * @param groupChatEnabled the groupChatEnabled to set
     */
    public void setGroupChatEnabled(boolean groupChatEnabled) {
        this.groupChatEnabled = groupChatEnabled;
    }

    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }


    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * @return the type
     */
    public ImType getType() {
        return type;
    }

    
    /**
     * @param type the type to set
     */
    public void setType(ImType type) {
        this.type = type;
    }

    
    /**
     * @return the messagetype
     */
    public MessageType getMessagetype() {
        return messagetype;
    }

    
    /**
     * @param messagetype the messagetype to set
     */
    public void setMessagetype(MessageType messagetype) {
        this.messagetype = messagetype;
    }

    
    /**
     * @return the detail
     */
    public ImDetailType getDetail() {
        return detail;
    }

    
    /**
     * @param detail the detail to set
     */
    public void setDetail(ImDetailType detail) {
        this.detail = detail;
    }
    
    
    public boolean isOnline() {
        return detail == ImDetailType.CONNECTED;
    }
    
    
    public boolean isConfigured() {
        return detail == ImDetailType.CONNECTED || detail == ImDetailType.DISCONNECTED;
    }

    /**
     * @param online
     * @param away
     * @param busy
     * @param offline
     */
    public void setPresenceBmp(Bitmap bmpOnline, Bitmap bmpAway, Bitmap bmpBusy, Bitmap bmpOffline) {
        this.bmpOnline = bmpOnline;
        this.bmpAway = bmpAway;
        this.bmpBusy = bmpBusy;
        this.bmpOffline = bmpOffline;
    }
 
    public Bitmap getBmpOnline() {
        return bmpOnline;
    }

    public Bitmap getBmpAway() {
        return bmpAway;
    }

    public Bitmap getBmpBusy() {
        return bmpBusy;
    }

    public Bitmap getBmpOffline() {
        return bmpOffline;
    }

}
