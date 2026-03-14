
package com.projectgoth.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.TextUtils;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.nemesis.model.Friend;
import com.projectgoth.ui.fragment.FriendListFragment.FriendListItemActionType;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.FriendViewHolder;
import com.projectgoth.ui.listener.OnSearchKeywordChangesListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dangui
 * 
 */
public class FriendListAdapter extends BaseAdapter implements OnSearchKeywordChangesListener {

    List<Friend>                     mFriendListData         = new ArrayList<Friend>();
    List<Friend>                     mOriginalFriendListData = new ArrayList<Friend>();

    private LayoutInflater           mInflater;
    private BaseViewListener<Friend> friendClickListener;
    private FriendListItemActionType actionType;

    private String filterKeyword;

    public FriendListAdapter() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public int getCount() {
        return mFriendListData.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parentView) {
        FriendViewHolder friendViewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_friend_list_item, null);
            friendViewHolder = new FriendViewHolder(convertView, actionType);
            convertView.setTag(R.id.holder, friendViewHolder);
        } else {
            friendViewHolder = (FriendViewHolder) convertView.getTag(R.id.holder);
        }

        Friend friend = mFriendListData.get(position);
        friendViewHolder.setFilterKeyword(filterKeyword);
        friendViewHolder.setData(friend);
        friendViewHolder.setBaseViewListener(friendClickListener);

        return convertView;
    }

    public void setFriendList(List<Friend> friendListData) {
        mFriendListData = friendListData;
        mOriginalFriendListData = friendListData;
    }

    @Override
    public void filterAndRefresh(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            mFriendListData = mOriginalFriendListData;
        } else {
            ArrayList<Friend> filteredUserListItems = filterUserList(keyword);
            mFriendListData = filteredUserListItems;
        }
        notifyDataSetChanged();
    }

    private ArrayList<Friend> filterUserList(String keyword) {
        ArrayList<Friend> filteredUserList = new ArrayList<Friend>();
        for (int i = 0; i < mOriginalFriendListData.size(); i++) {
            Friend friend = mOriginalFriendListData.get(i);
            String displayName = friend.getDisplayName();
            if (displayName.toLowerCase().contains(keyword.toLowerCase())) {
                filteredUserList.add(friend);
            }
        }
        return filteredUserList;
    }

    public void setFriendClickListener(BaseViewListener<Friend> listener) {
        this.friendClickListener = listener;
    }

    public void removeFriendFromList(Friend friend) {
        if (mFriendListData.contains(friend)) {
            mFriendListData.remove(friend);
            mOriginalFriendListData.remove(friend);
        }
        this.notifyDataSetChanged();

    }

    public void addFriendToList(Friend friend) {
        mFriendListData.add(friend);
        UserDatastore.getInstance().sortFriendList(mFriendListData);
        this.notifyDataSetChanged();

    }

    public void setFriendListItemActionType(FriendListItemActionType actionType) {
        this.actionType = actionType;
    }

    public void setFriendUnchecked(Friend friend) {
        if (mFriendListData.contains(friend)) {
            int index = mFriendListData.indexOf(friend);
            mFriendListData.get(index).setChecked(false);
        }
        this.notifyDataSetChanged();

    }

    public void setFilterKeyword(String filterKeyword) {
        this.filterKeyword = filterKeyword;
    }

}
