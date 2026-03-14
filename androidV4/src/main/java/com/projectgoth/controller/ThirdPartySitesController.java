/**
 * Copyright (c) 2013 Project Goth
 *
 * ThirdPartySitesController.java
 * Created Aug 15, 2013, 3:04:15 PM
 */

package com.projectgoth.controller;

import com.google.gson.Gson;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.ThirdPartySites;
import com.projectgoth.common.Logger;
import com.projectgoth.datastore.DataCache;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.listeners.ThirdPartySitesListener;

/**
 * @author warrenbalcos
 * 
 */
public class ThirdPartySitesController {

    private static final String TAG = "ThirdPartySitesController";

    private enum Type {
        STATUS, LINKED
    }

    /**
     * cache expiry in seconds (5 seconds)
     */
    private static final int                       REQUEST_CACHE_EXPIRY = 3;

    private DataCache<ThirdPartySites>             sitesCache;

    private byte[]                                 lock     = new byte[0];

    private static final ThirdPartySitesController INSTANCE = new ThirdPartySitesController();

    private ThirdPartySitesController() {
        try {
            sitesCache = new DataCache<ThirdPartySites>(Type.values().length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized ThirdPartySitesController getInstance() {
        return INSTANCE;
    }

    //@formatter:off
    private ThirdPartySitesListener thirdPartyListener = new ThirdPartySitesListener() {
        
        @Override
        public void onStatusReceived(ThirdPartySites result) {
            Logger.debug.log(TAG, "onStatusReceived: " + new Gson().toJson(result));
            cacheResult(result, Type.STATUS);
            BroadcastHandler.Application.sendFetchThirdPartySettingsCompleted();
        }
        
        @Override
        public void onLinkedReceived(ThirdPartySites result) {
            Logger.debug.log(TAG, "onLinkedReceived: " + new Gson().toJson(result));
            cacheResult(result, Type.LINKED);
            BroadcastHandler.Application.sendFetchThirdPartySettingsCompleted();
        }
    };
    //@formatter:on

    private void cacheResult(ThirdPartySites result, Type type) {
        synchronized (lock) {
            sitesCache.cacheData(type.name(), result, REQUEST_CACHE_EXPIRY);
        }
    }

    public ThirdPartySites getThirdPartySitesStatus() {
        Logger.debug.log(TAG, "getThirdPartySitesStatus");

        synchronized (lock) {
            if (sitesCache.isExpired(Type.STATUS.name())) {
                final ApplicationEx appEx = ApplicationEx.getInstance();
                if (appEx != null) {
                    final RequestManager requestManager = appEx.getRequestManager();
                    if (requestManager != null) {
                        requestManager.getThirdPartySitesStatus(thirdPartyListener);
                    }
                }
            }
            return sitesCache.getData(Type.STATUS.name());
        }
    }

    public ThirdPartySites getThirdPartySitesLinked() {
        Logger.debug.log(TAG, "getThirdPartySitesLinked");

        synchronized (lock) {
            if (sitesCache.isExpired(Type.LINKED.name())) {
                final ApplicationEx appEx = ApplicationEx.getInstance();
                if (appEx != null) {
                    final RequestManager requestManager = appEx.getRequestManager();
                    if (requestManager != null) {
                        requestManager.getThirdPartySitesLinked(thirdPartyListener);
                    }
                }
            }
            return sitesCache.getData(Type.LINKED.name());
        }
    }
    
    public ThirdPartySites getThirdPartySitesStatusAndLinked() {
        Logger.debug.log(TAG, "getThirdPartySitesStatusAndLinked");
        synchronized (lock) {
            ThirdPartySites result = new ThirdPartySites();

            ThirdPartySites status = getThirdPartySitesStatus();
            ThirdPartySites linked = getThirdPartySitesLinked();

            if (status != null && linked != null) {
                result.setFacebook(status.getFacebookStatus() && linked.getFacebookStatus());
                result.setTwitter(status.getTwitterStatus() && linked.getTwitterStatus());
            }
            return result;
        }
    }
    
}
