/**
 * Copyright (c) 2013 Project Goth
 *
 * BaseCustomFragmentActivity.java
 * Created May 20, 2014, 6:37:44 PM
 */

package com.projectgoth.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.enums.PostOriginalityEnum;
import com.projectgoth.blackhole.enums.MessageType;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Tools;
import com.projectgoth.common.migcommand.MigCommandsHandler;
import com.projectgoth.controller.StatusBarController;
import com.projectgoth.datastore.AlertsDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.events.AppEvents;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.model.MenuOption;
import com.projectgoth.music.deezer.DeezerPlayerManager;
import com.projectgoth.nemesis.enums.ChatTypeEnum;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.notification.NotificationType;
import com.projectgoth.service.NetworkService;
import com.projectgoth.ui.activity.CustomActionBar.CustomActionBarListener;
import com.projectgoth.ui.activity.CustomActionBarConfig.OverflowButtonState;
import com.projectgoth.ui.fragment.AttachmentPhotoFragment.PhotoEventListener;
import com.projectgoth.ui.fragment.BaseFragment;
import com.projectgoth.ui.fragment.NavigationDrawerFragment;
import com.projectgoth.ui.fragment.ShareboxFragment;
import com.projectgoth.ui.fragment.ShareboxFragment.ShareboxActionType;
import com.projectgoth.ui.fragment.UnlockedGiftListFragment;
import com.projectgoth.ui.widget.PopupMenu.OnPopupMenuListener;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.LogUtils;
import com.projectgoth.util.scheduler.ScheduledJobsHandler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author mapet
 * 
 */
public abstract class BaseCustomFragmentActivity extends BaseFragmentActivity implements CustomActionBarListener,
        OnPopupMenuListener,NavigationDrawerFragment.NavigationDrawerCallbacks  {

    protected CustomActionBar mDefaultActionBar;
    
    protected NavigationDrawerFragment mNavigationDrawerFragment;
    
    protected PhotoEventListener photoEventListener;

    private static final String LOG_TAG = AndroidLogger.makeLogTag(BaseCustomFragmentActivity.class);

    private Fragment mFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initActionBar();
        
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();
        
        if (action != null && action.equals(Intent.ACTION_VIEW)) {
            if (data != null) {
                Session.getInstance().setDeepLinkUri(data);
            }
        }

        //init currently activity for the very beginning
        if (ApplicationEx.getInstance().getCurrentActivity() == null) {
            ApplicationEx.getInstance().setCurrentActivity(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDefaultActionBar != null) {
            mDefaultActionBar.deinitActionBar();
        }
    }

    @Override
    public void onShowFragment(Fragment fragment) {
        Logger.debug.log(LogUtils.TAG_MAIN_UI, this.getClass().getSimpleName() + ".onShowFragment: " + fragment.getTag()
                + ", id: " + fragment.getId() + ", contentFrame: " + R.id.content_frame);

        MigCommandsHandler.getInstance().processPendingCommandUrl();
        processDeepLinkUri();
    }
    
    @Override
    public void onHideFragment(Fragment fragment) {
    }

    public void initActionBar() {
        CustomActionBarConfig config = new CustomActionBarConfig();
        config.setCustomViewLayoutSrc(R.layout.action_bar_default);
        mDefaultActionBar = new CustomActionBar(this, config, this, this);
        try {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(mDefaultActionBar.getActionBar());
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        mDefaultActionBar.setActionBarListener(this);
    }

    public void resetCustomActionBar() {
        CustomActionBarConfig config = new CustomActionBarConfig();
        config.setCustomViewLayoutSrc(R.layout.action_bar_default);
        mDefaultActionBar = new CustomActionBar(this, config, this, this);
        mDefaultActionBar.setActionBarListener(this);
        mDefaultActionBar.updateCustomActionBar(config, this, this);
    }
    
    @Override
    public ArrayList<MenuOption> getMenuOptions() {
        return null;
    }
    
    @Override
    public void onNavigationIconPressed() {
        UserDatastore.getInstance().getProfileWithUsername(Session.getInstance().getUsername(), false);
    }

    @Override
    public void onOverflowButtonPressed(final OverflowButtonState state) {
        // Do nothing.
    }

    @Override
    public void onBackIconPressed() {
        resetCustomActionBar();
    }
    
    @Override
    public void onSearchButtonPressed() {
        // Do nothing
    }

    @Override
    public void onMenuOptionClicked(MenuOption menuOption) {
    }
    
    @Override
    public void onPopupMenuDismissed() {
    }
    
    @Override
    public void initCustomViewInCustomActionBar(View customView) {
        TextView title = (TextView) customView.findViewById(R.id.ab_title);
        title.setText(I18n.tr("Feed"));
    }
    
    public void updateActionBarForFragment(final BaseFragment fragment) {  
        if (mDefaultActionBar == null) {
            initActionBar();
        }
        
        setActionBarListener(fragment.getCustomActionBarListener());
        setMenuOptionClickListener(fragment.getPopupMenuListener());
        updateActionBarFromConfig(fragment.getActionBarConfig());
    }
    
    public void updateActionBarFromConfig(final CustomActionBarConfig config) {
        mDefaultActionBar.updateActionBarConfig(config);
    }
    
    public void setActionBarListener(final CustomActionBarListener listener) {
        if (listener != null) {
            mDefaultActionBar.setActionBarListener(listener);
        } else {
            mDefaultActionBar.setActionBarListener(this);
        }
    }
    
    public void setMenuOptionClickListener(final OnPopupMenuListener listener) {
        if (listener != null) {
            mDefaultActionBar.setOnMenuOptionsClickListener(listener);
        } else {
            mDefaultActionBar.setOnMenuOptionsClickListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //updated the unread counter on Action bar onResume
        if (mDefaultActionBar != null) {
            mDefaultActionBar.onActionBarUpdateAvailable(false);
        }

        //show the connection status
        StatusBarController controller = StatusBarController.getInstance();
        controller.setActivity(this);
        controller.updateOnConnectionStatusChange();

        // for [non-login] users
        prepareNonLoginBar();

    }

    @Override
    public void registerReceivers() {
        Logger.debug.log(LOG_TAG, "registerReceivers:" + this);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(Events.Login.LOGOUT));
        localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(Events.Login.SUCCESS));
        localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(Events.Post.SENT));
        localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(Events.Post.SEND_ERROR));
        localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(AppEvents.Notification.UPDATE_AVAILABLE));
        localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(AppEvents.Notification.ACTION_BAR_UPDATE_AVAILABLE));
        localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(AppEvents.Notification.FETCH_ALERTS_FROM_SERVER_DONE));
        localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(Events.ContactGroup.ADD_ERROR));
        localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(Events.ContactGroup.REMOVE_ERROR));
        localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(Events.ContactGroup.UPDATE_ERROR));
        localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(Events.Contact.REMOVE_ERROR));
        localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(Events.Contact.MOVE_ERROR));
        localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(Events.MigAlert.UNREAD_POSTIVEALERT_RECEIVED));
        localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(Events.MigAlert.UNREAD_INTERSTITIAL_RECEIVED));
        localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(Events.User.SET_DISPLAY_PICTURE_ERROR));
        localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(Events.User.UPLOAD_TO_PHOTO_ALBUM_ERROR));
        localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(Events.ChatConversation.GroupChat.CREATE_ERROR));
        localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(Events.ChatRoom.CREATE_ERROR));
        localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(Events.MigStore.Item.PURCHASE_STORE_ITEM_COMPLETED));
        localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(AppEvents.NetworkService.STARTED));
        localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(AppEvents.NetworkService.NETWORK_STATUS_CHANGED));
        localBroadcastManager.registerReceiver(mReceiver, new IntentFilter(AppEvents.NetworkService.ERROR));
    }

    @Override
    protected void unregisterReceivers() {
        Logger.debug.log(LOG_TAG, "unregisterReceivers:" + this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    //@formatter:off
    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Logger.debug.log(LOG_TAG, "onReceive-" + action + ":"+ BaseCustomFragmentActivity.this);
            if (action.equals(Events.Login.LOGOUT)) {
                ScheduledJobsHandler.getInstance().stopAllJobs();
                setVisible(false);
                FragmentHandler.getInstance().showLoginActivity(BaseCustomFragmentActivity.this, true);
                BaseCustomFragmentActivity.this.finish();
                prepareNonLoginBar();
            } else if (action.equals(Events.Login.SUCCESS)) {
                AlertsDatastore.getInstance().startUnreadAlertsPollSchedule();
                prepareNonLoginBar();
            } else if (action.equals(Events.Post.SENT)) {
                final PostOriginalityEnum postOriginality = 
                        PostOriginalityEnum.fromValue(
                                intent.getIntExtra(Events.Post.Extra.ORIGINALITY, PostOriginalityEnum.ORIGINAL.value()));
                if (postOriginality.equals(PostOriginalityEnum.ORIGINAL)) {
                    GAEvent.Miniblog_CreatePostSuccess.send();
                    Tools.showToast(context, I18n.tr("Post sent"));
                } else if (postOriginality.equals(PostOriginalityEnum.REPLY)) {
                    GAEvent.Miniblog_ReplySuccess.send();
                    Tools.showToast(context, I18n.tr("Reply sent"));
                } else if (postOriginality.equals(PostOriginalityEnum.RESHARE)) {
                    GAEvent.Miniblog_RepostSuccess.send();
                    Tools.showToast(context, I18n.tr("Repost sent"));
                }
                ActionHandler.getInstance().clearShareboxContentState();
            } else if (action.equals(Events.Post.SEND_ERROR)) {
                int errorCode = intent.getIntExtra(Events.Misc.Extra.ERROR_TYPE, 0);
                if (errorCode == MigError.Type.DUPLICATE_POST.value()) {
                    Tools.showToast(context, I18n.tr("Sorry you have already posted that"));
                } else {
                    Tools.showToastForIntent(context, intent);
                }
            } else if (action.equals(AppEvents.Notification.UPDATE_AVAILABLE)) {
                if (!ApplicationEx.getInstance().isApplicationInBackground()) {
                    int type = intent.getIntExtra(AppEvents.Notification.Extra.TYPE, 0);
                    String id = intent.getStringExtra(AppEvents.Notification.Extra.ID);
                    handleNotificationAvailable(NotificationType.fromValue(type), id);
                }
            } else if (action.equals(AppEvents.Notification.ACTION_BAR_UPDATE_AVAILABLE)) {
                if (mDefaultActionBar != null) {
                    mDefaultActionBar.onActionBarUpdateAvailable(true);
                }
            } else if (action.equals(AppEvents.Notification.FETCH_ALERTS_FROM_SERVER_DONE)) {
                if (mDefaultActionBar != null) {
                    mDefaultActionBar.onActionBarUpdateAvailable(false);
                }
            } else if (action.equals(Events.ContactGroup.ADD_ERROR) ||
                    action.equals(Events.ContactGroup.REMOVE_ERROR) ||
                    action.equals(Events.ContactGroup.UPDATE_ERROR) ||
                    action.equals(Events.Contact.MOVE_ERROR) ||
                    action.equals(Events.Contact.REMOVE_ERROR) ||
                    action.equals(Events.User.SET_DISPLAY_PICTURE_ERROR) ||
                    action.equals(Events.User.UPLOAD_TO_PHOTO_ALBUM_ERROR) ||
                    action.equals(Events.Post.WATCH_ERROR) ||
                    action.equals(Events.Post.UNWATCH_ERROR) ||
                    action.equals(Events.Post.LOCK_ERROR) ||
                    action.equals(Events.Post.UNLOCK_ERROR) ||
                    action.equals(Events.ChatRoom.CREATE_ERROR)) {
                Tools.showToastForIntent(context, intent);
            } else if (action.equals(Events.MigAlert.UNREAD_POSTIVEALERT_RECEIVED)) {
                final ArrayList<String> alertIdList = intent.getStringArrayListExtra(Events.MigAlert.Extra.ID);

                ActionHandler.getInstance().displayPositiveAlert(ApplicationEx.getInstance().getCurrentActivity(), alertIdList);
            } else if (action.equals(Events.MigAlert.UNREAD_INTERSTITIAL_RECEIVED)) {
                final String message = intent.getStringExtra(Events.MigAlert.Extra.MESSAGE);
                final String actionUrl = intent.getStringExtra(Events.MigAlert.Extra.ACTION_URL);
                //To fix PT [#61886836]: use ApplicationEx.getInstance().getCurrentActivity() instead of MainDrawerLayoutActivity.this
                ActionHandler.getInstance().displayInterstitialBanner(ApplicationEx.getInstance().getCurrentActivity(), message, actionUrl);
            } else if (action.equals(Events.ChatConversation.GroupChat.CREATE_ERROR)) {
                final Bundle data = intent.getExtras();
                final String errorMsg = data.getString(Events.Misc.Extra.ERROR_MESSAGE);
                Tools.showToast(context, errorMsg);
            } else if (action.equals(Events.MigStore.Item.PURCHASE_STORE_ITEM_COMPLETED)) {
                if (getFragment() instanceof UnlockedGiftListFragment) {
                    onBackPressed();
                }
            } else if (action.equals(AppEvents.NetworkService.STARTED) ||
                    action.equals(AppEvents.NetworkService.NETWORK_STATUS_CHANGED)) {

                // update connection status bar
                StatusBarController controller = StatusBarController.getInstance();
                controller.setActivity(BaseCustomFragmentActivity.this);
                controller.updateOnConnectionStatusChange();

                // disable or enable buttons according to network status
                NetworkService networkService = ApplicationEx.getInstance().getNetworkService();

                if (networkService != null && mAttachedFragments != null) {
                    for (Map.Entry<String, WeakReference<Fragment>> entry: mAttachedFragments.entrySet()) {
                        Fragment fragment = entry.getValue().get();
                        if (fragment != null && fragment instanceof BaseFragment) {
                            if (networkService.isNetworkAvailable()) {
                                ((BaseFragment)fragment).enableNoConnectionDisableButton();
                            } else {
                                ((BaseFragment)fragment).disableNoConnectionDisableButton();
                            }
                        }
                    }
                }


            } else if (action.equals(AppEvents.NetworkService.ERROR)) {

                int errorType = intent.getIntExtra(AppEvents.NetworkService.Extra.ERROR_TYPE,
                        MigError.Type.UNKNOWN.value());
                MigError.Type error = MigError.Type.fromValue(errorType);
                if (error == MigError.Type.HTTP_REQUEST_TIMEOUT) {
                    //show slow connection
                    StatusBarController controller = StatusBarController.getInstance();
                    controller.setActivity(BaseCustomFragmentActivity.this);
                    controller.updateSlowConnectionStatus();
                }

            }
        }
    };
    //@formatter:on

    private void handleNotificationAvailable(NotificationType type, String notificationId) {
        Logger.debug.logWithTrace(LogUtils.TAG_NOTIFICATION, getClass(), "type: ", type, "id: ", notificationId);
        // let the fragment displayed in the content-frame handle the
        // notification alert
        BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment != null) {
            fragment.handleNotificationAvailable(type, notificationId);
        } else {
            // handle the notification the default way
            ApplicationEx.getInstance().getNotificationHandler().showStatusNotification();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // This is only called when application is still "alive" or in
        // background. Usually this is triggered when clicking on a notification
        // from notification manager
        Logger.debug.logWithTrace(LogUtils.TAG_NOTIFICATION, getClass(), intent);
        Logger.debug.log(LOG_TAG, "onNewIntent:" + this);
        super.onNewIntent(intent);
        processIntentAction(intent);
    }

    /**
     *  this method is to trigger an Action in the Intent, called in two cases:
     *  1. main activity not created -> create it -> onPostCreate -> processIntentAction
     *  2. activity created ->  onNewIntent -> processIntentAction
     *  @param intent
     */
    protected void processIntentAction(Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        Logger.debug.log(LOG_TAG, "processIntentAction:" + this + " Action:" + action);

        if (!TextUtils.isEmpty(action)) {
            Logger.debug.logWithTrace(LogUtils.TAG_NOTIFICATION, getClass(), action);
            if (action.equals(AppEvents.Application.SHOW_MUSIC_PAGE)) {
                ActionHandler.getInstance().displayMusicPage(this);
            } else if (action.equals(AppEvents.Notification.CHAT_SYSTEM_NOTIFICATION)) {
                final String chatId = intent.getStringExtra(Events.ChatConversation.Extra.CHAT_ID);
                final ChatTypeEnum chatType = 
                        ChatTypeEnum.fromValue(
                                intent.getByteExtra(Events.ChatConversation.Extra.CHAT_TYPE, ChatTypeEnum.UNKNOWN.getValue()));
                final MessageType imMessageType =
                        MessageType.fromValue(
                                intent.getByteExtra(Events.ChatConversation.Extra.IM_MESSAGE_TYPE, MessageType.UNKNOWN.getValue()));
                if (TextUtils.isEmpty(chatId) || chatType.equals(ChatTypeEnum.UNKNOWN) || imMessageType.equals(MessageType.UNKNOWN)) {
                    ActionHandler.getInstance().goToMyChats();
                } else {
                    ActionHandler.getInstance().displayChatConversation(this, chatId, chatType, imMessageType, true);
                }
            } else if (action.equals(AppEvents.Notification.MIGALERT_SYSTEM_NOTIFICATION)) {
                ActionHandler.getInstance().displayAlerts(this);
            } else if (action.equals(AppEvents.Notification.ACCUMULATED_FOLLOWERS_SYSTEM_NOTIFICATION)) {
                if (extras.containsKey(AppEvents.Notification.Extra.ACTION_URL)) {
                    final String actionUrl = extras.getString(AppEvents.Notification.Extra.ACTION_URL);
                    if (!TextUtils.isEmpty(actionUrl)) {
                        com.projectgoth.ui.UrlHandler.displayUrl(this, actionUrl);
                    }
                }
            } else if (action.equals(Intent.ACTION_SEND) && extras != null) {
                String presetTextContent = null;
                if (extras.containsKey(Intent.EXTRA_TEXT)) {
                    presetTextContent = extras.getCharSequence(Intent.EXTRA_TEXT).toString();
                }

                Uri presetPhotoUri = null;
                if (extras.containsKey(Intent.EXTRA_STREAM)) {
                    presetPhotoUri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
                }

                ActionHandler.getInstance().displaySharebox(this, ShareboxActionType.CREATE_NEW_POST, null,
                        presetTextContent, presetPhotoUri, null, true, ShareboxFragment.ShareboxSubActionType.NONE);
            }
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
    }
    //Push main content_frame up
    protected void pushContentFrameUp(){
        View view = findViewById(R.id.content_frame);
        if(view != null) {
            UIUtils.setMargins(view, 0, 0, 0, 0);
            View actionBarView = UIUtils.getActionBarView(this);
            //Actionbar might be null
            if (actionBarView != null) {
                actionBarView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setPhotoEventListener(PhotoEventListener photoEventListener) {
        this.photoEventListener = photoEventListener;
    }

    private void prepareNonLoginBar() {
        LinearLayout nonLoginBar = (LinearLayout)findViewById(R.id.nonlogin_bar);
        if (nonLoginBar != null) {
            if (!Session.getInstance().isLoggedIn()) {
                nonLoginBar.setVisibility(View.VISIBLE);
                TextView login = (TextView)findViewById(R.id.nonlogin_bar_login);
                login.setText(I18n.tr("LOG IN"));
                TextView register = (TextView)findViewById(R.id.nonlogin_bar_register);
                register.setText(I18n.tr("SIGN UP"));
                View.OnClickListener clickListener = new View.OnClickListener(){
                    public void onClick(View view){
                        FragmentHandler.getInstance().showLoginActivity(BaseCustomFragmentActivity.this);
                    }
                };
                login.setOnClickListener(clickListener);
                register.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentHandler.getInstance().showLoginActivity(BaseCustomFragmentActivity.this, true, null, LoginActivity.PreloadedFragmentKey.USERNAME);
                    }
                });

            } else {
                nonLoginBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mNavigationDrawerFragment != null
                && mNavigationDrawerFragment.isDrawerOpen()) {
            mNavigationDrawerFragment.toggleDrawer();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setFragment(Fragment fragment) {
        this.mFragment = fragment;
    }

    public Fragment getFragment() {
        return this.mFragment;
    }
    
    private void processDeepLinkUri() {
        if (Session.getInstance().isBlockUsers()) {
            return;
        }
        if (Session.getInstance().getDeepLinkUri() != null) {
            Uri uri = Session.getInstance().getDeepLinkUri();
            String host = uri.getHost();
            List<String> pathSegments = uri.getPathSegments();

            if (host.equalsIgnoreCase(Constants.DL_USER)) {
                String username = pathSegments.get(0);
                ActionHandler.getInstance().displayMainProfile(this, username);
            } else if (host.equalsIgnoreCase(Constants.DL_POST)) {
                ActionHandler.getInstance().displaySinglePostPage(this, pathSegments.get(1), false, false);
            } else if (host.equalsIgnoreCase(Constants.DL_MUSIC)) {
                long radioId = Long.valueOf(pathSegments.get(2));
                DeezerPlayerManager.getInstance().setBgPlayingRadioId(radioId);
                ActionHandler.getInstance().displayMusicPage(this);
            } else if (host.equalsIgnoreCase(Constants.DL_CHATROOM)) {
                ActionHandler.getInstance().displayPublicChat(this, pathSegments.get(0), -1);
            }

            Session.getInstance().setDeepLinkUri(null);
        }
    }
}
