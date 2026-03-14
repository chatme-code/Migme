/**
 * Copyright (c) 2013 Project Goth
 *
 * ProfileFragment.java
 * Created Aug 20, 2013, 3:53:22 PM
 */

package com.projectgoth.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Labels;
import com.projectgoth.b.data.Profile;
import com.projectgoth.b.data.Relationship;
import com.projectgoth.blackhole.enums.UserPermissionType;
import com.projectgoth.common.Config;
import com.projectgoth.common.Constants;
import com.projectgoth.common.ShareManager;
import com.projectgoth.common.TextUtils;
import com.projectgoth.common.Tools;
import com.projectgoth.common.WebURL;
import com.projectgoth.datastore.GiftsDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.model.MenuOption;
import com.projectgoth.nemesis.enums.ActivitySourceEnum;
import com.projectgoth.nemesis.model.Friend;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.fragment.ShareboxFragment.ShareboxActionType;
import com.projectgoth.ui.widget.ButtonEx;
import com.projectgoth.ui.widget.PopupMenu;
import com.projectgoth.ui.widget.UserBasicDetails;
import com.projectgoth.ui.widget.UserImageView;
import com.projectgoth.ui.widget.UserMiniDetails;
import com.projectgoth.ui.widget.util.ButtonUtil;
import com.projectgoth.util.ProfileUtils;

import java.util.ArrayList;

/**
 * @author dangui
 * 
 */
public class MiniProfilePopupFragment extends BaseDialogFragment implements OnClickListener {

    public static final String PARAM_USERNAME   = "PARAM_USERNAME";

    private Profile            profile;
    private String             username;
    private UserBasicDetails   userBasicDetails;
    private boolean            isSelf;
    private boolean            isProfilePrivate;

    private TextView           usernameTv;
    private TextView           userDetails;
    private TextView           startChat;

    private ImageView          coverPhoto;
    private UserImageView      userImageView;

    private PopupMenu          mPopupMenu;
    private ImageView          mPopupMenuMarker;

    private ButtonEx           chatBtn;

    private UserMiniDetails    userMiniDetails;

    //force fetch once is necessary because some requests like fetch "mention_autocomplete_list" doesn't return profile
    // with complete fields
    private boolean            shouldForceFetch = true;

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        username = args.getString(PARAM_USERNAME);
        isSelf = Session.getInstance().getUsername() != null && Session.getInstance().getUsername().equals(username);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_profile_popup;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userDetails = (TextView) view.findViewById(R.id.user_details);
        usernameTv = (TextView) view.findViewById(R.id.username);
        userBasicDetails = (UserBasicDetails) view.findViewById(R.id.user_basic_details);
        userImageView = (UserImageView) view.findViewById(R.id.user_image);
        coverPhoto = (ImageView) view.findViewById(R.id.cover_photo);
        chatBtn = (ButtonEx) view.findViewById(R.id.chat_button);
        startChat = (TextView) view.findViewById(R.id.start_chat);
        mPopupMenuMarker = (ImageView) view.findViewById(R.id.overflow_marker);

        bindOnClickListener(this, R.id.user_image, R.id.cover_photo, R.id.username, R.id.chat_button,
                R.id.more_options_button, R.id.send_gifts, R.id.start_chat, R.id.user_mini_details,
                R.id.more_options_container);

        userMiniDetails = (UserMiniDetails) view.findViewById(R.id.user_mini_details);

        if (isSelf) {
            if (chatBtn != null)
                chatBtn.setVisibility(View.GONE);
        } else {
            if (profile != null) {
                Friend friendDetails = UserDatastore.getInstance().findMig33User(username);
                if (friendDetails != null) {
                    userImageView.setUser(friendDetails);
                }
                checkRelationship();
            }
        }

        userBasicDetails.hideUsername();
        createPopupMenu();
    }

    public void setLabels(Labels labels) {
        userBasicDetails.setLabels(labels);
    }

    public void setMigLevelNumber(String strLevelNumber) {
        userBasicDetails.setMigLevel(String.format(I18n.tr("Level %s"), strLevelNumber));
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.Profile.RECEIVED);
        registerEvent(Events.Profile.FETCH_ERROR);
        registerEvent(Events.User.FETCH_BADGES_COMPLETED);
        registerEvent(Events.Contact.REMOVED);

        registerEvent(Events.User.BLOCKED);
        registerEvent(Events.User.UNBLOCKED);
        registerEvent(Events.User.BLOCK_ERROR);
        registerEvent(Events.User.UNBLOCK_ERROR);

        registerEvent(Events.User.FOLLOWED);
        registerEvent(Events.User.ALREADY_FOLLOWING);
        registerEvent(Events.User.PENDING_APPROVAL);

        registerEvent(Events.User.UNFOLLOWED);
        registerEvent(Events.User.REQUESTING_FOLLOWING);
        registerEvent(Events.User.NOT_FOLLOWING);

        registerEvent(Events.User.FOLLOW_ERROR);
        registerEvent(Events.User.UNFOLLOW_ERROR);
        registerEvent(Events.User.DISPLAY_PICTURE_SET);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(Events.Profile.RECEIVED)) {
            Bundle data = intent.getExtras();
            String username = data.getString(Events.User.Extra.USERNAME);

            if (this.username.equalsIgnoreCase(username)) {
                updateUserMiniProfile();
            }
        } else if (action.equals(Events.Profile.FETCH_ERROR) && profile == null) {
            // Only show the error message toast if the profile is null.
            // This way we prevent a lot of unnecessary toasts from being
            // displayed in cases of spotty connections.
            final String username = intent.getStringExtra(Events.User.Extra.USERNAME);
            if (username != null && this.username.equalsIgnoreCase(username)) {
                Tools.showToastForIntent(context, intent);
            }
        } else if (action.equals(Events.User.FETCH_BADGES_COMPLETED)) {
            Bundle data = intent.getExtras();
            String username = data.getString(Events.User.Extra.USERNAME);
            if (this.username.equalsIgnoreCase(username)) {
                updateUserMiniProfile();
            }
        } else if (action.equals(Events.Contact.REMOVED)) {
            String usernameRemoved = intent.getStringExtra(Events.Contact.Extra.USERNAME);
            if (usernameRemoved.equalsIgnoreCase(username)) {
                ActionHandler.getInstance().displayHomeScreen(getActivity());
            }

            Tools.showToastForIntent(context, intent);
        } else if (action.equals(Events.User.BLOCKED) || action.equals(Events.User.UNBLOCKED)) {
            String usernameBlocked = intent.getStringExtra(Events.User.Extra.USERNAME);
            final byte userPermissionTypeValue = intent.getByteExtra(Events.User.Extra.PERMISSION, (byte) 0);
            final UserPermissionType userPermissionType = UserPermissionType.fromValue(userPermissionTypeValue);
            if (usernameBlocked.equalsIgnoreCase(username) && userPermissionType == UserPermissionType.BLOCK) {
                ActionHandler.getInstance().displayHomeScreen(getActivity());
            }

            Tools.showToastForIntent(context, intent);
        } else if (action.equals(Events.User.BLOCK_ERROR) || action.equals(Events.User.UNBLOCK_ERROR)) {
            Tools.showToastForIntent(context, intent);
        } else if (action.equals(Events.User.FOLLOWED) || action.equals(Events.User.ALREADY_FOLLOWING)
                || action.equals(Events.User.PENDING_APPROVAL) || action.equals(Events.User.UNFOLLOWED)
                || action.equals(Events.User.REQUESTING_FOLLOWING) || action.equals(Events.User.NOT_FOLLOWING)) {
            updateUserMiniProfile();
            Tools.showToastForIntent(context, intent);
        } else if (action.equals(Events.User.FOLLOW_ERROR) || action.equals(Events.User.UNFOLLOW_ERROR)) {
            Tools.showToastForIntent(context, intent);
            updateUserMiniProfile();
        } else if (intent.getAction().equals(Events.User.DISPLAY_PICTURE_SET)) {
            updateUserMiniProfile();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUserMiniProfile();
    }

    private void updateUserMiniProfile() {

        profile = UserDatastore.getInstance().getProfileWithUsername(username, shouldForceFetch);
        if (shouldForceFetch) {
            shouldForceFetch = false;
        }
        if (profile != null) {
            userImageView.setUser(username);
            usernameTv.setText(username);
            usernameTv.setTextColor(UIUtils.getUsernameColorFromLabels(profile.getLabels(), false));
            String strUserInfo = Tools.formatProfileRemarksForPopup(profile);
            userDetails.setText(strUserInfo);

            setMigLevelNumber(Constants.BLANKSTR + profile.getMigLevel());
            setLabels(profile.getLabels());

            userMiniDetails.updateMiniDetails(profile);

            if (!TextUtils.isEmpty(profile.getCoverPhotoUrl())) {
                ImageHandler.getInstance().loadImageFromUrl(coverPhoto, profile.getCoverPhotoUrl(), false, 0);
            }

            if (!isSelf) {
                checkRelationship();
            }
            
            isProfilePrivate = ProfileUtils.isProfilePrivate(profile);
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        // for [non-login] users
        if (Session.getInstance().isBlockUsers()) {
            ActionHandler.getInstance().displayLoginDialogFragment(getActivity());
            return;
        }
        switch (viewId) {
            case R.id.send_gifts:
                if (profile != null) {
                    Relationship relationship = profile.getRelationship();
                    if (relationship != null) {
                        if (relationship.isFollowedBy()) {
                            GAEvent.Miniprofile_IsFanSendGift.send();
                        }
                    }
                }

                GAEvent.Miniprofile_MainActionSendGift.send();
                ActionHandler.getInstance().displayStore(getActivity(), username);
                dismissPopupDelay();
                break;
            case R.id.start_chat:
                GAEvent.Miniprofile_MainActionStartChat.send();
                ActionHandler.getInstance().displayPrivateChat(getActivity(), username);
                dismissPopupDelay();
                break;
            case R.id.chat_button:
                if (profile != null) {
                    Relationship relationship = profile.getRelationship();
                    if (relationship != null) {
                        if (relationship.isFriend()) {
                            GAEvent.Miniprofile_StartChat.send();
                            ActionHandler.getInstance().displayPrivateChat(getActivity(), username);
                        } else if (relationship.isFollower()) {
                            ActionHandler.getInstance().displayRequestFollow(getActivity(), username);
                        } else {
                            GAEvent.Miniprofile_Follow.send();
                            ActionHandler.getInstance().followOrUnfollowUser(username, ActivitySourceEnum.UNKNOWN);
                        }
                    } else {
                        GAEvent.Miniprofile_Follow.send();
                        ActionHandler.getInstance().followOrUnfollowUser(username, ActivitySourceEnum.UNKNOWN);
                    }
                }
                updateUserMiniProfile();
                break;
            case R.id.cover_photo:
            case R.id.user_image:
            case R.id.username:
                ActionHandler.getInstance().displayMainProfile(getActivity(), username);
                dismissPopupDelay();
                break;
            case R.id.more_options_container:
            case R.id.more_options_button:
                showPopupMenu();
                break;
            case R.id.menu_gifts:
                sendGiftListEvent();
                if (!isSelf && isProfilePrivate) {
                    showProfilePrivateToast();
                } else {
                    if (!Config.getInstance().isMyGiftsEnabled()) {
                        int giftCnt = 0;
                        if (profile != null) {
                            giftCnt = profile.getNumOfGiftsReceived();
                        }
                        ActionHandler.getInstance().displayBrowser(getActivity(),
                                String.format(WebURL.URL_GIFTS_RECEIVED, username),
                                String.format(I18n.tr("Gifts (%d)"), giftCnt), R.drawable.ad_gift_white);
                    } else {
                        if (profile != null) {
                            String userId;
                            if (isSelf) {
                                userId = Session.getInstance().getUserId();
                                ActionHandler.getInstance().displayMyGifts(getActivity(), userId);
                            } else {
                                userId = profile.getId().toString();
                                ActionHandler.getInstance().displayMyGiftsCardListFragment(getActivity(), I18n.tr("Gifts"),
                                        GiftsDatastore.Category.ALL.ordinal(), false, userId);
                            }
                        }
                    }
                    dismissPopupDelay();
                }
                break;
            case R.id.menu_badges:
                sendBadgeListEvent();
                if (!isSelf && isProfilePrivate) {
                    showProfilePrivateToast();
                } else {
                    ActionHandler.getInstance().displayBadgesList(getActivity(), username);
                    dismissPopupDelay();
                }
                break;
            case R.id.menu_fans:
                sendFanListEvent();
                if (!isSelf && isProfilePrivate) {
                    showProfilePrivateToast();
                } else {
                    ActionHandler.getInstance().displayFollowersList(getActivity(), username);
                    dismissPopupDelay();
                }
                break;
            default:
                break;
        }
    }

    private void showProfilePrivateToast() {
        Tools.showToast(getActivity(), String.format(I18n.tr("%s\'s profile is private"), username));
    }

    // This is overridden by MiniProfileChatFragment
    protected void sendGiftListEvent() {
        GAEvent.Miniprofile_GiftList.send();
    }

    // This is overridden by MiniProfileChatFragment
    protected void sendBadgeListEvent() {
        GAEvent.Miniprofile_BadgeList.send();
    }

    // This is overridden by MiniProfileChatFragment
    protected void sendFanListEvent() {
        GAEvent.Miniprofile_FanList.send();
    }

    protected void dismissPopupDelay() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Dialog dialog = getDialog();
                if (dialog != null) {
                    dialog.onBackPressed();
                }
            }
        }, 300);
    }

    private void createPopupMenu() {
        mPopupMenu = new PopupMenu(getActivity());
        mPopupMenu.setPopupGravity(Gravity.RIGHT | Gravity.TOP);
        mPopupMenu.setPopupMenuListener(this);
        mPopupMenu.setMarker(mPopupMenuMarker);
    }

    private void showPopupMenu() {
        mPopupMenu.setMenuOptions(getProfileMenuOptions());
        mPopupMenu.setPopupAnchor(mPopupMenuMarker);
        mPopupMenu.showAtLocation(0, mPopupMenu.mAnchorRect.top + mPopupMenu.mAnchorRect.height(), true);
    }

    private ArrayList<MenuOption> getProfileMenuOptions() {
        ArrayList<MenuOption> menuItems = new ArrayList<MenuOption>();

        if (profile != null) {
            menuItems.add(new MenuOption(I18n.tr("Share profile"), R.drawable.ad_share_white, R.id.option_item_share));
        }
        if (isSelf) {
            menuItems.add(new MenuOption(I18n.tr("Buy credit"), R.drawable.ad_credit_white,
                    R.id.option_item_buy_credits));
            menuItems.add(new MenuOption(I18n.tr("Settings"), R.drawable.ad_setting_white, R.id.option_item_settings));
        } else {
            menuItems.add(new MenuOption(I18n.tr("Send gift"), R.drawable.ad_gift_white, R.id.option_item_send_gift));
            menuItems.add(new MenuOption(I18n.tr("Mention"), R.drawable.ad_mention_white, R.id.option_item_mention));
            menuItems.add(new MenuOption(I18n.tr("Transfer credit"), R.drawable.ad_credit_white,
                    R.id.option_item_transfer_credit));
            menuItems.add(new MenuOption(I18n.tr("Report"), R.drawable.ad_report_white, R.id.option_item_report_abuse));
            menuItems.add(new MenuOption(I18n.tr("Block"), R.drawable.ad_block_white, R.id.option_item_block));

            if (profile != null) {
                Relationship relationship = profile.getRelationship();
                if (relationship != null) {
                    if (relationship.isFriend()) {
                        menuItems.add(new MenuOption(I18n.tr("Unfriend"), R.drawable.ad_relfan_white,
                                R.id.option_item_remove_friend));
                    } else if (relationship.isFollower()) {
                        menuItems.add(new MenuOption(I18n.tr("Unfan"), R.drawable.ad_relfan_white,
                                R.id.option_item_unfollow));
                    }
                }
            }
        }

        return menuItems;
    }

    @Override
    public void onMenuOptionClicked(MenuOption menuOption) {
        switch (menuOption.getActionId()) {
            case R.id.option_item_buy_credits:
                ActionHandler.getInstance().displayBrowser(getActivity(), WebURL.URL_BUYCREDIT, I18n.tr("Buy credit"),
                        R.drawable.ad_credit_white);
                break;
            case R.id.option_item_send_gift:
                GAEvent.Miniprofile_DropdownSendGift.send();
                ActionHandler.getInstance().displayStore(getActivity(), username);
                break;
            case R.id.option_item_mention:
                GAEvent.Miniprofile_DropdownMention.send();
                ActionHandler.getInstance().displaySharebox(getActivity(), ShareboxActionType.CREATE_NEW_POST, null,
                        "@" + username + Constants.SPACESTR, null, true);
                break;
            case R.id.option_item_transfer_credit:
                GAEvent.Miniprofile_DropdownTransferCredits.send();
                ActionHandler.getInstance().displayBrowser(getActivity(),
                        String.format(WebURL.URL_TRANSFER_CREDITS, username), I18n.tr("Transfer credit"),
                        R.drawable.ad_credit_white);
                break;
            case R.id.option_item_report_abuse:
                GAEvent.Miniprofile_DropdownReport.send();
                ActionHandler.getInstance().displayBrowser(getActivity(),
                        String.format(WebURL.URL_REPORT_USER, username), I18n.tr("Report abuse"),
                        R.drawable.ad_report_white);
                break;
            case R.id.option_item_block:
                GAEvent.Miniprofile_DropdownBlock.send();
                Friend friendToBlock = UserDatastore.getInstance().findMig33User(username);
                if (friendToBlock != null) {
                    ActionHandler.getInstance().blockFriend(getActivity(), null, friendToBlock.getDisplayName(),
                            friendToBlock.getUsername());
                } else {
                    ActionHandler.getInstance().blockFriend(getActivity(), null, username, username);
                }
                break;
            case R.id.option_item_remove_friend:
                GAEvent.Miniprofile_DropdownUnfriend.send();
                Friend friendToRemove = UserDatastore.getInstance().findMig33User(username);
                if (friendToRemove != null) {
                    ActionHandler.getInstance().removeFriend(getActivity(), friendToRemove.getDisplayName(),
                            friendToRemove.getContactID());
                }
                break;
            case R.id.option_item_camera:
                ActionHandler.getInstance().takePhoto(this, Constants.REQ_PIC_FROM_CAMERA_FOR_DISPLAY_PIC, false);
                break;
            case R.id.option_item_gallery:
                ActionHandler.getInstance().pickFromGallery(this, Constants.REQ_PIC_FROM_GALLERY_FOR_DISPLAY_PIC);
                break;
            case R.id.option_item_avatar:
                String guid = Session.getInstance().getAvatarPicGuid();
                if (guid != null) {
                    Tools.showToast(getActivity(), I18n.tr("Updating"));
                    UserDatastore.getInstance().requestSetDisplayPicture(guid);
                } else {
                    // no avatar set yet, open the avatar pagelet
                    ActionHandler.getInstance().displayBrowser(getActivity(), WebURL.URL_MY_AVATAR);
                }
                break;
            case R.id.option_item_unfollow:
                GAEvent.Miniprofile_DropdownUnfriend.send();
                ActionHandler.getInstance().unfollowFriend(getActivity(), username);
                break;
            case R.id.option_item_share:
                if (profile != null) {
                    ShareManager.shareProfile(getActivity(), profile.getUsername());
                }
                break;
            default:
                break;
        }
    }

    private void checkRelationship() {
        if (profile != null && chatBtn != null) {
            Relationship relationship = profile.getRelationship();
            chatBtn.setVisibility(View.VISIBLE);
            // AD-1115 remove check inProcessingUser, just base on relationship to display status icon
//            boolean isProcessingUser = FriendsController.getInstance().isChangeRelationshipWithUserInProgress(
//                    profile.getUsername());
//            if (isProcessingUser) {
//                chatBtn.setIcon(R.drawable.ad_fanof_white);
//            } else {
                if (relationship != null) {
                    if (relationship.isFriend()) {
                        chatBtn.setIcon(R.drawable.ad_friend_white);
                        chatBtn.setType(ButtonUtil.BUTTON_TYPE_TURQUOISE);
                        startChat.setVisibility(View.VISIBLE);
                    } else if (relationship.isFollower()) {
                        chatBtn.setIcon(R.drawable.ad_fanof_white);
                        startChat.setVisibility(View.GONE);
                    } else {
                        chatBtn.setIcon(R.drawable.ad_addfan_white);
                        startChat.setVisibility(View.GONE);
                    }
                } else {
                    chatBtn.setIcon(R.drawable.ad_addfan_white);
                }
//            }
        }
    }

}
