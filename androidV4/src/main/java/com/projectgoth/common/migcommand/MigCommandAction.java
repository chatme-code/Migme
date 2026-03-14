/**
 * Copyright (c) 2013 Project Goth
 *
 * MigCommandAction.java
 * Created Aug 5, 2014, 11:19:57 AM
 */

package com.projectgoth.common.migcommand;

import android.text.TextUtils;

import com.mig33.diggle.common.StringUtils;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.enums.PasswordTypeEnum;
import com.projectgoth.common.Constants;
import com.projectgoth.common.ShareManager;
import com.projectgoth.datastore.Session;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.fragment.ShareboxFragment.ShareboxActionType;

/**
 * Represents the blueprint of a command that is issued from an external source
 * to the client. Subtypes of this class must specify what action is to be
 * performed for the issued command. Subtypes can also specify what action must
 * be performed in case of an invalid session.
 * 
 * @author angelorohit
 * 
 */
public abstract class MigCommandAction {
    
    /**
     * Override this method if invalid session check should not be done 
     * 
     * @return Always returns true.
     */
    protected boolean shouldDoInvalidSessionCheck() {
        return true;
    }

    /**
     * Does common checks and routine calls for all {@link MigCommandAction}s.
     * 
     * @param params
     *            An array of String parameters that contain additional data for
     *            the issued command.
     * @return true if any of the common actions was performed and false
     *         otherwise.
     */
    protected boolean checkAndPerformActions(final String[] params) {
        boolean result = false;

        if (params == null || params.length == 0) {
            // Call doAction() for no parameters.
            result = doAction();
        } else if (params.length == 1) {
            // Call doAction() for single parameter.
            if (params[0] == null) {
                params[0] = Constants.BLANKSTR;
            }

            result = doAction(params[0]);
        }

        return result;
    }

    /**
     * Override this method to perform an action for those commands which do not
     * require any parameters. The overridden method should return true if the
     * action was successfully performed.
     * 
     * @return Always returns false.
     */
    protected boolean doAction() {
        return false;
    }

    /**
     * Override this method to perform an action for those commands which only
     * require a single parameter. The overridden method should return true if
     * the action was successfully performed.
     * 
     * @param param
     *            An String parameter that contains additional data for the
     *            issued command.
     * @return Always returns false.
     */
    protected boolean doAction(final String param) {
        return false;
    }

    /**
     * Override this method to specify any action to be performed for the issued
     * command having more than one parameter. Remember to call
     * super.doAction(params) and check its return value from the overridden
     * method.
     * 
     * @param params
     *            An array of String parameters that contain additional data for
     *            the issued command.
     * @return true if the action was successfully performed and false
     *         otherwise.
     */
    public boolean doAction(final String[] params) {
        return checkAndPerformActions(params);
    }

    /*
     * * MigCommandAction implementations
     */
    public static abstract class BaseShowBrowser extends MigCommandAction {

        protected String url;

        public BaseShowBrowser() {
            this.url = null;
        }

        public BaseShowBrowser(final String url) {
            super();
            this.url = url;
        }
        
        /**
         * Override {@link MigCommandAction#shouldDoInvalidSessionCheck()} to not do
         * anything because the browser can always be launched irrespective of
         * whether the user is logged in or not.
         */
        @Override
        protected boolean shouldDoInvalidSessionCheck() {
            return false;
        }

        /**
         * Opens the app browser.
         * 
         * @param url
         *            The url with which the app browser is to be launched.
         * @return true on success and false if the url is null or empty.
         */
        protected boolean openBrowser(final String url) {
            if (!TextUtils.isEmpty(url)) {
                ActionHandler.getInstance().displayBrowser(null, url);
                return true;
            }

            return false;
        }
    }

    public static class ShowBrowser extends MigCommandAction.BaseShowBrowser {

        public ShowBrowser(final String url) {
            super(url);
        }

        @Override
        public boolean doAction() {
            return openBrowser(url);
        }

        @Override
        public boolean doAction(final String param) {
            final String formattedUrl = String.format(url, param);
            return openBrowser(formattedUrl);
        }
    }

    public static class ShowMigStore extends MigCommandAction {

        @Override
        protected boolean doAction() {
            ActionHandler.getInstance().displayStore(null, null);
            return true;
        }

        @Override
        public boolean doAction(final String initialRecipient) {
            ActionHandler.getInstance().displayStore(null, initialRecipient);
            return true;
        }
    }

    public static class JoinChatroom extends MigCommandAction {

        @Override
        public boolean doAction(final String chatroomId) {
            ActionHandler.getInstance().displayPublicChat(null, chatroomId, 0);
            return true;
        }
    }

    public static class SearchTopic extends MigCommandAction {

        @Override
        public boolean doAction(final String hotTopic) {
            ActionHandler.getInstance().displayHotTopicPosts(null, hotTopic);
            return true;
        }
    }

    public static class StartPrivateChat extends MigCommandAction {

        @Override
        public boolean doAction(final String username) {
            ActionHandler.getInstance().displayPrivateChat(null, username);
            return true;
        }
    }

    public static class GoGamePage extends MigCommandAction {

        @Override
        public boolean doAction() {
            ActionHandler.getInstance().displayGameCentre(null);
            return true;
        }
    }

    public static class JoinLinkedChatroom extends MigCommandAction {

        @Override
        public boolean doAction(final String[] params) {
            boolean result = super.doAction(params);
            if (!result && params != null && params.length > 1) {
                try {
                    final String chatroomId = params[0].trim();
                    final int groupId = Integer.parseInt(params[1].trim());
                    ActionHandler.getInstance().displayPublicChat(null, chatroomId, groupId);
                    result = true;
                } catch (Exception ex) {
                }
            }

            return result;
        }
    }

    public static class ShowGroup extends MigCommandAction {
        
        @Override
        protected boolean shouldDoInvalidSessionCheck() {
            return false;
        }

        @Override
        public boolean doAction(final String groupId) {
            ActionHandler.getInstance().displayGroupPage(null, groupId);
            return true;
        }
    }

    public static class ShowChatroomList extends MigCommandAction {

        @Override
        public boolean doAction() {
            ActionHandler.getInstance().goToChatroomList();
            return true;
        }
    }

    public static class GotoLogin extends MigCommandAction {
        
        @Override
        protected boolean shouldDoInvalidSessionCheck() {
            return false;
        }

        @Override
        public boolean doAction() {
            ActionHandler.getInstance().showLogin(null);
            return true;
        }

        @Override
        public boolean doAction(final String username) {
            if (!TextUtils.isEmpty(username)) {
                ActionHandler.getInstance().showLogin(username);
                return true;
            }
            
            return false;
        }
    }

    public static class OpenUrl extends MigCommandAction.BaseShowBrowser {

        @Override
        public boolean doAction(final String[] params) {
            boolean result = super.doAction(params);
            if (!result && params != null && params.length > 1) {
                return openBrowser(params[1]);
            }

            return result;
        }
    }

    public static class ShowProfile extends MigCommandAction {

        @Override
        public boolean doAction(final String username) {
            ActionHandler.getInstance().displayProfile(null, username);
            return true;
        }
    }

    public static class ShowHotTopics extends MigCommandAction {

        @Override
        public boolean doAction() {
            ActionHandler.getInstance().displayHotTopics(null);
            return true;
        }
    }

    public static class ShowRecommendedUsers extends MigCommandAction {

        @Override
        public boolean doAction() {
            ActionHandler.getInstance().displayRecommendedUsers(null);
            return true;
        }
        
        // TODO: This method should be in the base class and implemented by sub classes
        // another method should have params -> getActionUrl(String... params)
        public static String getActionUrl() {
            return Constants.LINK_MIG33 + SupportedMigCommands.SHOW_RECOMMENDEDUSERS; 
        }
    }

    public static class DoLogout extends MigCommandAction {

        @Override
        public boolean doAction() {
            ApplicationEx.getInstance().getNetworkService().logout();
            return true;
        }
    }

    public static class DoSSOLogin extends MigCommandAction {

        @Override
        protected boolean shouldDoInvalidSessionCheck() {
            return false;
        }

        @Override
        public boolean doAction(final String param) {
            final int passwordType = Integer.parseInt(param);
            if (passwordType == PasswordTypeEnum.FACEBOOK_IM.value()) {
                ActionHandler.getInstance().startFacebookLogin(ApplicationEx.getInstance().getCurrentActivity());
                return true;
            }
            return false;
        }
        
        @Override
        public boolean doAction(final String[] params) {
            boolean result = super.doAction(params);
            if (!result && params != null) {
                try {
                    if (params.length > 2) {
                        final int passwordType = Integer.parseInt(params[0]);
                        final String userId = params[1];
                        final String accessToken = params[2];

                        ActionHandler.getInstance().sendFacebookSSOLogin(userId, accessToken, passwordType);
                        result = true;
                    }
                } catch (Exception ex) {
                }
            }

            return result;
        }
    }
    
    //- Remove this event after a while. Needs to stay here till corporate gets released again.
    public static class DoSSOLoginFiksu extends DoSSOLogin {
        @Override
        public boolean doAction(final String[] params) {
            return super.doAction(params);
        }
    }

    public static class SyncPhoneAddressbook extends MigCommandAction {

        @Override
        public boolean doAction() {
            ActionHandler.getInstance().beginRetrieveAddressBookContacts();
            ActionHandler.getInstance().displayRecommendedUsers(null);
            return true;
        }
    }

    public static class ShowPost extends MigCommandAction {

        @Override
        public boolean doAction(final String[] params) {
            boolean result = super.doAction(params);
            if (!result && params != null && params.length > 0) {
                final String postId = params[0];
                boolean isPostInGroup = false;

                if (params.length > 1) {
                    isPostInGroup = (params[1].equals("1") ? true : false);
                }

                if (!TextUtils.isEmpty(postId)) {
                    ActionHandler.getInstance().displaySinglePostPage(null, postId, isPostInGroup, false);
                    result = true;
                }
            }

            return result;
        }
    }

    public static class ShowSharebox extends MigCommandAction {

        @Override
        public boolean doAction(final String shareContent) {
            ActionHandler.getInstance().displaySharebox(null, ShareboxActionType.CREATE_NEW_POST, null, shareContent,
                    null, true);
            return true;
        }
    }

    public static class ShowShareTo extends MigCommandAction {

        @Override
        public boolean doAction(final String shareContent) {
            boolean result = super.doAction(shareContent);
            if (!result && !StringUtils.isEmpty(shareContent)) {
                ShareManager.shareWeb(shareContent);
                result = true;
            }
            return result;
        }
    }

    public static class ShowShareToFB extends  MigCommandAction {

        @Override
        public boolean doAction(final String shareContent) {
            boolean result = super.doAction(shareContent);
            if (!result && !StringUtils.isEmpty(shareContent)) {
                ShareManager.shareToFacebookOrTwitter(ApplicationEx.getInstance().getCurrentActivity(), "",
                        shareContent, ShareManager.ShareType.SHARE_TO_FACEBOOK.value());
                result = true;
            }
            return result;
        }

        protected boolean shouldDoInvalidSessionCheck() {
            return false;
        }
    }

    public static class ShowShareToTW extends  MigCommandAction {

        @Override
        public boolean doAction(final String shareContent) {
            boolean result = super.doAction(shareContent);
            if (!result && !StringUtils.isEmpty(shareContent)) {
                ShareManager.shareToFacebookOrTwitter(ApplicationEx.getInstance().getCurrentActivity(), "",
                        shareContent, ShareManager.ShareType.SHARE_TO_TWITTER.value());
                result = true;
            }
            return result;
        }

        protected boolean shouldDoInvalidSessionCheck() {
            return false;
        }
    }

    public static class ShowShareToMigme extends  MigCommandAction {

        @Override
        public boolean doAction(final String shareContent) {
            boolean result = super.doAction(shareContent);
            if (!result && !StringUtils.isEmpty(shareContent)) {
                ShareManager.shareToChat(ApplicationEx.getInstance().getCurrentActivity(), shareContent);
                result = true;
            }
            return result;
        }
    }

    public static class SendGift extends MigCommandAction {

        @Override
        protected boolean doAction() {
            ActionHandler.getInstance().displayStore(null, null);
            return true;
        }
        
        @Override
        public boolean doAction(final String initialRecipient) {
            ActionHandler.getInstance().displayStore(null, initialRecipient);
            return true;
        }
        
        @Override
        public boolean doAction(String[] params) {
            boolean result = super.doAction(params);
            if (!result && params != null) {
                if (params.length > 1) {
                    String recipient = params[0] != null ? params[0].trim() : Constants.BLANKSTR;
                    String giftId = params[1] != null ? params[1].trim() : Constants.BLANKSTR;
                    if(params.length == 2) {
                        ActionHandler.getInstance().displayGiftItem(null, giftId, recipient);
                        return true;
                    }
                    if(params.length == 3) {
                        String counterId = params[2] != null ? params[2].trim() : Constants.BLANKSTR;
                        ActionHandler.getInstance().displayGiftItem(null, giftId, recipient, counterId);
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public static class ShowFollowers extends MigCommandAction {

        @Override
        public boolean doAction() {
            ActionHandler.getInstance().displayFollowersList(null, Session.getInstance().getUsername());
            return true;
        }
    }

    public static class ShowBadges extends MigCommandAction {

        @Override
        public boolean doAction() {
            ActionHandler.getInstance().displayBadgesList(null, Session.getInstance().getUsername());
            return true;
        }
    }

    public static class ShowInviteFriends extends MigCommandAction {

        @Override
        public boolean doAction() {
            ActionHandler.getInstance().displayInviteFriends(null);
            return true;
        }

        public static String getActionUrl() {
            return Constants.LINK_MIG33 + SupportedMigCommands.SHOW_INVITE_FRIENDS;
        }
    }

}
