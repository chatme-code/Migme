/**
 * Copyright (c) 2013 Project Goth
 *
 * ProfileListAdapter.java
 * Created Jun 7, 2013, 10:21:00 AM
 */

package com.projectgoth.ui.adapter;

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.TextUtils;
import com.projectgoth.controller.FriendsController;
import com.projectgoth.listener.ProfileViewListener;
import com.projectgoth.nemesis.model.User;
import com.projectgoth.ui.fragment.ProfileListFragment;
import com.projectgoth.ui.holder.ProfileViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mapet
 * 
 */
public class ProfileListAdapter extends BaseAdapter {

    private List<User>             userList           = new ArrayList<User>();
    private List<User>             unfilteredUserList = new ArrayList<User>();
    private LayoutInflater         mInflater;
    private int                    mNumNewFollowers;
    
    private View                   searchMoreView;         
    private ProfileViewListener    profileViewListener;
    private int                    recommendLiteSize = 5;

    private ProfileListFragment.ProfileListType mListType;

    public ProfileListAdapter(final FragmentActivity activity, ProfileListFragment.ProfileListType listType) {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
        profileViewListener = new ProfileViewListener(activity, listType);
        mListType = listType;
    }
    
    public void setSearchMoreView(View view) {
        searchMoreView = view;
    }

    @Override
    public int getCount() {
        final int extraCount = ((searchMoreView != null) ? 1 : 0);
        if (userList != null) {
            return userList.size() + extraCount;
        }
        return extraCount;
    }

    @Override
    public Object getItem(int position) {
        if (userList != null && position < userList.size()) {
            return userList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItem(position) == null) {
            searchMoreView.setTag(R.id.holder, null);
            return searchMoreView;
        }

        ProfileViewHolder profileViewHolder;

        if (mListType == ProfileListFragment.ProfileListType.RECOMMENDED_PEOPLE_LITE && position == recommendLiteSize) {
            convertView = mInflater.inflate(R.layout.holder_list_footer, null);
            profileViewHolder = new ProfileViewHolder(convertView);
            convertView.setTag(R.id.holder, profileViewHolder);
            return convertView;
        } else {
            if (convertView == null || convertView.getTag(R.id.holder) == null) {
                convertView = mInflater.inflate(R.layout.holder_list_item, null);
                profileViewHolder = new ProfileViewHolder(convertView);
                convertView.setTag(R.id.holder, profileViewHolder);
            } else {
                profileViewHolder = (ProfileViewHolder) convertView.getTag(R.id.holder);
            }
        }

        User user = (User) getItem(position);
        boolean shouldShowNewMarker = false;

        if (position < mNumNewFollowers) {
            shouldShowNewMarker = true;
        }

        profileViewHolder.setData(user,
                FriendsController.getInstance().isChangeRelationshipWithUserInProgress(user.getUsername()),
                shouldShowNewMarker);
        profileViewHolder.setBaseViewListener(profileViewListener);

        return convertView;
    }

    /**
     * Sets the unfiltered user list for this adapter.
     * 
     * @param userListData
     *            A list containing unfiltered users.
     */
    public void setUserList(List<User> userListData) {
        if (userListData != null) {
            // if ProfileListType is RECOMMENDED_PEOPLE_LITE then only show recommendLiteSize users.
            if (mListType == ProfileListFragment.ProfileListType.RECOMMENDED_PEOPLE_LITE && userListData.size() > recommendLiteSize) {
                userList = userListData.subList(0, recommendLiteSize);
                unfilteredUserList = userListData.subList(0, recommendLiteSize);
            } else {
                userList = userListData;
                unfilteredUserList = userListData;
            }
            notifyDataSetChanged();
        }
    }

    /**
     * Sets the filtered user list for this adapter. The unfiltered user list
     * data is left untouched.
     * 
     * @param filteredUserListData
     *            A list containing the filtered users.
     */
    private void setFilteredUserList(final List<User> filteredUserListData) {
        if (filteredUserListData != null) {
            userList = filteredUserListData;
            notifyDataSetChanged();
        }
    }

    /**
     * Filters the user list based on the filterText.
     * 
     * @param filterText
     *            The text based on which the filter is done.
     * @return A list containing the filtered users.
     */
    private List<User> getFilteredUserList(final String filterText) {
        List<User> filteredUserList = new ArrayList<User>();

        if (unfilteredUserList != null) {
            for (User baseProfile : unfilteredUserList) {
                final String username = baseProfile.getUsername();
                if (username.toLowerCase().contains(filterText)) {
                    filteredUserList.add(baseProfile);
                }
            }
        }

        return filteredUserList;
    }

    /**
     * Filters the user list data.
     * 
     * @param filterText
     *            The text based on which the filter is done.
     */
    public void filterAndRefresh(final String filterText) {
        if (TextUtils.isEmpty(filterText)) {
            setUserList(unfilteredUserList);
        } else {
            final List<User> filteredUserList = getFilteredUserList(filterText);
            setFilteredUserList(filteredUserList);
        }
    }

    public void setNewFollowersCount(int count) {
        mNumNewFollowers = count;
    }

}
