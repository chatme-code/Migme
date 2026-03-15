/**
 * Copyright (c) 2013 Project Goth
 *
 * AttachmentStickerViewHolder.java
 * Created Jul 25, 2013, 2:48:06 PM
 */

package com.projectgoth.ui.holder;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.controller.EmoticonsController;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.nemesis.model.Sticker;

/**
 * @author mapet
 * 
 */
public class AttachmentStickerViewHolder extends BaseViewHolder<Object> {

    private final RelativeLayout container;
    private final ImageView mItem;

    public AttachmentStickerViewHolder(View rootView) {
        super(rootView);
        container = (RelativeLayout) rootView.findViewById(R.id.sticker_container);
        mItem = (ImageView) rootView.findViewById(R.id.item);
    }

    @Override
    public void setData(Object baseEmoticon) {
        super.setData(baseEmoticon);

        // loading animation start
        if (mItem.getAnimation() == null) {
            ImageHandler.imageRotationAnimationStart(mItem);
        }

        Sticker s = (Sticker) baseEmoticon;
        int size = ApplicationEx.getDimension(R.dimen.sticker_height);
        EmoticonsController.getInstance().loadResizedBaseEmoticonImage(mItem, s.getMainHotkey(),
                size, R.drawable.ad_loadstaticchat_grey, new ImageHandler.ImageLoadListener() {

                    @Override
                    public void onImageLoaded(Bitmap bitmap) {
                        mItem.clearAnimation();
                    }

                    @Override
                    public void onImageFailed(ImageView imageView) {
                        // TODO: handle error case
                    }
                });
    }

    public void setVerticalSpacing(int verticalSpacing) {
        container.setPadding(container.getPaddingLeft(), 
                verticalSpacing / 2,
                container.getPaddingRight(),
                verticalSpacing / 2);
    }
}
