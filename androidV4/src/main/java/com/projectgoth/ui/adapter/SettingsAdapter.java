/**
 * Copyright (c) 2013 Project Goth
 *
 * SettingsAdapter.java
 * Created Sep 4, 2013, 1:46:58 PM
 */

package com.projectgoth.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.model.SettingsItem;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.SettingsViewHolder;

import java.util.List;

/**
 * @author mapet
 * 
 */
public class SettingsAdapter extends BaseAdapter {

    private LayoutInflater                 mInflater;
    private List<SettingsItem>             mSettings;
    private BaseViewListener<SettingsItem> mSettingsClickListener;

    public SettingsAdapter() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public int getCount() {
        if (mSettings != null) {
            return mSettings.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mSettings != null) {
            return mSettings.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SettingsViewHolder settingsViewHolder;
        SettingsItem settingsItem = (SettingsItem) getItem(position);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_item_settings, null);
            settingsViewHolder = new SettingsViewHolder(convertView);
            convertView.setTag(R.id.holder, settingsViewHolder);
        } else {
            settingsViewHolder = (SettingsViewHolder) convertView.getTag(R.id.holder);
        }  

        settingsViewHolder.setData(settingsItem);
        settingsViewHolder.setBaseViewListener(mSettingsClickListener);

        return convertView;
    }

    public void setSettingsItems(List<SettingsItem> items) {
        mSettings = items;
    }

    public void setSettingsClickListener(BaseViewListener<SettingsItem> listener) {
        mSettingsClickListener = listener;
    }
}
