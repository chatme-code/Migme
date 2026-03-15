/**
 * Copyright (c) 2013 Project Goth
 *
 * AttachementUsedChatItemViewHolder.java
 * Created 23 May, 2014, 2:29:28 pm
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.controller.EmoticonsController;
import com.projectgoth.enums.UsedChatItemType;
import com.projectgoth.model.UsedChatItem;

/**
 * @author Dan
 * 
 */
public class UsedChatItemViewHolder extends BaseViewHolder<Object> {

    private final RelativeLayout container;
    private final ImageView mItem;
    private final ImageView mGiftIndicator;

    /**
     * @param rootView
     */
    public UsedChatItemViewHolder(View rootView) {
        super(rootView);
        container = (RelativeLayout) rootView.findViewById(R.id.used_item_container);
        mItem = (ImageView) rootView.findViewById(R.id.item);
        mGiftIndicator = (ImageView) rootView.findViewById(R.id.gift_indicator);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.projectgoth.ui.holder.BaseViewHolder#setData(java.lang.Object)
     */
    @Override
    public void setData(Object data) {
        super.setData(data);

        UsedChatItem item = (UsedChatItem) data;
        UsedChatItemType type = item.getType();

        switch (type) {
            case STICKER:
            {
                int size = ApplicationEx.getDimension(R.dimen.sticker_height);
                EmoticonsController.getInstance().loadResizedBaseEmoticonImage(mItem, item.getHotkey(), 
                        size, R.drawable.ad_loadstatic_grey, null);
                mGiftIndicator.setVisibility(View.GONE);
            }
                break;

            case GIFT:
            {
                int size = ApplicationEx.getDimension(R.dimen.sticker_height);
                EmoticonsController.getInstance().loadResizedBaseEmoticonImage(mItem, item.getHotkey(),
                        size, R.drawable.ad_loadstatic_grey, null);
                mGiftIndicator.setVisibility(View.VISIBLE);
            }
                break;

            default:
                break;
        }

    }
    
    public void setVerticalSpacing(int verticalSpacing) {
        container.setPadding(container.getPaddingLeft(), 
                verticalSpacing / 2,
                container.getPaddingRight(),
                verticalSpacing / 2);
    }

}
