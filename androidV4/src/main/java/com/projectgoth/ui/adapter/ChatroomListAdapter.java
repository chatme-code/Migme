/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatroomListAdapter.java
 * Created Jun 6, 2013, 10:41:46 AM
 */

package com.projectgoth.ui.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Logger;
import com.projectgoth.common.TextUtils;
import com.projectgoth.nemesis.model.ChatRoomCategory;
import com.projectgoth.nemesis.model.ChatRoomItem;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.BasicListCategoryFooterViewHolder;
import com.projectgoth.ui.holder.BasicListFooterViewHolder;
import com.projectgoth.ui.holder.ChatroomCategoryViewHolder;
import com.projectgoth.ui.holder.ChatroomCategoryViewHolder.ChatroomCategoryListener;
import com.projectgoth.ui.holder.ChatroomItemViewHolder;
import com.projectgoth.util.AndroidLogger;

/**
 * @author mapet
 * 
 */
public class ChatroomListAdapter extends BaseExpandableListAdapter {

    private static final String LOG_TAG = AndroidLogger.makeLogTag(ChatroomListAdapter.class);
    
    private final LayoutInflater           mInflater;

    // We want to hide recent rooms if there are no items in it.
    // fullChatroomCategories has a list of all (visible and invisible) categories) 
    // displayChatroomCategories just contains the list of visible.
    // The reason for maintaining two lists is that the fullChatroomCategories will 
    // ensure the correct ordering.
    private List<ChatRoomCategory>    fullChatroomCategories    = new ArrayList<ChatRoomCategory>();
    private List<ChatRoomCategory>    displayChatroomCategories = new ArrayList<ChatRoomCategory>();

    private BaseViewListener<ChatRoomItem> chatItemListener;
    private ChatroomCategoryListener       chatroomCategoryListener;
    
    private FooterClickListener            groupFooterListener;
    private boolean                        mIsRefreshButtonVisible = false;
    private boolean                        mIsGroupSizeVisible = false;
    
    private String                         mFilter = null;
    
    public enum FooterType {
        LIST_FOOTER,
        GROUP_FOOTER
    }
    
    public enum CategoryHideMode {
        DONT_HIDE,          // Don't hide categories
        HIDE_EMPTY_SELECTED,// Only hide those categories that are empty AND listed in mCategoriesToHideWhenEmpty
        HIDE_EMPTY          // Hide any empty category
    };
    
    private CategoryHideMode mCategoryHideMode = CategoryHideMode.DONT_HIDE;
    private final Set<Short> mCategoriesToHideWhenEmpty = new HashSet<Short>();

    private String mFooterText = null;
    private FooterType mFooterType = FooterType.GROUP_FOOTER;

    //@formatter:off
    private final BaseViewListener<ChatRoomCategory> groupFooterHolderClickListener = new BaseViewListener<ChatRoomCategory>() {
        
        @Override
        public void onItemClick(View v, ChatRoomCategory data) {

            if (groupFooterListener != null) {
                groupFooterListener.onGroupFooterClick(v, data);
            }
        }

        @Override
        public void onItemLongClick(View v, ChatRoomCategory data) {
            // DO NOTHING
        }
    };
    //@formatter:on
    
    private final OnClickListener listFooterHolderClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (groupFooterListener != null) {
                groupFooterListener.onListFooterClick(v);
            }
        }
    };
    
    public interface FooterClickListener {
        public void onGroupFooterClick(View v, ChatRoomCategory data);
        public void onListFooterClick(View v);
    }

    public ChatroomListAdapter() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    public void setChatroomCategories(List<ChatRoomCategory> chatroomCategories) {
        if (chatroomCategories == null) {
            Logger.error.logWithTrace(LOG_TAG, getClass(), "chatroomCategories is null!");
            return;
        }
        fullChatroomCategories = chatroomCategories;
        updateDisplayChatroomCategories();
        notifyDataSetChanged();
    }
    
    private void updateDisplayChatroomCategories() {
        switch (mCategoryHideMode) {
            case HIDE_EMPTY:
                displayChatroomCategories.clear();
                if (TextUtils.isEmpty(mFilter)) {
                    for (ChatRoomCategory category : fullChatroomCategories) {
                        if (category.getChatroomItemsSize() > 0)
                            displayChatroomCategories.add(category);
                    }
                } else {
                    for (ChatRoomCategory category : fullChatroomCategories) {
                        ChatRoomCategory filtered = category.getFiltered(mFilter);
                        if (filtered.getChatroomItemsSize() > 0)
                            displayChatroomCategories.add(filtered);
                    }
                }
                break;
            case HIDE_EMPTY_SELECTED:
                displayChatroomCategories.clear();
                if (TextUtils.isEmpty(mFilter)) {
                    for (ChatRoomCategory category : fullChatroomCategories) {
                        if (!mCategoriesToHideWhenEmpty.contains(category.getID()) ||
                            category.getChatroomItemsSize() > 0 ||
                            category.getIsLoading())
                            displayChatroomCategories.add(category);
                    }
                } else {
                    for (ChatRoomCategory category : fullChatroomCategories) {
                        ChatRoomCategory filtered = category.getFiltered(mFilter);
                        if (!mCategoriesToHideWhenEmpty.contains(category.getID()) ||
                            filtered.getChatroomItemsSize() > 0)
                            displayChatroomCategories.add(filtered);
                    }
                }
                break;
            case DONT_HIDE:
            default:
                displayChatroomCategories.clear();
                if (TextUtils.isEmpty(mFilter)) {
                    displayChatroomCategories.addAll(fullChatroomCategories);
                } else {
                    for (ChatRoomCategory category : fullChatroomCategories) {
                        displayChatroomCategories.add(category.getFiltered(mFilter));
                    }
                }
                break;
        }
    }

    public void setChatroomCategory(ChatRoomCategory chatroomCategory) {
        if (chatroomCategory != null) {
            int index = findIndexOfCategory(chatroomCategory.getID());
            if (index >= 0) {
                fullChatroomCategories.set(index, chatroomCategory);
            } else {
                fullChatroomCategories.add(chatroomCategory);
            }
            updateDisplayChatroomCategories();
            notifyDataSetChanged();
        }
    }

    private int findIndexOfCategory(short categoryId) {
        for (ChatRoomCategory category : fullChatroomCategories) {
            if (category.getID() == categoryId) {
                return fullChatroomCategories.indexOf(category);
            }
        }
        
        return -1;
    }       

    public int findGroupIndexOfCategory(short categoryId) {
        for (ChatRoomCategory category : displayChatroomCategories) {
            if (category.getID() == categoryId) {
                return fullChatroomCategories.indexOf(category);
            }
        }
        
        return -1;
    }       

    @Override
    public ChatRoomCategory getGroup(int groupPosition) {
        // Added a groupPosition < size() check to avoid Samsung specific crash.
        if (displayChatroomCategories != null && groupPosition < displayChatroomCategories.size()) {
            return displayChatroomCategories.get(groupPosition);
        }
        return null;
    }

    @Override
    public ChatRoomItem getChild(int groupPosition, int childPosition) {
        ChatRoomCategory chatroomCategory = getGroup(groupPosition);
        if (chatroomCategory != null) {
            List<ChatRoomItem> items = chatroomCategory.getChatRoomItems();
            if (items != null && childPosition >= 0 && childPosition < items.size()) {
                return items.get(childPosition);
            }
        }
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
            ViewGroup parent) {
        ChatRoomCategory chatRoomCategory = getGroup(groupPosition);
        boolean isFooter = isLastChild && hasGroupFooter(chatRoomCategory, groupPosition);
        if (isFooter) {
            convertView = getGroupFooterView(groupPosition, convertView, parent);
        } else {
            ChatroomItemViewHolder chatroomViewHolder = null;
            if (convertView == null || convertView.getTag(R.id.holder) == null) {
                convertView = mInflater.inflate(R.layout.holder_chatroom_list_item, parent, false);
                chatroomViewHolder = new ChatroomItemViewHolder(convertView);
                convertView.setTag(R.id.holder, chatroomViewHolder);
            } else {
                chatroomViewHolder = (ChatroomItemViewHolder) convertView.getTag(R.id.holder);
            }

            ChatRoomItem chatroomItem = getChild(groupPosition, childPosition);
            if (chatroomItem != null) {
                chatroomViewHolder.setData(chatroomItem);
                chatroomViewHolder.setBaseViewListener(chatItemListener);
            }
        }

        return convertView;
    }   

    @Override
    public int getChildrenCount(int groupPosition) {
        final ChatRoomCategory chatRoomCategory = getGroup(groupPosition);
        if (chatRoomCategory != null) {
            int size = chatRoomCategory.getChatroomItemsSize();
            if (hasGroupFooter(chatRoomCategory, groupPosition)) {
                size++;
            }
            return size;
        }
        return 0;
    }

    @Override
    public int getGroupCount() {
        if (displayChatroomCategories != null) {
            int size = displayChatroomCategories.size();
            if (hasListFooter()) {
                size++;
            }
            return size;
        }
        return 0;
    }

    @Override
    public long getGroupId(int groupPosition) {
        ChatRoomCategory chatroomCategory = getGroup(groupPosition);
        if (chatroomCategory != null) {
            return chatroomCategory.getID();
        }
        return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        boolean isFooter = groupPosition == getGroupCount()-1 && hasListFooter();
        if (isFooter) {
            convertView = getListFooterView(convertView, parent);
        } else {
            ChatroomCategoryViewHolder categoryView = null;
            ChatRoomCategory chatRoomCategory = getGroup(groupPosition);
            if (convertView == null || convertView.getTag() == null) {
                convertView = mInflater.inflate(R.layout.chatroom_category, parent, false);
                categoryView = new ChatroomCategoryViewHolder(convertView);
                categoryView.setListener(chatroomCategoryListener);
                convertView.setTag(categoryView);
            } else {
                categoryView = (ChatroomCategoryViewHolder) convertView.getTag();
            }
            
            if (chatRoomCategory != null && categoryView != null) {
                categoryView.setCategorySizeVisible(mIsGroupSizeVisible);
                categoryView.setData(chatRoomCategory);
                if (mIsRefreshButtonVisible && isExpanded) {
                    categoryView.showRefreshButton(true);
                    categoryView.animateRefreshIcon(chatRoomCategory.getIsLoading());
                } else {
                    categoryView.showRefreshButton(false);
                }
            }        
        }
        return convertView;
    }

    @SuppressWarnings("unchecked")  // Needed to avoid warning for categoryView assignment
    private View getGroupFooterView(int groupPosition, View convertView, ViewGroup parent) {
        BasicListCategoryFooterViewHolder<ChatRoomCategory> categoryView = null;
        
        if (convertView == null || convertView.getTag(R.id.holder_footer) == null) {
            convertView = mInflater.inflate(R.layout.holder_list_footer, parent, false);
            categoryView = new BasicListCategoryFooterViewHolder<ChatRoomCategory>(convertView);
            convertView.setTag(R.id.holder_footer, categoryView);
        } else {
            categoryView = (BasicListCategoryFooterViewHolder<ChatRoomCategory>) convertView.getTag(R.id.holder_footer);
        }

        ChatRoomCategory chatRoomCategory = getGroup(groupPosition);
        categoryView.setData(chatRoomCategory);
        // instead of using chatRoomCategory.getCategoryFooter() which is sent
        // from the server, we are using "View More" based on the mockup
        categoryView.setLabel(mFooterText);
        categoryView.setBaseViewListener(groupFooterHolderClickListener);

        return convertView;
    }

    private View getListFooterView(View convertView, ViewGroup parent) {
        BasicListFooterViewHolder categoryView = null;

        if (convertView == null || convertView.getTag(R.id.holder_footer) == null) {
            convertView = mInflater.inflate(R.layout.holder_list_footer, parent, false);
            categoryView = new BasicListFooterViewHolder(convertView);
            convertView.setTag(R.id.holder_footer, categoryView);
        } else {
            categoryView = (BasicListFooterViewHolder) convertView.getTag(R.id.holder_footer);
        }

        categoryView.setLabel(mFooterText);
        categoryView.setOnClickListener(listFooterHolderClickListener);

        return convertView;
    }

    private boolean hasGroupFooter(ChatRoomCategory chatRoomCategory, int groupPosition) {
        return (mFooterType == FooterType.GROUP_FOOTER &&
                !TextUtils.isEmpty(mFooterText) &&
                chatRoomCategory.hasMoreChatRooms() &&
                !chatRoomCategory.getIsLoading());
    }

    private boolean hasListFooter() {
        return (mFooterType == FooterType.LIST_FOOTER &&
                !TextUtils.isEmpty(mFooterText) &&
                !TextUtils.isEmpty(mFilter));
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void setChatItemListener(BaseViewListener<ChatRoomItem> listener) {
        chatItemListener = listener;
    }
    
    public void setChatroomCategoryListener(ChatroomCategoryListener listener) {
        chatroomCategoryListener = listener;
    }

    public void setFooterListener(FooterClickListener listener) {
        groupFooterListener = listener;
    }
    
    public void setRefreshButtonVisible(boolean visible) {
        mIsRefreshButtonVisible = visible;
    }
    
    public void setGroupSizeVisible(boolean visible) {
        mIsGroupSizeVisible = visible;
    }
    
    public void setFilter(String filter) {
        mFilter = filter.toLowerCase();
        
        updateDisplayChatroomCategories();
        notifyDataSetChanged();
    }
    
    public String getFilter() {
        return mFilter;
    }

    public void setFooterText(String text) {
        mFooterText = text;
    }

    public void setFooterType(FooterType type) {
        mFooterType = type;
    }
    
    public void setCategoryHideMode(CategoryHideMode mode) {
        mCategoryHideMode = mode;
    }
    
    public void addCategoryToHideWhenEmpty(short categoryId) {
        mCategoriesToHideWhenEmpty.add(categoryId);
    }
    
}
