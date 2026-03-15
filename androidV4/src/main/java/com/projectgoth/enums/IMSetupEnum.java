/**
 * Copyright (c) 2013 Project Goth
 *
 * IMIconEnum.java
 * Created Nov 26, 2013, 3:13:31 PM
 */

package com.projectgoth.enums;

import com.projectgoth.R;
import com.projectgoth.blackhole.enums.ImType;


/**
 * @author sarmadsangi
 *
 */
public enum IMSetupEnum {       
    FACEBOOK(R.drawable.ic_im_facebook, R.drawable.ic_im_facebook_offline, R.id.action_sign_in_off_facebook, ImType.FACEBOOK),
    GTALK(R.drawable.ic_im_gtalk, R.drawable.ic_im_gtalk_offline, R.id.action_sign_in_off_gtalk, ImType.GTALK),
    MSN(R.drawable.ic_im_msn, R.drawable.ic_im_msn_offline, R.id.action_sign_in_off_msn, ImType.MSN),
    YAHOO(R.drawable.ic_im_yahoo, R.drawable.ic_im_yahoo_offline, R.id.action_sign_in_off_yahoo, ImType.YAHOO);
    
    private int iconOnline;
    private int iconOffline;
    private int actionId;
    private ImType imType;
    
    private IMSetupEnum(int iconOnline, int iconOffline, int actionId, ImType imType) {
        this.iconOnline = iconOnline;
        this.iconOffline = iconOffline;
        this.actionId = actionId;
        this.imType = imType;
    }
    
    public int getIconOnline() {
        return iconOnline;
    }
   
    public void setIconOnline(int iconOnline) {
        this.iconOnline = iconOnline;
    }
    
    public int getIconOffline() {
        return iconOffline;
    }
    
    public void setIconOffline(int iconOffline) {
        this.iconOffline = iconOffline;
    }
    
    public ImType getImType() {
        return imType;
    }
    
    public void setImType(ImType imType) {
        this.imType = imType;
    }
    
    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    public static IMSetupEnum fromImTypeValue(ImType imType) {
        for (IMSetupEnum imIcon : IMSetupEnum.values()) {
            if(imIcon.imType == imType) {
                return imIcon;
            }
        }           
        return null;
    }
}
