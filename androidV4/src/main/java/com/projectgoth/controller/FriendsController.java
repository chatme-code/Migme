/**
 * Copyright (c) 2013 Project Goth
 * FriendsController.java
 * 
 * Jun 27, 2013 2:05:21 PM
 */

package com.projectgoth.controller;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.blackhole.enums.PresenceType;
import com.projectgoth.blackhole.enums.UserPermissionType;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.enums.ActivitySourceEnum;
import com.projectgoth.nemesis.listeners.ChangeRelationshipListener;
import com.projectgoth.nemesis.listeners.SetPermissionListener;
import com.projectgoth.nemesis.listeners.SetPresenceListener;
import com.projectgoth.nemesis.listeners.SetStatusMessageListener;
import com.projectgoth.nemesis.model.MigError;

import java.util.HashSet;
import java.util.Set;

/**
 * Purpose: - Provides utility methods to send Friend and user related requests.
 * 
 * @author angelorohit
 */
public class FriendsController {

    private final static FriendsController INSTANCE = new FriendsController();

    private static final Object            lock     = new Object();

    private Set<String>                    changeRelationshipInProgress;

    /**
     * Constructor.
     */
    private FriendsController() {
        changeRelationshipInProgress = new HashSet<String>();
    }

    public static synchronized FriendsController getInstance() {
        return INSTANCE;
    }

    /**
     * Sends a request to set the presence of the logged in user.
     * 
     * @param newPresence
     *            The new presence to set.
     */
    public void requestSetPresence(final PresenceType newPresence) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                final PresenceType currentPresence = Session.getInstance().getPresence();

                if (currentPresence != newPresence) {
                    // Immediately set the new presence,
                    // so we don't have to wait for the server to reply.
                    Session.getInstance().setPresence(newPresence);

                    requestManager.sendSetPresence(new SetPresenceListener() {

                        @Override
                        public void onSetPresence(PresenceType newPresence) {
                            Session.getInstance().setPresence(newPresence);
                            BroadcastHandler.User.sendPresenceSet(newPresence);
                        }

                        @Override
                        public void onError(MigError error) {
                            // Reset my presence back to what it was before
                            Session.getInstance().setPresence(currentPresence);
                            BroadcastHandler.User.sendSetPresenceError(error, newPresence);
                        }

                    }, newPresence);
                }
            }
        }
    }

    /**
     * Sends a request to set the status message of the logged in user.
     * 
     * @param newStatusMessage
     *            The new status message to set.
     */
    public void requestSetStatusMessage(final String newStatusMessage) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                final String currentStatusMessage = Session.getInstance().getStatusMessage();
                if (!currentStatusMessage.equals(newStatusMessage)) {
                    requestManager.sendSetStatusMessage(new SetStatusMessageListener() {

                        @Override
                        public void onStatusMessageSent(String newStatusMessage) {
                            Session.getInstance().setStatusMessage(newStatusMessage);
                            BroadcastHandler.User.sendStatusMessageSet();
                        }

                        @Override
                        public void onError(MigError error) {
                            BroadcastHandler.User.sendSetStatusMessageError(error);
                        }
                    }, newStatusMessage);
                }
            }
        }
    }

    /**
     * Sends a request to block a user.
     * 
     * @param username
     *            The username of the user to be blocked.
     */
    public void requestToBlockUser(final String username) {
        requestToSetUserPermission(username, UserPermissionType.BLOCK);
    }

    /**
     * Sends a request to unblock a user.
     * 
     * @param username
     *            The username of the user to be unblocked.
     */
    public void requestToUnblockUser(final String username) {
        requestToSetUserPermission(username, UserPermissionType.ALLOW);
    }

    /**
     * Sends a request to set the permission rights for a user.
     * 
     * @param username
     *            The username of the user whose permission is to be set.
     * @param userPermissionType
     *            The type of permission to be set (allow, block etc.)
     */
    private void requestToSetUserPermission(final String username, final UserPermissionType userPermissionType) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendSetPermission(new SetPermissionListener() {

                    @Override
                    public void onPermissionSet(final String username, final UserPermissionType userPermissionType) {
                        BroadcastHandler.User.sendPermissionSet(username, userPermissionType);
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        BroadcastHandler.User.sendSetPermissionError(error, username, userPermissionType);
                    }

                }, username, userPermissionType);
            }
        }
    }

    /**
     * Encapsulates request to follow or unfollow user. The actual request is
     * handled by the {@link UserDatastore}. This method does additional logic
     * to provide the UI with data on whether a user is currently being
     * followed/unfollowed by adding them in a
     * {@link #changeRelationshipInProgress} set. This set is updated once the
     * follow/unfollow request is done (irregardless of whether it failed or
     * succeeded).
     * 
     * @param username
     *            Username to follow or unfollow
     * @param activitySource
     *            The source from where the follow request originates. Can be
     *            null.
     */
    public void requestToFollowOrUnfollowUser(final String username, final ActivitySourceEnum activitySource) {
        UserDatastore.getInstance().followOrUnfollowUser(username, activitySource);
        synchronized (lock) {
            changeRelationshipInProgress.add(username);
        }
    }

    /**
     * Called by {@link ChangeRelationshipListener} from {@link UserDatastore}
     * when a follow or unfollow request has been completed.
     * 
     * @param username
     *            Username that was followed or unfollowed
     */
    public void onUserChangeRelationshipDone(final String username) {
        synchronized (lock) {
            changeRelationshipInProgress.remove(username);
        }
    }

    /**
     * Convenience method for checking if a request to follow or unfollow a user
     * is currently in progress.
     * 
     * @param username
     *            Username to check
     * @return True if request to unfollow or follow a user is currently in
     *         progress. False otherwise.
     */
    public boolean isChangeRelationshipWithUserInProgress(String username) {
        synchronized (lock) {
            return (changeRelationshipInProgress.contains(username));
        }
    }
}
