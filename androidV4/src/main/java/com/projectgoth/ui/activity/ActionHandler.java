/**
 * Copyright (c) 2013 Project Goth
 *
 * ActionHandler.java.java
 * Created May 30, 2013, 3:05:53 PM
 */

package com.projectgoth.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Action;
import com.projectgoth.b.data.Photo;
import com.projectgoth.b.data.Post;
import com.projectgoth.b.data.StoreItem;
import com.projectgoth.b.enums.AlertTypeEnum;
import com.projectgoth.b.enums.ObjectTypeEnum;
import com.projectgoth.b.enums.PasswordTypeEnum;
import com.projectgoth.b.enums.ViewTypeEnum;
import com.projectgoth.blackhole.enums.ImType;
import com.projectgoth.blackhole.enums.MessageType;
import com.projectgoth.blackhole.enums.PresenceType;
import com.projectgoth.blackhole.model.Captcha;
import com.projectgoth.common.Config;
import com.projectgoth.common.ConnectionDetail;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Tools;
import com.projectgoth.common.WebURL;
import com.projectgoth.controller.ChatController;
import com.projectgoth.controller.FacebookLoginController;
import com.projectgoth.controller.FriendsController;
import com.projectgoth.controller.PostsController;
import com.projectgoth.controller.StoreController;
import com.projectgoth.controller.StoreController.StoreItemFilterType;
import com.projectgoth.controller.SystemController;
import com.projectgoth.datastore.AddressBookDatastore;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.datastore.PostsDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.enums.AlertTypeTitleInfo;
import com.projectgoth.enums.CropImageType;
import com.projectgoth.enums.ViewPagerType;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.model.ContextMenuItem;
import com.projectgoth.model.LocationListItem;
import com.projectgoth.model.StorePagerItem;
import com.projectgoth.music.deezer.DeezerDetailListFragment;
import com.projectgoth.nemesis.enums.ActivitySourceEnum;
import com.projectgoth.nemesis.enums.ChatTypeEnum;
import com.projectgoth.nemesis.model.Friend;
import com.projectgoth.notification.AlertListener;
import com.projectgoth.ui.UrlHandler;
import com.projectgoth.ui.fragment.AccountBalanceFragment;
import com.projectgoth.ui.fragment.BadgeInfoFragment;
import com.projectgoth.ui.fragment.BadgesFragment;
import com.projectgoth.ui.fragment.BaseDialogFragment;
import com.projectgoth.ui.fragment.BaseFragment;
import com.projectgoth.ui.fragment.BrowserFragment;
import com.projectgoth.ui.fragment.CaptchaFragment;
import com.projectgoth.ui.fragment.ChatFragment;
import com.projectgoth.ui.fragment.ChatroomListFragment;
import com.projectgoth.ui.fragment.CreateChatroomFragment;
import com.projectgoth.ui.fragment.FullProfileFragment;
import com.projectgoth.ui.fragment.GameCentreFragment;
import com.projectgoth.ui.fragment.GiftCategoryFragment;
import com.projectgoth.ui.fragment.GiftCategoryParentFragment;
import com.projectgoth.ui.fragment.GiftCenterFragment;
import com.projectgoth.ui.fragment.GiftFragment;
import com.projectgoth.ui.fragment.GiftPreviewFragment;
import com.projectgoth.ui.fragment.GiftPurchasedFragment;
import com.projectgoth.ui.fragment.GiftRecipientSelectionFragment;
import com.projectgoth.ui.fragment.GiftSentFragment;
import com.projectgoth.ui.fragment.GlobalSearchFragment;
import com.projectgoth.ui.fragment.GlobalSearchPreviewFragment;
import com.projectgoth.ui.fragment.GroupPageFragment;
import com.projectgoth.ui.fragment.HotTopicsFragment;
import com.projectgoth.ui.fragment.InterstitialBannerFragment;
import com.projectgoth.ui.fragment.InviteFriendsFragment;
import com.projectgoth.ui.fragment.LocationListFragment;
import com.projectgoth.ui.fragment.LoginDialogFragment;
import com.projectgoth.ui.fragment.MainFragment;
import com.projectgoth.ui.fragment.MainFragment.ViewPagerFragmentIndex;
import com.projectgoth.ui.fragment.MiniProfilePopupFragment;
import com.projectgoth.ui.fragment.MusicFragment;
import com.projectgoth.ui.fragment.MusicGenreFilterFragment;
import com.projectgoth.ui.fragment.MyGiftsCardListFragment;
import com.projectgoth.ui.fragment.MyGiftsCategoryFragment;
import com.projectgoth.ui.fragment.MyGiftsListFragment;
import com.projectgoth.ui.fragment.MyGiftsOverviewFilterFragment;
import com.projectgoth.ui.fragment.MyGiftsOverviewFilterFragment.MyGiftsOverviewFilterType;
import com.projectgoth.ui.fragment.MyGiftsOverviewFilterFragment.MyGiftsOverviewSortingListener;
import com.projectgoth.ui.fragment.MyGiftsPagerFragment;
import com.projectgoth.ui.fragment.MyStickersFragment;
import com.projectgoth.ui.fragment.NotificationListFragment;
import com.projectgoth.ui.fragment.PhotoViewerFragment;
import com.projectgoth.ui.fragment.PositiveAlertFragment;
import com.projectgoth.ui.fragment.PostListFragment;
import com.projectgoth.ui.fragment.ProfileFragment;
import com.projectgoth.ui.fragment.ProfileListFragment;
import com.projectgoth.ui.fragment.RequestFollowFragment;
import com.projectgoth.ui.fragment.SendGiftFragment;
import com.projectgoth.ui.fragment.SettingsFragment;
import com.projectgoth.ui.fragment.SettingsFragment.SettingsGroupType;
import com.projectgoth.ui.fragment.ShareInChatFragment;
import com.projectgoth.ui.fragment.ShareToFragment;
import com.projectgoth.ui.fragment.ShareToFragment.ShareItemListener;
import com.projectgoth.ui.fragment.ShareboxFragment;
import com.projectgoth.ui.fragment.ShareboxFragment.ShareboxActionType;
import com.projectgoth.ui.fragment.ShareboxFragment.ShareboxSubActionType;
import com.projectgoth.ui.fragment.SinglePostFragment;
import com.projectgoth.ui.fragment.SinglePostFragment.HeaderTab;
import com.projectgoth.ui.fragment.SinglePostGiftFragment;
import com.projectgoth.ui.fragment.StartChatFragment;
import com.projectgoth.ui.fragment.StartChatFragment.StartChatActionType;
import com.projectgoth.ui.fragment.StickerPackDetailsFragment;
import com.projectgoth.ui.fragment.StoreFilterFragment;
import com.projectgoth.ui.fragment.StoreFilterFragment.StoreFilterType;
import com.projectgoth.ui.fragment.StoreFilterFragment.StoreSortingListener;
import com.projectgoth.ui.fragment.StorePagerFragment;
import com.projectgoth.ui.fragment.StoreSearchPreviewFragment;
import com.projectgoth.ui.fragment.UnlockedGiftFragment;
import com.projectgoth.ui.fragment.UnlockedGiftListFragment;
import com.projectgoth.ui.listener.ContextMenuItemListener;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.PostUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cherryv
 * 
 */
public class ActionHandler {

    private static final String  TAG = AndroidLogger.makeLogTag(ActionHandler.class);
    
    private static class ActionHandlerHolder {
        static final ActionHandler sINSTANCE = new ActionHandler();
    }

    public static ActionHandler getInstance() {
        return ActionHandlerHolder.sINSTANCE;
    }

    private ActionHandler() {
    }

    public void startLogin(String username, String password, boolean isVisible) {
        Session.getInstance().setUsername(username);
        Session.getInstance().setPassword(password);

        Session.getInstance().setPasswordType(PasswordTypeEnum.FUSION.value());
        Session.getInstance().setPresence(isVisible ? PresenceType.AVAILABLE : PresenceType.OFFLINE);
        ApplicationEx.getInstance().getNetworkService().startServerConnectionService();
    }

    public void showMainActivityAfterLogin(Activity activity) {
        showMainActivityAfterLogin(activity, true);
    }

    public void showMainActivityAfterLogin(Activity activity, boolean isFinishPrevious) {
        if (Session.getInstance().isLoggedIn()) {
            Session.getInstance().setServiceActive(true);
        }
        FragmentHandler.getInstance().showMainActivity(activity);
        if (isFinishPrevious) {
            activity.finish();
        }
    }

    /**
     * Can be use for any SSOlogin.
     */
    public void startSSOLogin(int passwordType, String sessionId, boolean isVisible) {
        Session.getInstance().setPasswordType(passwordType);
        Session.getInstance().setSessionId(sessionId);
        Session.getInstance().setPresence(isVisible ? PresenceType.AVAILABLE : PresenceType.OFFLINE);
        ApplicationEx.getInstance().getNetworkService().startServerConnectionService();
    }

    /**
     * This will process facebook login with access token & user ID.
     * 
     * @param userId
     * @param accessToken
     * @param passwordType
     * @param presence
     */
    public void sendFacebookSSOLogin(String userId, String accessToken, int passwordType) {
        FacebookLoginController fbController = FacebookLoginController.getInstance();
        fbController.setControllerContext(ApplicationEx.getContext());
        fbController.sendSSOLoginRequest(userId, accessToken, passwordType);
    }

    /**
     * This is initiate facebook login process regardless of where it is being
     * called from. But for session to be capture one has to implement
     * onActivityResult ie
     * com.facebook.Session.getActiveSession().onActivityResult(this,
     * requestCode, resultCode, data);
     * 
     * @param context
     */
    public void startFacebookLogin(Context context) {
        FacebookLoginController fbController = FacebookLoginController.getInstance();
        fbController.setControllerContext(context);
        fbController.startLogin();
    }

    public void showLogin(String username) {
        if (username != null) {
            Session.getInstance().setUsername(username);
        }

        BroadcastHandler.Application.sendShowLogin();
        if (Session.getInstance().isServiceActive()) {
            FragmentHandler.getInstance().showLoginActivity(ApplicationEx.getContext());
        }
    }

    /**
     * Action to follow or unfollow a user with the given username. NOTE: The
     * {@link UserDatastore} will internally decide whether to follow or
     * unfollow depending on the relationship of the profile matching username.
     * 
     * @param username
     *            The username of the user to be followed or unfollowed.
     * @param activitySource
     *            The source from where the follow request originates. Can be
     *            null.
     */
    public void followOrUnfollowUser(final String username, final ActivitySourceEnum activitySource) {
        FriendsController.getInstance().requestToFollowOrUnfollowUser(username, activitySource);
    }

    /**
     * Action to watch a post with given id.
     * 
     * @param postId
     *            The id of the post to be watched.
     */
    public void watchOrUnwatchPost(final String postId, final boolean shouldWatch) {
        PostsController.getInstance().requestWatchOrUnwatchPost(postId, shouldWatch);
    }

    public void lockOrUnlockPost(final String postId, final boolean shouldLock) {
        PostsController.getInstance().requestLockOrUnlockPost(postId, shouldLock);
    }

    /**
     * Action to delete a post with given id.
     * 
     * @param postId
     *            The id of the post to be deleted.
     */
    public void deletePost(final String postId) {
        PostsDatastore.getInstance().requestDeletePost(postId);
    }

    public void startGroupChat(final List<Friend> friends, final MessageType type , StartChatFragment.StartChatListener listener) {
        final int size = friends != null ? friends.size() : 0;
        if (size >= 2) {

            List<String> users = new ArrayList<String>();
            for (Friend friend : friends) {
                users.add(friend.getUsername());
            }

            ChatController.getInstance().startGroupChat(users, type , listener);

        } else {
            Logger.debug.flog(TAG, "Cannot start Group chat with Friends(%s) size of %d ", friends, size);
            // TODO: throw an error or send it to Error Handler
        }
    }

    public void inviteToGroupChat(final String groupChatId, final List<Friend> friends, final MessageType type) {
        final int size = friends != null ? friends.size() : 0;
        if (size > 0) {

            List<String> users = new ArrayList<String>();
            for (Friend friend : friends) {
                users.add(friend.getUsername());
            }

            ChatController.getInstance().inviteToGroupChat(groupChatId, users, type);
        }
    }

    /**
     * @param displayName
     * @param username
     */
    public void blockFriend(Context context, AlertListener listener, String displayName, String userName) {
        AlertHandler.getInstance().showBlockFriendDialog(context, listener, displayName, userName);
    }

    /**
     * @param displayName
     * @param username
     */
    public void removeFriend(Context context, String displayName, int contactId) {
        AlertHandler.getInstance().showRemoveFriendDialog(context, null, displayName, contactId);
    }

    /**
     * @param displayName
     * @param username
     */
    public void unfollowFriend(Context context, String username) {
        AlertHandler.getInstance().showUnfollowFriendDialog(context, null, username);
    }

    /**
     * @param name this is the localized name of the language (i.e. what is shown in the UI)
     * @param id
     */

    public void changeLanguage(Context context, String name, String id) {
        AlertHandler.getInstance().showChangeLanguageDialog(context, null, name, id);
    }

    /**
     * @param groupId
     * @param activity
     * @param username
     */
    public void moveFriend(Context context, int contactId, int groupId) {
        AlertHandler.getInstance().showMoveFriendDialog(context, contactId, groupId);
    }

    /**
     * @param groupId
     */
    public void removeContactGroup(Context context, int groupId, String groupName) {
        AlertHandler.getInstance().showRemoveContactGroupDialog(context, groupId, groupName);
    }

    /**
     * @param chat
     *            id
     * @param username
     */
    public void kickUser(String conversationId, String username) {
        ChatController.getInstance().requestKickChatRoomParticipant(conversationId, username);
    }

    /**
     * Convenience method for leaving a chat. Some chats (i.e. group chats)
     * require that a prompt gets displayed first before leaving the chat.
     * 
     * @param context
     *            Context to be used to display a prompt if required
     * @param listener
     *            Custom listener for handling the yes-no buttons in the
     *            confirmation prompt. Can be null. If null, it will use the
     *            default handling
     * @param conversationId
     *            ConversationId of the chat that user is leaving
     */
    public void leaveChatConversation(Context context, AlertListener listener, String conversationId) {
        ChatConversation conversation = ChatDatastore.getInstance().getChatConversationWithId(conversationId);
        if (conversation != null) {
            String chatId = conversation.getChatId();
            Logger.debug.log(TAG, "Leaving conversation with chatId: ", chatId);
            if (conversation.isPrivateChat()) {
                ChatController.getInstance().requestLeavePrivateChat(chatId, ImType.FUSION);
            } else if (conversation.isGroupChat()) {
                AlertHandler.getInstance().showLeaveGroupChatDialog(context, listener, conversationId);
            } else if (conversation.isChatroom()) {
                ChatController.getInstance().requestLeaveChatRoom(chatId);
            }
        } else {
            Logger.warning.log(TAG, "Can't leave chat conversation. Id ", conversationId, " not found!");
        }
    }

    public void beginRetrieveAddressBookContacts() {
        AddressBookDatastore.getInstance().setContentResolver(ApplicationEx.getContext().getContentResolver());
        AddressBookDatastore.getInstance().startRetrieveContactsAndSyncToServer();
    }

    // ======= HELPER METHODS FOR DISPLAYING DIFFERENT SCREENS ======== //
    
    public void displayConnectionSettings(Context context) {
        if (context == null) {
            context = ApplicationEx.getInstance().getCurrentActivity();
        }
        
        final ConnectionDetail connectionDetails = Config.getInstance().getConnectionDetail();
        AlertHandler.getInstance().showConnectionSelectorMenu(context, connectionDetails,
                Config.getInstance().isConnectionSelectorEnabled());
    }

    public boolean goToMyChats() {
        return displayChatManager(ViewPagerFragmentIndex.CHATMANAGER, false);
    }

    public boolean goToContactList(BaseFragmentActivity activity) {
        return false;
    }

    public boolean goToChatroomList() {
        return displayChatManager(ViewPagerFragmentIndex.CHATMANAGER, true);
    }

    private boolean displayChatManager(final MainFragment.ViewPagerFragmentIndex index, 
            final boolean shouldDisplayChatroomsTab) {
        final MainFragment mainFragment = FragmentHandler.getInstance().getMainFragment();
        mainFragment.showFragmentAtIndex(index, false, shouldDisplayChatroomsTab);
        
        FragmentHandler.getInstance().clearAllCustomPopActivities();
        return true;
    }
    
    public void displayChatroomList(Context context, boolean shouldShowSearch) {
        ChatroomListFragment fragment = FragmentHandler.getInstance().getChatroomListFragment(shouldShowSearch);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(context, fragment);
    }

    public void displayShareInChat(Context context, String url, String mimeType, String mimeData) {
        ShareInChatFragment fragment = FragmentHandler.getInstance().getShareInChatFragment(url, mimeType, mimeData);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(context, fragment);
    }

    public void displayPositiveAlert(final FragmentActivity activity, final ArrayList<String> alertIdList) {
        if (alertIdList.size() > 0) {
            PositiveAlertFragment fragment = displaySinglePositiveAlert(activity, alertIdList.remove(0));
            fragment.setOnDialogDismissListener(new BaseDialogFragment.DialogDismissListener() {

                @Override
                public void onDismiss() {
                    if (alertIdList.size() > 0) {
                        displaySinglePositiveAlert(activity, alertIdList.remove(0));
                    }
                }
            });
        }
    }

    private PositiveAlertFragment displaySinglePositiveAlert(final FragmentActivity activity, final String alertId) {
        PositiveAlertFragment fragment = FragmentHandler.getInstance().getPositiveAlertFragment(alertId);
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment, false);
        return fragment;
    }

    public void displayInterstitialBanner(FragmentActivity activity, String message, String actionUrl) {
        InterstitialBannerFragment fragment = FragmentHandler.getInstance().getInterstitialBannerFragment(
                I18n.tr("Check this out!"), message, actionUrl);
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment, false);
    }

    public void displayRecommendedUsers(FragmentActivity activity) {
        ProfileListFragment fragment = FragmentHandler.getInstance().getRecommendedUsersFragment();
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }

    public void displayRecommendedContacts(FragmentActivity activity) {
        ProfileListFragment fragment = FragmentHandler.getInstance().getRecommendedContactsFragment();
        if (fragment != null) {
            FragmentHandler.getInstance().showFragment(activity, fragment);
        }
    }

    public void displayAlerts(FragmentActivity activity) {
        NotificationListFragment fragment = new NotificationListFragment();
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment, true, false);
    }

    public void displaySharebox(FragmentActivity activity, ShareboxActionType action, String postId, String prefix,
            String groupId, boolean allowPostWhenReply) {
        ActionHandler.getInstance()
                .displaySharebox(activity, action, postId, prefix, null, groupId, allowPostWhenReply, ShareboxSubActionType.NONE);
    }

    public Fragment displaySharebox(FragmentActivity activity, ShareboxActionType action, String postId, String prefix,
                                String groupId, boolean allowPostWhenReply, ShareboxSubActionType subAction) {
        return ActionHandler.getInstance()
                .displaySharebox(activity, action, postId, prefix, null, groupId, allowPostWhenReply, subAction);
    }

    public Fragment displaySharebox(FragmentActivity activity, ShareboxActionType action, String postId, String prefix,
            Uri presetPhotoUri, String groupId, boolean allowPostWhenReply, ShareboxSubActionType subAction) {
        ShareboxFragment fragment = FragmentHandler.getInstance().getShareboxFragment(action, postId, prefix,
                presetPhotoUri, groupId, allowPostWhenReply, subAction);
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment);
        return fragment;
    }

    public void displayShareboxWithMimeData(FragmentActivity activity) {
        ShareboxFragment fragment = FragmentHandler.getInstance().getShareboxFragment(ShareboxActionType.SHARE_TO_POST, null,
                null, null, null, false, ShareboxSubActionType.NONE);
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment);
    }

    public void displayCaptcha(Context context, final Captcha captcha) {
        CaptchaFragment fragment = FragmentHandler.getInstance().getCaptchaFragment(captcha);
        if (fragment != null) {
            FragmentHandler.getInstance().showFragmentAsPopup(context, fragment, false);
        }
    }

    public void displayInviteFriends(FragmentActivity activity) {
        InviteFriendsFragment fragment = FragmentHandler.getInstance().getInviteFriendsFragment();
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }

    public void displayGlobalSearch(FragmentActivity activity, GlobalSearchFragment.SearchType type, String searchParam) {
        GlobalSearchFragment fragment = FragmentHandler.getInstance().createGlobalSearchFragment(type, searchParam);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }
    
    public void displayGlobalSearchPreview(FragmentActivity activity, String searchParam) {
        GlobalSearchPreviewFragment fragment = FragmentHandler.getInstance().createGlobalSearchPreviewFragment(searchParam);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }

    public void displayStoreSearchPreview(FragmentActivity activity, String searchParam, String initialRecipient,
                                          StorePagerItem.StorePagerType type) {
        StoreSearchPreviewFragment fragment = FragmentHandler.getInstance().createStoreSearchPreviewFragment(
                searchParam, initialRecipient, type);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }

    public void displayHotTopicPosts(FragmentActivity activity, String topic) {
        PostListFragment fragment = FragmentHandler.getInstance().getHotTopicResultsFragment(topic);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }

    public void displayMyStickers(FragmentActivity activity) {
        MyStickersFragment fragment = FragmentHandler.getInstance().getMyStickersFragment();
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }
    
    public void displayMyGifts(FragmentActivity activity, String userId) {
        MyGiftsPagerFragment fragment = FragmentHandler.getInstance().getMyGiftsPagerFragment(userId);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }
    
    public void displayMyGiftsOverviewFilterFragment(FragmentActivity activity, MyGiftsOverviewFilterType filterType,
            int selectedFilterType, MyGiftsOverviewSortingListener listener, String userId) {
        MyGiftsOverviewFilterFragment fragment = FragmentHandler.getInstance().getMyGiftsOverviewFilterFragment(
                filterType, selectedFilterType, listener, userId);
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment);
    }

    public void displayMyGiftsCardListFragment(FragmentActivity activity, String title, int category,
                                               boolean filter, String userId) {
        MyGiftsCardListFragment fragment = FragmentHandler.getInstance().getMyGiftsCardListFragment(title, category, filter, userId);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }

    public void displayMyGiftsCategoryFragment(FragmentActivity activity, int categoryIdx,
                                               MyGiftsCategoryFragment.CategoryListener listener, String userId) {
        MyGiftsCategoryFragment fragment = FragmentHandler.getInstance().getMyGiftsCategoryFragment(categoryIdx, listener, userId);
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment);
    }

    public void displayMusicFragment(FragmentActivity activity) {
        MusicFragment fragment = FragmentHandler.getInstance().getMusicFragment();
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }

    public void displayMusicGenreFilterFragment(FragmentActivity activity, int selectedIndex, MusicGenreFilterFragment.MusicGenreFilterListener listener) {
        MusicGenreFilterFragment fragment = FragmentHandler.getInstance().getMusicGenreFilterFragment(selectedIndex, listener);
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment);
    }

    public void displayLoginDialogFragment(FragmentActivity activity) {
        LoginDialogFragment fragment = FragmentHandler.getInstance().getLoginDialogFragment();
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment);
    }

    public void displayBrowser(FragmentActivity activity, String url, String title, int titleIcon) {
        BrowserFragment fragment = FragmentHandler.getInstance().getBrowserFragment(url, false, null, null, title, titleIcon);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }

    public void displayBrowser(FragmentActivity activity, String url) {
        BrowserFragment fragment = FragmentHandler.getInstance().getBrowserFragment(url, false, null, null, null, 0);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }
    
    public void displayBrowserAsPopup(Context context, String url) {
        displayBrowserAsPopup(context, url, false);
    }
    
    public void displayBrowserAsDialog(FragmentActivity activity, String url, String title, Drawable titleIcon) {
        BrowserFragment fragment = FragmentHandler.getInstance().getBrowserFragment(url, false, title, titleIcon, null, 0);
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment);
    }

    public void displayBrowserAsPopup(Context context, String url, boolean isFullyConstructedUrl) {
        BrowserFragment fragment = FragmentHandler.getInstance().getBrowserFragment(url, isFullyConstructedUrl,
                null, null, null, 0);
        FragmentHandler.getInstance().showFragmentAsPopup(context, fragment);
    }
    
    public void displayRechargeCreditsFromChat(FragmentActivity activity, String url, String title, int titleIcon) {
        BrowserFragment fragment = FragmentHandler.getInstance().getBrowserFragment(url, false, null, null, title, titleIcon);
        FragmentHandler.getInstance().showFragmentWithIdAsCustomPopupForResult(activity, fragment,
                Constants.REQ_SHOW_GIFT_CENTER_FROM_CHAT);
    }

    public void displayMentions(FragmentActivity activity) {
        // Mentions highlighted by default
        PostListFragment fragment = FragmentHandler.getInstance().getMentionsListFragment();
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }
    
    public void displayFavourites(FragmentActivity activity) {
        PostListFragment fragment = FragmentHandler.getInstance().getWatchedPostsFragment();
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }

    public void displayFeeds(FragmentActivity activity) {
        // highlighted by default
        PostListFragment fragment = FragmentHandler.getInstance().getFeedsListFragment();
        FragmentHandler.getInstance().showFragment(activity, fragment);
    }

    public void displaySinglePostPage(FragmentActivity activity, String postId, boolean isPostInGroup, boolean isReplyOrReshare) {
        displaySinglePostPage(activity, postId, isPostInGroup, HeaderTab.REPLY_TAB, isReplyOrReshare);
    }

    public void displaySinglePostPage(FragmentActivity activity, String postId, boolean isPostInGroup, HeaderTab selectedTab, boolean isReplyOrReshare) {
        SinglePostFragment fragment = FragmentHandler.getInstance().getSinglePostFragment(postId, isPostInGroup,
                selectedTab, isReplyOrReshare);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }

    public void displayChatConversation(FragmentActivity activity, String conversationId) {
        if (activity instanceof CustomPopupActivity) {
            activity.finish();
        }
        ChatFragment fragment = FragmentHandler.getInstance().getChatFragmentForId(conversationId);
        FragmentHandler.getInstance().showFragmentAsSingleTop(activity, fragment);
    }
    
    public void displayChatConversation(FragmentActivity activity, String chatId,
            ChatTypeEnum chatType, MessageType imMessageType, boolean isFromSystemNotification) {
        if (activity instanceof CustomPopupActivity) {
            activity.finish();
        }
        ChatFragment fragment = FragmentHandler.getInstance().getChatFragmentForChatId(chatId, chatType, imMessageType, isFromSystemNotification);
        FragmentHandler.getInstance().showFragmentAsSingleTop(activity, fragment);
    }

    public String displayPrivateChat(FragmentActivity activity, String username) {
        ChatConversation conversation = ChatDatastore.getInstance().findOrCreateConversation(ChatTypeEnum.MIG_PRIVATE,
                username, 0, MessageType.FUSION);
        if (conversation != null) {
            displayChatConversation(activity, conversation.getId());
            return conversation.getId();
        } else {
            // TODO: This should not happen, in case it does need to throw an
            // error
            Logger.error.log(TAG, "Failed to find/create the Private conversation: ", username);
            return null;
        }
    }

    public String displayPrivateIMChat(FragmentActivity activity, String username, String displayName, int groupId, MessageType messageType) {
        ChatConversation conversation = ChatDatastore.getInstance().findOrCreateIMConversation(ChatTypeEnum.IM,
                username, displayName, groupId, messageType);
        if (conversation != null) {
            displayChatConversation(activity, conversation.getId());
            return conversation.getId();
        } else {
            // TODO: This should not happen, in case it does need to throw an
            // error
            Logger.error.log(TAG, "Failed to find/create the Private conversation: ", username);
            return null;
        }
    }

    public void displayPublicChat(FragmentActivity activity, String chatName, int groupId) {
        //when clicking to join a chatroom again manually, remove it from chatRoomLeft list: AD-1105
        if (ChatDatastore.getInstance().didUserLeaveChat(chatName)) {
            ChatDatastore.getInstance().removeFromChatroomLeft(chatName);
        }

        ChatConversation conversation = ChatDatastore.getInstance().findOrCreateConversation(ChatTypeEnum.CHATROOM,
                chatName, groupId, MessageType.FUSION);
        if (conversation != null) {
            displayChatConversation(activity, conversation.getId());
        } else {
            // TODO: This should not happen, in case it does need to throw an
            // error
            Logger.error.flog(TAG,
                    "Failed to find/create the Public conversation: %s - groupId: %d", chatName, groupId);
        }
    }

    public void displayStartChat(FragmentActivity activity, StartChatActionType action, String conversationId,
            ArrayList<String> preselectedUsers) {
        StartChatFragment fragment = FragmentHandler.getInstance().getStartChatFragment(action, conversationId,
                preselectedUsers);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }

    public void displayCreateChatroom(FragmentActivity activity) {
        CreateChatroomFragment fragment = FragmentHandler.getInstance().getCreateChatroomFragment();
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }

    public void displayHotTopics(FragmentActivity activity) {
        HotTopicsFragment hotTopics = FragmentHandler.getInstance().getHotTopicsFragment();
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, hotTopics);
    }

    /**
     * Displays the mini profile page for a given user.
     * 
     * @param activity
     *            The {@link FragmentActivity} within which the profile page is
     *            to be displayed. Can be null.
     * @param username
     *            The username of the user whose profile page is to be
     *            displayed.
     */
    public void displayProfile(FragmentActivity activity, String username) {
        if (Session.getInstance().getUsername() == null || !Session.getInstance().getUsername().equals(username)) {
            displayMiniPopupProfile(activity, username);
        } else {
            displayMainProfile(activity, username);
        }
    }

    /**
     * Displays the profile page for a given user. The profile page
     * automatically scrolls to the given {@link ViewPagerType}
     * 
     * @param activity
     *            The {@link FragmentActivity} within which the profile page is
     *            to be displayed. Can be null.
     * @param username
     *            The username of the user whose profile page is to be
     *            displayed.
     */
    public void displayMainProfile(FragmentActivity activity, String username) {
        ProfileFragment fragment = FragmentHandler.getInstance().getProfileFragment(username);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }

    private void displayMiniPopupProfile(FragmentActivity activity, String username){
        MiniProfilePopupFragment fragment = FragmentHandler.getInstance().getMiniProfilePopupFragment(username);
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment);
    }

    /**
     * Displays the {@link LocationListFragment}
     * 
     * @param activity
     *            The {@link FragmentActivity} within which the LocationList
     *            page is to be displayed. Can be null.
     * @param listener
     *            An {@link LocationListFragment.EventListener} that can listen
     *            in on events related to selection and deselection of items in
     *            the list.
     * @param selectedItem
     *            A {@link LocactionListItem} that is to be marked as selected
     *            in the location list.
     */
    public void displayLocationList(FragmentActivity activity, LocationListFragment.EventListener listener,
            LocationListItem selectedItem) {
        BaseFragment fragment = FragmentHandler.getInstance().getLocationListFragment(listener, selectedItem);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }

    /**
     * Displays the badges section within the profile screen.
     * 
     * @param activity
     *            The {@link FragmentActivity} within which the profile page is
     *            to be displayed. Can be null.
     * @param username
     *            The username of the user whose profile page is to be
     *            displayed.
     */
    public void displayBadges(FragmentActivity activity, String username) {
        ProfileFragment fragment = FragmentHandler.getInstance().getProfileFragment(username);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }

    public void displayFullProfile(FragmentActivity activity, String username) {
        FullProfileFragment fragment = FragmentHandler.getInstance().getFullProfileFragment(username);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }
    
    public void displayFollowersList(FragmentActivity activity, String username) {
        ProfileListFragment fragment = FragmentHandler.getInstance().getFollowersListFragment(username, true);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }
    
    public void displayFollowingList(FragmentActivity activity, String username) {
        ProfileListFragment fragment = FragmentHandler.getInstance().getFollowingListFragment(username, true);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }
    
    public void displayBadgesList(FragmentActivity activity, String username) {
        BadgesFragment fragment = FragmentHandler.getInstance().getBadgesFragment(username);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }

    public void displayRequestFollow(FragmentActivity activity, String username) {
        RequestFollowFragment fragment = FragmentHandler.getInstance().getRequestFollowFragment(username);
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment, false);
    }

    public void displayStore(FragmentActivity activity, String initialRecipient) {
        StorePagerFragment fragment = FragmentHandler.getInstance().getStoreFragment(0, initialRecipient);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }

    public void displayStoreFromChat(FragmentActivity activity, String initialRecipient) {
        StorePagerFragment fragment = FragmentHandler.getInstance().getStoreFragment(0, initialRecipient);
        FragmentHandler.getInstance().showFragmentWithIdAsCustomPopupForResult(activity, fragment,
                Constants.REQ_SHOW_GIFT_CENTER_FROM_CHAT);
    }

    public void displayStore(FragmentActivity activity, int selectedTab, String initialRecipient) {
        StorePagerFragment fragment = FragmentHandler.getInstance().getStoreFragment(selectedTab, initialRecipient);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }
    
    public void displayStickerPackDetails(FragmentActivity activity, int packId, int referenceId) {
        StickerPackDetailsFragment fragment = FragmentHandler.getInstance().getStickerPackDetailsFragment(packId, referenceId);
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment);
    }

    public void displayGiftCategory(FragmentActivity activity, StoreItemFilterType giftFilterType, String categoryId,
            String categoryName, String searchString, float minPrice, float maxPrice, boolean isInChat,
            String conversationId, String initialRecipient) {
        GiftCategoryFragment fragment = FragmentHandler.getInstance().getGiftCategoryFragment(giftFilterType,
                categoryId, categoryName, searchString, minPrice, maxPrice, isInChat, conversationId,
                initialRecipient);
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment);
    }


    public void displayGiftCategoryParent(FragmentActivity activity, StoreItemFilterType giftFilterType, String categoryId,
            String categoryName, String conversationId, String initialRecipient, ArrayList<String> selectedUsers) {
        GiftCategoryParentFragment fragment = FragmentHandler.getInstance().getGiftCategoryParentFragment(
                giftFilterType, conversationId, categoryId, categoryName, initialRecipient, selectedUsers);
        FragmentHandler.getInstance().showFragmentAsDialog(activity.getSupportFragmentManager(), fragment, true, false);
    }

    public void displayGiftItem(FragmentActivity activity, String giftId, String initialRecipient) {
        GiftFragment fragment = FragmentHandler.getInstance().getGiftFragment(giftId, initialRecipient, null);
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment);
    }

    public void displayGiftItem(FragmentActivity activity, String giftId, String initialRecipient, String counterId) {
        GiftFragment fragment = FragmentHandler.getInstance().getGiftFragment(giftId, initialRecipient, counterId);
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment);
    }

    public void displayGiftPurchased(FragmentActivity activity, String giftId, String recipients) {
        GiftPurchasedFragment fragment = FragmentHandler.getInstance().getGiftPurchasedFragment(giftId, recipients);
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment);
    }

    public void displayGiftCenterFragment(FragmentActivity activity, String conversationId, String initialRecipient) {
        GiftCenterFragment fragment = FragmentHandler.getInstance().getGiftCenterFragment(conversationId,
                initialRecipient);
        FragmentHandler.getInstance().showFragmentAsDialog(activity.getSupportFragmentManager(), fragment, true, false);
    }

    public void displayGiftCenterFragment(FragmentActivity activity, ArrayList<String> selectedUsers, String initialRecipient) {
        GiftCenterFragment fragment = FragmentHandler.getInstance().getGiftCenterFragment(selectedUsers,
                initialRecipient);
        FragmentHandler.getInstance().showFragmentAsDialog(activity.getSupportFragmentManager(), fragment, true, false);
    }

    public void displaySendGiftFragment(FragmentActivity activity, ArrayList<String> recipientList,
                                        SendGiftFragment.ActionType actionType, String conversationId) {
        SendGiftFragment fragment = FragmentHandler.getInstance().getSendGiftFragment(recipientList, actionType, conversationId);
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment);
    }

    public void displaySendGiftFragment(FragmentActivity activity, ArrayList<String> recipientList,
                                        SendGiftFragment.ActionType actionType, String rootPostId, String postId) {
        SendGiftFragment fragment = FragmentHandler.getInstance().getSendGiftFragment(recipientList, actionType, rootPostId, postId);
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment);
    }

    public void displaySinglePostGiftFragment(FragmentActivity activity, String rootPostId, String parentPostId) {
        SinglePostGiftFragment fragment = FragmentHandler.getInstance().getSinglePostGiftFragment(rootPostId, parentPostId);
        FragmentHandler.getInstance().showFragmentAsDialog(activity.getSupportFragmentManager(), fragment, true, false);
    }

    public void displayGiftPreviewFragment(FragmentActivity activity, String giftId, String conversationId,
            boolean isFromRecent) {
        displayGiftPreviewFragment(activity, giftId, conversationId, isFromRecent, Constants.BLANKSTR, null);
    }

    public void displayGiftPreviewFragment(FragmentActivity activity, String giftId, String conversationId,
            boolean isFromRecent, String selectedRecipient, ArrayList<String> selectedUsers) {
        GiftPreviewFragment fragment = FragmentHandler.getInstance().getGiftPreviewFragment(giftId, conversationId,
                isFromRecent, selectedRecipient, selectedUsers);
        FragmentHandler.getInstance().showFragmentAsDialog(activity.getSupportFragmentManager(), fragment, true, false);
    }

    public void displaySinglePostGiftPreviewFragment(FragmentActivity activity, String giftId, String rootPostId, String parentPostId) {
        GiftPreviewFragment fragment = FragmentHandler.getInstance().getSinglePostGiftPreviewFragment(giftId, rootPostId, parentPostId);
        FragmentHandler.getInstance().showFragmentAsDialog(activity.getSupportFragmentManager(), fragment, true, false);
    }
    
    public void displayGiftRecipientSelectionFragment(FragmentActivity activity, String giftId, String conversationId, 
            boolean isFromRecent, ArrayList<String> selectedUsers) {
        GiftRecipientSelectionFragment fragment = FragmentHandler.getInstance().getGiftRecipientSelectionFragment(giftId,
                conversationId, isFromRecent, selectedUsers);
        FragmentHandler.getInstance().showFragmentAsDialog(activity.getSupportFragmentManager(), fragment, true, false);
    }

    /**
     * @param activity
     * @param username
     * @param badgeId
     */
    public void displayBadgeInfoFragment(FragmentActivity activity, String username, Integer badgeId) {
        BadgeInfoFragment fragment = FragmentHandler.getInstance().getBadgeInfoFragment(username, badgeId);
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment);
    }

    public void displayGroupPage(FragmentActivity activity, String groupId) {
        GroupPageFragment fragment = FragmentHandler.getInstance().getGroupFragmnet(groupId);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }
    
    public void displayGroupPageFromChat(FragmentActivity activity, String groupId) {
        GroupPageFragment fragment = FragmentHandler.getInstance().getGroupFragmnet(groupId);
        FragmentHandler.getInstance().showFragmentWithIdAsCustomPopupForResult(activity, fragment,
                Constants.REQ_SHOW_GIFT_CENTER_FROM_CHAT);
    }

    public void displayHomeScreen(FragmentActivity activity) {
        FragmentHandler.getInstance().popToRootFragment(activity);
    }

    public void displaySettings(FragmentActivity activity, SettingsGroupType type) {
        SettingsFragment fragment = FragmentHandler.getInstance().getSettingsFragment(type);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }
    
    public void displayGameCentre(FragmentActivity activity){
        GameCentreFragment fragment = FragmentHandler.getInstance().getGameCentreFragment();
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }
    
    public void displayMusicPage(FragmentActivity activity){
        MusicFragment fragment = FragmentHandler.getInstance().getMusicFragment();
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }

    public void displayMusicDetailPage(FragmentActivity activity){
        DeezerDetailListFragment fragment = FragmentHandler.getInstance().getDeezerDetailListFragment();
        FragmentHandler.getInstance().showFragmentAsCustomTranslucentPopup(activity, fragment);
    }
    

    public void displayShareToFragment(FragmentActivity activity, ShareItemListener listener){
        ShareToFragment fragment = FragmentHandler.getInstance().getShareToFragment(listener);
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment);
    }

    public void displayShareToFragmentFromDeezer(FragmentActivity activity, ShareItemListener listener){
        ShareToFragment fragment = FragmentHandler.getInstance().getShareToFragmentForDeezer(listener);
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment);
    }

    public void displayGiftSent(FragmentActivity activity) {
        GiftSentFragment fragment = FragmentHandler.getInstance().getGiftSentFragment();
        FragmentHandler.getInstance().showFragmentAsDialog(activity.getSupportFragmentManager(),
                fragment, false, true);
    }
    
    public void displayAccountBalance(FragmentActivity activity) {
        AccountBalanceFragment fragment = FragmentHandler.getInstance().getAccountBalanceFragment();
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment);
    }

    public void displayStoreFilterFragment(FragmentActivity activity, StoreFilterType filterType, 
            int storeType, StoreSortingListener listener, int categoryId, String giftFilterType) {
        StoreFilterFragment fragment = FragmentHandler.getInstance().getStoreFilterFragment(filterType, storeType, listener, categoryId, giftFilterType);
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment);
    }
    
    public void displayUnlockedGiftListFragment(FragmentActivity activity) {
        UnlockedGiftListFragment fragment = FragmentHandler.getInstance().getUnlockedGiftListFragment();
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }
    
    public void displayUnlockedGiftFragment(FragmentActivity activity, String giftId) {
        UnlockedGiftFragment fragment = FragmentHandler.getInstance().getUnlockedGiftFragment(giftId);
        FragmentHandler.getInstance().showFragmentAsDialog(activity, fragment);
    }

    /**
     * Displays the {@link PhotoViewerFragment} with the photo as the one given
     * in a specified post.
     * 
     * @param activity
     *            The {@link FragmentActivity} used as a context for the
     *            fragment to be displayed.
     * @param post
     *            The {@link Post} whose photo is to be displayed.
     */
    public void displayPhotoViewerFragmentForPost(FragmentActivity activity, Post post) {
        if (post != null) {
            final Photo photo = post.getPhoto();
            if (photo != null) {
                final String postAuthor = PostUtils.getPostAuthorUsername(post);
                if(photo.getBitMapByte() != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(photo.getBitMapByte(), 0, photo.getBitMapByte().length);
                    displayPhotoViewerFragment(activity, null, postAuthor, bitmap, true, false);
                } else {
                    // Use the photo url from the retrieved post.
                    final String url = UIUtils.getPhotoUrl(photo, null, Config.getInstance().getScreenWidth());
                    displayPhotoViewerFragment(activity, url, postAuthor, null, true, false);
                }
            }
        }
    }
    
    /**
     * Displays the {@link PhotoViewFragment} with the photo as the given url.
     * 
     * @param activity
     *            The {@link FragmentActivity} used as a context for the
     *            fragment to be displayed.
     * @param url
     *            The image url to be used as photo display.
     * @param post
     *            The post whose body contains the given url. Can be null.
     * @param shouldAllowSaveToDevice
     *            Whether the "save to device" icon should be shown in the
     *            {@link PhotoViewerFragment} or not.
     * @param isUrlForWebPage
     *            Whether the image should be displayed in a web view or not.
     *            Currently, we only show gif images in a web view.
     */
    public void displayPhotoViewerFragmentForUrlInPostBody(FragmentActivity activity, String url, Post post, 
            boolean shouldAllowSaveToDevice, boolean isUrlForWebPage) {
        if (post == null) {
            displayPhotoViewerFragment(activity, url, null, null, 
                    shouldAllowSaveToDevice, isUrlForWebPage);
        } else {
            displayPhotoViewerFragment(activity, url, PostUtils.getPostAuthorUsername(post), null, 
                    shouldAllowSaveToDevice, isUrlForWebPage);
        }
    }
    
    
    /**
     * Displays the {@link PhotoViewerFragment} with the photo as the Bitmap
     * provided. This is called when displaying the full view of an attached
     * photo in the sharebox.
     * 
     * @param activity
     *            The {@link FragmentActivity} used as a context for the
     *            fragment to be displayed.
     * @param photoBitmap
     *            The {@link Bitmap} to be set as the photo in the
     *            {@link PhotoViewerFragment}.
     */
    public void displayPhotoViewerFragmentForSharebox(FragmentActivity activity, Bitmap photoBitmap) {
        displayPhotoViewerFragment(activity, null, null, photoBitmap, false, false);
    }

    /**
     * Displays the {@link PhotoViewerFragment} with the photo as a given image
     * url.
     * 
     * @param activity
     *            The {@link FragmentActivity} used as a context for the
     *            fragment to be displayed.
     * @param imageUrl
     *            The url to an image that must be displayed as the photo in the
     *            fragment.
     * @param photoSender
     *            The username of the sender of the photo.
     * @param photoBitmap
     *            A Bitmap that can be displayed as the photo in the fragment.
     *            If this value is non-null, then the Bitmap is used instead of
     *            the imageUrl.
     * @param shouldAllowSaveToDevice
     *            Whether the "save to device" icon should be shown in the
     *            {@link PhotoViewerFragment} or not.
     * @param isUrlForWebPage
     *            Whether the image should be displayed in a web view or not.
     *            Currently, we only show gif images in a web view.
     */
    public void displayPhotoViewerFragment(FragmentActivity activity, 
            String imageUrl, String photoSender, Bitmap photoBitmap, boolean shouldAllowSaveToDevice, boolean isUrlForWebPage) {
        if (!TextUtils.isEmpty(imageUrl) || photoBitmap != null) {
            PhotoViewerFragment fragment = FragmentHandler.getInstance().getPhotoViewerFragment(imageUrl, photoSender, 
                    photoBitmap, shouldAllowSaveToDevice, isUrlForWebPage);
            FragmentHandler.getInstance().showFragmentAsPopup(activity, fragment);
        }
    }

    public void displayGameDetailPageFragment(FragmentActivity activity,FragmentHandler.GameDetailPageFragmentType type, String gameId){
        BaseFragment fragment = FragmentHandler.getInstance().getGameDetailPageFragment(type, gameId);
        FragmentHandler.getInstance().showFragmentAsCustomPopup(activity, fragment);
    }


    public void pickPhotoToUpload() {
        String title = I18n.tr("Upload photo");
        ArrayList<ContextMenuItem> menuItemList = generateUploadPhotoOptions();
        Tools.showContextMenu(title, menuItemList, new ContextMenuItemListener() {

            @Override
            public void onContextMenuItemClick(ContextMenuItem menuItem) {
                int optionId = menuItem.getId();
                switch (optionId) {
                    case R.id.option_item_camera:
                        takePhoto(ApplicationEx.getInstance().getCurrentActivity(), Constants.REQ_PIC_FROM_CAMERA_FOR_PHOTO_ALBUM,
                                false);
                        break;
                    case R.id.option_item_gallery:
                        pickFromGallery(ApplicationEx.getInstance().getCurrentActivity(),
                                Constants.REQ_PIC_FROM_GALLERY_FOR_PHOTO_ALBUM);
                        break;
                    default:
                        break;
                }
            }

        });
    }

    private ArrayList<ContextMenuItem> generateUploadPhotoOptions() {
        ArrayList<ContextMenuItem> menuItems = new ArrayList<ContextMenuItem>();
        menuItems.add(new ContextMenuItem(I18n.tr("Camera"), R.id.option_item_camera, null));
        menuItems.add(new ContextMenuItem(I18n.tr("Gallery"), R.id.option_item_gallery, null));

        return menuItems;
    }

    public void takePhoto(Activity activity, int requestCode, boolean thumbnailOnly) {
        Intent cameraIntent = getIntentForTakingPhoto(thumbnailOnly);
        activity.startActivityForResult(cameraIntent, requestCode);
    }

    public void takePhoto(Fragment fragment, int requestCode, boolean thumbnailOnly) {
        Intent cameraIntent = getIntentForTakingPhoto(thumbnailOnly);
        fragment.startActivityForResult(cameraIntent, requestCode);
    }

    private Intent getIntentForTakingPhoto(boolean thumbnailOnly) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (!thumbnailOnly) {
            File file = new File(Tools.getCapturedPhotoFile(ApplicationEx.getContext()));

            try {
                if (file.exists() && !file.delete()) {
                    throw new Exception("Unable to delete existing file " + file.getAbsolutePath());
                }

                File parent = file.getParentFile();
                if (null != parent && !parent.exists() && !parent.mkdirs()) {
                    throw new Exception("Unable to create directory " + parent.getAbsolutePath());
                }
            } catch (Exception e) {
                Logger.error.log(TAG, e);
            }

            try {
                file.createNewFile();
            } catch (Exception e) {
                Logger.error.log(TAG, e);
            }

            Uri uri = Uri.fromFile(file);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }

        return cameraIntent;
    }

    public void cropImage(Activity activity, Uri imageUri, CropImageType cropImageType, int requestCode) throws IOException {
        Intent cropIntent = getIntentForCroppingImage(imageUri, cropImageType);
        activity.startActivityForResult(cropIntent, requestCode);
    }

    public void cropImage(Fragment fragment, Uri imageUri, CropImageType cropImageType, int requestCode) throws IOException {
        Intent cropIntent = getIntentForCroppingImage(imageUri, cropImageType);
        fragment.startActivityForResult(cropIntent, requestCode);
    }

    private Intent getIntentForCroppingImage(Uri imageUri, CropImageType cropImageType) throws IOException {

        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(imageUri, "image/*");

        switch (cropImageType) {
            case PROFILE:
                cropIntent.putExtra("crop", "true");
                cropIntent.putExtra("aspectX", 1);
                cropIntent.putExtra("aspectY", 1);
                cropIntent.putExtra("outputX", Constants.DEFAULT_PHOTO_SIZE);
                cropIntent.putExtra("outputY", Constants.DEFAULT_PHOTO_SIZE);
                cropIntent.putExtra("scale", true);
                break;
            case FULL_IMAGE:
                cropIntent.putExtra("crop", "true");
                break;
        }

        // Create the temp file for cropping image
        File croppedFile = new File(Tools.getCroppedImageFile(ApplicationEx.getContext()));
        if (croppedFile.exists() && !croppedFile.delete()) {
            throw new IOException("Unable to delete the existing file " + croppedFile.getAbsolutePath());
        }

        File parent = croppedFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Unable to create directory " + parent.getAbsolutePath());
        }

        croppedFile.createNewFile();

        Uri CroppedFileUri = Uri.fromFile(croppedFile);

        cropIntent.putExtra("return-data", false);
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, CroppedFileUri);
        cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());

        return cropIntent;
    }

    public void pickFromGallery(Activity activity, int requestCode) {
        try {
            Intent galleryIntent = getIntentForPickingPhotoFromGallery();
            activity.startActivityForResult(galleryIntent, requestCode);
        } catch (ActivityNotFoundException e) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(WebURL.URL_GOOGLE_PLAY_PHOTOS));
            activity.startActivity(browserIntent);
            e.printStackTrace();
        }
    }

    public void pickFromGallery(Fragment fragment, int requestCode) {
        try {
            Intent galleryIntent = getIntentForPickingPhotoFromGallery();
            fragment.startActivityForResult(galleryIntent, requestCode);
        } catch (ActivityNotFoundException e) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(WebURL.URL_GOOGLE_PLAY_PHOTOS));
            fragment.startActivity(browserIntent);
            e.printStackTrace();
        }
    }

    private Intent getIntentForPickingPhotoFromGallery() throws ActivityNotFoundException {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        return galleryIntent;
    }

    public void pickImageFromGalleryAndCrop(Activity activity, CropImageType cropImageType, int requestCode) throws IOException {
        Intent galleryIntent = getIntentForPickingAndCropingFromGallery(cropImageType);
        activity.startActivityForResult(galleryIntent, requestCode);
    }

    public void pickImageFromGalleryAndCrop(Fragment fragment, CropImageType cropImageType, int requestCode) throws IOException {
        Intent galleryIntent = getIntentForPickingAndCropingFromGallery(cropImageType);
        fragment.startActivityForResult(galleryIntent, requestCode);
    }

    private Intent getIntentForPickingAndCropingFromGallery(CropImageType cropImageType) throws IOException {

        // Create the temp file for cropping image
        File file = new File(Tools.getCroppedImageFile(ApplicationEx.getContext()));
        if (file.exists() && !file.delete()) {
            throw new IOException("Unable to delete the existing file " + file.getAbsolutePath());
        }

        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Unable to create directory " + parent.getAbsolutePath());
        }

        Uri uri = Uri.fromFile(file);

        Intent galleryAndCropIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryAndCropIntent.setType("image/*");

        switch (cropImageType) {
            case PROFILE:
                galleryAndCropIntent.putExtra("crop", "true");
                galleryAndCropIntent.putExtra("aspectX", 1);
                galleryAndCropIntent.putExtra("aspectY", 1);
                galleryAndCropIntent.putExtra("outputX", Constants.DEFAULT_PHOTO_SIZE);
                galleryAndCropIntent.putExtra("outputY", Constants.DEFAULT_PHOTO_SIZE);
                galleryAndCropIntent.putExtra("scale", true);
                break;
            case FULL_IMAGE:
                galleryAndCropIntent.putExtra("crop", "true");
                break;
        }

        galleryAndCropIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        galleryAndCropIntent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());

        return galleryAndCropIntent;
    }

    public void clearShareboxContentState() {
        ShareboxFragment.clearContentState();
    }

    /**
     * Handles an alert action based on the type of the Action.
     * 
     * @param action
     *            The {@link Action} to be handled.
     * @param activity
     *            A {@link FragmentActivity} to be used when launching other
     *            Fragments.
     * @return true if the action was successfully handled and false otherwise.
     */
    public boolean handleAction(final Action action, final FragmentActivity activity, final AlertTypeEnum alertType) {
        if (action != null) {
            final ObjectTypeEnum type = action.getType();

            // AD-1819: Clicking new fan notification should direct to new fan page
            // The action type of new fan notification should be ObjectTypeEnum.URL, but server returns ObjectTypeEnum.API
            // Android client gets around that by adding below OR judgement condition: alertType == AlertTypeEnum.NEW_FOLLOWER_ALERT
            if ((type == ObjectTypeEnum.URL || alertType == AlertTypeEnum.NEW_FOLLOWER_ALERT) && activity != null) {
                AlertTypeTitleInfo titleInfo = AlertTypeTitleInfo.getTitleInfo(alertType);
                String url = action.getUrlFromView(ViewTypeEnum.TOUCH);
                if (TextUtils.isEmpty(url)) {
                    Crashlytics.log(Log.ERROR, "NO_TOUCH_URL", "AlertType:" + alertType + " Action:" + action);
                    url = action.getUrlFromView(ViewTypeEnum.ALL);
                }
                if (!TextUtils.isEmpty(url)) {
                    UrlHandler.displayUrl(activity, url, titleInfo.text, titleInfo.icon);
                }
                return true;
            } else if (type == ObjectTypeEnum.API) {
                final String url = action.getUrlFromView(ViewTypeEnum.TOUCH);
                if (!TextUtils.isEmpty(url)) {
                    SystemController.getInstance().sendAction(url, action.getHttpContentType(), action.getHttpMethod());
                    return true;
                }
            } else if (type == ObjectTypeEnum.RECOMMENDATION && activity != null) {
                ActionHandler.getInstance().displayRecommendedUsers(activity);
                return true;
            }
        }

        return false;
    }

    public void showStickerPurchaseConfirmDlg(Context context, final StoreItem data) {

        if (data == null || data.getReferenceData().getOwned()) {
            return;
        }

        //click callback
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:

                        StoreController.getInstance().purchaseStickerPack(
                                Integer.toString(data.getId().intValue()));
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        //show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String price = data.getRoundedPrice() + Constants.SPACESTR + data.getLocalCurrency();
        String message = String.format(I18n.tr("Do you want to buy \"%s\" for %s ?"), data.getName(), price);
        builder.setMessage(message)
                .setPositiveButton(I18n.tr("Buy"), dialogClickListener)
                .setNegativeButton(I18n.tr("Cancel"), dialogClickListener).show();

    }

}
