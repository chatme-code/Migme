package com.projectgoth.music.deezer;

import com.deezer.sdk.model.Track;

/**
 * Created by justinhsu on 4/13/15.
 */
public interface DeezerPlaybackHandler {

    public void setTrackInfo(Track track);

    public void getTrackList(long radioId);

    public void requestTrackList(final long radioId);

    public void setPlayingCoverVisible(boolean toShow);

    public void setProgress(double percent);


}
