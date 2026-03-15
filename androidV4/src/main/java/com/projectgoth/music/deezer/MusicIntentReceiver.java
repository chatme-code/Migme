package com.projectgoth.music.deezer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.projectgoth.notification.system.NativeNotificationManager;

/**
 * Created by shiyukun on 7/5/15.
 */
public class MusicIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {

            // send an intent to our MusicService to telling it to pause the audio
            DeezerPlayerManager.getInstance().lockscreenAction(NativeNotificationManager.BUTTON_PAUSE_ID);
        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                return;
            }
            int keyCode = keyEvent.getKeyCode();
            switch (keyCode) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    if(DeezerPlayerManager.getInstance().isPlaying()){
                        DeezerPlayerManager.getInstance().pause();
                    }else{
                        DeezerPlayerManager.getInstance().play();
                    }
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    DeezerPlayerManager.getInstance().play();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    DeezerPlayerManager.getInstance().pause();
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    DeezerPlayerManager.getInstance().playNext();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    break;
            }
        }
    }
}