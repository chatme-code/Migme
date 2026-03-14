/**
 * Copyright (c) 2013 Project Goth
 *
 * UnlockedGiftViewHolder.java
 * Created Jan 6, 2015, 11:56:34 AM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.b.data.StoreUnlockedItem;
import com.projectgoth.controller.EmoticonsController;

/**
 * @author mapet
 * 
 */
public class UnlockedGiftViewHolder extends BaseViewHolder<StoreUnlockedItem> {
    
    private ImageView   giftImage;
    private TextView    giftName;

    public UnlockedGiftViewHolder(View view) {
        super(view);

        giftImage = (ImageView) view.findViewById(R.id.gift_image);
        giftName = (TextView) view.findViewById(R.id.gift_name);
    }

    @Override
    public void setData(StoreUnlockedItem data) {
        super.setData(data);
        
        giftName.setText(data.getStoreItemData().getName() + String.format(" (%d)", data.getCount()));
        
        String hotkey = data.getStoreItemData().getGiftHotkey();
        if (hotkey != null) {
            EmoticonsController.getInstance().loadGiftEmoticonImage(giftImage, hotkey,
                    R.drawable.ad_loadstaticchat_grey);
        }

        giftImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }
}
