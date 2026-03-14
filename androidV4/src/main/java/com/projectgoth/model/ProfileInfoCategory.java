/**
 * Copyright (c) 2013 Project Goth
 *
 * ProfileInfoCategory.java
 * Created Sep 29, 2014, 5:21:20 PM
 */

package com.projectgoth.model;

/**
 * @author mapet
 * 
 */
public class ProfileInfoCategory {

    private String label;
    private String details;
    private String status;
    private int    action;
    private int    unreadCount;

    private int    giftCount;
    private int    badgeCount;
    private int    fanCount;
    private int    fanOfCount;

    private Type   type = Type.Info;

    public enum Type {
        Info, Main
    }

    public ProfileInfoCategory(String label, int action) {
        this(label, null, null, action);
    }

    public ProfileInfoCategory(String label, String details, String status, int action) {
        super();
        this.label = label;
        this.details = details;
        this.status = status;
        this.action = action;
    }

    public ProfileInfoCategory(int giftCount, int badgeCount, int fanCount, int fanOfCount) {
        super();
        this.giftCount = giftCount;
        this.badgeCount = badgeCount;
        this.fanCount = fanCount;
        this.fanOfCount = fanOfCount;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label
     *            the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the details
     */
    public String getDetails() {
        return details;
    }

    /**
     * @param details
     *            the details to set
     */
    public void setDetails(String details) {
        this.details = details;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the action
     */
    public int getAction() {
        return action;
    }

    /**
     * @param action
     *            the action to set
     */
    public void setAction(int action) {
        this.action = action;
    }

    /**
     * @return the unreadCount
     */
    public int getUnreadCount() {
        return unreadCount;
    }

    /**
     * @param unreadCount
     *            the unreadCount to set
     */
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    /**
     * @return the giftCount
     */
    public int getGiftCount() {
        return giftCount;
    }

    /**
     * @param giftCount
     *            the giftCount to set
     */
    public void setGiftCount(int giftCount) {
        this.giftCount = giftCount;
    }

    /**
     * @return the badgeCount
     */
    public int getBadgeCount() {
        return badgeCount;
    }

    /**
     * @param badgeCount
     *            the badgeCount to set
     */
    public void setBadgeCount(int badgeCount) {
        this.badgeCount = badgeCount;
    }

    /**
     * @return the fanCount
     */
    public int getFanCount() {
        return fanCount;
    }

    /**
     * @param fanCount
     *            the fanCount to set
     */
    public void setFanCount(int fanCount) {
        this.fanCount = fanCount;
    }

    /**
     * @return the fanOfCount
     */
    public int getFanOfCount() {
        return fanOfCount;
    }

    /**
     * @param fanOfCount
     *            the fanOfCount to set
     */
    public void setFanOfCount(int fanOfCount) {
        this.fanOfCount = fanOfCount;
    }

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(Type type) {
        this.type = type;
    }
    
}
