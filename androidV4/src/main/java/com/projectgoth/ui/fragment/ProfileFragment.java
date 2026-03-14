/**
 * Copyright (c) 2013 Project Goth
 *
 * ProfileFragment.java
 * Created Aug 20, 2013, 3:53:22 PM
 */

package com.projectgoth.ui.fragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.nineoldandroids.view.ViewHelper;
import com.projectgoth.R;
import com.projectgoth.b.data.Profile;
import com.projectgoth.b.data.Relationship;
import com.projectgoth.blackhole.enums.UserPermissionType;
import com.projectgoth.common.Config;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.ShareManager;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.common.Tools;
import com.projectgoth.common.WebURL;
import com.projectgoth.datastore.AlertsDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.enums.CropImageType;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.model.ContextMenuItem;
import com.projectgoth.model.MenuOption;
import com.projectgoth.nemesis.enums.ActivitySourceEnum;
import com.projectgoth.nemesis.enums.RequestTypeEnum;
import com.projectgoth.nemesis.model.Friend;
import com.projectgoth.nemesis.utils.RetryConfig;
import com.projectgoth.ui.UrlHandler;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.activity.CustomActionBarConfig.OverflowButtonState;
import com.projectgoth.ui.adapter.ProfilePagerAdapter;
import com.projectgoth.ui.adapter.ProfilePagerAdapter.HeaderDataProvider;
import com.projectgoth.ui.fragment.SettingsFragment.SettingsGroupType;
import com.projectgoth.ui.fragment.ShareboxFragment.ShareboxActionType;
import com.projectgoth.ui.holder.ProfileTabHolder;
import com.projectgoth.ui.holder.ProfileTabHolder.ProfileTabListener;
import com.projectgoth.ui.holder.ProfileTabHolder.SelectedTab;
import com.projectgoth.ui.listener.ContextMenuItemListener;
import com.projectgoth.ui.widget.ButtonEx;
import com.projectgoth.ui.widget.ClickableSpanEx;
import com.projectgoth.ui.widget.ClickableSpanEx.ClickableSpanExListener;
import com.projectgoth.ui.widget.PopupMenu.OnPopupMenuListener;
import com.projectgoth.ui.widget.UserBasicDetails;
import com.projectgoth.ui.widget.UsernameWithLabelsView;
import com.projectgoth.ui.widget.util.ButtonUtil;
import com.projectgoth.util.AndroidLogger;

/**
 * @author dangui
 * 
 */
public class ProfileFragment extends BaseViewPagerFragment implements OnClickListener, ContextMenuItemListener,
        ClickableSpanExListener {

    private static final String    TAG            = AndroidLogger.makeLogTag(ProfileFragment.class);

    public static final String     PARAM_USERNAME = "PARAM_USERNAME";

    private LinearLayout           header;
    private View                   headerTabView;

    private ProfilePagerAdapter    adapter;
    private ProfileTabHolder       headerTab;

    private Profile                profile;
    private String                 username;
    private boolean                isSelf;

    private TextView               name;
    private TextView               userDetails;
    private UsernameWithLabelsView labels;
    private ButtonEx               relationshipBtn;

    private ImageView              coverPhoto;
    private ImageView              displayPic;
    private ImageView              presenceIcon;

    private int                    headerHeight;
    private int                    minHeaderTranslation;
    
    private UserBasicDetails       mHeaderUserInfo;


    private Uri                    selectedImageUri;

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        username = args.getString(PARAM_USERNAME);
        isSelf = Session.getInstance().isSelfByUsername(username);
        profile = UserDatastore.getInstance().getProfileWithUsername(username, true);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_profile;
    }
    
    @Override
    protected FragmentStatePagerAdapter createAdapter(FragmentManager fragmentManager) {
        adapter = new ProfilePagerAdapter(fragmentManager, getActivity());
        adapter.setProfile(profile);
        adapter.setHeaderDataProvider(new HeaderDataProvider() {

            @Override
            public int getHeaderSize() {
                headerHeight = header.getMeasuredHeight();
                minHeaderTranslation = -headerHeight + headerTabView.getMeasuredHeight();
                return headerHeight;
            }
        });
        
        adapter.setPagerScrollListener(adapter.new PagerScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount,
                                 int position) {
                if (adapter.getCurrentPos() == position) {
                    View headerPh = adapter.getCurrentHeaderPlaceholder();
                    int headerPlaceHolderY = minHeaderTranslation;
                    if (headerPh != null) {
                        headerPlaceHolderY = (int) ViewHelper.getY(headerPh);
                        //adjust the Y position of the header for list with divider, cause we cannot remove
                        //the divider above the header once the divider of the list is set
                        if (headerPh.getTag() != null) {
                            int listDividerHeight = (Integer) headerPh.getTag();
                            headerPlaceHolderY -= listDividerHeight;
                        }

                    }

                    if (headerPlaceHolderY <= minHeaderTranslation) {
                        headerPlaceHolderY = minHeaderTranslation;
                    }

                    Logger.debug.log("PagerScrollListener.onScroll", "headerPlaceHolderY:" + headerPlaceHolderY);

                    ViewHelper.setTranslationY(header, headerPlaceHolderY);
                }
            }
        });

        return adapter;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        header = (LinearLayout) view.findViewById(R.id.header);
        headerTabView = view.findViewById(R.id.tab_holder);
        
        initHeader();
        updateProfileData();

        headerTab = new ProfileTabHolder(headerTabView);
        headerTab.setSelectedTab(SelectedTab.Info);
        headerTab.setProfileTabListener(new ProfileTabListener() {

            @Override
            public void onProfilePostsIconClicked() {
                viewPager.setCurrentItem(SelectedTab.Posts.ordinal(), true);
            }

            @Override
            public void onProfileInfoIconClicked() {
                viewPager.setCurrentItem(SelectedTab.Info.ordinal(), true);
            }
        });

        viewPager.setBackgroundColor(Theme.getColor(ThemeValues.LIGHT_BACKGROUND_COLOR));
        viewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                adapter.setCurrentPos(position);
                headerTab.setSelectedTab(SelectedTab.fromValue(position));
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Do Nothing
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    int scrollHeight = (int) (header.getMeasuredHeight() + ViewHelper.getTranslationY(header));
                    adapter.adjustScroll(minHeaderTranslation, scrollHeight);
                }
            }
        });

        viewPager.setCurrentItem(0);
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
        registerEvent(Events.User.SET_DISPLAY_PICTURE_ERROR);

    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Bundle data = intent.getExtras();

        if (action.equals(Events.Profile.RECEIVED)) {
            String username = data.getString(Events.User.Extra.USERNAME);
            if (this.username.equalsIgnoreCase(username)) {
                updateProfileData();
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
            String username = data.getString(Events.User.Extra.USERNAME);
            if (this.username.equalsIgnoreCase(username)) {
                updateProfileData();
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
            updateProfileData();
        } else if (action.equals(Events.User.FOLLOW_ERROR) || action.equals(Events.User.UNFOLLOW_ERROR)) {
            updateProfileData();
            Tools.showToastForIntent(context, intent);
        } else if (intent.getAction().equals(Events.User.DISPLAY_PICTURE_SET)) {
            Tools.showToast(getActivity(), I18n.tr("Picture updated"));
            updateProfileData();
        } else if (intent.getAction().equals(Events.User.SET_DISPLAY_PICTURE_ERROR)) {
            Tools.showToast(getActivity(), I18n.tr("Failed to update"));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateProfileData();

        initHeaderHeight();

        RetryConfig.getInstance().addRetryRequest(RequestTypeEnum.GET_PROFILE);
    }

    @Override
    public void onPause() {
        super.onPause();
        RetryConfig.getInstance().removeRetryRequest(RequestTypeEnum.GET_PROFILE);
    }

    private void updateProfileData() {
        profile = UserDatastore.getInstance().getProfileWithUsername(username, false);

        if (isSelf) {
            ImageHandler.getInstance().loadDisplayPictureFromGuid(displayPic, Session.getInstance().getDisplayableGuid(), Config
                    .getInstance().getDisplayPicSizeNormal(), true);
        }

        if (profile != null) {

            if (isSelf) {
                AlertsDatastore.getInstance().requestGetUnreadMentionCount(profile.getId().toString());
            }

            ImageHandler.getInstance().loadDisplayPictureOfUser(displayPic, username, profile.getDisplayPictureType(), Config
                    .getInstance().getDisplayPicSizeNormal(), true);

            name.setText(username);
            name.setTextColor(UIUtils.getUsernameColorFromLabels(profile.getLabels(), false));
            labels.setLabels(profile.getLabels());

            userDetails.setText(String.format(I18n.tr("Level %d"), profile.getMigLevel()));

            if (!TextUtils.isEmpty(profile.getCoverPhotoUrl())) {
                ImageHandler.getInstance().loadImageFromUrl(coverPhoto, profile.getCoverPhotoUrl(), false, 0);
            }

            if (!isSelf) {
                Friend friendDetails = UserDatastore.getInstance().findMig33User(username);
                if (friendDetails != null) {
                    presenceIcon.setVisibility(View.VISIBLE);
                    presenceIcon.setImageResource(Tools.getFusionPresenceResource(friendDetails.getPresence()));
                }
                checkRelationship();
            }
        }
        updateActionBar(getActivity());
        adapter.setProfile(profile);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        switch (viewId) {
            case R.id.chat_button:
                if (profile != null) {
                    Relationship relationship = profile.getRelationship();
                    if (relationship != null) {
                        if (relationship.isFriend()) {
                            GAEvent.Profile_OtherProfileButtonChat.send();
                            ActionHandler.getInstance().displayPrivateChat(getActivity(), username);
                        } else if (relationship.isFollower()) {
                            ActionHandler.getInstance().displayRequestFollow(getActivity(), username);
                        } else {
                            GAEvent.Profile_OtherProfileButtonFollow.send();
                            ActionHandler.getInstance().followOrUnfollowUser(username, ActivitySourceEnum.UNKNOWN);
                        }
                    } else {
                        GAEvent.Profile_OtherProfileButtonFollow.send();
                        ActionHandler.getInstance().followOrUnfollowUser(username, ActivitySourceEnum.UNKNOWN);
                    }
                }
                updateProfileData();
                break;
            case R.id.display_pic_profile:
                if (isSelf) {
                    String title = I18n.tr("Set display picture via");
                    ArrayList<ContextMenuItem> menuItemList = generateSetDisplayPictureOptions();
                    Tools.showContextMenu(title, menuItemList, this);
                }
                break;
            case R.id.share:
                if (profile != null) {
                    ShareManager.shareProfile(getActivity(), profile.getUsername());
                }
                break;
        }
    }

    private ArrayList<ContextMenuItem> generateSetDisplayPictureOptions() {
        ArrayList<ContextMenuItem> menuItems = new ArrayList<ContextMenuItem>();

        menuItems.add(new ContextMenuItem(I18n.tr("Camera"), R.id.option_item_camera, null));
        menuItems.add(new ContextMenuItem(I18n.tr("Gallery"), R.id.option_item_gallery, null));
        menuItems.add(new ContextMenuItem(I18n.tr("Avatar"), R.id.option_item_avatar, null));

        return menuItems;
    }

    @Override
    public void onContextMenuItemClick(ContextMenuItem menuItem) {
        int optionId = menuItem.getId();
        switch (optionId) {
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
                    ActionHandler.getInstance().displayBrowser(getActivity(), WebURL.URL_MY_AVATAR, I18n.tr("Avatar"),
                            R.drawable.ad_user_white);
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == Constants.REQ_PIC_FROM_CAMERA_FOR_DISPLAY_PIC && resultCode == Activity.RESULT_OK) {
            try {

                File file = new File(Tools.getCapturedPhotoFile(getActivity()));
                Uri selectedUri = Uri.fromFile(file);

                // Ask for cropping the selected image
                ActionHandler.getInstance().cropImage(this, selectedUri, CropImageType.PROFILE, Constants.REQ_CROP_IMAGE_FOR_CAMERA_IMAGE);

            } catch (IOException | ActivityNotFoundException e) {
                Logger.error.log(TAG, e);

                // ActivityNotFoundException: lack of cropping image app
                // IOException: do not have storage space for creating the temp file for cropping
                // The cropping is not successful; adopt the original selected photo
                Bitmap photo = Tools.loadImageFromCapturedPhotoFile(getActivity());
                if (photo != null) {
                    // no need to resize again here, it's already resized when
                    // it's loaded previously, which is good for memory usage
                    byte[] imageData = Tools.getBitmapDataForUpload(photo, false);

                    Tools.showToast(getActivity(), I18n.tr("Updating"));

                    UserDatastore.getInstance().requestUploadProfilePhoto(imageData);
                    photo.recycle();
                }

                try {
                    Tools.deleteCapturedPhotoFile(getActivity());
                    Tools.deleteCroppedImageFile(getActivity());
                } catch (IOException ioe) {
                    Logger.error.log(TAG, e);
                }

            } catch (Exception e) {
                Logger.error.log(TAG, e);
            }

        } else if (requestCode == Constants.REQ_PIC_FROM_GALLERY_FOR_DISPLAY_PIC && resultCode == Activity.RESULT_OK) {

            try {

                if (intent != null) {
                    Uri selectedUri = intent.getData();
                    this.selectedImageUri = intent.getData();

                    // Ask for cropping the image
                    ActionHandler.getInstance().cropImage(this, selectedUri, CropImageType.PROFILE, Constants.REQ_CROP_IMAGE_FOR_GALLERY_IMAGE);
                }

            } catch (IOException | ActivityNotFoundException e) {
                Logger.error.log(TAG, e);

                // ActivityNotFoundException: lack of cropping image app
                // IOException: do not have storage space for creating the temp file for cropping
                // The cropping is not successful; adopt the original selected photo
                if (this.selectedImageUri != null) {
                    Uri selectedImageUri = this.selectedImageUri;
                    try {
                        Bitmap selectedBmp;
                        selectedBmp = Tools.resizeAndRotateImage(getActivity(), selectedImageUri,
                                Constants.DEFAULT_PHOTO_SIZE, Constants.DEFAULT_PHOTO_SIZE);

                        if (selectedBmp != null) {
                            // no need to resize again here, it's already resized
                            // when it's loaded previously, which is good for memory
                            // usage
                            byte[] imageData = Tools.getBitmapDataForUpload(selectedBmp, false);

                            Tools.showToast(getActivity(), I18n.tr("Updating"));

                            UserDatastore.getInstance().requestUploadProfilePhoto(imageData);

                            selectedBmp.recycle();
                        } else {
                            Tools.showToast(getActivity(), I18n.tr("Failed to update"));
                        }

                    } catch (Exception exception) {
                        Logger.error.log(TAG, exception);
                    }
                }

                try {
                    Tools.deleteCroppedImageFile(getActivity());
                } catch (IOException ioe) {
                    Logger.error.log(TAG, e);
                }

            } catch (Exception e) {
                Logger.error.log(TAG, e);
            }

        } else if (requestCode == Constants.REQ_CROP_IMAGE_FOR_CAMERA_IMAGE && resultCode == Activity.RESULT_OK) {
            try {
                // The cropping is successful; adopt the cropped image to upload
                Bitmap photo = Tools.loadImageFromCroppedImageFile(getActivity());
                if (photo != null) {
                    // no need to resize again here, it's already resized when
                    // it's loaded previously, which is good for memory usage
                    byte[] imageData = Tools.getBitmapDataForUpload(photo, false);

                    Tools.showToast(getActivity(), I18n.tr("Updating"));

                    UserDatastore.getInstance().requestUploadProfilePhoto(imageData);
                    photo.recycle();
                } else {
                    Tools.showToast(getActivity(), I18n.tr("Failed to update"));
                }

                Tools.deleteCapturedPhotoFile(getActivity());
                Tools.deleteCroppedImageFile(getActivity());

            } catch (Exception e) {
                Logger.error.log(TAG, e);
            }

        } else if (requestCode == Constants.REQ_CROP_IMAGE_FOR_GALLERY_IMAGE && resultCode == Activity.RESULT_OK) {

            // The cropping is successful; adopt the cropped image to upload
            File croppedFile = new File(Tools.getCroppedImageFile(getActivity()));
            Uri selectedImageUri = Uri.fromFile(croppedFile);

            try {
                Bitmap selectedBmp;
                selectedBmp = Tools.resizeAndRotateImage(getActivity(), selectedImageUri,
                        Constants.DEFAULT_PHOTO_SIZE, Constants.DEFAULT_PHOTO_SIZE);

                if (selectedBmp != null) {
                    // no need to resize again here, it's already resized
                    // when it's loaded previously, which is good for memory
                    // usage
                    byte[] imageData = Tools.getBitmapDataForUpload(selectedBmp, false);

                    Tools.showToast(getActivity(), I18n.tr("Updating"));

                    UserDatastore.getInstance().requestUploadProfilePhoto(imageData);

                    selectedBmp.recycle();
                } else {
                    Tools.showToast(getActivity(), I18n.tr("Failed to update"));
                }

                Tools.deleteCroppedImageFile(getActivity());

            } catch (Exception e) {
                Logger.error.log(TAG, e);
            }

        }
    }

    private void checkRelationship() {
        if (profile != null) {
            relationshipBtn.setVisibility(View.VISIBLE);

            Relationship relationship = profile.getRelationship();
            // AD-1115 remove check inProcessingUser, just base on relationship to display status icon
//            boolean isProcessingUser = FriendsController.getInstance().isChangeRelationshipWithUserInProgress(username);
//            if (isProcessingUser) {
//                relationshipBtn.setIcon(R.drawable.ad_fanof_white);
//            }
            if (relationship != null) {
                if (relationship.isFriend()) {
                    relationshipBtn.setIcon(R.drawable.ad_friend_white);
                    relationshipBtn.setType(ButtonUtil.BUTTON_TYPE_TURQUOISE);
                } else if (relationship.isFollower()) {
                    relationshipBtn.setIcon(R.drawable.ad_fanof_white);
                } else {
                    relationshipBtn.setIcon(R.drawable.ad_addfan_white);
                }
            } else {
                relationshipBtn.setIcon(R.drawable.ad_addfan_white);
            }
        }
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = super.getActionBarConfig();
        config.setCustomViewLayoutSrc(R.layout.action_bar_profile);
        config.setNavigationButtonState(NavigationButtonState.BACK);
        config.setShowOverflowButtonState(OverflowButtonState.POPUP);
        return config;
    }

    @Override
    public void initCustomViewInCustomActionBar(View customView) {
        mHeaderUserInfo = (UserBasicDetails) customView.findViewById(R.id.user_details);
        mHeaderUserInfo.setVisibility(View.VISIBLE);
        mHeaderUserInfo.setUsernameColor(Theme.getColor(ThemeValues.LIGHT_TEXT_COLOR));
        mHeaderUserInfo.setUsername(username);
        mHeaderUserInfo.showMainIcon();
        if (isSelf) {
            mHeaderUserInfo.setSelfImage(profile);
        } else {
            mHeaderUserInfo.setUserImage(profile);
        }
    }

    @Override
    public ArrayList<MenuOption> getMenuOptions() {
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

            if (profile != null && profile.getRelationship() != null) {
                if (profile.getRelationship().isFriend()) {
                    menuItems.add(new MenuOption(I18n.tr("Unfriend"), R.drawable.ad_relfan_white,
                            R.id.option_item_remove_friend));
                } else if (profile.getRelationship().isFollower()) {
                    menuItems.add(new MenuOption(I18n.tr("Unfan"), R.drawable.ad_relfan_white,
                            R.id.option_item_unfollow));
                }
            }
        }

        return menuItems;
    }

    @Override
    public void onMenuOptionClicked(MenuOption menuOption) {
        switch (menuOption.getActionId()) {
            case R.id.option_item_buy_credits:
                GAEvent.Profile_OwnProfileDropdownBuyCredits.send();
                ActionHandler.getInstance().displayBrowser(getActivity(), WebURL.URL_BUYCREDIT, I18n.tr("Buy credit"),
                        R.drawable.ad_credit_white);
                break;
            case R.id.option_item_send_gift:
                GAEvent.Profile_OtherProfileDropdownSendGift.send();
                ActionHandler.getInstance().displayStore(getActivity(), username);
                break;
            case R.id.option_item_mention:
                GAEvent.Profile_OtherProfileDropdownMention.send();
                ActionHandler.getInstance().displaySharebox(getActivity(), ShareboxActionType.CREATE_NEW_POST, null,
                        "@" + username + Constants.SPACESTR, null, true);
                break;
            case R.id.option_item_transfer_credit:
                GAEvent.Profile_OtherProfileDropdownTransferCredits.send();
                String urlTransferCredits = String.format(WebURL.URL_TRANSFER_CREDITS, username);
                ActionHandler.getInstance().displayBrowser(getActivity(), urlTransferCredits,
                        I18n.tr("Transfer credit"), R.drawable.ad_credit_white);
                break;
            case R.id.option_item_report_abuse:
                GAEvent.Profile_OtherProfileDropdownReport.send();
                String urlReport = String.format(WebURL.URL_REPORT_USER, username);
                ActionHandler.getInstance().displayBrowser(getActivity(), urlReport, I18n.tr("Report"),
                        R.drawable.ad_report_white);
                break;
            case R.id.option_item_block:
                GAEvent.Profile_OtherProfileDropdownBlock.send();
                Friend friendToBlock = UserDatastore.getInstance().findMig33User(username);
                if (friendToBlock != null) {
                    ActionHandler.getInstance().blockFriend(getActivity(), null, friendToBlock.getDisplayName(),
                            friendToBlock.getUsername());
                } else {
                    ActionHandler.getInstance().blockFriend(getActivity(), null, username, username);
                }
                break;
            case R.id.option_item_remove_friend:
                GAEvent.Profile_OtherProfileDropdownUnfriend.send();
                Friend friendToRemove = UserDatastore.getInstance().findMig33User(username);
                if (friendToRemove != null) {
                    ActionHandler.getInstance().removeFriend(getActivity(), friendToRemove.getDisplayName(),
                            friendToRemove.getContactID());
                }
                break;
            case R.id.option_item_unfollow:
                GAEvent.Profile_OtherProfileDropdownUnfriend.send();
                ActionHandler.getInstance().unfollowFriend(getActivity(), username);
                break;
            case R.id.option_item_settings:
                GAEvent.Profile_OwnProfileDropdownSettings.send();
                ActionHandler.getInstance().displaySettings(getActivity(), SettingsGroupType.FIRST_LEVEL);
                break;
            case R.id.option_item_share:
                if (profile != null) {
                    ShareManager.shareProfile(getActivity(), profile.getUsername());
                }
                break;
        }
    }

    @Override
    public OnPopupMenuListener getPopupMenuListener() {
        return this;
    }

    private View initHeader() {

        name = (TextView) header.findViewById(R.id.username);
        name.setText(username);
        userDetails = (TextView) header.findViewById(R.id.user_details);
        labels = (UsernameWithLabelsView) header.findViewById(R.id.labels);

        displayPic = (ImageView) header.findViewById(R.id.display_pic_profile);
        presenceIcon = (ImageView) header.findViewById(R.id.display_pic_overlay);
        coverPhoto = (ImageView) header.findViewById(R.id.cover_photo);
        relationshipBtn = (ButtonEx) header.findViewById(R.id.chat_button);
        displayPic.setOnClickListener(this);
        displayPic.setImageResource(R.drawable.icon_default_avatar);
        relationshipBtn.setOnClickListener(this);

        header.findViewById(R.id.share).setOnClickListener(this);

        if (isSelf) {
            relationshipBtn.setVisibility(View.GONE);
        } else {
            if (profile != null) {
                Friend friendDetails = UserDatastore.getInstance().findMig33User(username);
                if (friendDetails != null) {
                    presenceIcon.setVisibility(View.VISIBLE);
                    presenceIcon.setImageResource(Tools.getFusionPresenceResource(friendDetails.getPresence()));
                }

                checkRelationship();
            }
        }

        return header;
    }

    @Override
    public void onClick(View v, ClickableSpanEx span, String value) {
        UrlHandler.displayUrl(getActivity(), value);
    }

    /**
     *  we need this method to init the height of ProfileInfoFragment's header. which is based on ProfileFragment's
     *  header. otherwise we'll see a moment that ProfileInfoFragment's header covered by the header of ProfileFragment w
     *  hen launch it.
     * */
    private void initHeaderHeight() {

        header.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener(){

                    @Override
                    public void onGlobalLayout() {
                        // gets called after layout has been done but before display
                        // so we can get the header's height

                        if (!UIUtils.hasJellyBean()) {
                            header.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            header.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        //call this to update the height of ProfileInfoFragment's header on first time launch
                        adapter.initPlaceHolderHeader();
                    }

                });

    }
}
