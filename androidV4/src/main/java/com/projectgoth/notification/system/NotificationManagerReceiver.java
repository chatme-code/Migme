package com.projectgoth.notification.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.deezer.sdk.player.event.PlayerState;
import com.projectgoth.i18n.I18n;
import com.projectgoth.music.deezer.DeezerPlayerManager;
import com.projectgoth.ui.activity.MainDrawerLayoutActivity;

public class NotificationManagerReceiver extends BroadcastReceiver {
    
    public final static String  TAG                             = "NativeNotification";
    private static final long   FIRST_TIME_REMINDER_INTERVAL    = 60 * 60 * 24 * 7 * 1000; // 7 days mill seconds
    private static final long   HOURS_72_REMINDER_INTERVAL      = 60 * 60 * 72 * 1000; // 72 hours mill seconds


    @Override
    public void onReceive(Context context, Intent intent) {
//        Intent startIntent = new Intent(context, MainDrawerLayoutActivity.class);
//        startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(startIntent);
        String action = intent.getAction();
        Log.i(TAG, "NativeNotificationReceiver");
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {

            long startTime = NativeNotificationManager.getInstance().getFirstStartTime();
            boolean isLogined = NativeNotificationManager.getInstance().getIsLogined();

//            NativeNotificationManager.getInstance().displayNotification(
//                    I18n.tr("We miss you! " + startTime), 
//                    I18n.tr("Nice stories : " + isLogined));
            long curTime = System.currentTimeMillis();
            if (isLogined) {
                long lastTime = NativeNotificationManager.getInstance().getLastTime();
                if (curTime - lastTime >= HOURS_72_REMINDER_INTERVAL) {
                    NativeNotificationManager.getInstance().displayNotification(
                            I18n.tr("Your friends miss you!"),
                            I18n.tr("We're all having fun without you, come back and join the party."));
                }
            } else {
                if (startTime == 0) {
                    // this is first restart
                    // now don't record first time when user restart phone
//                    NativeNotificationManager.getInstance().setFirstStartTime(curTime);
                } else {
                    if (curTime - startTime >= FIRST_TIME_REMINDER_INTERVAL) {
                        NativeNotificationManager.getInstance().displayNotification(
                                I18n.tr("Tap to become an explorer!"),
                                I18n.tr("Meet new friends, become a celebrity & explore the world of migme."));
                    }
                }
            }
        } else if (action.equals(NativeNotificationManager.ACTION_BUTTON)) {
            int buttonId = intent.getIntExtra(NativeNotificationManager.INTENT_BUTTONID_TAG, 0);
            switch (buttonId) {
                case NativeNotificationManager.BUTTON_STOP_ID:
                    DeezerPlayerManager.getInstance().stop();
                    DeezerPlayerManager.getInstance().clearBgPlayingRadio();
                    NativeNotificationManager.getInstance().clearNotify(NativeNotificationManager.PLAYER_NOTIFICATION_ID);
                    break;
                case NativeNotificationManager.BUTTON_PLAY_ID:
                    if (DeezerPlayerManager.getInstance().isPlaying()) {
                        DeezerPlayerManager.getInstance().pause();
                    } else if (DeezerPlayerManager.getInstance().isPaused()) {
                        DeezerPlayerManager.getInstance().play();
                    }
                    NativeNotificationManager.getInstance().displayNotificationBar(null);
                    break;
                case NativeNotificationManager.BUTTON_NEXT_ID:
                    PlayerState playState = DeezerPlayerManager.getInstance().getPlayerState();
                    //avoid crash at deezer sdk
                    if (playState != PlayerState.STARTED) {
                        DeezerPlayerManager.getInstance().playNext();
                    }
                    break;
                case NativeNotificationManager.BUTTON_MAIN_ID:
                    Intent startIntent = new Intent(context, MainDrawerLayoutActivity.class);
                    startIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    context.startActivity(startIntent);
                    break;
                default:
                    break;
            }
        }
    }
}