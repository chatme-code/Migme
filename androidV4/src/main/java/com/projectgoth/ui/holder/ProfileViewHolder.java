/**
 * Copyright (c) 2013 Project Goth
 *
 * ProfileViewHolder.java
 * Created Jun 7, 2013, 10:20:26 AM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Profile;
import com.projectgoth.b.data.Relationship;
import com.projectgoth.b.enums.UserProfileDisplayPictureChoiceEnum;
import com.projectgoth.common.Config;
import com.projectgoth.common.TextUtils;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.ThirdPartyIMController;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.nemesis.model.Friend;
import com.projectgoth.nemesis.model.User;
import com.projectgoth.ui.widget.ButtonEx;
import com.projectgoth.ui.widget.UsernameWithLabelsView;
import com.projectgoth.ui.widget.util.ButtonUtil;

/**
 * @author mapet
 * 
 */
public class ProfileViewHolder extends BaseViewHolder<User> {

    private final RelativeLayout         mainContainer;
    private final ImageView              iconMain;
    private final ImageView              iconOverlay;
    private final UsernameWithLabelsView title;
    private final TextView               subTitle;
    private final ButtonEx               optionButton;
    private final View                   newMarker;

    private boolean                      isProcessingUser;
    private boolean                      shouldShowNewMarker;

    public ProfileViewHolder(View view) {
        super(view);

        mainContainer = (RelativeLayout) view.findViewById(R.id.list_item_container);
        iconMain = (ImageView) view.findViewById(R.id.icon_main);
        iconOverlay = (ImageView) view.findViewById(R.id.icon_overlay);
        title = (UsernameWithLabelsView) view.findViewById(R.id.title);
        subTitle = (TextView) view.findViewById(R.id.subtitle);
        optionButton = (ButtonEx) view.findViewById(R.id.option_button);

        newMarker = (View) view.findViewById(R.id.new_marker);

        title.setClickable(false);
        subTitle.setClickable(false);
        title.setLongClickable(false);
        subTitle.setLongClickable(false);

        title.setOnClickListener(this);
        iconMain.setOnClickListener(this);
        optionButton.setOnClickListener(this);
    }

    @Override
    public void setData(User userData) {
        super.setData(userData);

        if (userData != null) {
            Friend friend = userData.getFriend();
            Profile prof = userData.getProfile();
            Relationship relationship = null;

            UserProfileDisplayPictureChoiceEnum type = UserProfileDisplayPictureChoiceEnum.PROFILE_PICTURE;
            if (prof != null) {
                relationship = prof.getRelationship();
                type = prof.getDisplayPictureType();
            }
            ImageHandler.getInstance().loadDisplayPictureOfUser(iconMain, userData.getUsername(), type,
                    Config.getInstance().getDisplayPicSizeNormal(), true);

            title.setUsername(userData.getUsername());
            if (userData.getProfile() != null) {
                title.setLabels(userData.getProfile().getLabels());
            }
            subTitle.setVisibility(View.GONE);
            newMarker.setVisibility(View.GONE);

            if (!TextUtils.isEmpty(userData.getStatusMessage())) {
                subTitle.setVisibility(View.VISIBLE);
                subTitle.setSelected(true);
                subTitle.setTextColor(Theme.getColor(ThemeValues.LIST_ROW_DESC_COLOR));
                subTitle.setText(userData.getStatusMessage());
            }

            if (friend != null) {
                if (friend.isFusionContact()) {
                    iconOverlay.setImageResource(Tools.getFusionPresenceResource(friend.getPresence()));
                } else if (friend.isIMContact()) {
                    iconOverlay.setImageBitmap(ThirdPartyIMController.getInstance().getIMContactPresenceBmp(
                            friend.getIMType(), friend.getPresence()));
                } else {
                    iconOverlay.setImageResource(Tools.getFusionPresenceResource(friend.getPresence()));
                }
                iconOverlay.setVisibility(View.VISIBLE);
            }

            optionButton.setVisibility(View.VISIBLE);
            //reset clickable , it could be false
            optionButton.setClickable(true);

            // AD-1115 remove check inProcessingUser, just base on relationship to display status icon
//            if (isProcessingUser) {
//                optionButton.setIcon(R.drawable.ad_fanof_white);
//                optionButton.setType(ButtonUtil.BUTTON_TYPE_ORANGE);
//            } else {
            if (relationship != null) {
                if (relationship.isFriend()) {
                    optionButton.setIcon(R.drawable.ad_friend_white);
                    optionButton.setType(ButtonUtil.BUTTON_TYPE_TURQUOISE);
                } else {
                    if (relationship.isFollower() && !relationship.isFollowedBy()) {
                        optionButton.setIcon(R.drawable.ad_fanof_white);
                    } else if (!relationship.isFollower() && relationship.isFollowedBy()) {
                        optionButton.setIcon(R.drawable.ad_addfan_white);
                    } else if (relationship.isFollowerPendingApproval()) {
                        optionButton.setClickable(false);
                        optionButton.setIcon(R.drawable.ad_pending_white);
                    } else {
                        //not following each other
                        optionButton.setIcon(R.drawable.ad_addfan_white);
                    }
                    optionButton.setType(ButtonUtil.BUTTON_TYPE_ORANGE);
                }
            } else {
                optionButton.setIcon(R.drawable.ad_addfan_white);
                optionButton.setType(ButtonUtil.BUTTON_TYPE_ORANGE);
            }
//            }

            if (shouldShowNewMarker) {
                mainContainer.setBackgroundColor(ApplicationEx.getContext().getResources().getColor(R.color.light_turquoise));
                newMarker.setVisibility(View.VISIBLE);
            } else {
                mainContainer.setBackgroundColor(ApplicationEx.getContext().getResources().getColor(R.color.white));
            }
        }
    }

    public void setData(User profileData, boolean processingUser) {
        setData(profileData, processingUser, false);
    }

    public void setData(User profileData, boolean processingUser, boolean shouldShowNewMarker) {
        this.isProcessingUser = processingUser;
        this.shouldShowNewMarker = shouldShowNewMarker;
        setData(profileData);
    }

}
