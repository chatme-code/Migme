/**
 * Copyright (c) 2013 Project Goth
 *
 * DeezerPlayerManager.java
 * Created Apr 7, 2015, 4:20:02 PM
 */

package com.projectgoth.music.deezer;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RemoteControlClient;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.deezer.sdk.model.Album;
import com.deezer.sdk.model.Artist;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.player.RadioPlayer;
import com.deezer.sdk.player.event.PlayerState;
import com.deezer.sdk.player.event.RadioPlayerListener;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Constants;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.listener.MobilePhoneStateListener;
import com.projectgoth.notification.system.NativeNotificationManager;
import com.projectgoth.ui.holder.content.action.DeezerContentViewAction;
import com.projectgoth.util.AndroidLogger;

/**
 * @author shiyukun
 */
public class DeezerPlayerManager implements AudioManager.OnAudioFocusChangeListener {

    private static final String         TAG                             = AndroidLogger.makeLogTag(DeezerPlayerManager.class);
    private static DeezerPlayerManager  INSTANCE;
    private RadioPlayerEx               mRadioPlayer;
    private DeezerConnect               mDeezerConnect;
    private static final String         DEEZER_APP_ID                   = "154351";
    public static final int             INVALID_RADIO_ID                = -1;
    // used for landing page background music
    private long                        mBgPlayingRadioId               = INVALID_RADIO_ID;
    private Track                       mBgPlayingTrack;
    private RadioPlayerListener         mCurrentListener;
    private DeezerContentViewAction     mCurrentContentViewAction;
    private String                      mCurrentPlayerId                = Constants.BLANKSTR;
    private BaseDeezerPlayback          mCurrentPlayback;
    private AudioManager                mAudioManager;
    private RemoteControlClient         mRemoteControlClient;
    private ComponentName               mMediaButtonReceiverComponet;
    private AudioFocus                  mAudioFocus;

    private DeezerPlayerManager() {
        TelephonyManager telephonyManager = (TelephonyManager) ApplicationEx.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new MobilePhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
        mDeezerConnect = new DeezerConnect(ApplicationEx.getContext(), DEEZER_APP_ID);
        if (mRadioPlayer != null && mRadioPlayer.getPlayerState() == PlayerState.RELEASED) {
            mRadioPlayer = null;
        }

        if (mRadioPlayer == null) {
            try {
                RadioPlayer radioPlayer = new RadioPlayer(ApplicationEx.getInstance(), mDeezerConnect,
                        new WifiAndMobileNetworkStateChecker());
                mRadioPlayer = new RadioPlayerEx(ApplicationEx.getContext(), radioPlayer);
            } catch (TooManyPlayersExceptions tooManyPlayersExceptions) {
                Log.e(TAG, "too many radio instance, it should not happen");
            } catch (DeezerError deezerError) {
                Log.e(TAG, "couldn't create deezer player, reason: " + deezerError.getMessage());
            }
        }
        initLockscreen();
    }

    public synchronized static DeezerPlayerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DeezerPlayerManager();
        }
        return INSTANCE;
    }

    public DeezerConnect getDeezerConnect() {
        return mDeezerConnect;
    }

    public RadioPlayerEx getRadioPlayerEx() {
        return mRadioPlayer;
    }

    public void playRadio(long radioId) {
        if (mRadioPlayer == null) {
            return;
        }
        mRadioPlayer.playRadio(radioId);
        lockscreenAction(NativeNotificationManager.BUTTON_PLAY_ID);
    }

    public void pause() {
        if (mRadioPlayer == null) {
            return;
        }
        mRadioPlayer.pause();
        if (NativeNotificationManager.getInstance().isDisplayNotificationPlayer()) {
            NativeNotificationManager.getInstance().displayNotificationBar(null);
        }
        lockscreenAction(NativeNotificationManager.BUTTON_PAUSE_ID);
    }

    public void stop() {
        if (mRadioPlayer == null) {
            return;
        }
        mRadioPlayer.stop();
        clearBgPlayingRadio();
        NativeNotificationManager.getInstance().clearNotify(NativeNotificationManager.PLAYER_NOTIFICATION_ID);

    }

    public void play() {
        if (mRadioPlayer == null)
            return;
        mRadioPlayer.play();
        if (NativeNotificationManager.getInstance().isDisplayNotificationPlayer()) {
            NativeNotificationManager.getInstance().displayNotificationBar(null);
        }
        lockscreenAction(NativeNotificationManager.BUTTON_PLAY_ID);
    }

    public void playNext() {
        if (mRadioPlayer == null) {
            return;
        }
        PlayerState status = mRadioPlayer.getPlayerState();
        if (status == PlayerState.PLAYING || status == PlayerState.PAUSED) {
            mRadioPlayer.skipToNextTrack();
        }
        NativeNotificationManager.getInstance().displayNotificationBar(null);
        lockscreenAction(NativeNotificationManager.BUTTON_NEXT_ID);
    }

    public boolean isPlaying() {
        if (mRadioPlayer == null) {
            return false;
        }
        PlayerState status = mRadioPlayer.getPlayerState();
        return status == PlayerState.PLAYING;
    }

    public boolean isPaused() {
        if (mRadioPlayer == null) {
            return true;
        }
        PlayerState status = mRadioPlayer.getPlayerState();
        return status == PlayerState.PAUSED;
    }

    public PlayerState getPlayerState() {
        if (mRadioPlayer == null)
            return PlayerState.INITIALIZING;
        return mRadioPlayer.getPlayerState();
    }

    public long getBgPlayingRadioId() {
        return mBgPlayingRadioId;
    }

    public void setBgPlayingRadioId(long bgPlayingRadioId) {
        this.mBgPlayingRadioId = bgPlayingRadioId;
    }

    public void clearBgPlayingRadio() {
        this.mBgPlayingRadioId = INVALID_RADIO_ID;
        this.mBgPlayingTrack = null;
    }

    public void clearBgPlayingTrack() {
        this.mBgPlayingTrack = null;
    }

    public Track getBgPlayingTrack() {
        return mBgPlayingTrack;
    }

    public void setBgPlayingTrack(Track bgPlayingTrack) {
        this.mBgPlayingTrack = bgPlayingTrack;
    }

    public long getTrackDuration() {
        if (mRadioPlayer == null)
            return 0;
        return mRadioPlayer.getTrackDuration();
    }

    public void clearRadioPlayerListener() {
        if (mRadioPlayer == null) {
            return;
        }
        if (mCurrentListener != null) {
            mRadioPlayer.removePlayerListener(mCurrentListener);
        }
        mCurrentContentViewAction = null;
        mCurrentListener = null;
    }

    public void resetRadioPlayerListener(DeezerContentViewAction listener) {
        if (mRadioPlayer == null) {
            return;
        }

        if (mCurrentListener != null) {
            if (listener != mCurrentListener) {
                mRadioPlayer.removePlayerListener(mCurrentListener);
            } else {
                return;
            }
        }
        mCurrentContentViewAction = listener;
        mCurrentListener = listener;
        addListener(mCurrentListener);
    }

    public void resetRadioPlayerListener(BaseDeezerPlayback listener) {
        if (mRadioPlayer == null) {
            return;
        }

        if (mCurrentListener != null) {
            if (listener != mCurrentListener) {
                mRadioPlayer.removePlayerListener(mCurrentListener);
            } else {
                return;
            }
        }
        mCurrentPlayback = listener;
        mCurrentListener = listener;
        addListener(mCurrentListener);
    }

    public void addListener(RadioPlayerListener listener) {
        if (mRadioPlayer == null) {
            return;
        }
        mRadioPlayer.addPlayerListener(listener);
    }

    public void attachPlayer(DeezerContentViewAction playback) {
        if (mRadioPlayer == null) {
            return;
        }
        mRadioPlayer.addOnBufferErrorListener(playback.getPlayBackHandler());
        mRadioPlayer.addOnBufferStateChangeListener(playback.getPlayBackHandler());
        mRadioPlayer.addOnBufferProgressListener(playback.getPlayBackHandler());
        mRadioPlayer.addOnPlayerStateChangeListener(playback.getPlayBackHandler());
        mRadioPlayer.addOnPlayerProgressListener(playback.getPlayBackHandler());

    }

    public void attachPlayer(BaseDeezerPlayback playback) {
        if (mRadioPlayer == null) {
            return;
        }
        mRadioPlayer.addOnBufferErrorListener(playback);
        mRadioPlayer.addOnBufferStateChangeListener(playback);
        mRadioPlayer.addOnBufferProgressListener(playback);
        mRadioPlayer.addOnPlayerStateChangeListener(playback);
        mRadioPlayer.addOnPlayerProgressListener(playback);
    }

    public void attachPlayer(DeezerPlayback playback) {
        mRadioPlayer.addOnBufferErrorListener(playback);
        mRadioPlayer.addOnBufferStateChangeListener(playback);
        mRadioPlayer.addOnBufferProgressListener(playback);

        mRadioPlayer.addOnPlayerStateChangeListener(playback);
        mRadioPlayer.addOnPlayerProgressListener(playback);
    }

    public void detachPlayer(DeezerPlayback playback) {
        mRadioPlayer.removeOnBufferErrorListener(playback);
        mRadioPlayer.removeOnBufferStateChangeListener(playback);
        mRadioPlayer.removeOnBufferProgressListener(playback);
        mRadioPlayer.removeOnPlayerStateChangeListener(playback);
        mRadioPlayer.removeOnPlayerProgressListener(playback);
    }

    public void detachPlayback() {
        removeAllListener();
        mCurrentPlayback = null;
    }

    public void release() {
        if (mRadioPlayer != null) {
            mRadioPlayer.release();
        }
    }

    public void removeAllListener() {
        if (mRadioPlayer == null) {
            return;
        }
        if (mCurrentContentViewAction != null) {
            mRadioPlayer.removeOnBufferErrorListener(mCurrentContentViewAction.getPlayBackHandler());
            mRadioPlayer.removeOnBufferProgressListener(mCurrentContentViewAction.getPlayBackHandler());
            mRadioPlayer.removeOnBufferStateChangeListener(mCurrentContentViewAction.getPlayBackHandler());
            mRadioPlayer.removeOnPlayerStateChangeListener(mCurrentContentViewAction.getPlayBackHandler());
            mRadioPlayer.removeOnPlayerProgressListener(mCurrentContentViewAction.getPlayBackHandler());
        }
        if (mCurrentPlayback != null) {
            mRadioPlayer.removeOnBufferErrorListener(mCurrentPlayback);
            mRadioPlayer.removeOnBufferProgressListener(mCurrentPlayback);
            mRadioPlayer.removeOnBufferStateChangeListener(mCurrentPlayback);
            mRadioPlayer.removeOnPlayerStateChangeListener(mCurrentPlayback);
            mRadioPlayer.removeOnPlayerProgressListener(mCurrentPlayback);
        }
    }

    public void removeListener(RadioPlayerListener listener) {
        if (mRadioPlayer != null)
            mRadioPlayer.removePlayerListener(listener);
    }

    public PlaybackHandler getCurrentPlaybackHandler() {
        if (mCurrentContentViewAction != null) {
            return mCurrentContentViewAction.getPlayBackHandler();
        } else if (mCurrentPlayback != null) {
            return mCurrentPlayback;
        }
        return null;
    }

    public String getCurrentPlayerId() {
        return mCurrentPlayerId;
    }

    public void setCurrentPlayerId(String currentPlayerId) {
        this.mCurrentPlayerId = currentPlayerId;
    }

    public String getCurrentTrackArtistname() {
        if (mBgPlayingTrack == null) {
            return "";
        } else {
            Artist artist = mBgPlayingTrack.getArtist();
            if (artist != null) {
                return artist.getName();
            } else {
                return "";
            }
        }
    }

    public String getCurrentTrackTitle() {
        if (mBgPlayingTrack == null) {
            return "";
        } else {
            return mBgPlayingTrack.getTitle();
        }
    }

    public Album getCurrentAlbum() {
        if (mBgPlayingTrack != null) {
            return mBgPlayingTrack.getAlbum();
        } else {
            return null;
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch(focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                pause();
                break;
        }
    }

    enum AudioFocus {
        NoFocusNoDuck,
        NoFocusCanDuck,
        Focused
    }

    private void initLockscreen() {
        if (!UIUtils.hasICS()) {
            return;
        }
        mAudioManager = (AudioManager) ApplicationEx.getContext().getSystemService(Context.AUDIO_SERVICE);
        mMediaButtonReceiverComponet = new ComponentName(ApplicationEx.getContext(), MusicIntentReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(mMediaButtonReceiverComponet);
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.setComponent(mMediaButtonReceiverComponet);
        PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(ApplicationEx.getContext(), 0, intent, 0);
        mRemoteControlClient = new RemoteControlClient(mediaPendingIntent);
        mAudioManager.registerRemoteControlClient(mRemoteControlClient);
    }

    public void lockscreenAction(int action) {
        if (mRemoteControlClient == null || !UIUtils.hasICS())
            return;
        if (action == NativeNotificationManager.BUTTON_PLAY_ID) {
            tryToGetAudioFocus();
            mAudioManager.registerMediaButtonEventReceiver(mMediaButtonReceiverComponet);
            mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
            mRemoteControlClient.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                    RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                    RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
                    RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS);

        } else if (action == NativeNotificationManager.BUTTON_PAUSE_ID) {
            mAudioManager.registerMediaButtonEventReceiver(mMediaButtonReceiverComponet);
            mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);

        } else if (action == NativeNotificationManager.BUTTON_NEXT_ID) {
            mAudioManager.registerMediaButtonEventReceiver(mMediaButtonReceiverComponet);

        } else if (action == NativeNotificationManager.BUTTON_STOP_ID) {

        }
    }

    private void tryToGetAudioFocus() {
        boolean flag = AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (mAudioFocus != AudioFocus.Focused && mAudioManager != null && flag) {
            mAudioFocus = AudioFocus.Focused;
        }
    }
}
