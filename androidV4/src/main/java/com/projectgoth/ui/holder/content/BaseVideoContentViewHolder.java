/**
 * Copyright (c) 2013 Project Goth
 *
 * ImageContentViewHolder.java
 * Created Dec 2, 2014, 11:33:38 AM
 */

package com.projectgoth.ui.holder.content;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.VideoView;
import com.projectgoth.R;
import com.projectgoth.b.data.mime.VideoMimeData;
import com.projectgoth.ui.widget.ImageViewEx.IconOverlay;


/**
 * Represents a content view holder for video content.
 *
 * Adapted from http://stackoverflow.com/questions/13814055/how-to-play-youtube-videos-in-android-video-view
 *
 * @author jthlim
 */
public abstract class BaseVideoContentViewHolder<T extends VideoMimeData> extends ContentViewHolder<T, FrameLayout> {

    protected       VideoView       videoView;

    /**
     * Constructor.
     * @param ctx           The {@link android.content.Context} to be used for inflation.
     * @param mimeData      The {@link com.projectgoth.b.data.mime.ImageMimeData} to be used as data for this holder.
     * @param iconOverlay   The {@link com.projectgoth.ui.widget.ImageViewEx.IconOverlay} to be used as the overlay for the {@link com.projectgoth.ui.widget.ImageViewEx}.
     */
    protected BaseVideoContentViewHolder(final Context ctx, final T mimeData, final IconOverlay iconOverlay) {
        super(ctx, mimeData);
    }

    @Override
    public int getLayoutId() {
        return R.layout.content_view_video;
    }

    @Override
    protected void initializeView() {
        videoView = (VideoView) view.findViewById(R.id.video);
        videoView.setZOrderOnTop(true);
    }

}
