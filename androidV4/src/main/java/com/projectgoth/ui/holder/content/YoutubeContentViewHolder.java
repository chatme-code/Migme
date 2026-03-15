/**
 * Copyright (c) 2013 Project Goth
 *
 * YoutubeContentViewHolder.java
 * Created Dec 2, 2014, 4:11:22 PM
 */

package com.projectgoth.ui.holder.content;

import android.content.Context;

import com.projectgoth.b.data.mime.YoutubeMimeData;
import com.projectgoth.ui.widget.ImageViewEx.IconOverlay;


/**
 * Represents a content view for youtube content.
 * @author angelorohit
 */
public class YoutubeContentViewHolder extends BaseImageContentViewHolder<YoutubeMimeData> {

    /**
     * Constructor.
     * @param ctx       The {@link Context} to be used for inflation. 
     * @param mimeData  The {@link YoutubeMimeData} to be used as data for this holder.
     */
    public YoutubeContentViewHolder(Context ctx, YoutubeMimeData mimeData) {
        super(ctx, mimeData, IconOverlay.PLAY);
    }

    @Override
    protected boolean isPlayableThumbnail() {
        return true;
    }

    @Override
    protected boolean shouldEnsureYoutubeThumbnail() {
        return true;
    }
}