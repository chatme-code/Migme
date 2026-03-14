/**
 * Copyright (c) 2013 Project Goth
 *
 * ThirdPartyIMController.java
 * Created Nov 6, 2013, 5:18:55 PM
 */

package com.projectgoth.controller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.blackhole.enums.ImType;
import com.projectgoth.blackhole.enums.PresenceType;
import com.projectgoth.common.Logger;
import com.projectgoth.datastore.Session;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.model.IMItem;
import com.projectgoth.nemesis.NetworkResponseListener;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.listeners.ThirdPartyIMListener;

/**
 * 
 * @author sarmadsangi
 *
 */

public class ThirdPartyIMController {
    private static final String TAG = "ThirdPartyIMController";

    private static final ThirdPartyIMController INSTANCE = new ThirdPartyIMController();

    public static final byte    IM_SESSION_LOGGED_IN            = 1;
    public static final byte    IM_SESSION_LOGGED_OUT           = 0;

    private ThirdPartyIMController() {
        
    }

    public static synchronized ThirdPartyIMController getInstance() {
        return INSTANCE;
    }
    
    public void signInOutIm(NetworkResponseListener listener, ImType type, boolean login) {
        Logger.debug.log(TAG, "setIMSessionStatus");
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendSignInOutIm(listener, login, type, PresenceType.AVAILABLE, true);
            }
        }
    }
    
    public boolean isImConfigured(ImType type) {
        IMItem imItem = Session.getInstance().getIMItem(type);
        if(imItem != null) {
            return imItem.isConfigured();
        }
        return false;
    }
    
    public boolean isImOnline(ImType type) {
        IMItem imItem = Session.getInstance().getIMItem(type);
        if(imItem != null) {
            return imItem.isOnline();
        }
        return false;
    }

    public void requestIMPresenceIcons(ThirdPartyIMListener listener, ImType imType) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
                final RequestManager requestManager = appEx.getRequestManager();
                if (requestManager != null) {
                    ImType[] imTypes = new ImType[1];
                    imTypes[0] = imType;
                    requestManager.sendGetIMIcons(listener, imTypes);
            }
        }
    }
    
    /**
     * @param imType
     * @param presence
     * @return
     */
    public Bitmap getIMContactPresenceBmp(ImType imType, PresenceType presence) {
        IMItem imItem = Session.getInstance().getIMItem(imType);
        Bitmap bmp = null;
        if(imItem != null) {
            if (imItem.isIMLoggedIn()) {
                switch (presence) {
                    case AVAILABLE:
                        bmp = imItem.getBmpOnline();
                        break;
                    case AWAY:
                        bmp = imItem.getBmpAway();
                        break;
                    case BUSY:
                        bmp = imItem.getBmpBusy();
                        break;
                    default:
                        bmp = imItem.getBmpOffline();
                }
            } else {
                bmp = imItem.getBmpOffline();
            }
            
            //request it from server if it is null
            if (bmp == null) {
                requestIMPresenceIcons(new ThirdPartyIMListener() {
                    
                    @Override
                    public void onIMIconsReceived(ImType imType, byte[] online, byte[] away, byte[] busy, byte[] offline) {
                        IMItem imItem = Session.getInstance().getIMItem(imType);
                        
                        if (imItem != null) {
                            imItem.setPresenceBmp(BitmapFactory.decodeByteArray(online, 0, online.length),
                                    BitmapFactory.decodeByteArray(away, 0, away.length), 
                                    BitmapFactory.decodeByteArray(busy, 0, busy.length), 
                                    BitmapFactory.decodeByteArray(offline, 0, offline.length) );
                            BroadcastHandler.Contact.sendFetchIMIconsCompleted();
                        }
                        
                    }
                }, imItem.getType());
            }
        }
                
        return bmp;
    }
    
}
