/**
 * Copyright (c) 2013 Project Goth
 *
 * ImageContentViewHolder.java
 * Created Dec 3, 2014, 4:00:21 PM
 */

package com.projectgoth.ui.holder.content;

import android.content.Context;

import com.migme.commonlib.enums.ImageFileType;
import com.projectgoth.b.data.mime.ImageMimeData;
import com.projectgoth.ui.widget.ImageViewEx.IconOverlay;


/**
 * Represents a content view holder for regular images (includes GIF images as well).
 * @author angelorohit
 *
 */
public class ImageContentViewHolder extends BaseImageContentViewHolder<ImageMimeData> {
    
    /**
     * Constructor.
     * @param ctx       The {@link Context} to be used for inflation. 
     * @param mimeData  The {@link ImageMimeData} to be used as data for this holder.
     */
    public ImageContentViewHolder(Context ctx, ImageMimeData mimeData) {
        super(ctx, mimeData, IconOverlay.NONE);
        
        if (ImageFileType.isGifUrl(mimeData.getUrl())) {
            setIconOverlay(IconOverlay.GIF);
        }
    }
}
