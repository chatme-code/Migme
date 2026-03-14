/**
 * Copyright (c) 2013 Project Goth
 *
 * DeviceModeHandler.java
 * Created Sep 11, 2013, 11:36:11 AM
 */

package com.projectgoth.common;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.notification.system.NativeNotificationManager;
import com.projectgoth.ui.activity.BaseFragmentActivity;
import com.projectgoth.util.AndroidLogger;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author cherryv
 * 
 */
public class DeviceModeHandler {

    private static final String LOG_TAG = AndroidLogger.makeLogTag(DeviceModeHandler.class);

    private KeyguardManager      keyguardManager;

    private boolean              inBackground = true;

    private Timer mActivityTransitionTimer;
    private TimerTask mActivityTransitionTimerTask;
    private final long MAX_ACTIVITY_TRANSITION_TIME_MS = 1000;

    public DeviceModeHandler(Context context) {
        keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    }

    /**
     * Called to change the state of the application
     * 
     * @param isInBackground
     */
    private void setInBackground(boolean isInBackground) {
        if (inBackground != isInBackground) {
            Logger.debug.log(LOG_TAG, "DeviceModeHandler.setInBackground: Changing BG state to: ", isInBackground);
            inBackground = isInBackground;

            BroadcastHandler.Application.sendBackgroundStateChanged();
        }
    }

    public boolean isInBackground() {
        return inBackground;
    }
    
    public boolean isPhoneLocked() {
        return keyguardManager.inKeyguardRestrictedInputMode();
    }

    // ============= DEVICE MODE HANDLING ==============//

    private void goToNormalMode() {
        // TODO
    }

    private void requestClientToSleep() {
        goToSleepMode(false);
    }

    private void goToSleepMode(boolean immediate) {
        // TODO
    }

    /**
     * a solution of detecting if the app is at background which got the most support here:
     * http://stackoverflow.com/questions/4414171/how-to-detect-when-an-android-app-goes-to-the-background-and-come-back-to-the-fo
     */
    public void startActivityTransitionTimer() {
        this.mActivityTransitionTimer = new Timer();

        //need to create time task everytime, otherwise timer would throw java.lang.IllegalStateException: TimerTask is canceled
        mActivityTransitionTimerTask = new TimerTask() {
            public void run() {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!inBackground) {
                            setInBackground(true);
                            requestClientToSleep();

                            // record last open time
                            long lastTime = System.currentTimeMillis();
                            NativeNotificationManager.getInstance().setLastTime(lastTime);
                            ApplicationEx.getInstance().onApplicationEnterBackground();
                        }
                    }
                });
            }
        };

        this.mActivityTransitionTimer.schedule(mActivityTransitionTimerTask,
                MAX_ACTIVITY_TRANSITION_TIME_MS);
    }

    public void stopActivityTransitionTimer() {
        if (this.mActivityTransitionTimerTask != null) {
            this.mActivityTransitionTimerTask.cancel();
            this.mActivityTransitionTimerTask = null;
        }

        if (this.mActivityTransitionTimer != null) {
            this.mActivityTransitionTimer.cancel();
            this.mActivityTransitionTimer = null;
        }

        if (inBackground) {
            setInBackground(false);
            goToNormalMode();

            ApplicationEx.getInstance().onApplicationEnterForeground();
        }
    }

}
