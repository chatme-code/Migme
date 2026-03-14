/**
 * Copyright (c) 2013 Project Goth
 *
 * StickerListViewHolder.java
 * Created Dec 9, 2014, 9:23:44 AM
 */

package com.projectgoth.ui.holder;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.imagefetcher.ImageHandler.ImageLoadListener;
import com.projectgoth.model.StickerStoreItem;
import com.projectgoth.ui.widget.ButtonEx;
import com.projectgoth.ui.widget.util.ButtonUtil;

/**
 * @author mapet
 * 
 */
public class StickerListViewHolder extends BaseViewHolder<StickerStoreItem> {

    private ImageView stickerPackImage;
    private TextView  stickerPackName;
    private TextView  stickerPackPrice;
    private ImageView stickerPackRibbon;
    private ButtonEx  optionBtn;
    private boolean   isPurchaseInProcess;

    public StickerListViewHolder(View view) {
        super(view);

        stickerPackImage = (ImageView) view.findViewById(R.id.sticker_pack_image);
        stickerPackName = (TextView) view.findViewById(R.id.sticker_pack_name);
        stickerPackPrice = (TextView) view.findViewById(R.id.sticker_pack_price);
        stickerPackRibbon = (ImageView) view.findViewById(R.id.sticker_pack_ribbon);
        optionBtn = (ButtonEx) view.findViewById(R.id.option_button);

        optionBtn.setOnClickListener(this);
    }

    public void setData(StickerStoreItem data, boolean isPurchaseInProcess) {
        this.isPurchaseInProcess = isPurchaseInProcess;
        setData(data);
    }

    @Override
    public void setData(StickerStoreItem data) {
        super.setData(data);

        stickerPackName.setText(data.getStoreItem().getName());

        final float roundedPrice = data.getStoreItem().getRoundedPrice();
        stickerPackPrice.setText(roundedPrice + Constants.SPACESTR + data.getStoreItem().getLocalCurrency());

        if (data.getPackData() != null && data.getPackData().getBaseEmoticonPack() != null) {
            String icon = data.getPackData().getBaseEmoticonPack().getIconUrl();

            if (icon != null) {
                if (icon.startsWith(Constants.LINK_DRAWABLE)) {
                    int resId = Tools.getDrawableResId(ApplicationEx.getContext(), icon);
                    stickerPackImage.setImageResource(resId);

                } else {

                    // loading animation start
                    if (stickerPackImage.getAnimation() == null) {
//                        stickerPackImage.setImageResource(R.drawable.ad_loadstaticchat_grey);
                        ImageHandler.imageRotationAnimationStart(stickerPackImage);
                    }

                    ImageHandler.getInstance().loadImageFromUrlWithCallback(stickerPackImage, icon, false, R.drawable.ad_loadstaticchat_grey, -1, new ImageLoadListener() {

                        @Override
                        public void onImageLoaded(Bitmap bitmap) {
                            stickerPackImage.clearAnimation();
                        }

                        @Override
                        public void onImageFailed(ImageView imageView) {
                            
                        }
                    });
                }
            }

            if (isPurchaseInProcess) {
                optionBtn.setIcon(R.drawable.ad_loadstatic_white);
                optionBtn.setType(ButtonUtil.BUTTON_TYPE_ORANGE);
            } else {
                if (data.getPackData().isOwnPack() || data.getStoreItem().getReferenceData().getOwned()) {
                    optionBtn.setIcon(R.drawable.ad_tick_white);
                    optionBtn.setType(ButtonUtil.BUTTON_TYPE_TURQUOISE);
                } else {
                    optionBtn.setIcon(R.drawable.ad_download_white);
                    optionBtn.setType(ButtonUtil.BUTTON_TYPE_ORANGE);
                }
            }
        }

        // Gift ribbon -- new, featured or none.
        if (data.getStoreItem().getFeatured()) {
            stickerPackRibbon.setImageResource(R.drawable.ad_featured_ribbon);
            stickerPackRibbon.setVisibility(View.VISIBLE);
        } else {
            if (data.getStoreItem().isNew()) {
                stickerPackRibbon.setImageResource(R.drawable.ad_new_ribbon);
                stickerPackRibbon.setVisibility(View.VISIBLE);
            } else {
                stickerPackRibbon.setVisibility(View.GONE);
            }
        }

        stickerPackImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

}
