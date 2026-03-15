/**
 * Copyright (c) 2013 Project Goth
 *
 * MusicAdaper.java
 * Created Mar 13, 2015, 6:24:26 PM
 */

package com.projectgoth.ui.adapter;

import java.util.List;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.deezer.sdk.model.Track;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.ui.holder.MusicTrackViewHolder;

/**
 * @author freddie.w
 * 
 */
public class RadioTrackAdaper extends BaseAdapter {

    private List<Track>    mTrackList;
    private LayoutInflater mInflater;
    private Track          mCurrentPlayTrack;

    public RadioTrackAdaper() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    public void setTrackList(@NonNull List<Track> list) {
        this.mTrackList = list;
    }

    @Override
    public int getCount() {
        return mTrackList.size();
    }

    @Override
    public Object getItem(int position) {
        return mTrackList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Track track = (Track) getItem(position);
        MusicTrackViewHolder trackViewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_deezer_track_item, null);
            trackViewHolder = new MusicTrackViewHolder(convertView);
            convertView.setTag(R.id.holder, trackViewHolder);

        } else {
            trackViewHolder = (MusicTrackViewHolder) convertView.getTag(R.id.holder);
        }
        
        if (mCurrentPlayTrack != null) {
            trackViewHolder.setCurrentlyPlayingTrack(mCurrentPlayTrack);
        }
        trackViewHolder.setData(track);
        
        return convertView;
    }
    
    public void setCurrentPlayTrack(final Track track) {
        mCurrentPlayTrack = track;
        notifyDataSetChanged();
    }
}
