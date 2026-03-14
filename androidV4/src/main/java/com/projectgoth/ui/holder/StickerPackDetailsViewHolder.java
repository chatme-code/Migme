/**
 * Copyright (c) 2013 Project Goth
 *
 * StickerPackDetailsViewHolder.java
 * Created Dec 16, 2014, 6:11:35 PM
 */

package com.projectgoth.ui.holder;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.controller.EmoticonsController;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.nemesis.model.Sticker;

/**
 * @author mapet
 * 
 */
public class StickerPackDetailsViewHolder extends BaseViewHolder<Sticker> {

    private final ImageView packItem;

    public StickerPackDetailsViewHolder(View rootView) {
        super(rootView);
        packItem = (ImageView) rootView.findViewById(R.id.item);
    }

    @Override
    public void setData(Sticker data) {
        super.setData(data);

        // loading animation start
        if (packItem.getAnimation() == null) {
            ImageHandler.imageRotationAnimationStart(packItem);
        }

        int size = ApplicationEx.getDimension(R.dimen.sticker_height);
        EmoticonsController.getInstance().loadResizedBaseEmoticonImage(packItem, data.getMainHotkey(),
                size, R.drawable.ad_loadstaticchat_grey, new ImageHandler.ImageLoadListener() {
                    @Override
                    public void onImageLoaded(Bitmap bitmap) {
                        packItem.clearAnimation();
                    }

                    @Override
                    public void onImageFailed(ImageView imageView) {
                        //TODO: handle error case
                    }
                });
    }

}
