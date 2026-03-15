/**
 * Copyright (c) 2013 Project Goth
 *
 * FragmentUtils.java
 * Created Mar 13, 2014, 9:42:17 AM
 */

package com.projectgoth.util;

import android.os.Bundle;
import android.view.View;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.enums.EveryoneOrFollowerAndFriendPrivacyEnum;
import com.projectgoth.common.TextUtils;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.StoreController.StoreItemFilterType;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.ContextMenuItem;
import com.projectgoth.model.ViewPagerItem;
import com.projectgoth.nemesis.model.ContactGroup;
import com.projectgoth.nemesis.model.Friend;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.FragmentHandler;
import com.projectgoth.ui.fragment.AttachmentPagerFragment;
import com.projectgoth.ui.fragment.BaseFragment;
import com.projectgoth.ui.fragment.FriendListFragment;
import com.projectgoth.ui.fragment.FriendListFragment.ContactGroupListener;
import com.projectgoth.ui.fragment.FriendListFragment.FriendListItemActionType;
import com.projectgoth.ui.fragment.FriendListFragment.FriendsListListener;
import com.projectgoth.ui.fragment.GiftCenterFragment;
import com.projectgoth.ui.fragment.PostListFragment;
import com.projectgoth.ui.fragment.ProfileInfoFragment;
import com.projectgoth.ui.fragment.SinglePostGiftFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Contains helper utilities pertaining to fragments
 * 
 * @author mapet
 * 
 */
public class FragmentUtils {

    public static final String PARAM_USERNAME          = "PARAM_USERNAME";
    public static final String PARAM_URL               = "PARAM_URL";
    public static final String PARAM_IS_FULL_URL       = "PARAM_IS_FULL_URL";
    public static final String PARAM_USERID            = "PARAM_USERID";
    public static final String PARAM_NUMOFPOSTS        = "PARAM_NUMOFPOST";
    public static final String PARAM_FEEDPRIVACY       = "PARAM_FEEDPRIVACY";
    public static final String PARAM_MESSAGE           = "PARAM_MESSAGE";
    public static final String PARAM_INITIAL_RECIPIENT = "PARAM_INITIAL_RECIPIENT";
    public static final String PARAM_ALLOW_FILTER      = "PARAM_ALLOW_FILTER";

    public static BaseFragment getFragmentByType(ViewPagerItem item) {

        Bundle args = item.getArgs();
        BaseFragment fragment = null;
        FragmentHandler fragmentHandler = FragmentHandler.getInstance();

        switch (item.getType()) {
            case BADGES:
            {
                if (args != null) {
                    String username = args.getString(PARAM_USERNAME);
                    if (!TextUtils.isEmpty(username)) {
                        fragment = fragmentHandler.getBadgesFragment(username);
                    }
                }
                break;
            }
            case STORE_STICKER:
            {
                fragment = fragmentHandler.getStickerStoreFragment();
                break;
            }
            case BROWSER:
            case BROWSER_GIFTS:
            case BROWSER_GROUPS:
            case BROWSER_GAMES:
            case BROWSER_CHATROOMS:
            case BROWSER_PHOTOS:
            case BROWSER_FOOTPRINTS:
            {
                if (args != null) {
                    String url = args.getString(PARAM_URL);
                    boolean isFullyConstructedUrl = args.getBoolean(PARAM_IS_FULL_URL);
                    if (!TextUtils.isEmpty(url)) {
                        fragment = fragmentHandler.getBrowserFragment(url, isFullyConstructedUrl);
                    }
                }
                break;
            }
            case STORE_EMOTICON:
            case STORE_AVATAR:
            {
                if (args != null) {
                    String url = args.getString(PARAM_URL);
                    boolean isFullyConstructedUrl = args.getBoolean(PARAM_IS_FULL_URL);
                    if (!TextUtils.isEmpty(url)) {
                        fragment = FragmentHandler.getInstance().getBrowserFragment(url, isFullyConstructedUrl, null,
                                null, I18n.tr("Store"), R.drawable.ad_store_white);
                    }
                }
                break;
            }
            case CHATROOM_LIST:
            {
                boolean allow = false;
                if (args != null) {
                    allow = args.getBoolean(PARAM_ALLOW_FILTER);
                }
                fragment = fragmentHandler.getChatroomListFragment(allow);
                break;
            }
            case GIFT_LIST:
            {
                if (args != null) {
                    String initialRecipient = args.getString(PARAM_INITIAL_RECIPIENT);
                    fragment = fragmentHandler.getGiftsFragment(initialRecipient);
                }
                break;
            }
            case CHAT_LIST:
            {
                boolean allow = false;
                if (args != null) {
                    allow = args.getBoolean(PARAM_ALLOW_FILTER);
                }
                fragment = fragmentHandler.getChatListFragment(allow, false);
                break;
            }
            case POST_FEEDS_LIST:
            {
                fragment = fragmentHandler.getFeedsListFragment();
                break;
            }
            case POST_MENTIONS_LIST:
            {
                fragment = fragmentHandler.getMentionsListFragment();
                break;
            }
            case POST_WATCHED_LIST:
            {
                fragment = fragmentHandler.getWatchedPostsFragment();
                break;
            }
            case PROFILE_POST_LIST:
            {
                if (args != null) {
                    String userId = args.getString(PARAM_USERID);
                    String username = args.getString(PARAM_USERNAME);
                    int numOfPosts = args.getInt(PARAM_NUMOFPOSTS);
                    EveryoneOrFollowerAndFriendPrivacyEnum feedPrivacy = EveryoneOrFollowerAndFriendPrivacyEnum
                            .fromValue(args.getInt(PARAM_FEEDPRIVACY));

                    PostListFragment postList = fragmentHandler.getUserPostFragment(userId, username, numOfPosts,
                            feedPrivacy);
                    postList.setShouldUpdateActionBarOnAttach(false);
                    postList.setHasHeaderPlaceHolder(item.hasHeaderPlaceHolder());
                    fragment = postList;
                }
                break;
            }
            case PROFILE_INFO:
            {
                if (args != null) {
                    String username = args.getString(PARAM_USERNAME);
                    ProfileInfoFragment info = fragmentHandler.getProfileInfoFragment(username);
                    info.setShouldUpdateActionBarOnAttach(false);
                    info.setHasHeaderPlaceHolder(item.hasHeaderPlaceHolder());
                    fragment = info;
                }
                break;
            }
            case PROFILE_FOLLOWERS_LIST:
            {
                if (args != null) {
                    String username = args.getString(PARAM_USERNAME);
                    if (!TextUtils.isEmpty(username)) {
                        fragment = fragmentHandler.getFollowersListFragment(username, true);
                    }
                }
                break;
            }
            case PROFILE_FOLLOWING_LIST:
            {
                if (args != null) {
                    String username = args.getString(PARAM_USERNAME);
                    if (!TextUtils.isEmpty(username)) {
                        fragment = fragmentHandler.getFollowingListFragment(username, true);
                    }
                }
                break;
            }
            case STORE_GIFT:
            {
                if (args != null) {
                    String initialRecipient = args.getString(PARAM_INITIAL_RECIPIENT);
                    fragment = fragmentHandler.getGiftsFragment(initialRecipient);
                }
                break;
            }
            case ATTACHMENT_GRID:
            {
                if (args != null) {
                    int packId = args.getInt(AttachmentPagerFragment.PARAM_PACK_ID);
                    int numOfColumn = args.getInt(AttachmentPagerFragment.PARAM_PAGER_COLUMNS);
                    fragment = fragmentHandler.getAttachmentFragment(packId, numOfColumn);
                }

                break;
            }
            case GIFT_CENTER_POPULAR_GIFTS:
            {
                if (args != null) {
                    boolean isFromSinglePost = args.getBoolean(SinglePostGiftFragment.PARAM_IS_FROM_SINGLE_POST, false);
                    String parentPostId = args.getString(SinglePostGiftFragment.PARAM_POST_PARENT_ID);
                    String rootPostId = args.getString(SinglePostGiftFragment.PARAM_POST_ROOT_ID);
                    if (isFromSinglePost) {
                        fragment = fragmentHandler.getSinglePostGiftCategoryFragment(StoreItemFilterType.POPULAR, rootPostId, parentPostId);
                    } else {
                        String conversationId = args.getString(GiftCenterFragment.PARAM_CONVERSATION_ID);
                        String initialRecipient = args.getString(PARAM_INITIAL_RECIPIENT);
                        ArrayList<String> selectedUsers = args.getStringArrayList(GiftCenterFragment.PARAM_SELECTED_USERS);
                        fragment = fragmentHandler.getGiftCenterGiftCategoryFragment(StoreItemFilterType.POPULAR,
                                conversationId, null, null, initialRecipient, selectedUsers);
                    }
                }
                break;
            }
            case GIFT_CENTER_NEW_GIFTS:
            {
                if (args != null) {
                    boolean isFromSinglePost = args.getBoolean(SinglePostGiftFragment.PARAM_IS_FROM_SINGLE_POST, false);
                    String parentPostId = args.getString(SinglePostGiftFragment.PARAM_POST_PARENT_ID);
                    String rootPostId = args.getString(SinglePostGiftFragment.PARAM_POST_ROOT_ID);
                    if (isFromSinglePost) {
                        fragment = fragmentHandler.getSinglePostGiftCategoryFragment(StoreItemFilterType.NEW, rootPostId, parentPostId);
                    } else {
                        String conversationId = args.getString(GiftCenterFragment.PARAM_CONVERSATION_ID);
                        String initialRecipient = args.getString(PARAM_INITIAL_RECIPIENT);
                        ArrayList<String> selectedUsers = args.getStringArrayList(GiftCenterFragment.PARAM_SELECTED_USERS);
                        fragment = fragmentHandler.getGiftCenterGiftCategoryFragment(StoreItemFilterType.NEW,
                                conversationId, null, null, initialRecipient, selectedUsers);
                    }

                }
                break;
            }
            case GIFT_CENTER_CATEGORY_LIST:
            {
                if (args != null) {
                    String conversationId = args.getString(GiftCenterFragment.PARAM_CONVERSATION_ID);
                    String initialRecipient = args.getString(PARAM_INITIAL_RECIPIENT);
                    ArrayList<String> selectedUsers = args.getStringArrayList(GiftCenterFragment.PARAM_SELECTED_USERS);
                    fragment = fragmentHandler.getGiftCenterCategoryListFragment(conversationId, initialRecipient,
                            selectedUsers);
                }
                break;
            }
            case MY_GIFTS:
            {
                if (args != null) {
                    String userId = args.getString(PARAM_USERID);
                    fragment = fragmentHandler.getMyGiftsListFragment(userId);
                }
                break;
            }
            case MY_GIFTS_OVERVIEW:
            {
                if (args != null) {
                    String userId = args.getString(PARAM_USERID);
                    fragment = fragmentHandler.getMyGiftsOverviewFragment(userId);
                }
                break;
            }
            case CONTACT_LIST:
            {
                final FriendListFragment friendListFragment = fragmentHandler.createFriendListFragment(true,
                        FriendListItemActionType.DEFAULT, null, false);
                friendListFragment.setFriendsListListener(new FriendsListListener() {

                    @Override
                    public void onFriendItemLongPressed(View v, Friend friend) {
                        List<ContextMenuItem> menuItemList = Tools.getContextMenuOptions(friend, true, true);
                        Tools.showContextMenu(friend.getDisplayName(), menuItemList, friendListFragment);
                    }

                    @Override
                    public void onFriendItemClicked(View v, Friend friend) {
                        if (v.getId() == R.id.user_image) {
                            ActionHandler.getInstance().displayProfile(ApplicationEx.getInstance().getCurrentActivity(),
                                    friend.getUsername());
                        } else {
                            if (friend.isFusionContact()) {
                                ActionHandler.getInstance().displayPrivateChat(ApplicationEx.getInstance().getCurrentActivity(),
                                        friend.getUsername());
                            }
                        }
                    }
                });

                friendListFragment.setContactGroupListener(new ContactGroupListener() {

                    @Override
                    public void onContactGroupItemToggle(ContactGroup contactGroup) {
                    }

                    @Override
                    public void onContactGroupItemLongPressed(ContactGroup contactGroup) {
                        String title = I18n.tr("Options");
                        ArrayList<ContextMenuItem> menuItemList = Tools.getContactGroupContextMenuOptions(contactGroup);
                        Tools.showContextMenu(title, menuItemList, friendListFragment);
                    }
                });

                fragment = friendListFragment;
                break;
            }
            default:
                break;
        }

        if (fragment != null) {
            fragment.setPagerScrollListener(item.getPagerScrollListener());
            fragment.setPagerPosition(item.getPostion());
        }

        return fragment;
    }

}
