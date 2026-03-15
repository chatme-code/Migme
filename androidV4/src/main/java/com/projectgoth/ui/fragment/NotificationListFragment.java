
package com.projectgoth.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.TextView;
import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Action;
import com.projectgoth.b.data.Alert;
import com.projectgoth.b.data.Profile;
import com.projectgoth.b.data.Variable;
import com.projectgoth.b.data.VariableLabel;
import com.projectgoth.b.enums.AlertTypeEnum;
import com.projectgoth.b.enums.NotificationTypeEnum;
import com.projectgoth.b.enums.ObjectTypeEnum;
import com.projectgoth.common.Config;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.TextUtils;
import com.projectgoth.common.Tools;
import com.projectgoth.common.WebURL;
import com.projectgoth.controller.ProgressDialogController;
import com.projectgoth.datastore.AlertsDatastore;
import com.projectgoth.datastore.GiftsDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.events.AppEvents;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.ContextMenuItem;
import com.projectgoth.model.MenuOption;
import com.projectgoth.model.NotificationCategory;
import com.projectgoth.notification.NotificationType;
import com.projectgoth.ui.UrlHandler;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.activity.CustomPopupActivity;
import com.projectgoth.ui.adapter.NotificationListAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.NotificationCategoryViewHolder;
import com.projectgoth.ui.listener.ContextMenuItemListener;
import com.projectgoth.ui.widget.ClickableSpanEx;
import com.projectgoth.ui.widget.ClickableSpanEx.ClickableSpanExListener;
import com.projectgoth.util.ArrayUtils;
import com.projectgoth.util.CrashlyticsLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author dangui
 * 
 */
public class NotificationListFragment extends ExpandableListFragment<NotificationListAdapter> implements
        NotificationCategoryViewHolder.NotificationCategoryListener,
        NotificationListAdapter.GroupFooterClickListener, ContextMenuItemListener,
        ClickableSpanExListener, BaseViewListener<Alert> {

    private View emptyView;
    private Bundle state = new Bundle();

    private ArrayList<NotificationCategory> mNotificationCategoryList;
    private List<Alert> mAlertList;
    // deprecate the use of mAlreadyReadNotificationCategory, since we now do not add alerts to mAlreadyReadNotificationCategory
    private NotificationCategory mAlreadyReadNotificationCategory;
    private boolean mShowRefreshIndicator = true;
    private List<NotificationCategory> mClickedNotificationCategoryList;
    private boolean mTextClickHandled = false;
    private int mListPosition = 0;
    private int mItemPosition = 0;
    private static final String LIST_POSITION_KEY = "listPosition";
    private static final String ITEM_POSITION_KEY = "itemPosition";
    private static final int FRAGMENT_CLOSE_DELAY = 300;
    private int mUnreadMentionsCount = ApplicationEx.getInstance().getNotificationHandler().getUnreadMentionCount();
    private String mRequestProfile;

    @Override
    protected NotificationListAdapter createAdapter() {
        NotificationListAdapter adapter = new NotificationListAdapter();

        // set listeners to view holder
        adapter.setChatItemListener(this);
        adapter.setNotificationCategoryListener(this);
        adapter.setGroupFooterListener(this);
        adapter.setOnUsernameClickListener(this);

        // set data to adapter
        adapter.setFullNotificationCategories(getNotificationCategoryList(true));

        AlertsDatastore.getInstance().requestGetUnreadMentionCount(Session.getInstance().getUserId().toString());
        return adapter;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_notification;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // will create adapter
        super.onViewCreated(view, savedInstanceState);
        
        setListEmptyView(createEmptyView());
        mExpandableListView.setAdapter(mAdapter);
        setupExpandStatus();
        
        setupOnBackPressListener();
    }

    private void setupExpandStatus() {
        int groupCount = mExpandableListView.getCount();
        if (groupCount > 0) {
            // collapse all group
            for (int i = 0; i < groupCount - 1; i++) {
                mExpandableListView.collapseGroup(i);
            }
            // always expand the already read group
            if (mNotificationCategoryList != null && mNotificationCategoryList.size() > 0 &&
                    mNotificationCategoryList.get(groupCount - 1).getNotificationTypeEnum() == NotificationTypeEnum.ALREADY_READ) {
                mExpandableListView.expandGroup(groupCount - 1);
            }
        }
        // do not show the default group indicator
        mExpandableListView.setGroupIndicator(null);
    }

    // used for pulling down the listView
    @Override
    public void onRefresh() {
        if (Session.getInstance().isNetworkConnected()) {
            sendGAEventsForAlertActions(null, null, ActionType.REFRESH);
            AlertsDatastore.getInstance().requestGetUnreadMentionCount(Session.getInstance().getUserId().toString());
            refreshAllCategories(true);
            //readClickedCategoryAlerts();
        } else {
            setPullToRefreshComplete();
        }
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.MigAlert.FETCH_ALL_COMPLETED);
        registerEvent(Events.MigAlert.FETCH_ALL_ERROR);
        registerEvent(Events.MigAlert.ACTION_SUCCESS);
        registerEvent(Events.MigAlert.ACTION_ERROR);
        registerEvent(Events.MigAlert.BEGIN_FETCH_ALL);
        registerEvent(Events.ChatMessage.RECEIVED);
        registerEvent(Events.ChatConversation.RECEIVED);
        registerEvent(Events.MigAlert.UNREAD_MENTION_COUNT_RECEIVED);
        registerEvent(AppEvents.NetworkService.ERROR);
        registerEvent(Events.Emoticon.RECEIVED);
        registerEvent(Events.Emoticon.BITMAP_FETCHED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(Events.MigAlert.BEGIN_FETCH_ALL) && mShowRefreshIndicator) {
            // disable the automatic pulling down of the listView
            // setPullToRefreshAsRefreshing();
        } else if (action.equals(Events.MigAlert.FETCH_ALL_COMPLETED)) {
            refreshAllCategories(false);
            setPullToRefreshComplete();
        } else if (action.equals(Events.MigAlert.FETCH_ALL_ERROR)) {
            setPullToRefreshComplete();
        } else if (action.equals(Events.MigAlert.ACTION_SUCCESS)) {
            ProgressDialogController.getInstance().hideProgressDialog();
            onRefresh();
            Tools.showToastForIntent(context, intent);
            if (mRequestProfile != null) {
                //Sync user profile cache
                if (!TextUtils.isEmpty(mRequestProfile)) {
                    UserDatastore.getInstance().requestGetProfile(mRequestProfile);
                    mRequestProfile = null;
                }
            }
        } else if (action.equals(Events.MigAlert.ACTION_ERROR)) {
            ProgressDialogController.getInstance().hideProgressDialog();
            Tools.showToastForIntent(context, intent);
        } else  if (action.equals(Events.ChatMessage.RECEIVED) || action.equals(Events.ChatConversation.RECEIVED)) {
            if (mNotificationCategoryList != null && mNotificationCategoryList.size() > 0 && mNotificationCategoryList.get(0).isChatMessageNotification()) {
                mAdapter.notifyDataSetChanged();
                setTitle(createNewTitle());
            } else {
                refreshAllCategories(true);
            }
        } else if (action.equals(Events.MigAlert.UNREAD_MENTION_COUNT_RECEIVED)) {
            mUnreadMentionsCount = ApplicationEx.getInstance().getNotificationHandler().getUnreadMentionCount();
            refreshAllCategories(false);
            setPullToRefreshComplete();
        } else if (action.equals(AppEvents.NetworkService.ERROR)) {
            setPullToRefreshComplete();
        } else if (action.equals(Events.Emoticon.RECEIVED) || action.equals(Events.Emoticon.BITMAP_FETCHED)) {
            //just to show the emotions
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v, ClickableSpanEx span, String value) {
        // only username is supported currently
        UrlHandler.displayUrl(getActivity(), value);
        mTextClickHandled = true;
    }
    
    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_alert_white;
    }

    @Override
    protected String getTitle() {
        return createNewTitle();
    }

    private String createNewTitle() {

        int unreadAlertCount = ApplicationEx.getInstance().getNotificationHandler().getAllUnreadMessagesCount() + mUnreadMentionsCount;
        if (mAlertList != null) {
            unreadAlertCount = unreadAlertCount + mAlertList.size();
        }

        // no alerts will be added into mAlreadyReadNotificationCategory anymore
        if (mAlreadyReadNotificationCategory != null) {
            unreadAlertCount = unreadAlertCount - mAlreadyReadNotificationCategory.getAlertItemsSize();
        }

        int alreadyReadAlertCount = 0;
        if (mAlertList != null) {
            for (Alert alert : mAlertList) {
                if (AlertsDatastore.getInstance().checkWhetherAlertIsRead(alert)) {
                    ++alreadyReadAlertCount;
                }
            }
        }

        unreadAlertCount = unreadAlertCount - alreadyReadAlertCount;

        if (unreadAlertCount > 0) {
            if (unreadAlertCount > Constants.MAX_COUNT_DISPLAY_NOTIFICATIONS) {
                return String.format("%s (%s)", I18n.tr("Notifications"), Constants.MAX_COUNT_DISPLAY_NOTIFICATIONS + Constants.PLUSSTR);
            }
            return String.format("%s (%d)", I18n.tr("Notifications"), unreadAlertCount);
        } else {
            return I18n.tr("Notifications");
        }
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = super.getActionBarConfig();
        config.setNavigationButtonState(NavigationButtonState.BACK);
//        config.setShowOverflowButtonState(OverflowButtonState.POPUP);
        return config;
    }

    /*
     * @see
     * com.projectgoth.ui.fragment.BaseFragment#handleNotificationAvailable(
     * com.projectgoth.notification.StatusAlert.NotificationType,
     * java.lang.String)
     */
    @Override
    public void handleNotificationAvailable(NotificationType type, String notificationId) {
        if (type == NotificationType.MIG_ALERT_NOTIFICATION) {
            updateNotifications();
        } else {
            super.handleNotificationAvailable(type, notificationId);
        }
    }

    /*
     * @see com.projectgoth.ui.fragment.BaseFragment#updateNotifications()
     */
    @Override
    protected void updateNotifications() {
        ApplicationEx.getInstance().getNotificationHandler()
                .removeAllNotifications(NotificationType.MIG_ALERT_NOTIFICATION, true);
    }

    private void gotoGiftPage(Alert alert) {
        if (alert != null) {
            AlertsDatastore.getInstance().setAlertReadStatus(alert, true);
        }

        updatedCategoriesWhenRead();
        GAEvent.Profile_GiftList.send();
        ActionHandler.getInstance().displayMyGiftsCardListFragment(getActivity(), I18n.tr("Gifts"),
                GiftsDatastore.Category.ALL.ordinal(), false, Session.getInstance().getUserId());
    }

    @Override
    public void onItemClick(View v, Alert data) {
        if (data.getType() == AlertTypeEnum.VIRTUALGIFT_ALERT) {
            gotoGiftPage(data);
        } else {
            // Alert button handling are based on tags
            if (executeAlertAction(v, data)) {
                return;
            }
        }

        // If the view is not the button, handle based on viewId
        int viewId = v.getId();
        switch (viewId) {
            case R.id.picture:
                break;
            case R.id.container:
                break;
            default:
                break;
        }
    }

    private boolean executeAlertAction(View v, Alert data) {
        Integer actionId = (Integer) v.getTag(R.id.action_id);

        if (!mTextClickHandled) {
            if (actionId != null && actionId == R.id.action_execute_action) {
                Action action = (Action) v.getTag(R.id.action_alert_action);
                if (action != null) {
                    executeAlertActionInternal(action, data);
                    return true;
                }
            } else if (actionId != null && actionId == R.id.action_waiting_action) {
                createActionContextMenu(data);
                return true;

            }
        }
        mTextClickHandled = false;
        return false;
    }

    private void executeAlertActionInternal(Action action, Alert data) {

        sendGAEventsForAlertActions(action, data, ActionType.NORMAL);

        boolean didHandleAction = ActionHandler.getInstance().handleAction(action, getActivity(), data.getType());
        // Show a progress dialog if the action was successfully handled
        // and the action type is API.
        if (didHandleAction && action.getType() == ObjectTypeEnum.API) {
            ProgressDialogController.getInstance().showProgressDialog(getActivity(),
                    ProgressDialogController.ProgressType.Hangon);
        }

        AlertsDatastore.getInstance().setAlertReadStatus(data, true);
        updatedCategoriesWhenRead();
    }

    enum ActionType {
        ACCEPT, REJECT, NORMAL, GROUP, REFRESH, CHAT, MENTION
    }
    //Create a single function to handle GA
    private void sendGAEventsForAlertActions(Action action, Alert data, ActionType actionType) {
        if (actionType == ActionType.GROUP) {
            GAEvent.Notification_ClickGroupHeader.send();
        } else if (actionType == ActionType.CHAT) {
            GAEvent.Notification_ClickChatMessages.send();
        } else if (actionType == ActionType.REFRESH) {
            GAEvent.Notification_Refresh.send();
        } else if (actionType == ActionType.MENTION) {
            GAEvent.Notification_ViewMention.send();
        } else if (data.getType() == AlertTypeEnum.REPLY_TO_MIGBO_POST_ALERT) {
            GAEvent.Notification_ViewReply.send();
        } else if (data.getType() == AlertTypeEnum.MENTIONED_IN_MIGBO_POST_ALERT) {
            GAEvent.Notification_ViewMention.send();
        } else if (data.getType() == AlertTypeEnum.INCOMING_CREDIT_TRANSFER_ALERT) {
            GAEvent.Notification_ViewAccountCredits.send();
        } else if (data.getType() == AlertTypeEnum.MIGLEVEL_INCREASE_ALERT) {
            GAEvent.Notification_LevelUpMoreAbout.send();
        } else if (data.getType() == AlertTypeEnum.MUTUAL_FOLLOWING_ALERT) {
            if (data.getActions()[0] == action) {
                GAEvent.Notification_ChatNewFriends.send();
            } else {
                GAEvent.Notification_GiftNewFriend.send();
            }
        } else if (data.getType() == AlertTypeEnum.NEW_BADGE_ALERT) {
            GAEvent.Notification_ViewBadges.send();
        } else if (data.getType() == AlertTypeEnum.VIRTUALGIFT_ALERT) {
            if (data.getActions()[0] == action) {
                GAEvent.Notification_ViewGift.send();
            } else {
                GAEvent.Notification_GiftBack.send();
            }
        } else if (data.getType() == AlertTypeEnum.GROUP_INVITE) {
            if (actionType == ActionType.ACCEPT) {
                GAEvent.Notification_GroupInviteAccept.send();
            } else if (actionType == ActionType.REJECT) {
                GAEvent.Notification_GroupInviteReject.send();
            }
        } else if (data.getType() == AlertTypeEnum.GAME_INVITE) {
            if (actionType == ActionType.ACCEPT) {
                GAEvent.Notification_GameInviteAccept.send();
            } else if (actionType == ActionType.REJECT) {
                GAEvent.Notification_GameInviteReject.send();
            }
        } else if (data.getType() == AlertTypeEnum.FOLLOWING_REQUEST) {
            if (actionType == ActionType.ACCEPT) {
                GAEvent.Notification_FollowRequestAccept.send();
            } else if (actionType == ActionType.REJECT) {
                GAEvent.Notification_FollowRequestReject.send();
            }
        }
    }

    private void updatedCategoriesWhenRead() {
        //reset adapter here, somehow it duplicated entry if remove all data from one category to another category, need to verify
        long[] expandedIds = getExpandedIds();
        rememberScrollPosition();
        mExpandableListView.setAdapter(createAdapter());
        refreshAllCategories(false);
        restoreExpandedState(expandedIds);
        mExpandableListView.setSelectionFromTop(mListPosition, mItemPosition);
    }

    private void rememberScrollPosition() {
        // Save position of first visible item
        mListPosition = mExpandableListView.getFirstVisiblePosition();
        state.putInt(LIST_POSITION_KEY, mListPosition);

        // Save scroll position of item
        View itemView = mExpandableListView.getChildAt(0);
        mItemPosition = itemView == null ? 0 : itemView.getTop();
        state.putInt(ITEM_POSITION_KEY, mItemPosition);
    }

    private void restoreExpandedState(long[] expandedIds) {
        if (expandedIds != null) {
            for (int i = 0; i < mAdapter.getGroupCount(); i++) {
                long id = mAdapter.getGroupId(i);
                if (ArrayUtils.inArray(expandedIds, id)) {
                    mExpandableListView.expandGroup(i);
                }
            }
        }
        // would not happen anymore
        if (mNotificationCategoryList != null && mNotificationCategoryList.size() > 0 &&
                mNotificationCategoryList.get(mAdapter.getGroupCount() - 1).getNotificationTypeEnum() == NotificationTypeEnum.ALREADY_READ) {
            mExpandableListView.expandGroup(mAdapter.getGroupCount() - 1);
            if (mAdapter.getGroupCount() == 1) {
                mExpandableListView.setPinnedHeaderView(null);
            } else {
                mExpandableListView.setPinnedHeaderView(headerView);
            }
        }

    }

    private long[] getExpandedIds() {
        ArrayList<Long> expandedIds = new ArrayList<Long>();
        for (int i = 0; i < mAdapter.getGroupCount(); i++) {
            if (mExpandableListView.isGroupExpanded(i)) {
                expandedIds.add(mAdapter.getGroupId(i));
            }
        }
        return ArrayUtils.toLongArray(expandedIds);
    }

    @Override
    public void onItemLongClick(View v, Alert data) {
        createReadContextMenu(data);
    }

    private void createActionContextMenu(Alert data) {
        final List<ContextMenuItem> menuItemList = generateActionMenuItems(data);
        if (menuItemList != null && !menuItemList.isEmpty()) {
            Tools.showContextMenu(Constants.BLANKSTR, menuItemList, this);
        }
    }

    private List<ContextMenuItem> generateActionMenuItems(final Alert alert) {
        List<ContextMenuItem> menuItems = new ArrayList<ContextMenuItem>();

        if (alert != null) {
            menuItems.add(new ContextMenuItem(I18n.tr("Accept"), R.id.action_accept, alert));
            menuItems.add(new ContextMenuItem(I18n.tr("Reject"), R.id.action_reject, alert));
        }

        return menuItems;
    }

    private void createReadContextMenu(Alert data) {
        final List<ContextMenuItem> menuItemList = generateMenuItems(data);
        if (menuItemList != null && !menuItemList.isEmpty()) {
            Tools.showContextMenu(Constants.BLANKSTR, menuItemList, this);
        }
    }

    // used for listView item long click
    private List<ContextMenuItem> generateMenuItems(final Alert alert) {
        List<ContextMenuItem> menuItems = new ArrayList<ContextMenuItem>();

        if (alert != null) {
            if (AlertsDatastore.getInstance().checkWhetherAlertIsRead(alert)) {
                menuItems.add(new ContextMenuItem(I18n.tr("Mark as unread"), R.id.action_mark_unread, alert));
            } else {
                menuItems.add(new ContextMenuItem(I18n.tr("Mark as read"), R.id.action_mark_read, alert));
            }
        }

        return menuItems;
    }

    @Override
    public void onContextMenuItemClick(ContextMenuItem menuItem) {
        Alert alert;
        switch (menuItem.getId()) {
            case R.id.action_mark_read:
                AlertsDatastore.getInstance().setAlertReadStatus(((Alert) menuItem.getData()), true);
                break;
            case R.id.action_mark_unread:
                AlertsDatastore.getInstance().setAlertReadStatus(((Alert) menuItem.getData()), false);
                break;
            case R.id.action_accept:
                alert = (Alert) menuItem.getData();
                sendGAEventsForAlertActions(null, alert, ActionType.ACCEPT);
                executeAlertActionInternal(((Alert) menuItem.getData()).getActions()[0], alert);
                mRequestProfile = getUserNameFromVariable(alert.getVariables());
                break;
            case R.id.action_reject:
                alert = (Alert) menuItem.getData();
                sendGAEventsForAlertActions(null, alert, ActionType.REJECT);
                executeAlertActionInternal(((Alert) menuItem.getData()).getActions()[1], (Alert) menuItem.getData());
                break;
        }
        updatedCategoriesWhenRead();
    }

    /**
     * @return emptyView
     */
    private View createEmptyView() {
        String intro, finalHint;
        SpannableString spannable;

        emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_view_text, null);

        intro = I18n.tr("Check here for notifications on new fans, messages, gifts, or replies to your posts.");
        finalHint = Constants.BLANKSTR;
        spannable = new SpannableString(finalHint);

        TextView emptyTitle = (TextView) emptyView.findViewById(R.id.empty_text_title);
        emptyTitle.setText(intro);

        TextView emptyHint = (TextView) emptyView.findViewById(R.id.empty_text_hint);
        emptyHint.setMovementMethod(LinkMovementMethod.getInstance());
        emptyHint.setText(spannable);

        if (TextUtils.isEmpty(intro))
            emptyTitle.setVisibility(View.GONE);
        if (TextUtils.isEmpty(finalHint))
            emptyHint.setVisibility(View.GONE);

        return emptyView;
    }

    @Override
    public void onGroupFooterClick(View v, NotificationCategory data) {
        Logger.debug.log("footer", "onGroupFooterClick: ", data);
        if (data.getNotificationTypeEnum() == NotificationTypeEnum.NEW_GIFTS) {
            gotoProfileGiftPage(data.getAlertList());
        } else {
            readAllAlert(data.getAlertList());
        }
    }

    private void refreshAllCategories(final boolean shouldForceFetch) {
        if (mAdapter != null) {
            // set data to adapter
            mAdapter.setFullNotificationCategories(getNotificationCategoryList(shouldForceFetch));

            if (isLastGroupAlreadyReadGroup()) {
                mExpandableListView.expandGroup(mAdapter.getGroupCount() - 1);
            }

            // reset the empty view
            if (mAdapter.getGroupCount() == 0) {
                emptyView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.GONE);
            }
        }
    }

    private boolean isLastGroupAlreadyReadGroup() {
        if (mExpandableListView == null || mAdapter == null) {
            return false;
        } else {
            int groupCount = mAdapter.getGroupCount();

            if (groupCount > 0) {
                NotificationTypeEnum notificationTypeEnum = mNotificationCategoryList.get(groupCount - 1).getNotificationTypeEnum();
                return notificationTypeEnum == NotificationTypeEnum.ALREADY_READ;
            }
            return false;
        }
    }

    private String getUserNameFromVariable(Variable vars[]) {
        for (Variable var : vars) {
            VariableLabel varLabel = var.getLabel();
            if (varLabel != null) {
                String label = varLabel.getText();
                if (label != null && label.length() > 0) {
                    return label;
                }
            }
        }
        return "";
    }

    private ArrayList<NotificationCategory> getNotificationCategoryList(boolean shouldForceFetch) {

        mShowRefreshIndicator = shouldForceFetch;
        HashMap<NotificationTypeEnum, ArrayList<Alert>> hashMap = new HashMap<NotificationTypeEnum, ArrayList<Alert>>();
        mNotificationCategoryList = new ArrayList<NotificationCategory>();

        // group the different type of alerts from the alert list
        mAlertList = AlertsDatastore.getInstance().getAllAlerts(shouldForceFetch);

        short i = 0;
        for (Alert alert : mAlertList) {
            if (alert.getType() == null) {
                //alert.getType may return null, it would cause some troubles in NotificationCategory.getNotificationTypeEnum
                CrashlyticsLog.log(new IllegalAccessException(), "alert type is null");
            } else {
                NotificationTypeEnum type = NotificationCategory.getNotificationTypeEnum(alert);
                ArrayList<Alert> alerts = hashMap.get(type);
                if (alerts == null) {
                    alerts = new ArrayList<Alert>();
                    alerts.add(alert);
                    hashMap.put(type, alerts);

                    // create the new category
                    NotificationCategory category = new NotificationCategory(type.toString(), i);
                    i++;
                    //category.setCategoryFooterLabel(I18n.tr("Mark this group as read"));
                    category.setNotificationTypeEnum(type);
                    category.setAlertList(alerts);

                    // add it in list, always add already read on top, we will reverse it later
                    if (type == NotificationTypeEnum.ALREADY_READ) {
                        mNotificationCategoryList.add(0, category);
                    } else {
                        if (type == NotificationTypeEnum.NEW_GIFTS) {
                            category.setCategoryFooterLabel(I18n.tr("View all gifts"));
                        }
                        mNotificationCategoryList.add(category);
                    }

                } else {
                    alerts.add(alert);
                }
            }
        }
        // if have already read, shift to last
        if (mNotificationCategoryList.size() > 0 && mNotificationCategoryList.get(0).getNotificationTypeEnum() == NotificationTypeEnum.ALREADY_READ) {
            mAlreadyReadNotificationCategory = mNotificationCategoryList.remove(0);
            mNotificationCategoryList.add(mAlreadyReadNotificationCategory);
        }

        // insert the mention notification
        if (mUnreadMentionsCount > 0) {
            NotificationCategory mentionCategory = new NotificationCategory();
            mentionCategory.setNotificationTypeEnum(NotificationTypeEnum.NEW_MENTIONS);
            mentionCategory.setCounterOverwrite(mUnreadMentionsCount);
            mNotificationCategoryList.add(0, mentionCategory);
        }

        // insert the chat notification
        if (ApplicationEx.getInstance().getNotificationHandler().getUnreadConversationsCount() > 0) {
            NotificationCategory chatNotification = new NotificationCategory();
            chatNotification.setNotificationTypeEnum(NotificationTypeEnum.NEW_MESSAGES);
            chatNotification.setChatMessageNotification(true);
            mNotificationCategoryList.add(0, chatNotification);
        }

        setTitle(createNewTitle());

        return mNotificationCategoryList;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        super.onScrollStateChanged(view, scrollState);
    }

    // implementation of OnGroupClickListener
    // used for click on the titles of "New people", "New gifts", "New on feed" and "Others"
    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

        sendGAEventsForAlertActions(null, null, ActionType.GROUP);

        NotificationCategory clickedNotificationCategory = (NotificationCategory) v.findViewById(R.id.notification_group_layout).getTag();
        if (mClickedNotificationCategoryList == null) {
            mClickedNotificationCategoryList = new ArrayList<NotificationCategory>();
        }
        if (clickedNotificationCategory != null) {
            mClickedNotificationCategoryList.add(clickedNotificationCategory);
        }
        return false;
    }

    // implementation of NotificationCategoryListener
    // used for click on the titles of "New messages" and "New mentions"
    @Override
    public void onTitleClicked(NotificationCategory notificationCategory) {
        if (notificationCategory.getNotificationTypeEnum() == NotificationTypeEnum.NEW_MESSAGES) {
            sendGAEventsForAlertActions(null, null, ActionType.CHAT);
            getActivity().finish();
            ActionHandler.getInstance().goToMyChats();
        }

        if (notificationCategory.getNotificationTypeEnum() == NotificationTypeEnum.NEW_MENTIONS) {
            sendGAEventsForAlertActions(null, null, ActionType.MENTION);
            mUnreadMentionsCount = 0;
            ApplicationEx.getInstance().getNotificationHandler().resetUnreadMentionCount();
            ActionHandler.getInstance().displayMentions(getActivity());
            refreshAllCategories(false);
        }
    }

    @Override
    public ArrayList<MenuOption> getMenuOptions() {
        ArrayList<MenuOption> menuItems = new ArrayList<MenuOption>();

        MenuOption readAll = new MenuOption(I18n.tr("Read all"), new MenuOption.MenuAction() {

            @Override
            public void onAction(MenuOption option, boolean isSelected) {
                readAllAlert(mAlertList);
            }
        });
        readAll.setIcon(Tools.getBitmap(R.drawable.ad_read_white));

        menuItems.add(readAll);

        return menuItems;
    }

    private void readAllAlert(List<Alert> alertList) {
        if (alertList == null) {
            return;
        } else {
            for (Alert alert : alertList) {
                AlertsDatastore.getInstance().setAlertReadStatus(alert, true);
            }
            updatedCategoriesWhenRead();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //readClickedCategoryAlerts();
    }

    private void readClickedCategoryAlerts() {
        if (mClickedNotificationCategoryList != null) {
            for (NotificationCategory notificationCategory : mClickedNotificationCategoryList) {
                readAllAlert(notificationCategory.getAlertList());
            }
        }
        mClickedNotificationCategoryList = null;
    }

    private void gotoProfileGiftPage(List<Alert> alerts) {
        // Read all gifts
        if (alerts != null) {
            for (Alert alert : alerts) {
                AlertsDatastore.getInstance().setAlertReadStatus(alert, true);
            }
        }
        updatedCategoriesWhenRead();

        GAEvent.Profile_GiftList.send();

        if (!Config.getInstance().isMyGiftsEnabled()) {
            String username = Session.getInstance().getUsername();
            Profile profile = UserDatastore.getInstance().getProfileWithUsername(username, false);
            int numGift = 0;

            if (profile != null) {
                numGift = profile.getNumOfGiftsReceived();
            }
            ActionHandler.getInstance().displayBrowser(getActivity(),
                    String.format(WebURL.URL_GIFTS_RECEIVED, username),
                    String.format(I18n.tr("Gifts (%d)"), numGift), R.drawable.ad_gift_white);
        } else {
            ActionHandler.getInstance().displayMyGifts(getActivity(), Session.getInstance().getUserId());
        }
    }

    @Override
    public void onBackIconPressed() {
        if (mClickedNotificationCategoryList != null) {
            for (NotificationCategory notificationCategory : mClickedNotificationCategoryList) {
                List<Alert> alertList = notificationCategory.getAlertList();
                readAllAlert(alertList);
            }
        }
        updatedCategoriesWhenRead();

        final FragmentActivity activity = getActivity();
        if (activity != null) {
            final FragmentManager manager = activity.getSupportFragmentManager();
            
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_top);
            transaction.detach(this).commit();
            
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (manager.findFragmentByTag(getTag()) != null) {
                        activity.onBackPressed();
                    }
                }
            }, FRAGMENT_CLOSE_DELAY);
        }
    }
    
    private void setupOnBackPressListener() {
        final Activity activity = getActivity();
        
        if (activity instanceof CustomPopupActivity) {
            ((CustomPopupActivity) activity).setOnBackPresslistener(new CustomPopupActivity.OnBackPressListener() {

                @Override
                public boolean onBackPress() {
                    onBackIconPressed();
                    return true;
                }
            });
        }
    }
}
