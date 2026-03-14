/**
 * Copyright (c) 2013 Project Goth
 *
 * PostsController.java
 * Created Jul 25, 2013, 1:49:41 PM
 */

package com.projectgoth.controller;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.datastore.PostsDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.enums.PostCategoryTypeEnum;
import com.projectgoth.nemesis.listeners.SimpleResponseListener;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.nemesis.model.MigResponse;


/**
 * @author angelorohit
 */
public class PostsController {

    private final static PostsController INSTANCE = new PostsController();
    
    /**
     * Constructor
     */
    private PostsController() {
    }

    /**
     * A single point of entry for this controller.
     * @return An instance of the controller.
     */
    public static synchronized PostsController getInstance() {
        return INSTANCE;
    }
    
    public void requestWatchOrUnwatchPost(final String postId, final boolean shouldWatch) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {            
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                // Pre-emptively set the post as watched / unwatched.
                PostsDatastore.getInstance().markPostWithIdAsWatchedOrUnwatched(postId, shouldWatch);
                BroadcastHandler.Post.sendWatchedUnwatched(shouldWatch, postId);
                
                requestManager.sendWatchOrUnwatchPost(new SimpleResponseListener() {
                    
                    @Override
                    public void onSuccess(MigResponse response) {
                        PostsDatastore.getInstance().markPostWithIdAsAlteredOrRemoved(postId, false, true);
                        // Force fetch user profile.
                        UserDatastore.getInstance().getProfileWithUsername(Session.getInstance().getUsername(), true);
                    }
                    
                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        
                        // An error occurred and the post could not be watched / unwatched.
                        PostsDatastore.getInstance().markPostWithIdAsWatchedOrUnwatched(postId, !shouldWatch);
                        BroadcastHandler.Post.sendWatchUnwatchError(error, shouldWatch, postId);
                    }
                }, postId, shouldWatch);                
            }
        }
    }    
    
    public void requestLockOrUnlockPost(final String postId, final boolean doLock) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {            
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                   // Pre-emptively set the post as locked / unlocked.
                   PostsDatastore.getInstance().markPostWithIdAsLockedOrUnlocked(postId, doLock);
                   BroadcastHandler.Post.sendLockedUnlocked(doLock, postId);
                   
                   requestManager.sendLockOrUnlockPost(new SimpleResponseListener() {
                        
                   @Override
                   public void onSuccess(MigResponse response) {
                       PostsDatastore.getInstance().markPostWithIdAsAlteredOrRemoved(postId, false, true);
                   }
                        
                   @Override
                   public void onError(MigError error) {
                       super.onError(error);
                       
                       // An error occurred and the post could not be locked / unlocked.
                       PostsDatastore.getInstance().markPostWithIdAsLockedOrUnlocked(postId, !doLock);
                       BroadcastHandler.Post.sendLockUnlockError(error, doLock, postId);
                   }
               }, postId, doLock);
            }
        }
    }
    
    public void requestTagPost(final String postId, final int tagId) {
        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {            
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                // Pre-emptively set the tagId on the post.
                PostsDatastore.getInstance().markPostWithIdAsTagged(postId, tagId);
                BroadcastHandler.Post.sendTagged(postId, tagId);
                requestManager.sendTagPost(new SimpleResponseListener() {

                    @Override
                    public void onSuccess(MigResponse response) {
                        PostsDatastore.getInstance().markPostWithIdAsAlteredOrRemoved(postId, false, true);
                    }
                    
                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        
                        BroadcastHandler.Post.sendTagError(error, postId, tagId);
                    }
                    
                }, postId, tagId);
            }
        }
    }

}
