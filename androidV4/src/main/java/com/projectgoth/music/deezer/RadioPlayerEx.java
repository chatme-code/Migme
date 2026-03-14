package com.projectgoth.music.deezer;

import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.player.PlayerWrapper;
import com.deezer.sdk.player.RadioPlayer;
import com.deezer.sdk.player.event.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension of {@link com.deezer.sdk.player.RadioPlayer} to support more functions
 * Add functions:
 * 1. Support audio focus change
 * 2. Support current playing track info
 *
 * @author freddie.w
 */
public class RadioPlayerEx implements PlayerWrapper, RadioPlayerListener,
        AudioManager.OnAudioFocusChangeListener {

    private RadioPlayer     mRadioPlayer;
    private AudioManager    mAudioManager;

    protected final List<RadioPlayerListener> mRadioPlayerListenerList;

    public RadioPlayerEx(Context context, @NonNull RadioPlayer player) {
        mRadioPlayer = player;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mRadioPlayerListenerList = new ArrayList<RadioPlayerListener>();
    }

    public void playRadio(long radioId) {
        mRadioPlayer.playRadio(radioId);
    }

    public void addPlayerListener(RadioPlayerListener listener) {
        if (mRadioPlayerListenerList.isEmpty()) {
            mRadioPlayer.addPlayerListener(this);
        }

        mRadioPlayerListenerList.add(listener);
    }

    public void removePlayerListener(RadioPlayerListener listener) {
        if (!mRadioPlayerListenerList.isEmpty()) {
            mRadioPlayerListenerList.remove(listener);
        }

        if (mRadioPlayerListenerList.isEmpty()) {
            mRadioPlayer.removePlayerListener(this);
        }
    }

    @Override
    public PlayerState getPlayerState() {
        return mRadioPlayer.getPlayerState();
    }

    @Override
    public void setPlayerProgressInterval(long interval) {
        mRadioPlayer.setPlayerProgressInterval(interval);
    }

    @Override
    public void addOnPlayerProgressListener(OnPlayerProgressListener listener) {
        mRadioPlayer.addOnPlayerProgressListener(listener);
    }

    @Override
    public void removeOnPlayerProgressListener(OnPlayerProgressListener listener) {
        mRadioPlayer.removeOnPlayerProgressListener(listener);
    }

    @Override
    public void addOnPlayerStateChangeListener(OnPlayerStateChangeListener listener) {
        mRadioPlayer.addOnPlayerStateChangeListener(listener);
    }

    @Override
    public void removeOnPlayerStateChangeListener(OnPlayerStateChangeListener listener) {
        mRadioPlayer.removeOnPlayerStateChangeListener(listener);
    }

    @Override
    public void addOnPlayerErrorListener(OnPlayerErrorListener listener) {
        mRadioPlayer.addOnPlayerErrorListener(listener);
    }

    @Override
    public void removeOnPlayerErrorListener(OnPlayerErrorListener listener) {
        mRadioPlayer.removeOnPlayerErrorListener(listener);
    }

    @Override
    public void addOnBufferProgressListener(OnBufferProgressListener listener) {
        mRadioPlayer.addOnBufferProgressListener(listener);
    }

    @Override
    public void removeOnBufferProgressListener(OnBufferProgressListener listener) {
        mRadioPlayer.removeOnBufferProgressListener(listener);
    }

    @Override
    public void addOnBufferStateChangeListener(OnBufferStateChangeListener listener) {
        mRadioPlayer.addOnBufferStateChangeListener(listener);
    }

    @Override
    public void removeOnBufferStateChangeListener(OnBufferStateChangeListener listener) {
        mRadioPlayer.removeOnBufferStateChangeListener(listener);
    }

    @Override
    public void addOnBufferErrorListener(OnBufferErrorListener listener) {
        mRadioPlayer.addOnBufferErrorListener(listener);
    }

    @Override
    public void removeOnBufferErrorListener(OnBufferErrorListener listener) {
        mRadioPlayer.removeOnBufferErrorListener(listener);
    }

    @Override
    public void play() {
        mRadioPlayer.play();
    }

    @Override
    public void seek(long position) {
        mRadioPlayer.seek(position);
    }

    @Override
    public void pause() {
        mRadioPlayer.pause();
    }

    @Override
    public void stop() {
        mRadioPlayer.stop();
    }

    @Override
    public long getTrackDuration() {
        return mRadioPlayer.getTrackDuration();
    }

    @Override
    public boolean skipToPreviousTrack() {
        return mRadioPlayer.skipToPreviousTrack();
    }

    @Override
    public boolean skipToNextTrack() {
        return mRadioPlayer.skipToNextTrack();
    }

    @Override
    public boolean skipToTrack(int trackId) {
        return mRadioPlayer.skipToTrack(trackId);
    }

    @Override
    public void setRepeatMode(RepeatMode repeatMode) {
        mRadioPlayer.setRepeatMode(repeatMode);
    }

    @Override
    public RepeatMode getRepeatMode() {
        return mRadioPlayer.getRepeatMode();
    }

    @Override
    public long getPosition() {
        return mRadioPlayer.getPosition();
    }

    @Override
    public void release() {
        mRadioPlayer.release();
        mAudioManager.abandonAudioFocus(this);
    }

    @Override
    public boolean isAllowedToSeek() {
        return mRadioPlayer.isAllowedToSeek();
    }

    @Override
    public boolean setStereoVolume(float left, float right) {
        return mRadioPlayer.setStereoVolume(left, right);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange <= 0) {
            stop();
        }
    }

    @Override
    public void onAllTracksEnded() {
        for(RadioPlayerListener listener : mRadioPlayerListenerList) {
            listener.onAllTracksEnded();
        }

        DeezerPlayerManager.getInstance().clearBgPlayingTrack();
    }

    @Override
    public void onPlayTrack(Track track) {
        DeezerPlayerManager.getInstance().setBgPlayingTrack(track);
        for(RadioPlayerListener listener : mRadioPlayerListenerList) {
            listener.onPlayTrack(track);
        }
    }

    @Override
    public void onTrackEnded(Track track) {
        for(RadioPlayerListener listener : mRadioPlayerListenerList) {
            listener.onTrackEnded(track);
        }
    }

    @Override
    public void onRequestException(Exception error, Object requestId) {
        for(RadioPlayerListener listener : mRadioPlayerListenerList) {
            listener.onRequestException(error, requestId);
        }
    }

    @Override
    public void onTooManySkipsException() {
        for(RadioPlayerListener listener : mRadioPlayerListenerList) {
            listener.onTooManySkipsException();
        }
    }

}
