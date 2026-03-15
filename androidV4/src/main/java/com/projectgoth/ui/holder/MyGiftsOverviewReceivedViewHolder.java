/**
 * Copyright (c) 2013 Project Goth
 *
 * MyGiftsOverviewReceivedViewHolder.java
 * Created Jan 28, 2015, 3:47:52 PM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.GiftReceivedLeaderboardItem;
import com.projectgoth.controller.EmoticonsController;

/**
 * @author mapet
 * 
 */
public class MyGiftsOverviewReceivedViewHolder extends BaseViewHolder<GiftReceivedLeaderboardItem> {

    private final ImageView icon;
    private final TextView  rank;
    private final TextView  name;
    private final TextView  count;
    private final int       order;

    public MyGiftsOverviewReceivedViewHolder(View rootView, int order) {
        super(rootView);
        icon = (ImageView) rootView.findViewById(R.id.icon);
        rank = (TextView) rootView.findViewById(R.id.rank);
        name = (TextView) rootView.findViewById(R.id.name);
        count = (TextView) rootView.findViewById(R.id.count);
        this.order = order;
    }

    @Override
    public void setData(GiftReceivedLeaderboardItem receivedItem) {
        super.setData(receivedItem);

        if (receivedItem != null) {
            name.setText(receivedItem.getGiftName());
            count.setText(String.valueOf(receivedItem.getCount()));
            rank.setText(String.format("%d.", order));
            
            int size = ApplicationEx.getDimension(R.dimen.vg_request_size);
            EmoticonsController.getInstance().loadResizedBaseEmoticonImage(icon, receivedItem.getHotkey(),
                    size, R.drawable.ad_loadstatic_grey, null);
        }
    }

}
