
package com.projectgoth.ui.adapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.nemesis.model.ContactGroup;
import com.projectgoth.nemesis.model.Friend;
import com.projectgoth.ui.fragment.FriendListFragment.FriendListItemActionType;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.ContactGroupViewHolder;
import com.projectgoth.ui.holder.ContactGroupViewHolder.ContactGroupClickListener;
import com.projectgoth.ui.holder.FriendViewHolder;
import com.projectgoth.ui.listener.OnSearchKeywordChangesListener;

/**
 * @author dangui
 */

public class GroupedFriendListAdapter extends BaseExpandableListAdapter implements OnSearchKeywordChangesListener {

    List<ContactGroup> mGroupData = new ArrayList<ContactGroup>();
    SparseArray<List<Friend>> mGroupedFriendsData = new SparseArray<List<Friend>>();
    List<ContactGroup> mOriginalGroupData = new ArrayList<ContactGroup>();
    SparseArray<List<Friend>> mOriginalGroupedFriendsData = new SparseArray<List<Friend>>();

    private LayoutInflater mInflater;

    private BaseViewListener<Friend> onClickListener;
    private ContactGroupClickListener onGroupClickListener;
    private FriendListItemActionType actionType = FriendListItemActionType.DEFAULT;

    private String filterKeyword;

    private boolean isFriendListGrouped;

    public GroupedFriendListAdapter() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ContactGroup group = (ContactGroup) getGroup(groupPosition);
        if (null == group || mGroupedFriendsData.get(group.getGroupID()) == null) {
            return null;
        }
        List<Friend> friendList = mGroupedFriendsData.get(group.getGroupID());
        if (friendList != null) {
            return friendList.get(childPosition);
        }
        return null;

    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                             ViewGroup parent) {

        FriendViewHolder friendViewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_friend_list_item, null);
            friendViewHolder = new FriendViewHolder(convertView, actionType);
            convertView.setTag(R.id.holder, friendViewHolder);
        } else {
            friendViewHolder = (FriendViewHolder) convertView.getTag(R.id.holder);
        }

        Friend friend = (Friend) getChild(groupPosition, childPosition);
        friendViewHolder.setFilterKeyword(filterKeyword);
        friendViewHolder.setData(friend);
        friendViewHolder.setBaseViewListener(onClickListener);

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ContactGroup group = (ContactGroup) getGroup(groupPosition);
        if (null == group) {
            return 0;
        }
        List<Friend> list = mGroupedFriendsData.get(group.getGroupID());
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        if (groupPosition < mGroupData.size()) {
            return mGroupData.get(groupPosition);
        }
        return null;

    }

    @Override
    public int getGroupCount() {
        int count = mGroupData.size();
        return count;

    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ContactGroupViewHolder categoryView = null;
        ContactGroup group = (ContactGroup) getGroup(groupPosition);

        if (convertView == null) {

            convertView = mInflater.inflate(R.layout.holder_contact_group_item, parent, false);
            categoryView = new ContactGroupViewHolder(convertView);
            categoryView.setListener(onGroupClickListener);
            convertView.setTag(categoryView);

        } else {
            categoryView = (ContactGroupViewHolder) convertView.getTag();
        }

        if (group != null) {
            if (!isFriendListGrouped || group.isIMGroup()) {
                categoryView.setShowGroupIcon(false);
            } else {
                categoryView.setShowGroupIcon(true);
            }

            categoryView.setData(group);
            categoryView.setGroupTitle(group.getGroupName(), getChildrenCount(groupPosition));
        }

        categoryView.setGroupPosition(groupPosition);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int arg0, int arg1) {
        return false;
    }

    public void setGroupList(List<ContactGroup> groupData) {
        mGroupData = groupData;
        mOriginalGroupData = groupData;

    }

    public void setGroupedFriendsList(SparseArray<List<Friend>> groupedFriendsData) {
        mGroupedFriendsData = groupedFriendsData;
        mOriginalGroupedFriendsData = groupedFriendsData;

    }

    @Override
    public void filterAndRefresh(String keyword) {

        if (TextUtils.isEmpty(keyword)) {
            mGroupData = mOriginalGroupData;
            mGroupedFriendsData = mOriginalGroupedFriendsData;
            this.notifyDataSetChanged();
        } else {
            mGroupData = new ArrayList<ContactGroup>();
            mGroupedFriendsData = new SparseArray<List<Friend>>();
            filterData(keyword);
            this.notifyDataSetChanged();
        }
    }


    private void filterData(String keyword) {

        for (int i = 0; i < mOriginalGroupData.size(); i++) {
            ContactGroup group = mOriginalGroupData.get(i);
            List<Friend> childOfGroup = mOriginalGroupedFriendsData.get(group.getGroupID());
            if (childOfGroup == null)
                continue;
            for (Iterator<Friend> iterator = childOfGroup.iterator(); iterator.hasNext(); ) {
                Friend friend = (Friend) iterator.next();
                if (friend.getDisplayName().toLowerCase().contains(keyword.toLowerCase())) {
                    addFilteredFriend(group, friend);
                }
            }
        }
    }


    private void addFilteredFriend(ContactGroup group, Friend friend) {

        List<Friend> childOfGroup = mGroupedFriendsData.get(group.getGroupID());
        if (childOfGroup == null) {
            List<Friend> newfriendsList = new ArrayList<Friend>();
            newfriendsList.add(friend);
            mGroupedFriendsData.put(group.getGroupID(), newfriendsList);
            mGroupData.add(group);
        } else {
            childOfGroup.add(friend);
        }

    }

    public boolean hasNoFriend() {
        SparseArray<List<Friend>> childData = mOriginalGroupedFriendsData;
        for (int i = 0; i < childData.size(); i++) {
            List<Friend> friendsOfGroup = childData.valueAt(i);
            if (friendsOfGroup.size() > 0) {
                return false;
            }
        }
        return true;
    }

    public void setFriendClickListener(BaseViewListener<Friend> onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void reorderGroup(int groupId, boolean onlineFriendsOnly, boolean showSMSContacts, boolean groupFusionFriends) {
        // the friend list of the group will be sorted here
        final List<Friend> sortedFriendList = UserDatastore.getInstance().getFriendsForContactGroupWithId(groupId,
                onlineFriendsOnly, showSMSContacts, groupFusionFriends);

        mOriginalGroupedFriendsData.put(groupId, sortedFriendList);
    }

    public void removeFriendFromList(Friend friend) {
        for (int i = 0; i < mGroupedFriendsData.size(); i++) {
            ContactGroup group = mGroupData.get(i);
            List<Friend> childOfGroup = mGroupedFriendsData.get(group.getGroupID());
            if (childOfGroup == null)
                continue;

            if (childOfGroup.contains(friend)) {
                childOfGroup.remove(friend);
                mGroupedFriendsData.put(group.getGroupID(), childOfGroup);
            }
        }
        this.notifyDataSetChanged();

    }

    public void addFriendToList(Friend friend) {
        for (int i = 0; i < mGroupedFriendsData.size(); i++) {
            ContactGroup group = mGroupData.get(i);
            List<Friend> childOfGroup = mGroupedFriendsData.get(group.getGroupID());
            if (childOfGroup == null)
                continue;

            childOfGroup.add(friend);
            mGroupedFriendsData.put(group.getGroupID(), childOfGroup);
        }
        this.notifyDataSetChanged();

    }

    public void setOnGroupClickListener(ContactGroupClickListener onGroupClickListener) {
        this.onGroupClickListener = onGroupClickListener;
    }

    public void setFriendListItemActionType(FriendListItemActionType actionType) {
        this.actionType = actionType;
    }

    public void setFriendUnchecked(Friend friend) {
        for (int i = 0; i < mGroupedFriendsData.size(); i++) {
            ContactGroup group = mGroupData.get(i);
            List<Friend> childOfGroup = mGroupedFriendsData.get(group.getGroupID());
            if (childOfGroup == null)
                continue;

            if (childOfGroup.contains(friend)) {
                int index = childOfGroup.indexOf(friend);
                childOfGroup.get(index).setChecked(false);
            }
        }
        this.notifyDataSetChanged();

    }

    /**
     * once more than one fusion contacts selected in start chat fragment, the IM contacts are not selectable
     */
    public void setIMContactsSelectable(boolean isSelectable) {
        for (int i = 0; i < mGroupedFriendsData.size(); i++) {
            ContactGroup group = mGroupData.get(i);
            if (group.isIMGroup()) {
                group.setSelectable(isSelectable);
                List<Friend> childOfGroup = mGroupedFriendsData.get(group.getGroupID());

                if (childOfGroup == null)
                    continue;

                for (Friend friend : childOfGroup) {
                    friend.setSelectable(isSelectable);
                }
            }
        }
    }


    public void setFilterKeyword(String filterKeyword) {
        this.filterKeyword = filterKeyword;
    }

    public void setFriendListGrouped(boolean isFriendListGrouped) {
        this.isFriendListGrouped = isFriendListGrouped;
    }
}
