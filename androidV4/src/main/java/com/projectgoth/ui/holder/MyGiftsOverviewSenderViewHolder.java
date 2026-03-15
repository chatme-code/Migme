/**
 * Copyright (c) 2013 Project Goth
 *
 * MyGiftsOverviewViewHolder.java
 * Created Jan 23, 2015, 5:01:10 PM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.b.data.GiftSenderLeaderboardItem;
import com.projectgoth.b.data.Profile;
import com.projectgoth.common.Config;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.imagefetcher.ImageHandler;

/**
 * @author mapet
 * 
 */
public class MyGiftsOverviewSenderViewHolder extends BaseViewHolder<GiftSenderLeaderboardItem> {

    private final ImageView icon;
    private final TextView  rank;
    private final TextView  name;
    private final TextView  count;
    private final int       order;

    public MyGiftsOverviewSenderViewHolder(View rootView, int order) {
        super(rootView);
        icon = (ImageView) rootView.findViewById(R.id.icon);
        rank = (TextView) rootView.findViewById(R.id.rank);
        name = (TextView) rootView.findViewById(R.id.name);
        count = (TextView) rootView.findViewById(R.id.count);
        this.order = order;
    }

    @Override
    public void setData(GiftSenderLeaderboardItem senderItem) {
        super.setData(senderItem);

        if (senderItem != null) {
            name.setText(senderItem.getSenderUserName());
            count.setText(String.valueOf(senderItem.getCount()));
            rank.setText(String.format("%d.", order));

            Profile profile = UserDatastore.getInstance().getProfileWithUsername(senderItem.getSenderUserName(), false);
            if (profile != null) {
                ImageHandler.getInstance().loadDisplayPictureOfUser(icon, senderItem.getSenderUserName(),
                        profile.getDisplayPictureType(), Config.getInstance().getDisplayPicSizeNormal(), true);
            }
        }
    }

}
