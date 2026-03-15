/**
 * Copyright (c) 2013 Project Goth
 *
 * BadgeViewHolder.java
 * Created Aug 23, 2013, 10:04:03 AM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.b.data.Badge;
import com.projectgoth.common.Constants;
import com.projectgoth.common.TimeAgo;
import com.projectgoth.common.Tools;
import com.projectgoth.imagefetcher.ImageHandler;

/**
 * @author dangui
 * 
 */
public class BadgeViewHolder extends BaseViewHolder<Badge> {

    private final ImageView picture;
    private final TextView  name;
    private final TextView  unlockText;

    /**
     * @param rootView
     */
    public BadgeViewHolder(View rootView) {
        super(rootView);
        picture = (ImageView) rootView.findViewById(R.id.picture);
        name = (TextView) rootView.findViewById(R.id.name);
        unlockText = (TextView) rootView.findViewById(R.id.timestamp);
    }

    @Override
    public void setData(Badge badge) {
        super.setData(badge);

        if(badge.getUnlockedTimestamp() == null) {
            unlockText.setVisibility(View.GONE);
            picture.setImageResource(R.drawable.ad_badgelock);
        } else {
            int size = Constants.BADGES_SIZE_LARGE;
            String url = Tools.constructBadgeUrl(badge.getImageName(), size, false);
            ImageHandler.getInstance().loadImageFromUrl(picture, url, false, R.drawable.ad_loadstatic_grey);
            unlockText.setVisibility(View.VISIBLE);
            unlockText.setText(TimeAgo.format(badge.getUnlockedTimestamp()));
        }
        name.setText(badge.getName());
    }
}
