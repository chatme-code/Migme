/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatroomListFragment.java
 * Created Jun 6, 2013, 11:15:49 AM
 */

package com.projectgoth.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.AbsListView;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.common.Logger;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.datastore.ChatDatastore.ChatNotification;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.nemesis.model.ChatRoomCategory;
import com.projectgoth.nemesis.model.ChatRoomItem;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.OverflowButtonState;
import com.projectgoth.ui.adapter.ChatroomListAdapter;
import com.projectgoth.ui.adapter.ChatroomListAdapter.CategoryHideMode;
import com.projectgoth.ui.adapter.ChatroomListAdapter.FooterClickListener;
import com.projectgoth.ui.adapter.ChatroomListAdapter.FooterType;
import com.projectgoth.ui.fragment.GlobalSearchFragment.SearchType;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.ChatroomCategoryViewHolder.ChatroomCategoryListener;

/**
 * @author mapet
 * 
 */
public class ChatroomListFragment extends ExpandableListFragment<ChatroomListAdapter> implements
        BaseViewListener<ChatRoomItem>, OnClickListener, ChatroomCategoryListener, FooterClickListener {

    private static final String  LOG_TAG = AndroidLogger.makeLogTag(ChatroomListFragment.class);

    private TextView            mChatNotificationText;
    private ChatNotification    mChatNotificationData;
    private ViewStub            mFirstTimeLoadingStub;
    private View                mInflatedStubView;
    private boolean             mIsStubInflated = false;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chatroom_list;
    }

    /**
     * Checks whether pull to refresh is enabled for this fragment.
     * 
     * @return true if enabled and false otherwise.
     */
    @Override
    protected boolean isPullToRefreshEnabled() {
        return isFiltering() ? false : mIsPullToRefreshEnabled; 
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        refreshAllCategories(false);
        expandDefaultGroup();
        
        invokeOnViewCreated();

        mFirstTimeLoadingStub = (ViewStub) view.findViewById(R.id.loading_layout_stub);
        checkWhetherShowFirstTimeLoadingView();

    }

    @Override 
    public void addListViewHeader() {
        if (!isFiltering()) {
            final View headerView = LayoutInflater.from(ApplicationEx.getContext()).inflate(R.layout.chat_list_chat_notification, null);
            mChatNotificationText = (TextView) headerView.findViewById(R.id.chat_notification);
            mExpandableListView.addHeaderView(headerView);
            
            mChatNotificationText.setOnClickListener(this);
            refreshChatNotification();
        }
    }
    
    private void showListViewHeader(final boolean shouldShow) {
        if (mChatNotificationText != null) {
            mChatNotificationText.setVisibility((shouldShow) ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void updateTitle()
    {
        if(!getCurrentTitle().matches(getTitle())) {
            super.updateTitle();
            showTitleAnimation();
        }
    }

    @Override
    public  void updateIcon() {
        if(getCurrentTitleIconTag() == 0 || getCurrentTitleIconTag() != getTitleIcon()) {
            super.updateIcon();
            showTitleIconAnimation();
        }
    }
    
    /**
     * @see ExpandableListFragment#createAdapter()
     */
    @Override
    protected ChatroomListAdapter createAdapter() {
        ChatroomListAdapter adapter = new ChatroomListAdapter();
        adapter.setChatItemListener(this);
        adapter.setChatroomCategoryListener(this);
        adapter.setFooterListener(this);
        updateListAdapter(adapter);
        return adapter;
    }

    @Override
    public void onResume() {
        super.onResume();
        expandAllGroups();
    }

    @Override
    public void onPause() {
        super.onPause();
        Tools.hideVirtualKeyboard(getActivity());
    }

    @Override
    public void onItemClick(View v, ChatRoomItem data) {
        if (Tools.hideVirtualKeyboard(getActivity())) {
            //If software keyboard showing just hide it and do nothing
            return;
        }
        Logger.debug.log(LOG_TAG, "onItemClicked: ", v.getId(), " chatItem: ", data.getName());
        if (isFiltering()) GAEvent.Chat_ClickChatroomFilterResult.send();
        ActionHandler.getInstance().displayPublicChat(getActivity(), data.getName(), data.getGroupId());
    }

    private void refreshAllCategories(final boolean shouldForceFetch) {
        if (mAdapter != null) {
            mAdapter.setChatroomCategories(ChatDatastore.getInstance().getAllChatRoomCategories(shouldForceFetch));
            performFilter(getFilterText());
        }
    }

    private void refreshChatroomCategory(ChatRoomCategory category) {
        if (mAdapter != null) {
            mAdapter.setChatroomCategory(category);
            performFilter(getFilterText());
        }
    }

    private void refreshChatroomCategory(short categoryId, final boolean shouldForceFetch) {
        refreshChatroomCategory(ChatDatastore.getInstance().getChatRoomCategoryWithId(categoryId, shouldForceFetch));
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.ChatRoom.BEGIN_FETCH_FOR_CATEGORY);
        registerEvent(Events.ChatRoom.RECEIVED);
        registerEvent(Events.ChatRoom.FAVOURITED);
        registerEvent(Events.ChatRoom.UNFAVOURITED);
        registerEvent(Events.ChatRoom.FETCH_FOR_CATEGORY_COMPLETED);
        registerEvent(Events.ChatRoomCategory.FETCH_ALL_COMPLETED);
        registerEvent(Events.ChatRoom.FETCH_FOR_CATEGORY_ERROR);
        registerEvent(Events.ChatRoomCategory.FETCH_ALL_ERROR);
        registerEvent(Events.ChatRoom.FAVOURITE_ERROR);
        registerEvent(Events.ChatRoom.UNFAVOURITE_ERROR);
        registerEvent(Events.ChatRoom.CHAT_NOTIFICATION);
    }

    public void onClick(View v) {
        if (Tools.hideVirtualKeyboard(getActivity())) {
            //If software keyboard showing just hide it and do nothing
            return;
        }
        int viewId = v.getId();

        switch (viewId) {
            case R.id.chat_notification:
                if (mChatNotificationData != null) {
                    ActionHandler.getInstance().displayBrowser(getActivity(),
                            mChatNotificationData.getUrl(),
                            I18n.tr("Chat rooms stats"),
                            R.drawable.ad_chatroom_white);
                }
                break;
        }
    }

    @Override
    public void onGroupFooterClick(View v, ChatRoomCategory data) {
        if (Tools.hideVirtualKeyboard(getActivity())) {
            //If software keyboard showing just hide it and do nothing
            return;
        }
        if (isFiltering()) {
            checkAndPerformGlobalSearch(getFilterText());
        } else {
            ChatDatastore.getInstance().loadMoreChatRoomCategoryWithId(data.getID());
        }
    }
    
    @Override
    public void onListFooterClick(View v) {
        if (Tools.hideVirtualKeyboard(getActivity())) {
            //If software keyboard showing just hide it and do nothing
            return;
        }
        if (isFiltering()) {
            checkAndPerformGlobalSearch(getFilterText());
        }
    }
    
    @Override
    protected void performGlobalSearch(final String searchString) {
        super.performGlobalSearch(searchString);
        
        ActionHandler.getInstance().displayGlobalSearch(getActivity(), SearchType.CHATROOM, searchString);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Events.ChatRoom.BEGIN_FETCH_FOR_CATEGORY)) {
            mAdapter.notifyDataSetChanged();
        } else if (action.equals(Events.ChatRoom.FETCH_FOR_CATEGORY_COMPLETED)) {
            final short categoryId = intent.getShortExtra(Events.ChatRoomCategory.Extra.ID, (short) -1);
            refreshChatroomCategory(categoryId, false);
            mAdapter.notifyDataSetChanged();
            expandChatroomCategory(categoryId);
        } else if (action.equals(Events.ChatRoom.FETCH_FOR_CATEGORY_ERROR)) {
            mAdapter.notifyDataSetChanged();
        } else if (action.equals(Events.ChatRoomCategory.FETCH_ALL_COMPLETED)) {
            refreshAllCategories(false);
            setPullToRefreshComplete();
            checkWhetherShowFirstTimeLoadingView();
        } else if (action.equals(Events.ChatRoom.RECEIVED) || action.equals(Events.ChatRoom.FAVOURITED)
                || action.equals(Events.ChatRoom.UNFAVOURITED)) {
            final short categoryId = intent.getShortExtra(Events.ChatRoomCategory.Extra.ID, (short) -1);
            refreshChatroomCategory(categoryId, false);
        } else if (action.equals(Events.ChatRoomCategory.FETCH_ALL_ERROR)
                || action.equals(Events.ChatRoom.FAVOURITE_ERROR) || action.equals(Events.ChatRoom.UNFAVOURITE_ERROR)) {
            Tools.showToastForIntent(context, intent);
            setPullToRefreshComplete();
            checkWhetherShowFirstTimeLoadingView();
            setPullToRefreshEnabled(true);
        } else if (action.equals(Events.ChatRoom.CHAT_NOTIFICATION)) {
            refreshChatNotification();
        }
    }

    private void refreshChatNotification() {
        mChatNotificationData = ChatDatastore.getInstance().getChatNotification();
        if (mChatNotificationText != null && mChatNotificationData != null) {
            mChatNotificationText.setText(mChatNotificationData.getMessage());
        }
    }
    
    /**
     * @param categoryId
     * 
     */
    private void expandChatroomCategory(short categoryId) {
        ChatRoomCategory category = ChatDatastore.getInstance().getChatRoomCategoryWithId(categoryId, false);
        int pos = -1;
        
        if (category != null && mAdapter != null) {
            pos = mAdapter.findGroupIndexOfCategory(category.getID());
        }
        
        if (pos >= 0) {
            expandGroup(pos);
        }
    }

    protected void expandDefaultGroup() {
        boolean firstGroupExpanded = false;
        int groupCount = mAdapter.getGroupCount();

        for (int pos = 0; pos < groupCount; pos++) {
            if (firstGroupExpanded) {
                collapseGroup(pos);
                continue;
            }

            if (mAdapter.getChildrenCount(pos) > 0) {
                expandGroup(pos);
                firstGroupExpanded = true;
            }
        }
    }

    @Override
    public void onItemLongClick(View v, ChatRoomItem data) {
        // Nothing to do here.
    }

    /**
     * @see ExpandableListFragment#onRefresh()
     */
    @Override
    public void onRefresh() {
        super.onRefresh();
        Tools.hideVirtualKeyboard(getActivity());
        refreshAllCategories(true);
    }

    /**
     * @see com.projectgoth.ui.holder.ChatroomCategoryViewHolder.ChatroomCategoryListener.onRefreshIconClicked(int)
     */
    @Override
    public void onRefreshIconClicked(ChatRoomCategory chatRoomCategory) {
        if (Tools.hideVirtualKeyboard(getActivity())) {
            //If software keyboard showing just hide it and do nothing
            return;
        }
        if (mAdapter != null) {
            if (chatRoomCategory != null) {
                refreshChatroomCategory(chatRoomCategory.getID(), true);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        super.onScrollStateChanged(view, scrollState);
    }
    
    @Override
    public void performFilter(final String filterString) {
        super.performFilter(filterString);
        if (isFiltering()) {
            mAdapter.setFooterText(String.format(I18n.tr("Find chat room for %s"), filterString));
        }
        mAdapter.setFilter(filterString);
    }
    
    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_chat_white;
    }

    @Override
    protected String getTitle() {
        return I18n.tr("Chat rooms");
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = super.getActionBarConfig();
        config.setShowSearchButton(false);
        config.setShowOverflowButtonState(OverflowButtonState.ALERT);
        return config;
    }

    @Override
    protected void onModeChanged(Mode newMode) {
        super.onModeChanged(newMode);

        updateListAdapter(mAdapter);
        if (isFiltering()) {
            showListViewHeader(false);
        } else {
            showListViewHeader(true);
        }
    }
    
    private void updateListAdapter(ChatroomListAdapter adapter) {
        final boolean isFiltering = isFiltering();
        adapter.setRefreshButtonVisible(!isFiltering);
        adapter.setGroupSizeVisible(isFiltering);
        if (isFiltering) {
            // The footer text is set in performFilter() since the filter string is needed 
            adapter.setFooterType(FooterType.LIST_FOOTER);
            adapter.setCategoryHideMode(CategoryHideMode.HIDE_EMPTY);
        } else {
            adapter.setFooterText(I18n.tr("See more"));
            adapter.setFooterType(FooterType.GROUP_FOOTER);
            adapter.setCategoryHideMode(CategoryHideMode.HIDE_EMPTY_SELECTED);
            adapter.addCategoryToHideWhenEmpty(ChatRoomCategory.CATEGORY_ID_RECENT);
        }
    }

    private void checkWhetherShowFirstTimeLoadingView() {
        if (!mIsStubInflated) {
            mInflatedStubView = mFirstTimeLoadingStub.inflate();
            if (mInflatedStubView == null) {
                return;
            }
            TextView textLoading = (TextView) mInflatedStubView.findViewById(R.id.text_loading);
            textLoading.setText(I18n.tr("Loading"));
            mIsStubInflated = true;
        }

        if (mInflatedStubView == null) {
            return;
        }

        if (mAdapter.getGroupCount() == 0) {
            mInflatedStubView.setVisibility(View.VISIBLE);
            setPullToRefreshEnabled(false);
        } else {
            mInflatedStubView.setVisibility(View.GONE);
            setPullToRefreshEnabled(true);
        }
    }

}
