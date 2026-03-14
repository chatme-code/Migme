package com.projectgoth.music.deezer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.deezer.sdk.player.event.OnPlayerErrorListener;
import com.deezer.sdk.player.event.RadioPlayerListener;

/**
 * Created by justinhsu on 3/23/15.
 */
public abstract class BaseDeezerPlayback extends RelativeLayout implements PlaybackHandler ,RadioPlayerListener, OnPlayerErrorListener {
    public BaseDeezerPlayback(Context context) {
        super(context);
    }

    public BaseDeezerPlayback(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseDeezerPlayback getPlayBack() {
        return this;
    }
}
