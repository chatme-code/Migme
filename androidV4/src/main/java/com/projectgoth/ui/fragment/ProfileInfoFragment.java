/**
 * Copyright (c) 2013 Project Goth
 *
 * ProfileInfoFragment.java
 * Created Nov 11, 2014, 10:53:17 PM
 */

package com.projectgoth.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Profile;
import com.projectgoth.common.Config;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Tools;
import com.projectgoth.common.WebURL;
import com.projectgoth.datastore.GiftsDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.ProfileInfoCategory;
import com.projectgoth.model.ProfileInfoCategory.Type;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.adapter.ProfileInfoAdapter;
import com.projectgoth.ui.adapter.ProfilePagerAdapter.HeaderPlaceHolderInterface;
import com.projectgoth.ui.holder.ProfileInfoHolder.ProfileInfoListener;
import com.projectgoth.ui.holder.ProfileMainHolder.ProfileMainHolderListener;
import com.projectgoth.util.ProfileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author warrenbalcos
 * 
 */
public class ProfileInfoFragment extends BaseListFragment implements ProfileInfoListener, ProfileMainHolderListener {

    public static final String         PARAM_USERNAME       = "PARAM_USERNAME";

    private ProfileInfoAdapter         profileInfoAdapter;

    private Profile                    profile;

    private String                     username;

    private boolean                    isSelf;

    private boolean                    isProfilePrivate;

    private int                        unreadMentionsCount = ApplicationEx.getInstance().getNotificationHandler().getUnreadMentionCount();

    private boolean                    hasHeaderPlaceHolder;

    private View                       headerPlaceholder;
    private View                       emptyView;
    
    //@formatter:off
    private HeaderPlaceHolderInterface headerImplementation = new HeaderPlaceHolderInterface() {
        
        @Override
        public void updatePlaceholderHeader(int height) {
            if (headerPlaceholder != null && headerPlaceholder.getMeasuredHeight() != height) {
                ListView.LayoutParams layoutParams = new ListView.LayoutParams(1, height);
                headerPlaceholder.setLayoutParams(layoutParams);
            }
            int normalMargin = ApplicationEx.getDimension(R.dimen.normal_margin);
            changeEmptyViewMargins(height + normalMargin);
        }
        
        @Override
        public View getHeaderPlaceholder() {
            return headerPlaceholder;
        }
        
        @Override
        public void adjustScroll(int minTranslation, int scrollHeight) {
//            Logger.info.log("ProfileFragment", "INFO: FirstVisiblePosition: " , mList.getFirstVisiblePosition(), " scrollHeight: ", scrollHeight);
            if (scrollHeight == minTranslation && mList.getFirstVisiblePosition() >= 1) {
                return;
            }

            mList.setSelectionFromTop(1, scrollHeight);
        }
    };
    //@formatter:on

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        username = args.getString(PARAM_USERNAME);
        isSelf = Session.getInstance().isSelfByUsername(username);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_profile_info;
    }

    @Override
    protected BaseAdapter createAdapter() {
        profileInfoAdapter = new ProfileInfoAdapter();
        profileInfoAdapter.setProfileInfoListener(this);
        profileInfoAdapter.setProfileMainHolderListener(this);
        return profileInfoAdapter;
    }

    @Override
    protected View createHeaderView() {
        if (hasHeaderPlaceHolder) {
            mIsHeaderEnabled = true;
            headerPlaceholder = new View(ApplicationEx.getContext());
            return headerPlaceholder;
        }
        return null;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateProfileData();
    }
    
    @Override
    public HeaderPlaceHolderInterface getHeaderPlaceHolderImplementation() {
        return headerImplementation;
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.Profile.RECEIVED);
        registerEvent(Events.User.FETCH_BADGES_COMPLETED);

        registerEvent(Events.User.FOLLOWED);
        registerEvent(Events.User.ALREADY_FOLLOWING);
        registerEvent(Events.User.PENDING_APPROVAL);

        registerEvent(Events.User.UNFOLLOWED);
        registerEvent(Events.User.REQUESTING_FOLLOWING);
        registerEvent(Events.User.NOT_FOLLOWING);

        registerEvent(Events.MigAlert.UNREAD_MENTION_COUNT_RECEIVED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(Events.MigAlert.UNREAD_MENTION_COUNT_RECEIVED)) {
            unreadMentionsCount = ApplicationEx.getInstance().getNotificationHandler().getUnreadMentionCount();
            updateProfileData();
        } else {
            String user = intent.getStringExtra(Events.User.Extra.USERNAME);
            if (username.equalsIgnoreCase(user)) {
                updateProfileData();
            }
        }
    }

    private void updateProfileData() {

        List<ProfileInfoCategory> profileInfoList = new ArrayList<ProfileInfoCategory>();

        int gift = 0;
        int fan = 0;
        int fanOf = 0;
        int badge = UserDatastore.getInstance().getUnlockedBadgesCounter(username);

        String remarks = null;
        String statusMessage = null;

        String favourites = I18n.tr("Favorites");
        String chatrooms = I18n.tr("Chat Rooms");
        String photos = I18n.tr("Photos");
        String games = I18n.tr("Games");

        profile = UserDatastore.getInstance().getProfileWithUsername(username, false);
        if (profile != null && hasCompleteProfile()) {

            isProfilePrivate = ProfileUtils.isProfilePrivate(profile);

            gift = profile.getNumOfGiftsReceived();
            fan = profile.getNumOfFollowers();
            fanOf = profile.getNumOfFollowing();

            statusMessage = profile.getStatusMessage();
            if (!TextUtils.isEmpty(statusMessage)) {
                statusMessage = String.format("\"%s\"", statusMessage);
            }
            remarks = Tools.formatProfileRemarks(profile);

            int watchPosts = profile.getNumOfWatchPosts();
            if (watchPosts > 0) {
                favourites = String.format(I18n.tr("Favorites (%d)"), watchPosts);
            }

            int chatroomsOwned = profile.getNumOfChatroomsOwned();
            if (chatroomsOwned > 0) {
                chatrooms = String.format(I18n.tr("Chat Rooms (%d)"), chatroomsOwned);
            }

            int photosUploaded = profile.getNumOfMigPhotosUploaded();
            if (photosUploaded > 0) {
                photos = String.format(I18n.tr("Photos (%d)"), photosUploaded);
            }

            int gamesPlayed = profile.getNumOfGamesPlayed();
            if (gamesPlayed > 0) {
                games = String.format(I18n.tr("Games (%d)"), gamesPlayed);
            }

            ProfileInfoCategory category = new ProfileInfoCategory(gift, badge, fan, fanOf);
            category.setType(Type.Main);
            profileInfoList.add(category);
            profileInfoList
                    .add(new ProfileInfoCategory(I18n.tr("About"), remarks, statusMessage, R.id.action_profile_about));

            if (isSelf) {
                ProfileInfoCategory pic = new ProfileInfoCategory(I18n.tr("Mentions"), R.id.action_profile_mentions);
                pic.setUnreadCount(unreadMentionsCount);
                profileInfoList.add(pic);
                profileInfoList.add(new ProfileInfoCategory(favourites, R.id.action_profile_favourites));
            }

            profileInfoList.add(new ProfileInfoCategory(chatrooms, R.id.action_profile_chatrooms));
            profileInfoList.add(new ProfileInfoCategory(photos, R.id.action_profile_photos));
            profileInfoList.add(new ProfileInfoCategory(games, R.id.action_profile_games));

        }
        profileInfoAdapter.setProfileCategoryListData(profileInfoList);
        profileInfoAdapter.notifyDataSetChanged();

        showOrHideEmptyViewIfNeeded();
    }

    @Override
    protected void showOrHideEmptyViewIfNeeded() {
        if (hasCompleteProfile()) {
            mEmptyViewContainer.setVisibility(View.GONE);
            mEmptyViewContainer.removeAllViews();
            emptyView = null;
        } else {
            mEmptyViewContainer.setVisibility(View.VISIBLE);
            setListEmptyView(createEmptyView());

        }
    }

    /**
     * @return emptyView
     */
    private View createEmptyView() {
        emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_view_loading, mList, false);
        ImageView loadingIcon = (ImageView) emptyView.findViewById(R.id.loading_icon);
        loadingIcon.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.loading_icon_rotate));

        return emptyView;
    }

    private void changeEmptyViewMargins(int top) {
        if (mIsHeaderEnabled && emptyView != null) {
            FrameLayout.LayoutParams emptyViewLayoutParams =
                    new FrameLayout.LayoutParams(emptyView.getLayoutParams());
            emptyViewLayoutParams.setMargins(0, top, 0, 0);
            Logger.debug.log("changeEmptyViewMargins", "height:" + emptyViewLayoutParams.topMargin);
            emptyView.setLayoutParams(emptyViewLayoutParams);
        }
    }

    private boolean hasCompleteProfile() {
        if (profile == null || profile.getNumOfFollowersInteger() == null ||
                profile.getNumOfFollowingInteger() == null || profile.getNumOfGiftsReceivedInteger() == null) {
            return false;
        } else  {
            return true;
        }
    }

    @Override
    public void onMainHolderClicked(ProfileInfoCategory data, int viewId) {
        if (data != null) {
            if (!isSelf && isProfilePrivate) {
                Tools.showToast(getActivity(),
                        I18n.tr(String.format("%s\'s profile is protected", profile.getUsername())));
                return;
            }

            switch (viewId) {
                case R.id.menu_gifts:
                    GAEvent.Profile_GiftList.send();
                    
                    if (!Config.getInstance().isMyGiftsEnabled()) {
                        int numGift = 0;
                        if (profile != null) {
                            numGift = profile.getNumOfGiftsReceived();
                        }
                        ActionHandler.getInstance().displayBrowser(getActivity(),
                                String.format(WebURL.URL_GIFTS_RECEIVED, username),
                                String.format(I18n.tr("Gifts (%d)"), numGift), R.drawable.ad_gift_white);
                    } else {
                        if (isSelf) {
                            ActionHandler.getInstance().displayMyGifts(getActivity(), Session.getInstance().getUserId());
                        } else {
                            if (profile != null) {
                                ActionHandler.getInstance().displayMyGiftsCardListFragment(getActivity(), I18n.tr("Gifts"),
                                        GiftsDatastore.Category.ALL.ordinal(), false, profile.getId().toString());
                            }
                        }
                    }
                    break;
                case R.id.menu_badges:
                    GAEvent.Profile_BadgeList.send();
                    ActionHandler.getInstance().displayBadgesList(getActivity(), username);
                    break;
                case R.id.menu_fans:
                    GAEvent.Profile_FanList.send();
                    ActionHandler.getInstance().displayFollowersList(getActivity(), username);
                    break;
                case R.id.menu_fan_of:
                    GAEvent.Profile_FanOfList.send();
                    ActionHandler.getInstance().displayFollowingList(getActivity(), username);
                    break;
            }
        }
    }

    @Override
    public void onProfileInfoClicked(ProfileInfoCategory data) {
        if (data != null) {
            int count = 0;

            switch (data.getAction()) {
                case R.id.action_profile_about:
                    if (isSelf)
                        GAEvent.Profile_OwnProfileAbout.send();
                    else
                        GAEvent.Profile_OtherProfileAbout.send();
                    if (!isSelf && isProfilePrivate) {
                        showProfilePrivateToast();
                    } else {
                        ActionHandler.getInstance().displayFullProfile(getActivity(), username);
                    }
                    break;
                case R.id.action_profile_mentions:
                    GAEvent.Profile_MentionList.send();
                    unreadMentionsCount = 0;
                    ApplicationEx.getInstance().getNotificationHandler().resetUnreadMentionCount();
                    ActionHandler.getInstance().displayMentions(getActivity());
                    break;
                case R.id.action_profile_favourites:
                    GAEvent.Profile_FavoriteList.send();
                    ActionHandler.getInstance().displayFavourites(getActivity());
                    break;
                case R.id.action_profile_chatrooms:
                    GAEvent.Profile_ChatroomList.send();
                    if (!isSelf && isProfilePrivate) {
                        showProfilePrivateToast();
                    } else {
                        count = 0;
                        if (profile != null) {
                            count = profile.getNumOfChatroomsOwned();
                        }
                        ActionHandler.getInstance().displayBrowser(getActivity(),
                                String.format(WebURL.URL_USER_OWNED_CHATROOMS, username),
                                String.format(I18n.tr("Chat rooms (%d)"), count), R.drawable.ad_chatroom_white);
                    }
                    break;
                case R.id.action_profile_photos:
                    GAEvent.Profile_PhotoList.send();
                    if (!isSelf && isProfilePrivate) {
                        showProfilePrivateToast();
                    } else {
                        count = 0;
                        if (profile != null) {
                            count = profile.getNumOfMigPhotosUploaded();
                        }
                        ActionHandler.getInstance().displayBrowser(getActivity(),
                                String.format(WebURL.URL_PHOTOS, username),
                                String.format(I18n.tr("Photos (%d)"), count), R.drawable.ad_gallery_white);
                    }
                    break;
                case R.id.action_profile_games:
                    GAEvent.Profile_GameList.send();
                    if (!isSelf && isProfilePrivate) {
                        showProfilePrivateToast();
                    } else {
                        count = 0;
                        if (profile != null) {
                            count = profile.getNumOfGamesPlayed();
                        }
                        ActionHandler.getInstance().displayBrowser(getActivity(),
                                String.format(WebURL.URL_GAMES_PLAYED, username),
                                String.format(I18n.tr("Games (%d)"), count), R.drawable.ad_play_white);
                    }
                    break;
            }
        }
    }

    private void showProfilePrivateToast() {
        Tools.showToast(getActivity(), I18n.tr(String.format("%s\'s profile is protected", profile.getUsername())));
    }

    /**
     * @param hasHeaderPlaceHolder
     *            the hasHeaderPlaceHolder to set
     */
    public void setHasHeaderPlaceHolder(boolean hasHeaderPlaceHolder) {
        this.hasHeaderPlaceHolder = hasHeaderPlaceHolder;
    }
    
}
