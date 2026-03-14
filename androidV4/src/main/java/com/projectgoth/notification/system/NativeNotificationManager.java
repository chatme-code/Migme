/**
 * Copyright (c) 2013 Project Goth
 *
 * NativeNotificationManager.java
 * Created Apr 2, 2015, 3:10:02 PM
 */

package com.projectgoth.notification.system;

import java.util.concurrent.atomic.AtomicInteger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.RemoteViews;
import com.deezer.sdk.model.Album;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.player.event.RadioPlayerListener;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.events.AppEvents;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.imagefetcher.ImageHandler.ImageLoadListener;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.music.deezer.DeezerPlayerManager;
import com.projectgoth.ui.activity.MainDrawerLayoutActivity;

/**
 * @author shiyukun
 * 
 */
public class NativeNotificationManager implements RadioPlayerListener {

    public final static String               TAG                           = "notificationBar";

    private static NativeNotificationManager INSTANCE;
    private Context                          context;
    private NotificationManager              myNotificationManager;
    private RemoteViews                      playerView;
    private RemoteViews                      bigPlayerView;

    NotificationCompat.Builder               mBuilder;

    public static AtomicInteger              notificationBadge             = new AtomicInteger();

    public static final String               NOTIFICATION_PREFERENCES_NAME = "Mig33NativeNotificationPreferences";
    public static final String               NOTIFICATION_FIRST_START_TIME = "Mig33NotiveNotificationFirstStartTime";
    public static final String               NOTIFICATION_LAST_TIME        = "Mig33NotiveNotificationLastTime";
    public static final String               NOTIFICATION_IS_LOGINED       = "Mig33NotiveNotificationIsLogined";
    public static final String               NOTIFICATION_BADGE            = "Mig33NotiveNotificationBadge";
    private SharedPreferences                preferences;

    public static int                        COMMON_NOTIFICAITON_ID        = 100;
    public static int                        PLAYER_NOTIFICATION_ID        = 200;

    public final static String               INTENT_BUTTONID_TAG           = "ButtonId";
    public final static int                  BUTTON_STOP_ID                = 1;
    public final static int                  BUTTON_PLAY_ID                = 2;
    public final static int                  BUTTON_NEXT_ID                = 3;
    public final static int                  BUTTON_MAIN_ID                = 4;
    public final static int                  BUTTON_PAUSE_ID               = 5;
    public final static String               ACTION_BUTTON                 = "com.projectgoth.action.NotificationButtonClick";

    private boolean                          isShowPlayer                  = false;

    private NativeNotificationManager() {
        context = ApplicationEx.getInstance();
        preferences = context.getSharedPreferences(NOTIFICATION_PREFERENCES_NAME, Context.MODE_PRIVATE);
        myNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public synchronized static NativeNotificationManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NativeNotificationManager();
        }
        return INSTANCE;
    }

    public long getFirstStartTime() {
        return preferences.getLong(NOTIFICATION_FIRST_START_TIME, 0);
    }

    public void setFirstStartTime(long time) {
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putLong(NOTIFICATION_FIRST_START_TIME, time);
        preferencesEditor.commit();
    }

    public long getLastTime() {
        return preferences.getLong(NOTIFICATION_LAST_TIME, 0);
    }

    public void setLastTime(long time) {
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putLong(NOTIFICATION_LAST_TIME, time);
        preferencesEditor.commit();
    }

    public void setBadge(int count) {
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putInt(NOTIFICATION_BADGE, count);
        preferencesEditor.commit();
    }

    public boolean getIsLogined() {
        return preferences.getBoolean(NOTIFICATION_IS_LOGINED, false);
    }

    public void setIsLogined(boolean isLogined) {
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putBoolean(NOTIFICATION_IS_LOGINED, isLogined);
        preferencesEditor.commit();
    }

    public void displayNotification(String title, String content) {
        displayNotification(title, content, true);
    }

    public void displayNotification(String title, String content, boolean showBadge) {
        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle(title).setContentText(content).setOngoing(false).setAutoCancel(true)
                .setSmallIcon(R.drawable.icon_application);

        Intent resultIntent = new Intent(context, MainDrawerLayoutActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        myNotificationManager.notify(COMMON_NOTIFICAITON_ID, mBuilder.build());

        if (showBadge) {
            int badge = preferences.getInt(NOTIFICATION_BADGE, 0);
            try {
                ShortcutBadger.setBadge(context, badge + 1);
            } catch (ShortcutBadgeException e) {
                e.printStackTrace();
            }
            setBadge(badge + 1);
        }
    }

    public boolean isDisplayNotificationPlayer() {
        return isShowPlayer;
    }

    public void displayNotificationBar(Bitmap bitmap) {
        isShowPlayer = true;
        if (context == null || DeezerPlayerManager.getInstance().getBgPlayingRadioId() <= 0) {
            return;
        }
        if (!UIUtils.hasJellyBean()) {
            displayNotification(DeezerPlayerManager.getInstance().getCurrentTrackTitle(), DeezerPlayerManager
                    .getInstance().getCurrentTrackArtistname(), false);
            return;
        }
        playerView = getPlayerView();
        bigPlayerView = getBigPlayerView();
        Album album = DeezerPlayerManager.getInstance().getCurrentAlbum();

        if (bitmap == null) {
            if (album != null) {
                final String coverUrl = album.getCoverUrl();
                if (!TextUtils.isEmpty(coverUrl)) {
                    ImageHandler.getInstance().loadImage(coverUrl, new ImageLoadListener() {

                        @Override
                        public void onImageLoaded(Bitmap bitmap) {
                            if (bitmap != null) {
                                displayNotificationBar(bitmap);
                            }
                        }

                        @Override
                        public void onImageFailed(ImageView imageView) {
                        }
                    });
                }
            }
        } else {
            playerView.setImageViewBitmap(R.id.trackCover, bitmap);
            bigPlayerView.setImageViewBitmap(R.id.trackCover, bitmap);
        }

        mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.icon_application)
                .setAutoCancel(false).setContent(playerView);

        if (DeezerPlayerManager.getInstance().isPlaying()) {
            mBuilder.setOngoing(true);
        } else {
            mBuilder.setOngoing(false);
        }
        setActionForButtons();

        Intent resultIntent = new Intent(context, MainDrawerLayoutActivity.class);
        resultIntent.setAction(AppEvents.Application.SHOW_MUSIC_PAGE);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);

        Notification notify = mBuilder.build();
        notify.bigContentView = bigPlayerView;

        myNotificationManager.notify(PLAYER_NOTIFICATION_ID, notify);

    }

    public RemoteViews getBigPlayerView() {
        bigPlayerView = new RemoteViews(context.getPackageName(), R.layout.notification_player_bar_big);
        if (DeezerPlayerManager.getInstance().isPlaying()) {
            bigPlayerView.setImageViewResource(R.id.playbackPlayPause, R.drawable.ic_action_pause_green_solid);
        } else {
            bigPlayerView.setImageViewResource(R.id.playbackPlayPause, R.drawable.ic_action_play_green);
        }

        bigPlayerView.setTextViewText(R.id.artistName, DeezerPlayerManager.getInstance().getCurrentTrackArtistname());
        bigPlayerView.setTextViewText(R.id.trackName, DeezerPlayerManager.getInstance().getCurrentTrackTitle());
        return bigPlayerView;
    }

    public RemoteViews getPlayerView() {
        playerView = new RemoteViews(context.getPackageName(), R.layout.notification_player_bar);
        if (DeezerPlayerManager.getInstance().isPlaying()) {
            playerView.setImageViewResource(R.id.playbackPlayPause, R.drawable.ic_action_pause_green_solid);
        } else {
            playerView.setImageViewResource(R.id.playbackPlayPause, R.drawable.ic_action_play_green);
        }
        playerView.setTextViewText(R.id.artistName, DeezerPlayerManager.getInstance().getCurrentTrackArtistname());
        playerView.setTextViewText(R.id.trackName, DeezerPlayerManager.getInstance().getCurrentTrackTitle());
        return playerView;
    }

    public void setActionForButtons() {
        Intent buttonIntent = new Intent(ACTION_BUTTON);

        // set stop button
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_STOP_ID);
        PendingIntent intentStop = PendingIntent.getBroadcast(context, 1, buttonIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        playerView.setOnClickPendingIntent(R.id.playbackStop, intentStop);
        bigPlayerView.setOnClickPendingIntent(R.id.close, intentStop);

        // set play or pause button
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PLAY_ID);
        PendingIntent intentPlay = PendingIntent.getBroadcast(context, 2, buttonIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        playerView.setOnClickPendingIntent(R.id.playbackPlayPause, intentPlay);
        bigPlayerView.setOnClickPendingIntent(R.id.playbackPlayPause, intentPlay);

        // set next button
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_NEXT_ID);
        PendingIntent intentNext = PendingIntent.getBroadcast(context, 3, buttonIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        playerView.setOnClickPendingIntent(R.id.playbackNext, intentNext);
        bigPlayerView.setOnClickPendingIntent(R.id.playbackNext, intentNext);
    }

    public void addListerToPlayer() {
        DeezerPlayerManager.getInstance().addListener(this);
    }

    public void clearNotify(int notifyId) {
        myNotificationManager.cancel(notifyId);
        if (notifyId == PLAYER_NOTIFICATION_ID) {
            isShowPlayer = false;
        }
    }

    @Override
    public void onAllTracksEnded() {

    }

    @Override
    public void onPlayTrack(Track track) {
        DeezerPlayerManager.getInstance().setBgPlayingTrack(track);
        displayNotificationBar(null);
    }

    @Override
    public void onRequestException(Exception arg0, Object arg1) {

    }

    @Override
    public void onTrackEnded(Track arg0) {

    }

    @Override
    public void onTooManySkipsException() {
    }
}
