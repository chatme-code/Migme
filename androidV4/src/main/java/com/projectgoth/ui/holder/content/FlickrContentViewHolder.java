/**
 * Copyright (c) 2013 Project Goth
 *
 * FlickrContentViewHolder.java
 * Created Feb 10, 2015, 2:44:33 PM
 */

package com.projectgoth.ui.holder.content;

import android.content.Context;

import com.projectgoth.R;
import com.projectgoth.b.data.mime.FlickrMimeData;
import com.projectgoth.common.Constants;
import com.projectgoth.common.TextUtils;
import com.projectgoth.controller.GifController;
import com.projectgoth.datastore.MimeDatastore;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.ui.widget.ImageViewEx.IconOverlay;

/**
 * @author mapet
 * 
 */
public class FlickrContentViewHolder extends BaseImageContentViewHolder<FlickrMimeData> {

    public FlickrContentViewHolder(Context ctx, FlickrMimeData mimeData) {
        super(ctx, mimeData, IconOverlay.NONE);
    }

    @Override
    protected boolean isPlayableThumbnail() {
        return true;
    }

    @Override
    public boolean applyMimeData() {
        if (mimeData != null) {
            String thumbnailURL = Constants.BLANKSTR;

            if (TextUtils.isEmpty(mimeData.getThumbnailUrl())) {
                FlickrMimeData flickrMimeData = (FlickrMimeData) MimeDatastore.getInstance().getFlickrMimeData(mimeData.getUrl());
                if (flickrMimeData != null) {
                    thumbnailURL = flickrMimeData.getThumbnailUrl();
                }
            } else {
                thumbnailURL = mimeData.getThumbnailUrl();
            }

            int placeHolderResId = R.drawable.ad_gallery_grey;
            ImageHandler.getInstance().loadImage(thumbnailURL, view, placeHolderResId);
            return true;
        }

        return false;
    }

}
