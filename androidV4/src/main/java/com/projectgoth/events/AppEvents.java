/**
 * Copyright (c) 2013 Project Goth
 *
 * EventsNew.java
 * Created Feb 17, 2014, 5:05:07 PM
 */

package com.projectgoth.events;

/**
 * @author angelorohit
 * 
 */
public abstract class AppEvents {

    public abstract class Application {

        public static final String SHOW_LOGIN               = "com.projectgoth.events.AppEvents.Application.SHOW_LOGIN";
        public static final String SHOW_SOCIAL_SPACE        = "com.projectgoth.events.AppEvents.Application.SHOW_SOCIAL_SPACE";
        public static final String DATABASE_DID_UPGRADE     = "com.projectgoth.events.AppEvents.Application.SHOW_SOCIAL_SPACE";
        public static final String BACKGROUND_STATE_CHANGED = "com.projectgoth.events.AppEvents.Application.BACKGROUND_STATE_CHANGED";
        public static final String SHOW_MUSIC_PAGE          = "com.projectgoth.events.AppEvents.Application.SHOW_MUSIC_PAGE";

        public abstract class Extra {

            public static final String FRAGMENT_ID = "com.projectgoth.events.AppEvents.Application.Extra.FRAGMENT_ID";
        }
    }

    public abstract class NetworkService {

        public static final String STARTED      = "com.projectgoth.events.AppEvents.NetworkService.STARTED";
        public static final String STOPPED      = "com.projectgoth.events.AppEvents.NetworkService.STOPPED";
        public static final String DISCONNECTED = "com.projectgoth.events.AppEvents.NetworkService.DISCONNECTED";
        public static final String ERROR        = "com.projectgoth.events.AppEvents.Network.ERROR";
        public static final String NETWORK_STATUS_CHANGED  = "com.projectgoth.events.AppEvents.Network.NETWORK_STATUS_CHANGED";

        public abstract class Extra {
            public static final String ERROR_TYPE = "com.projectgoth.events.AppEvents.NetworkService.Extra.ERROR_TYPE";
        }
    }

    public abstract class Misc {

        public abstract class Extra {

            public static final String FORMATTED_MESSAGE = "com.projectgoth.events.AppEvents.Misc.Extra.FORMATTED_MESSAGE";
            public static final String ERROR_TYPE        = "com.mig33.diggle.events.Events.Generic.Extra.ERROR_TYPE";
        }
    }

    public abstract class Notification {

        public static final String CHAT_SYSTEM_NOTIFICATION                  = "com.projectgoth.events.AppEvents.Notification.CHAT_SYSTEM_NOTIFICATION";
        public static final String MIGALERT_SYSTEM_NOTIFICATION              = "com.projectgoth.events.AppEvents.Notification.MIGALERT_SYSTEM_NOTIFICATION";
        public static final String ACCUMULATED_FOLLOWERS_SYSTEM_NOTIFICATION = "com.projectgoth.events.AppEvents.Notification.ACCUMULATED_FOLLOWERS_SYSTEM_NOTIFICATION";
        public static final String UPDATE_AVAILABLE                          = "com.projectgoth.events.AppEvents.Notification.UPDATE_AVAILABLE";
        public static final String ACTION_BAR_UPDATE_AVAILABLE               = "com.projectgoth.events.AppEvents.Notification.ACTION_BAR_UPDATE_AVAILABLE";
        public static final String FETCH_ALERTS_FROM_SERVER_DONE             = "com.projectgoth.events.AppEvents.Notification.FETCH_ALERTS_FROM_SERVER_DONE";
        public static final String NEW_FOLLOWER_NOTIFICATION                 = "com.projectgoth.events.AppEvents.Notification.NEW_FOLLOWER_NOTIFICATION";

        public abstract class Extra {

            public static final String TYPE       = "com.projectgoth.events.AppEvents.Notification.Extra.TYPE";
            public static final String ID         = "com.projectgoth.events.AppEvents.Notification.Extra.ID";
            public static final String ACTION_URL = "com.projectgoth.events.AppEvents.Notification.Extra.ACTION_URL";
        }
    }

    public abstract class Location {

        public static final String RECEIVED                      = "com.projectgoth.events.AppEvents.Location.RECEIVED";
        public static final String FETCH_ERROR                   = "com.projectgoth.events.AppEvents.Location.FETCH_ERROR";
        public static final String FETCH_NEARBY_PLACES_COMPLETED = "com.projectgoth.events.AppEvents.Location.FETCH_NEARBY_PLACES_COMPLETED";
        public static final String FETCH_NEARBY_PLACES_ERROR     = "com.projectgoth.events.AppEvents.Location.FETCH_NEARBY_PLACES_ERROR";
    }
    
    public abstract class Games {
        
        public static final String FETCH_BANNER_COMPLETED        = "com.projectgoth.events.AppEvents.Games.FETCH_BANNER_COMPLETED";
        public static final String FETCH_GAMES_COMPLETED         = "com.projectgoth.events.AppEvents.Games.FETCH_GAMES_COMPLETED";
        public static final String FETCH_GAME_COMPLETED          = "com.projectgoth.events.AppEvents.Games.FETCH_GAME_COMPLETED";
    }
    
    public abstract class Music {
        
        public static final String FETCH_MUSIC_COMPLETED         = "com.projectgoth.events.AppEvents.Music.FETCH_MUSIC_COMPLETED";
        public static final String FETCH_MUSIC_GENRES_COMPLETED  = "com.projectgoth.events.AppEvents.Music.FETCH_MUSIC_GENRES_COMPLETED";
    }
}
