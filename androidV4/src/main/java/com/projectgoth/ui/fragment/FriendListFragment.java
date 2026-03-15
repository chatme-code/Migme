
package com.projectgoth.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Config;
import com.projectgoth.common.Constants;
import com.projectgoth.common.TextUtils;
import com.projectgoth.common.Tools;
import com.projectgoth.common.WebURL;
import com.projectgoth.controller.ThirdPartyIMController;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.model.ContextMenuItem;
import com.projectgoth.nemesis.model.ContactGroup;
import com.projectgoth.nemesis.model.Friend;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.AlertHandler;
import com.projectgoth.ui.activity.AlertHandler.TextInputListener;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.OverflowButtonState;
import com.projectgoth.ui.adapter.FriendListAdapter;
import com.projectgoth.ui.adapter.GroupedFriendListAdapter;
import com.projectgoth.ui.fragment.GlobalSearchFragment.SearchType;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.BasicListFooterViewHolder;
import com.projectgoth.ui.holder.ContactGroupViewHolder.ContactGroupClickListener;
import com.projectgoth.ui.listener.ContextMenuItemListener;
import com.projectgoth.ui.widget.GifImageView;
import com.projectgoth.ui.widget.PinnedHeaderExpandableListView;

import java.util.List;

/**
 * @author dangui
 * 
 */
public class FriendListFragment extends BaseSearchFragment implements BaseViewListener<Friend>,
        ContactGroupClickListener, ContextMenuItemListener, OnClickListener {

    public static final String             PARAM_ENABLE_GROUPED_CONTACTS  = "PARAM_ENABLE_GROUPED_CONTACTS";
    public static final String             PARAM_INITIAL_SELECTED_USERS   = "PARAM_INITIAL_SELECTED_USERS";
    public static final String             PARAM_LIST_ITEM_ACTION_TYPE    = "PARAM_LIST_ITEM_ACTION_TYPE";
    public static final String             PARAM_SHOW_ONLINE_FRIENDS_ONLY = "PARAM_SHOW_ONLINE_FRIENDS_ONLY";

    private FrameLayout                    container;
    private ListView                       friendListView;
    private PinnedHeaderExpandableListView groupedFriendListView;
    private View                           emptyView;
    private View                           footerView;

    private FriendListAdapter              mListAdapter;
    private GroupedFriendListAdapter       mGroupedListAdapter;

    private FriendsListListener            friendsListListener;
    private ContactGroupListener           contactGroupListener;
    private CheckboxChangeListener         checkboxChangeListener;
    private CustomFriendListHeaderProvider headerProvider;

    private boolean                        isExpandable;
    private boolean                        showOnlineFriendsOnly          = false;
    private boolean                        isFusionFriendsGrouped         = true;
    private boolean                        showSMSContacts                = false;

    private FriendListItemActionType       friendListItemActionType;
    private List<String>                   preselectedUsers;

    private String                         filterKeyword                  = Constants.BLANKSTR;

    public interface FriendsListListener {

        public void onFriendItemClicked(View v, Friend friend);

        public void onFriendItemLongPressed(View v, Friend friend);
    }

    public interface ContactGroupListener {

        public void onContactGroupItemLongPressed(ContactGroup contactGroup);

        public void onContactGroupItemToggle(ContactGroup contactGroup);
    }

    public interface CustomFriendListHeaderProvider {

        public View getCustomHeader();
    }

    public interface CheckboxChangeListener {

        public void onCheckboxClicked(View v, Friend friend);
    }

    public enum FriendListItemActionType {
        DEFAULT(0), POPUPMENU(1), CHECKBOX(2);

        private int type;

        private FriendListItemActionType(int type) {
            this.type = type;
        }

        public int getType() {
            return this.type;
        }

        public static FriendListItemActionType fromValue(int type) {
            for (FriendListItemActionType listType : values()) {
                if (listType.getType() == type) {
                    return listType;
                }
            }
            return DEFAULT;
        }
    }

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        isExpandable = args.getBoolean(PARAM_ENABLE_GROUPED_CONTACTS);
        preselectedUsers = args.getStringArrayList(PARAM_INITIAL_SELECTED_USERS);
        friendListItemActionType = FriendListItemActionType.fromValue(args.getInt(PARAM_LIST_ITEM_ACTION_TYPE));
        showOnlineFriendsOnly = args.getBoolean(PARAM_SHOW_ONLINE_FRIENDS_ONLY, false);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_friend_list;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        container = (FrameLayout) view.findViewById(R.id.container);

        friendListView = (ListView) view.findViewById(R.id.friend_list);
        groupedFriendListView = (PinnedHeaderExpandableListView) view.findViewById(R.id.expandable_friend_list);

        LayoutInflater inflater = LayoutInflater.from(ApplicationEx.getContext());
        footerView = inflater.inflate(R.layout.holder_list_footer, null);

        BasicListFooterViewHolder footerViewHolder = new BasicListFooterViewHolder(footerView);
        footerView.setTag(R.id.holder_footer, footerViewHolder);

        footerViewHolder.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Tools.hideVirtualKeyboard(getActivity())) {
                    // If software keyboard showing just hide it and do nothing
                    return;
                }

                ActionHandler.getInstance().displayGlobalSearch(getActivity(), SearchType.PEOPLE, filterKeyword);
            }
        });
        footerView.setVisibility(View.GONE);
        groupedFriendListView.addFooterView(footerView);

        mListAdapter = new FriendListAdapter();
        mGroupedListAdapter = new GroupedFriendListAdapter();
        mGroupedListAdapter.setFriendListGrouped(isFusionFriendsGrouped);

        if (isExpandable) {
            friendListView.setVisibility(View.GONE);
            groupedFriendListView.setVisibility(View.VISIBLE);

            if (headerProvider != null) {
                groupedFriendListView.addHeaderView(headerProvider.getCustomHeader());
                groupedFriendListView.setHeaderDividersEnabled(true);
            }

            mGroupedListAdapter.setFriendClickListener(this);
            mGroupedListAdapter.setOnGroupClickListener(this);
            mGroupedListAdapter.setFriendListItemActionType(friendListItemActionType);

            if (emptyView != null) {
                emptyView.setVisibility(View.GONE);
                if (emptyView.getParent() == null) {
                    ((ViewGroup) groupedFriendListView.getParent()).addView(emptyView);
                }
                groupedFriendListView.setEmptyView(emptyView);
            }
            groupedFriendListView.setAdapter(mGroupedListAdapter);

            View headerView = mGroupedListAdapter.getGroupView(0, true, null, container);
            groupedFriendListView.setPinnedHeaderView(headerView);
            groupedFriendListView.setOnScrollListener(this);

        } else {
            friendListView.setVisibility(View.VISIBLE);
            groupedFriendListView.setVisibility(View.GONE);

            friendListView.setOnScrollListener(this);

            if (headerProvider != null) {
                friendListView.addHeaderView(headerProvider.getCustomHeader());
                friendListView.setHeaderDividersEnabled(true);
            }

            mListAdapter.setFriendClickListener(this);
            mListAdapter.setFriendListItemActionType(friendListItemActionType);

            if (emptyView != null) {
                emptyView.setVisibility(View.GONE);
                if (emptyView.getParent() == null) {
                    ((ViewGroup) friendListView.getParent()).addView(emptyView);
                }
                friendListView.setEmptyView(emptyView);
            }
            friendListView.setAdapter(mListAdapter);
        }

        updateFriendsData();

        if (isExpandable) {
            expandDefaultGroup();
        }
    }

    private void updateFriendsData() {
        if (isExpandable) {
            List<ContactGroup> groupList = UserDatastore.getInstance().getContactGroups(Config.getInstance().isImEnabled(), isFusionFriendsGrouped);
            mGroupedListAdapter.setGroupList(groupList);
            SparseArray<List<Friend>> groupedFriends = UserDatastore.getInstance().getGroupedFriends(
                    showOnlineFriendsOnly, showSMSContacts, isFusionFriendsGrouped);
            mGroupedListAdapter.setGroupedFriendsList(groupedFriends);

        } else {
            List<Friend> friendList = UserDatastore.getInstance().getAllFusionFriends(showOnlineFriendsOnly,
                    showSMSContacts);

            // remove preselectedUsers if parameter is not null
            // this is used by StartChatFragment
            if (preselectedUsers != null) {
                for (String username : preselectedUsers) {
                    Friend friend = UserDatastore.getInstance().findMig33User(username);
                    if (friend != null) {
                        friendList.remove(friend);
                    }
                }
            }

            mListAdapter.setFriendList(friendList);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        setShouldUpdateActionBarOnAttach(false);
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateFriendsData();
        refreshFriendList();

        showOrHideEmptyViewIfNeeded();

        if (!TextUtils.isEmpty(filterKeyword)) {
            performFilter(filterKeyword);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Tools.hideVirtualKeyboard(ApplicationEx.getInstance().getCurrentActivity());
    }

    @Override
    public void onItemClick(View v, Friend data) {
        if (checkboxChangeListener != null) {
            checkboxChangeListener.onCheckboxClicked(v, data);
        }

        if (friendsListListener != null) {
            friendsListListener.onFriendItemClicked(v, data);
        }
    }

    @Override
    public void onItemLongClick(View v, Friend data) {
        if (friendsListListener != null && !data.isIMContact()) {
            friendsListListener.onFriendItemLongPressed(v, data);
        }
    }

    void notifyDataSetChanged(boolean reloadData) {
        if (reloadData) {
            updateFriendsData();
            showOrHideEmptyViewIfNeeded();
        }

        refreshFriendList();
    }

    private void showOrHideEmptyViewIfNeeded() {
        if (isFriendListEmpty()) {
            emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_view_friend_list, null);
            setupEmptyView(emptyView);

            // add the empty view
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            container.addView(emptyView, params);

        } else {
            // hide emptyView
            if (emptyView != null && emptyView.getParent() != null) {
                ((ViewGroup) emptyView.getParent()).removeView(emptyView);
            }
        }
    }
    
    private void setupEmptyView(View emptyView) {
        GifImageView gifView = (GifImageView) emptyView.findViewById(R.id.empty_chat_list_icon);
        gifView.setOnClickListener(this);
        gifView.setCyclePlay(false);
        gifView.setGifId(R.drawable.bubble_burst);
        gifView.startAnimation();

        TextView hint = (TextView) emptyView.findViewById(R.id.hint);
        hint.setText(I18n.tr("New here?"));

        TextView hint2 = (TextView) emptyView.findViewById(R.id.hint2);
        String finalText = I18n.tr("Start by making friends.");
        String link = I18n.tr("making friends");
        SpannableString spannableStr = new SpannableString(finalText);
        UIUtils.setLinkSpan(spannableStr, finalText, link, new UIUtils.LinkClickListener() {

            @Override
            public void onClick() {
                ActionHandler.getInstance().displayInviteFriends(getActivity());
            }
        });
        hint2.setMovementMethod(LinkMovementMethod.getInstance());
        hint2.setText(spannableStr);
    }

    private boolean isFriendListEmpty() {
        if (isExpandable) {
            if (mGroupedListAdapter.getGroupCount() == 0) {
                return true;
            }
        } else {
            if (mListAdapter.getCount() == 0) {
                return true;
            }
        }
        return false;
    }

    public void filterAndRefresh(String filterKeyword) {
        if (isExpandable) {
            mGroupedListAdapter.filterAndRefresh(filterKeyword);
        } else {
            mListAdapter.filterAndRefresh(filterKeyword);
        }
    }

    private void refreshFriendList() {
        if (isExpandable) {
            mGroupedListAdapter.notifyDataSetChanged();
        } else {
            mListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * case 1: a friend's presence changed case 2: a friend's status message
     * changed case 3: a friend's display picture changed case 4: a new friend
     * came case 5: removed a friend case 6: create a group case 7: remove a
     * group case 8: IM status changed
     * 
     * 
     */
    // TODO create a group; rename group; delete group; move friend; update
    // contact
    @Override
    protected void registerReceivers() {
        registerEvent(Events.Contact.PRESENCE_CHANGED);
        registerEvent(Events.Contact.DISPLAY_PICTURE_CHANGED);
        registerEvent(Events.Contact.STATUSMESSAGE_CHANGED);
        registerEvent(Events.Contact.FETCH_ALL_COMPLETED);
        registerEvent(Events.Contact.REMOVED);
        registerEvent(Events.Contact.RECEIVED);
        registerEvent(Events.ContactGroup.RECEIVED);
        registerEvent(Events.ContactGroup.REMOVED);
        registerEvent(Events.Contact.IM_STATUS_CHANGED);
        registerEvent(Events.Contact.FETCH_IM_ICONS_COMPLETED);
        registerEvent(Events.Profile.RECEIVED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Events.Contact.PRESENCE_CHANGED)) {
            Bundle data = intent.getExtras();
            if (data != null) {
                int groupId = data.getInt(Events.ContactGroup.Extra.ID);
                // TODO the onlineFriendOnly should be setup when we add the
                // show online friend only option
                reorderFriendListOfGroup(groupId, showOnlineFriendsOnly, showSMSContacts, isFusionFriendsGrouped);
            }
            notifyDataSetChanged(false);
        } else if (intent.getAction().equals(Events.Contact.FETCH_ALL_COMPLETED)) {
            notifyDataSetChanged(true);
            if (isExpandable) {
                expandDefaultGroup();
            }
        } else if (intent.getAction().equals(Events.Contact.REMOVED)
                || intent.getAction().equals(Events.Contact.RECEIVED)
                || intent.getAction().equals(Events.ContactGroup.RECEIVED)
                || intent.getAction().equals(Events.ContactGroup.REMOVED)
                || intent.getAction().equals(Events.Contact.IM_STATUS_CHANGED)) {
            notifyDataSetChanged(true);
        } else if (intent.getAction().equals(Events.Contact.DISPLAY_PICTURE_CHANGED)
                || intent.getAction().equals(Events.Contact.STATUSMESSAGE_CHANGED)
                || intent.getAction().equals(Events.Contact.FETCH_IM_ICONS_COMPLETED)
                || intent.getAction().equals(Events.Profile.RECEIVED)) {
            notifyDataSetChanged(false);
        }
    }

    private void reorderFriendListOfGroup(int groupId, boolean showOnlineFriendsOnly, boolean showSMSContacts,
            boolean isFusionFriendsGrouped) {
        if (isExpandable) {
            mGroupedListAdapter.reorderGroup(groupId, showOnlineFriendsOnly, showSMSContacts, isFusionFriendsGrouped);
        } else {
            updateFriendsData();
        }
    }

    @Override
    public void performFilter(final String filterString) {
        super.performFilter(filterString);
        filterKeyword = filterString;

        boolean hasFilter = !TextUtils.isEmpty(filterString);

        if (isFiltering() || hasFilter) {
            int newVisibility = hasFilter ? View.VISIBLE : View.GONE;
            if (newVisibility != footerView.getVisibility()) {
                footerView.setVisibility(newVisibility);
            }

            if (hasFilter) {
                // Update the footer
                String newLabel = String.format(I18n.tr("Find people for %s"), filterString);
                ((BasicListFooterViewHolder) footerView.getTag(R.id.holder_footer)).setLabel(newLabel);
            }
        }

        if (isExpandable) {
            mGroupedListAdapter.filterAndRefresh(filterString);
        } else if (mListAdapter != null) {
            mListAdapter.filterAndRefresh(filterString);
        }
    }

    @Override
    protected void onModeChanged(Mode newMode) {
        super.onModeChanged(newMode);

        if (newMode == Mode.FILTERING && emptyView != null) {
            emptyView.setVisibility(View.GONE);
            footerView.setVisibility(View.VISIBLE);
        } else {
            showOrHideEmptyViewIfNeeded();
        }
    }

    @Override
    protected void performGlobalSearch(final String searchString) {
        super.performGlobalSearch(searchString);
        ActionHandler.getInstance().displayGlobalSearch(getActivity(), SearchType.PEOPLE, searchString);
    }

    /**
     * expand the first group which has a child by default
     */
    protected void expandDefaultGroup() {
        boolean firstGroupExpanded = false;
        int groupCount = mGroupedListAdapter.getGroupCount();

        for (int pos = 0; pos < groupCount; pos++) {
            if (firstGroupExpanded) {
                groupedFriendListView.collapseGroup(pos);
                continue;
            }
            if (mGroupedListAdapter.getChildrenCount(pos) > 0) {
                groupedFriendListView.expandGroup(pos);
                firstGroupExpanded = true;
            }
        }
    }

    public boolean showOnlineFriendsOnly() {
        return showOnlineFriendsOnly;
    }

    public void setShowOnlineFriendsOnly(final boolean showOnlineFriendsOnly) {
        this.showOnlineFriendsOnly = showOnlineFriendsOnly;
    }

    public boolean isFusionFriendsGrouped() {
        return isFusionFriendsGrouped;
    }

    public void setFusionFriendsGrouped(boolean isFusionFriendsGrouped) {
        this.isFusionFriendsGrouped = isFusionFriendsGrouped;
        if (mGroupedListAdapter != null) {
            mGroupedListAdapter.setFriendListGrouped(isFusionFriendsGrouped);
        }
    }

    public boolean isShowSMSContacts() {
        return showSMSContacts;
    }

    public void setShowSMSContacts(boolean showSMSContacts) {
        this.showSMSContacts = showSMSContacts;
    }

    /**
     * @return the friendsListListener
     */
    public FriendsListListener getFriendsListListener() {
        return friendsListListener;
    }

    /**
     * @param friendsListListener
     *            the friendsListListener to set
     */
    public void setFriendsListListener(FriendsListListener friendsListListener) {
        this.friendsListListener = friendsListListener;
    }

    public void addFriendToList(Friend friend) {
        if (isExpandable) {
            mGroupedListAdapter.addFriendToList(friend);
        } else {
            mListAdapter.addFriendToList(friend);
        }
    }

    public void removeFriendFromList(Friend friend) {
        if (isExpandable) {
            mGroupedListAdapter.removeFriendFromList(friend);
        } else {
            mListAdapter.removeFriendFromList(friend);
        }
    }

    public void setHeaderProvider(CustomFriendListHeaderProvider headerProvider) {
        this.headerProvider = headerProvider;
    }

    public void disableCustomHeader(View header) {
        if (isExpandable) {
            groupedFriendListView.removeHeaderView(header);
        } else {
            friendListView.removeHeaderView(header);
        }
    }

    public void enableCustomHeader(View header) {
        if (isExpandable && groupedFriendListView.getHeaderViewsCount() == 0) {
            groupedFriendListView.addHeaderView(header);
            groupedFriendListView.setAdapter(mGroupedListAdapter);
            expandDefaultGroup();
        } else if (friendListView.getHeaderViewsCount() == 0) {
            friendListView.addHeaderView(header);
            friendListView.setAdapter(mListAdapter);
        }
    }

    public void setContactGroupListener(ContactGroupListener listener) {
        this.contactGroupListener = listener;
    }

    @Override
    public void onContactGroupClickListener(int groupPosition) {
        if (groupedFriendListView.isGroupExpanded(groupPosition)) {
            groupedFriendListView.collapseGroup(groupPosition);
        } else {
            groupedFriendListView.expandGroup(groupPosition);
        }
    }

    @Override
    public void onContactGroupLongPress(int groupPosition) {
        ContactGroup data = (ContactGroup) mGroupedListAdapter.getGroup(groupPosition);
        if (contactGroupListener != null) {
            contactGroupListener.onContactGroupItemLongPressed(data);
        }
    }

    public void onGroupToggleButtonListener(int groupPosition) {
        ContactGroup data = (ContactGroup) mGroupedListAdapter.getGroup(groupPosition);

        if (data.isIMGroup() && contactGroupListener != null) {
            Boolean isImOnline = ThirdPartyIMController.getInstance().isImOnline(data.getType().getImType());
            contactGroupListener.onContactGroupItemToggle(data);

            if (isImOnline) {
                groupedFriendListView.collapseGroup(groupPosition);
            } else {
                groupedFriendListView.expandGroup(groupPosition);
            }
        }
    }

    public void setCheckboxChangeListener(CheckboxChangeListener checkboxChangeListener) {
        this.checkboxChangeListener = checkboxChangeListener;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        if (view instanceof PinnedHeaderExpandableListView) {
            ((PinnedHeaderExpandableListView) view).configureHeaderView(firstVisibleItem);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        super.onScrollStateChanged(view, scrollState);
    }

    public void disableIMContactsSelection() {
        if (isExpandable) {
            mGroupedListAdapter.setIMContactsSelectable(false);
        }
    }

    public void enableIMContactsSelection() {
        if (isExpandable) {
            mGroupedListAdapter.setIMContactsSelectable(true);
        }
    }

    @Override
    protected String getTitle() {
        return I18n.tr("Friends");
    }

    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_userppl_white;
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = super.getActionBarConfig();
        config.setShowSearchButton(false);
        config.setShowOverflowButtonState(OverflowButtonState.ALERT);
        return config;
    }

    @Override
    public void updateTitle() {
        if (!getCurrentTitle().matches(getTitle())) {
            super.updateTitle();
            showTitleAnimation();
        }
    }

    @Override
    public void updateIcon() {
        if (getCurrentTitleIconTag() == 0 || getCurrentTitleIconTag() != getTitleIcon()) {
            super.updateIcon();
            showTitleIconAnimation();
        }
    }

    @Override
    public void onContextMenuItemClick(ContextMenuItem menuItem) {
        Friend friend;
        ContactGroup contactGroup;

        int id = menuItem.getId();
        switch (id) {
            case R.id.option_item_view_profile:
                friend = (Friend) menuItem.getData();
                ActionHandler.getInstance().displayProfile(getActivity(), friend.getUsername());
                break;
            case R.id.option_item_chat:
                friend = (Friend) menuItem.getData();
                ActionHandler.getInstance().displayPrivateChat(getActivity(), friend.getUsername());
                break;
            case R.id.option_item_send_gift:
                friend = (Friend) menuItem.getData();
                ActionHandler.getInstance().displayStore(getActivity(), friend.getUsername());
                break;
            case R.id.option_item_report_abuse:
                friend = (Friend) menuItem.getData();
                final String urlReport = String.format(WebURL.URL_REPORT_USER, friend.getUsername());
                ActionHandler.getInstance().displayBrowser(getActivity(), urlReport);
                break;
            case R.id.option_item_move_to_group:
                friend = (Friend) menuItem.getData();
                ActionHandler.getInstance().moveFriend(getActivity(), friend.getContactID(), friend.getGroupID());
                break;
            case R.id.option_item_block:
                friend = (Friend) menuItem.getData();
                ActionHandler.getInstance().blockFriend(getActivity(), null, friend.getDisplayName(),
                        friend.getUsername());
                break;
            case R.id.option_item_remove_friend:
                friend = (Friend) menuItem.getData();
                ActionHandler.getInstance().removeFriend(getActivity(), friend.getDisplayName(), friend.getContactID());
                break;
            case R.id.option_item_add_contact_group:
                AlertHandler.showTextInputDialog(getActivity(), new TextInputListener() {
                    @Override
                    public void onOk(String groupName) {
                        UserDatastore.getInstance().requestAddContactGroup(groupName);
                    }

                    @Override
                    public void onCancel() {
                    }
                }, I18n.tr("Create contact group")).show();
                break;
            case R.id.option_item_rename_contact_group:
            {
                contactGroup = (ContactGroup) menuItem.getData();
                final int groupId = contactGroup.getGroupID();
                String title = String.format(I18n.tr("Rename contact group %s"), contactGroup.getGroupName());
                AlertHandler.showTextInputDialog(getActivity(), new TextInputListener() {
                    @Override
                    public void onOk(String newGroupName) {
                        UserDatastore.getInstance().requestUpdateContactGroup(groupId, newGroupName);
                    }

                    @Override
                    public void onCancel() {
                    }
                }, title).show();
                break;
            }
            case R.id.option_item_remove_contact_group:
            {
                contactGroup = (ContactGroup) menuItem.getData();
                final int groupId = contactGroup.getGroupID();
                ActionHandler.getInstance().removeContactGroup(getActivity(), groupId, contactGroup.getGroupName());
                break;
            }
            default:
                break;
        }
    }
    
    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        switch (viewId) {
            case R.id.empty_chat_list_icon:
                GifImageView gifView = (GifImageView) view;

                if (!gifView.isAnimating()) {
                    gifView.startAnimation();
                }
                break;
        }
    }

}
