/**
 * Copyright (c) 2013 Project Goth
 *
 * StickerContentViewHolder.java
 * Created Dec 3, 2014, 4:16:18 PM
 */

package com.projectgoth.ui.holder.content;

import android.content.Context;
import android.widget.ImageView;

import com.projectgoth.R;
import com.projectgoth.b.data.mime.StickerMimeData;
import com.projectgoth.controller.EmoticonsController;

/**
 * Represents a content view holder for stickers.
 * @author angelorohit
 *
 */
public class StickerContentViewHolder extends ContentViewHolder<StickerMimeData, ImageView> {

    /**
     * Constructor.
     * @param ctx       The {@link Context} to be used for inflation. 
     * @param mimeData  The {@link StickerMimeData} to be used as data for this holder.
     */
    public StickerContentViewHolder(Context ctx, StickerMimeData mimeData) {
        super(ctx, mimeData);
    }

    @Override
    public int getLayoutId() {
        return R.layout.content_view_sticker;
    }

    @Override
    public boolean applyMimeData() {
        if (mimeData != null) {
            EmoticonsController.getInstance().loadStickerEmoticonImage(view, mimeData.getHotkey(),
                    R.drawable.ad_gallery_grey);
            return true;
        }
        return false;
    }

    @Override
    public boolean getProperty(final Property property) {
        switch (property) {
            case NO_MESSAGE_BACKGROUND:
                return true;
            default:
                return super.getProperty(property);
        }
    }
}
