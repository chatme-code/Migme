/**
 * Copyright (c) 2013 Project Goth
 *
 * MusicViewHolder.java
 * Created Mar 13, 2015, 6:29:56 PM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.TextView;

import com.deezer.sdk.model.Track;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;

/**
 * @author freddie.w
 * 
 */
public class MusicTrackViewHolder extends BaseViewHolder<Track> {

    private final TextView mTrackName;
    private final TextView mArtistName;
    private Track          mCurrentlyPlayingTrack;

    public MusicTrackViewHolder(View rootView) {
        super(rootView);
        mTrackName = (TextView) rootView.findViewById(R.id.trackName);
        mArtistName = (TextView) rootView.findViewById(R.id.artistName);
    }

    @Override
    public void setData(Track item) {
        super.setData(item);
        
        mTrackName.setTextColor(ApplicationEx.getColor(R.color.white_text_color));
        mArtistName.setTextColor(ApplicationEx.getColor(R.color.white_text_color));
        
        if (mCurrentlyPlayingTrack != null &&
                (item.getId() == mCurrentlyPlayingTrack.getId())) {
            mTrackName.setTextColor(ApplicationEx.getColor(R.color.default_green));
            mArtistName.setTextColor(ApplicationEx.getColor(R.color.default_green));
        } 

        mTrackName.setText(item.getTitle());
        mArtistName.setText(item.getArtist().getName());
    }

    public void setCurrentlyPlayingTrack(final Track currentlyPlayingTrack) {
        this.mCurrentlyPlayingTrack = currentlyPlayingTrack;
    }
}
