package com.projectgoth.listener;

import android.telephony.TelephonyManager;

import com.projectgoth.music.deezer.DeezerPlayerManager;

/**
 * Created by justinhsu on 6/23/15.
 */
public class MobilePhoneStateListener extends android.telephony.PhoneStateListener {
    private boolean isPlayingBeforeCall = false;

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);
        DeezerPlayerManager deezerPlayerManager = DeezerPlayerManager.getInstance();
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                if (deezerPlayerManager.isPaused() && isPlayingBeforeCall) {
                    isPlayingBeforeCall = !isPlayingBeforeCall;
                    deezerPlayerManager.play();
                }
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //TODO: should add method from blocking playing music
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                if (deezerPlayerManager.isPlaying()) {
                    isPlayingBeforeCall = true;
                    deezerPlayerManager.pause();
                }
                break;
        }
    }
}

