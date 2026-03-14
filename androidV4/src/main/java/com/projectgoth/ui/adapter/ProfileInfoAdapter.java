/**
 * Copyright (c) 2013 Project Goth
 *
 * ProfileInfoAdapter.java
 * Created Sep 29, 2014, 5:06:06 PM
 */

package com.projectgoth.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.model.ProfileInfoCategory;
import com.projectgoth.ui.holder.ProfileInfoHolder;
import com.projectgoth.ui.holder.ProfileInfoHolder.ProfileInfoListener;
import com.projectgoth.ui.holder.ProfileMainHolder;
import com.projectgoth.ui.holder.ProfileMainHolder.ProfileMainHolderListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mapet
 * 
 */
public class ProfileInfoAdapter extends BaseAdapter {

    private List<ProfileInfoCategory> profileCategoryListData = new ArrayList<ProfileInfoCategory>();

    private ProfileInfoListener       profileInfoListener;
    private ProfileMainHolderListener profileMainHolderListener;
    private LayoutInflater            inflater;

    public ProfileInfoAdapter() {
        super();
        inflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public int getCount() {
        return profileCategoryListData.size();
    }

    @Override
    public ProfileInfoCategory getItem(int position) {
        if (position >= 0 && position < profileCategoryListData.size()) {
            return profileCategoryListData.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return ProfileInfoCategory.Type.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        ProfileInfoCategory item = getItem(position);
        if (item != null) {
            return item.getType().ordinal();
        }
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ProfileInfoCategory profileInfoCategory = (ProfileInfoCategory) getItem(position);

        switch (profileInfoCategory.getType()) {
            case Main:
                ProfileMainHolder profileMainHolder;

                if (convertView == null || convertView.getTag() == null) {
                    convertView = inflater.inflate(R.layout.profile_main_container, parent, false);
                    profileMainHolder = new ProfileMainHolder(convertView);
                    convertView.setTag(profileMainHolder);
                } else {
                    profileMainHolder = (ProfileMainHolder) convertView.getTag();
                }

                profileMainHolder.setProfileMainHolderListener(profileMainHolderListener);
                profileMainHolder.setData(profileInfoCategory);
                break;
            case Info:
                ProfileInfoHolder profileInfoViewHolder;

                if (convertView == null || convertView.getTag() == null) {
                    convertView = inflater.inflate(R.layout.profile_category_container, parent, false);
                    profileInfoViewHolder = new ProfileInfoHolder(convertView);
                    convertView.setTag(profileInfoViewHolder);
                } else {
                    profileInfoViewHolder = (ProfileInfoHolder) convertView.getTag();
                }

                profileInfoViewHolder.setProfileInfoListener(profileInfoListener);
                profileInfoViewHolder.setData(profileInfoCategory);
                break;
        }

        return convertView;
    }

    public void setProfileCategoryListData(List<ProfileInfoCategory> data) {
        if (data == null) {
            data = new ArrayList<ProfileInfoCategory>();
        }
        profileCategoryListData = data;
    }

    public void setProfileInfoListener(ProfileInfoListener listener) {
        profileInfoListener = listener;
    }

    public void setProfileMainHolderListener(ProfileMainHolderListener listener) {
        profileMainHolderListener = listener;
    }
}
