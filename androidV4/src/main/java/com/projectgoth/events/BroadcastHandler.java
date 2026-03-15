/**
 * Copyright (c) 2013 Project Goth
 *
 * BroadcastHandler.java
 * Created Feb 5, 2014, 10:34:30 AM
 */

package com.projectgoth.events;

import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

import com.mig33.diggle.events.BaseBroadcastHandler;
import com.mig33.diggle.events.BroadcastEvent;
import com.mig33.diggle.events.EventReceiver;
import com.projectgoth.app.ApplicationEx;

/**
 * @author angelorohit
 * 
 */
public class BroadcastHandler extends BaseBroadcastHandler {

    private Context ctx = null;

    /**
     * Private constructor.
     */
    private BroadcastHandler() {
    }

    /**
     * One-time initialization of {@link BroadcastHandler}. This must be invoked
     * before any event broadcasting takes place.
     */
    public static synchronized void initialize() {
        if (instance == null) {
            instance = new BroadcastHandler();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.mig33.diggle.events.BaseBroadcastHandler#broadcastEvent(com.mig33
     * .diggle.events.BroadcastEvent)
     */
    @Override
    public void broadcastEvent(BroadcastEvent evt) {
        if (ctx == null) {
            ctx = ApplicationEx.getContext();
        }

        if (ctx != null) {
            BroadcastEventImpl broadcastEventImpl = (BroadcastEventImpl) evt;

            // Add a formatted message to the event (if available).
            final String formattedMessage = EventMessageProvider.getInstance().getFormattedMessageForEvent(evt);
            if (formattedMessage != null) {
                evt.putExtra(AppEvents.Misc.Extra.FORMATTED_MESSAGE, formattedMessage);
            }
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(ApplicationEx.getContext());
            localBroadcastManager.sendBroadcast(broadcastEventImpl.getIntent());
        }
    }

    /*
     * This operation is not supported for Android.
     * 
     * @see
     * com.mig33.diggle.events.BaseBroadcastHandler#registerForEvent(com.mig33
     * .diggle.events.EventReceiver, java.lang.String)
     */
    @Override
    public void registerForEvent(EventReceiver listener, String eventName) {
        // Do nothing here. Events are registered in a different way.
        throw new UnsupportedOperationException("Events in Android are registered in a different way.");
    }

    /*
     * This operation is not supported for Android.
     * 
     * @see
     * com.mig33.diggle.events.BaseBroadcastHandler#unregister(com.mig33.diggle
     * .events.EventReceiver)
     */
    @Override
    public boolean unregister(EventReceiver listener) {
        // Do nothing here. Events are unregistered in a different way.
        throw new UnsupportedOperationException("Events in Android are unregistered in a different way.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.mig33.diggle.events.BaseBroadcastHandler#makeBroadcastEventWithName
     * (java.lang.String)
     */
    @Override
    public BroadcastEvent makeEventWithName(String eventName) {
        return new BroadcastEventImpl(eventName);
    }

    /*******************************************
     * 
     * Events specific to this client.
     * 
     *******************************************/

    public static abstract class Application extends BaseBroadcastHandler.Application {

        /**
         * Broadcasts an {@link AppEvents.Application#SHOW_LOGIN} when the
         * application is directed to show the login screen.
         * 
         * @throws IllegalStateException
         *             Thrown if this BroadcastHandler has not been initialized.
         */
        public static void sendShowLogin() throws IllegalStateException {
            getValidInstance().broadcastEventWithName(AppEvents.Application.SHOW_LOGIN);
        }

        /**
         * Broadcasts an {@link AppEvents.Application#SHOW_SOCIAL_SPACE} when
         * the application is directed to show the social space side panel.
         * 
         * @throws IllegalStateException
         *             Thrown if this BroadcastHandler has not been initialized.
         */
        public static void sendShowSocialSpace() throws IllegalStateException {
            getValidInstance().broadcastEventWithName(AppEvents.Application.SHOW_SOCIAL_SPACE);
        }

        /**
         * Broadcasts an {@link AppEvents.Application#DATABASE_DID_UPGRADE}
         * event to say that the database has been upgraded. Persistent storages
         * that listen in on this event will take appropriate action for their
         * tables.
         * 
         * @throws IllegalStateException
         *             Thrown if this BroadcastHandler has not been initialized.
         */
        public static void sendDatabaseDidUpgrade() throws IllegalStateException {
            getValidInstance().broadcastEventWithName(AppEvents.Application.DATABASE_DID_UPGRADE);
        }

        /**
         * Broadcasts an {@link AppEvents.Application#BACKGROUND_STATE_CHANGED}
         * event when the application goes from running in the foreground to the
         * background and vice-versa.
         * 
         * @throws IllegalStateException
         *             Thrown if this BroadcastHandler has not been initialized.
         */
        public static void sendBackgroundStateChanged() throws IllegalStateException {
            getValidInstance().broadcastEventWithName(AppEvents.Application.BACKGROUND_STATE_CHANGED);
        }
    }

    public static abstract class NetworkService {

        /**
         * Broadcasts an {@link AppEvents.NetworkService#STARTED} event when the
         * {@link NetworkService} has been started.
         * 
         * @throws IllegalStateException
         *             Thrown if this BroadcastHandler has not been initialized.
         */
        public static void sendStarted() throws IllegalStateException {
            getValidInstance().broadcastEventWithName(AppEvents.NetworkService.STARTED);
        }

        /**
         * Broadcasts an {@link AppEvents.NetworkService#STOPPED} event when the
         * {@link NetworkService} has been stopped.
         * 
         * @throws IllegalStateException
         *             Thrown if this BroadcastHandler has not been initialized.
         */
        public static void sendStopped() throws IllegalStateException {
            getValidInstance().broadcastEventWithName(AppEvents.NetworkService.STOPPED);
        }

        /**
         * Broadcasts an {@link AppEvents.NetworkService#DISCONNECTED} event
         * when the {@link NetworkService} has experienced a disconnection.
         * 
         * @throws IllegalStateException
         *             Thrown if this BroadcastHandler has not been initialized.
         */
        public static void sendDisconnected() throws IllegalStateException {
            getValidInstance().broadcastEventWithName(AppEvents.NetworkService.DISCONNECTED);
        }

        public static void sendError(int errorType) throws IllegalStateException {
            final BaseBroadcastHandler inst = getValidInstance();
            BroadcastEvent evt = inst.makeEventWithName(AppEvents.NetworkService.ERROR);
            evt.putExtra(AppEvents.NetworkService.Extra.ERROR_TYPE, errorType);
            inst.broadcastEvent(evt);
        }

        public static void sendNetworkStatusChanged() {
            getValidInstance().broadcastEventWithName(AppEvents.NetworkService.NETWORK_STATUS_CHANGED);
        }

    }

    public static abstract class Notification {

        /**
         * Broadcasts an
         * {@link AppEvents.Notification#NEW_FOLLOWER_NOTIFICATION} when a user
         * has new followers.
         * 
         * @throws IllegalStateException
         *             Thrown if this BroadcastHandler has not been initialized.
         */
        public static void sendNewFollowerUpdate() throws IllegalStateException {
            getValidInstance().broadcastEventWithName(AppEvents.Notification.NEW_FOLLOWER_NOTIFICATION);
        }
    }

    public static abstract class Location {

        /**
         * Broadcasts an {@link AppEvents.Location#RECEIVED} when the current
         * location for the device has been successfully received.
         * 
         * @throws IllegalStateException
         *             Thrown if this BroadcastHandler has not been initialized.
         */
        public static void sendReceived() throws IllegalStateException {
            getValidInstance().broadcastEventWithName(AppEvents.Location.RECEIVED);
        }

        /**
         * Broadcasts an {@link AppEvents.Location#FETCH_ERROR} when the current
         * location for the device could not be fetched.
         * 
         * @throws IllegalStateException
         *             Thrown if this BroadcastHandler has not been initialized.
         */
        public static void sendFetchError() throws IllegalStateException {
            getValidInstance().broadcastEventWithName(AppEvents.Location.FETCH_ERROR);
        }

        /**
         * Broadcasts an {@link AppEvents.Location#FETCH_NEARBY_PLACES_COMPLETED}
         * when nearby places have been successfully fetched. 
         * 
         * @throws IllegalStateException
         *             Thrown if this BroadcastHandler has not been initialized.
         */
        public static void sendFetchNearbyPlacesCompleted() throws IllegalStateException {
            getValidInstance().broadcastEventWithName(AppEvents.Location.FETCH_NEARBY_PLACES_COMPLETED);
        }

        /**
         * Broadcasts an {@link AppEvents.Location#FETCH_NEARBY_PLACES_ERROR} when
         * nearby places could not be successfully fetched.
         * 
         * @throws IllegalStateException
         *             Thrown if this BroadcastHandler has not been initialized.
         */
        public static void sendFetchNearbyPlacesError() throws IllegalStateException {
            getValidInstance().broadcastEventWithName(AppEvents.Location.FETCH_NEARBY_PLACES_ERROR);
        }        
    }
    
    public static abstract class Game {
        /**
         * Broadcasts an {@link AppEvents.Games#FETCH_BANNER_COMPLETED}
         * when game banners have been successfully fetched. 
         * 
         * @throws IllegalStateException
         *             Thrown if this BroadcastHandler has not been initialized.
         */
        public static void sendFetchBannersCompleted() throws IllegalStateException {
            getValidInstance().broadcastEventWithName(AppEvents.Games.FETCH_BANNER_COMPLETED);
        }
        
        /**
         * Broadcasts an {@link AppEvents.Games#FETCH_GAMES_COMPLETED}
         * when game games have been successfully fetched. 
         * 
         * @throws IllegalStateException
         *             Thrown if this BroadcastHandler has not been initialized.
         */
        public static void sendFetchGamesCompleted() throws IllegalStateException {
            getValidInstance().broadcastEventWithName(AppEvents.Games.FETCH_GAMES_COMPLETED);
        }
        
    }
    
    public static abstract class Music {
        
        public static void sendFetchMusicCompleted() throws IllegalStateException {
            getValidInstance().broadcastEventWithName(AppEvents.Music.FETCH_MUSIC_COMPLETED);
        }
        
        public static void sendFetchMusicGenresCompleted() throws IllegalStateException {
            getValidInstance().broadcastEventWithName(AppEvents.Music.FETCH_MUSIC_GENRES_COMPLETED);
        }
    }
    
}
