/**
 * Copyright (c) 2013 Project Goth
 *
 * ProfileMainHolder.java
 * Created Oct 7, 2014, 12:55:35 AM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.model.ProfileInfoCategory;

/**
 * @author mapet
 * 
 */
public class ProfileMainHolder extends BaseViewHolder<ProfileInfoCategory> {

    private TextView                  menuGifts;
    private TextView                  menuBadges;
    private TextView                  menuFans;
    private TextView                  menuFanOf;

    private ProfileMainHolderListener profileMainHolderListener;
    private ProfileInfoCategory       data;

    public interface ProfileMainHolderListener {

        public void onMainHolderClicked(ProfileInfoCategory data, int viewId);

    }

    public ProfileMainHolder(View rootView) {
        super(rootView);
        menuGifts = (TextView) rootView.findViewById(R.id.menu_gifts);
        menuBadges = (TextView) rootView.findViewById(R.id.menu_badges);
        menuFans = (TextView) rootView.findViewById(R.id.menu_fans);
        menuFanOf = (TextView) rootView.findViewById(R.id.menu_fan_of);
    }

    @Override
    public void setData(ProfileInfoCategory data) {
        super.setData(data);
        this.data = data;

        if (data.getGiftCount() > 0) {
            menuGifts.setText(Tools.formatCounters(data.getGiftCount(), Constants.MAX_COUNT_DISPLAY_PROFILE));
        }

        if (data.getBadgeCount() > 0) {
            menuBadges.setText(Tools.formatCounters(data.getBadgeCount(), Constants.MAX_COUNT_DISPLAY_PROFILE));
        }

        if (data.getFanCount() > 0) {
            menuFans.setText(Tools.formatCounters(data.getFanCount(), Constants.MAX_COUNT_DISPLAY_PROFILE));
        }

        if (data.getFanOfCount() > 0) {
            menuFanOf.setText(Tools.formatCounters(data.getFanOfCount(), Constants.MAX_COUNT_DISPLAY_PROFILE));
        }
    }

    public void setProfileMainHolderListener(ProfileMainHolderListener listener) {
        profileMainHolderListener = listener;

        menuGifts.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (profileMainHolderListener != null) {
                    profileMainHolderListener.onMainHolderClicked(data, R.id.menu_gifts);
                }
            }
        });

        menuBadges.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (profileMainHolderListener != null) {
                    profileMainHolderListener.onMainHolderClicked(data, R.id.menu_badges);
                }
            }
        });

        menuFans.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (profileMainHolderListener != null) {
                    profileMainHolderListener.onMainHolderClicked(data, R.id.menu_fans);
                }
            }
        });

        menuFanOf.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (profileMainHolderListener != null) {
                    profileMainHolderListener.onMainHolderClicked(data, R.id.menu_fan_of);
                }
            }
        });
    }

}
