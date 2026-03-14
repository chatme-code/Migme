/**
 * Copyright (c) 2013 Project Goth
 *
 * ProfileInfoHolder.java
 * Created Sep 29, 2014, 5:46:23 PM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.common.TextUtils;
import com.projectgoth.model.ProfileInfoCategory;

/**
 * @author mapet
 * 
 */
public class ProfileInfoHolder extends BaseViewHolder<ProfileInfoCategory> {

    private RelativeLayout      profileInfoContainer;
    private TextView            profileInfoLabel;
    private TextView            userDetails;
    private TextView            userStatus;
    private TextView            notificationCounter;
    private RelativeLayout      userInfoContainer;
    private ImageView           rightArrow;
    private ProfileInfoListener profileInfoListener;
    private ProfileInfoCategory data;

    public interface ProfileInfoListener {

        public void onProfileInfoClicked(ProfileInfoCategory data);

    }

    public ProfileInfoHolder(View rootView) {
        super(rootView);
        profileInfoContainer = (RelativeLayout) rootView.findViewById(R.id.profile_category_container);
        profileInfoLabel = (TextView) rootView.findViewById(R.id.category_name);
        notificationCounter = (TextView) rootView.findViewById(R.id.notification_counter);
        userInfoContainer = (RelativeLayout) rootView.findViewById(R.id.more_info_container);
        userDetails = (TextView) rootView.findViewById(R.id.user_details);
        userStatus = (TextView) rootView.findViewById(R.id.user_status);
        rightArrow = (ImageView) rootView.findViewById(R.id.arrow_right);
    }

    @Override
    public void setData(ProfileInfoCategory data) {
        super.setData(data);
        this.data = data;

        profileInfoLabel.setText(data.getLabel());

        rightArrow.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(data.getDetails())) {
            setUserDetails(data.getDetails());
        }

        if (!TextUtils.isEmpty(data.getStatus())) {
            setUserStatus(data.getStatus());
        }

        if (data.getUnreadCount() > 0) {
            setUnreadCounter(data.getUnreadCount());
        } else {
            notificationCounter.setVisibility(View.GONE);
        }

    }

    private void setUserDetails(String details) {
        userInfoContainer.setVisibility(View.VISIBLE);
        userDetails.setText(details);
    }

    private void setUserStatus(String status) {
        userInfoContainer.setVisibility(View.VISIBLE);
        userStatus.setText(status);
    }

    private void setUnreadCounter(int count) {
        notificationCounter.setVisibility(View.VISIBLE);
        notificationCounter.setText(String.valueOf(count));
    }

    public void setProfileInfoListener(ProfileInfoListener listener) {
        profileInfoListener = listener;

        profileInfoContainer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (profileInfoListener != null) {
                    profileInfoListener.onProfileInfoClicked(data);
                }
            }
        });

        rightArrow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (profileInfoListener != null) {
                    profileInfoListener.onProfileInfoClicked(data);
                }
            }
        });
    }
}
