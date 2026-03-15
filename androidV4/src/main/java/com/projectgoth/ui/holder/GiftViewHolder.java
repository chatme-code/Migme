/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftCategoryViewHolder.java
 * Created Dec 8, 2013, 1:21:46 PM
 */

package com.projectgoth.ui.holder;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.StoreItem;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.EmoticonsController;
import com.projectgoth.datastore.Session;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.imagefetcher.UIUtils;

/**
 * @author mapet
 * 
 */
public class GiftViewHolder extends BaseViewHolder<StoreItem> {

    private final ImageView giftImage;
    private final TextView  giftName;
    private final TextView  giftPrice;
    private final ImageView giftClassLabel;
    private final ImageView giftRibbon;
    private boolean         isInChat = false;

    public GiftViewHolder(View view) {
        super(view);

        giftImage = (ImageView) view.findViewById(R.id.gift_image);
        giftName = (TextView) view.findViewById(R.id.gift_name);
        giftPrice = (TextView) view.findViewById(R.id.gift_price);
        giftClassLabel = (ImageView) view.findViewById(R.id.gift_class_label);
        giftRibbon = (ImageView) view.findViewById(R.id.gift_ribbon);

        giftImage.setOnClickListener(this);
        giftName.setOnClickListener(this);
        giftPrice.setOnClickListener(this);
    }

    @Override
    public void setData(StoreItem data) {
        super.setData(data);

        giftName.setText(data.getName());

        // gift class label
        if (data.isGroupOnly()) {
            giftClassLabel.setImageResource(R.drawable.ad_store_ggroup);
            giftClassLabel.setVisibility(View.VISIBLE);
        } else if (data.isPremium()) {
            giftClassLabel.setImageResource(R.drawable.ad_store_pgift);
            giftClassLabel.setVisibility(View.VISIBLE);
        } else {
            giftClassLabel.setVisibility(View.GONE);
        }

        if (!data.isAvailableForLevel(Session.getInstance().getMigLevel())) {
            Tools.setGrayscaleFilter(giftImage);
            giftPrice.setText(String.format(I18n.tr("Level %s and above"), data.getMigLevelMin()));

        } else if (!isInChat && data.isGroupOnly()) {
            Tools.setGrayscaleFilter(giftImage);
            giftPrice.setText(I18n.tr("Group exclusive"));

        } else {
            final float roundedPrice = data.getRoundedPrice();
            giftPrice.setText(roundedPrice + Constants.SPACESTR + data.getLocalCurrency());
            giftImage.clearColorFilter();
        }

        // Gift ribbon -- new, featured or none.
        if (data.getFeatured()) {
            giftRibbon.setImageResource(R.drawable.ad_featured_ribbon);
            giftRibbon.setVisibility(View.VISIBLE);
        } else {
            if (data.isNew()) {
                giftRibbon.setImageResource(R.drawable.ad_new_ribbon);
                giftRibbon.setVisibility(View.VISIBLE);
            } else {
                giftRibbon.setVisibility(View.GONE);
            }
        }

        if (giftImage.getAnimation() == null) {
            Bitmap loadBitmap = UIUtils.getBitmapFromDrawableResource(giftImage.getContext(), R.drawable.ad_loadstaticchat_grey);
            giftImage.setImageBitmap(loadBitmap);
            ImageHandler.imageRotationAnimationStart(giftImage);
        }

        final String hotkey = data.getGiftHotkey();
        giftImage.setTag(hotkey);
        if (!TextUtils.isEmpty(hotkey)) {
            int size = ApplicationEx.getDimension(R.dimen.vg_request_size);
            EmoticonsController.getInstance().loadResizedBaseEmoticonImage(null, hotkey, size,
                    R.drawable.ad_loadstaticchat_grey, new ImageHandler.ImageLoadListener() {
                      @Override
                        public void onImageLoaded(Bitmap bitmap) {
                          if (giftImage != null) {
                              giftImage.clearAnimation();
                              if (giftImage.getTag().equals(hotkey) && bitmap != null) {
                                  giftImage.setImageBitmap(bitmap);
                              }
                          }
                        }

                        @Override
                        public void onImageFailed(ImageView imageView) {
                            //TODO: handle error case
                        }
                    });
        }

        // the loading icon is small, set it center inside so that it is not stretched
        if (isDisplayingLoadingIcon()) {
            giftImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            giftImage.setImageResource(R.drawable.ad_loadstaticchat_grey);
        } else {
            giftImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
    }

    private boolean isDisplayingLoadingIcon() {
        Object tag = giftImage.getTag(R.id.image_loading);
        if (tag != null) {
            Boolean isLoading = (Boolean) tag;
            return isLoading.booleanValue();
        } else {
            return false;
        }
    }

    public boolean isGiftingInChat() {
        return isInChat;
    }

    public void setGiftingInChat(boolean isGiftingInChat) {
        this.isInChat = isGiftingInChat;
    }

}
