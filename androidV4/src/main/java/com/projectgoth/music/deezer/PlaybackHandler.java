package com.projectgoth.music.deezer;

import com.deezer.sdk.player.event.OnBufferErrorListener;
import com.deezer.sdk.player.event.OnBufferProgressListener;
import com.deezer.sdk.player.event.OnBufferStateChangeListener;
import com.deezer.sdk.player.event.OnPlayerProgressListener;
import com.deezer.sdk.player.event.OnPlayerStateChangeListener;

public interface PlaybackHandler extends
        OnPlayerProgressListener,
        OnBufferProgressListener,
        OnPlayerStateChangeListener,
        OnBufferStateChangeListener,
        OnBufferErrorListener {
}
