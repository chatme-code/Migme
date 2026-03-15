/**
 * Copyright (c) 2013 Project Goth
 *
 * GroupController.java
 * Created Aug 14, 2013, 6:31:25 PM
 */

package com.projectgoth.controller;

import com.google.gson.Gson;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Group;
import com.projectgoth.b.data.GroupResponse;
import com.projectgoth.b.data.SuccessResponse;
import com.projectgoth.common.Logger;
import com.projectgoth.datastore.DataCache;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.listeners.GroupListener;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.util.AndroidLogger;

/**
 * @author warrenbalcos
 * 
 */
public class GroupsController {
    
    private static final String           TAG                  = AndroidLogger.makeLogTag(GroupsController.class);
    
    private static final Object           CACHE_LOCK           = new Object();
    
    private DataCache<Group>              groupCache;
    
    private static final int              GROUP_MAX_CACHE_SIZE = 20;

    private static final GroupsController INSTANCE             = new GroupsController();

    private GroupsController() {
        try {
            groupCache = new DataCache<Group>(GROUP_MAX_CACHE_SIZE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized GroupsController getInstance() {
        return INSTANCE;
    }

    //@formatter:off
    private GroupListener groupListener = new GroupListener() {
        
        @Override
        public void onLeaveGroupResponse(SuccessResponse response, String groupId) {
            Logger.debug.log(TAG, "onLeaveGroupResponse: ", groupId, " response: ", new Gson().toJson(response));
            BroadcastHandler.Group.sendLeft(groupId);
        }
        
        @Override
        public void onJoinGroupResponse(SuccessResponse response, String groupId) {
            Logger.debug.log(TAG, "onJoinGroupResponse: ", groupId, " response: ", new Gson().toJson(response));
            BroadcastHandler.Group.sendJoined(response.getSuccess(), groupId);
        }
        
        @Override
        public void onRequestJoinGroupResponse(SuccessResponse response, String groupId) {
            Logger.debug.log(TAG, "onRequestJoinGroupError: groupId", groupId);
            BroadcastHandler.Group.sendJoinRequestSent(response.getSuccess(), groupId);
        }
        
        @Override
        public void onGetGroupResponse(GroupResponse response, String groupId) {
            Logger.debug.log(TAG, "onGetGroupResponse: ", groupId, " response: ", new Gson().toJson(response));
            synchronized (CACHE_LOCK) {
                groupCache.cacheData(groupId, response.getGroup());
            }
            
            BroadcastHandler.Group.sendFetchInfoCompleted(groupId);
        }     
        
        @Override
        public void onJoinGroupError(MigError error, String groupId) {
            Logger.debug.log(TAG, "onJoinGroupError: groupId", groupId);
            BroadcastHandler.Group.sendJoinError(error, groupId);
        }
        
        @Override
        public void onLeaveGroupError(MigError error, String groupId) {
            Logger.debug.log(TAG, "onLeaveGroupError: groupId", groupId);
            BroadcastHandler.Group.sendLeaveError(error, groupId);
        }
        
        @Override
        public void onGetGroupError(MigError error, String groupId) {
            Logger.debug.log(TAG, "onGetGroupError: groupId", groupId);
            BroadcastHandler.Group.sendFetchError(error, groupId);
        }

        @Override
        public void onRequestJoinGroupError(MigError error, String groupId) {
            Logger.debug.log(TAG, "onRequestJoinGroupError: groupId", groupId);
            BroadcastHandler.Group.sendSendJoinRequestError(error, groupId);
        }

    };
    //@formatter:on

    public Group getGroup(final String groupId, final boolean shouldForceFetch) {
        synchronized (CACHE_LOCK) {
            if (groupId != null) {
                if (groupCache.isExpired(groupId) || shouldForceFetch) {
                    final ApplicationEx appEx = ApplicationEx.getInstance();
                    if (appEx != null) {
                        final RequestManager requestManager = appEx.getRequestManager();
                        if (requestManager != null) {
                            BroadcastHandler.Group.sendBeginFetchInfo(groupId);
                            requestManager.getGroup(groupListener, groupId);
                        }
                    }
                }
                
                return groupCache.getData(groupId);
            }
            return null;
        }
    }

    public void joinGroup(String groupId) {
        if (groupId != null) {
            final ApplicationEx appEx = ApplicationEx.getInstance();
            if (appEx != null) {
                final RequestManager requestManager = appEx.getRequestManager();
                if (requestManager != null) {
                    requestManager.joinGroup(groupListener, groupId);
                }
            }
        }
    }

    public void requestJoinGroup(String groupId) {
        if (groupId != null) {
            final ApplicationEx appEx = ApplicationEx.getInstance();
            if (appEx != null) {
                final RequestManager requestManager = appEx.getRequestManager();
                if (requestManager != null) {
                    requestManager.requestJoinGroup(groupListener, groupId);
                }
            }
        }
    }
    
    public void leaveGroup(String groupId) {
        if (groupId != null) {
            final ApplicationEx appEx = ApplicationEx.getInstance();
            if (appEx != null) {
                final RequestManager requestManager = appEx.getRequestManager();
                if (requestManager != null) {
                    requestManager.leaveGroup(groupListener, groupId);
                }
            }
        }
    }

}
