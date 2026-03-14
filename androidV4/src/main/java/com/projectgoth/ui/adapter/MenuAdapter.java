/**
 * Copyright (c) 2013 Project Goth
 *
 * MenuAdapter.java
 * Created Feb 24, 2014, 11:41:56 AM
 */

package com.projectgoth.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.model.MenuOption;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.MenuViewHolder;

import java.util.List;

/**
 * @author mapet
 * 
 */
public class MenuAdapter extends BaseAdapter {

    private List<MenuOption>             mMenuOptionList;
    private LayoutInflater               mInflater;
    private BaseViewListener<MenuOption> menuOptionListener;

    public MenuAdapter() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public int getCount() {
        if (mMenuOptionList != null) {
            return mMenuOptionList.size();
        }
        return 0;
    }

    @Override
    public MenuOption getItem(int position) {
        if (mMenuOptionList != null && position < getCount()) {
            return mMenuOptionList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MenuViewHolder menuViewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_menu_item, null);
            menuViewHolder = new MenuViewHolder(convertView);
            convertView.setTag(R.id.holder, menuViewHolder);
        } else {
            menuViewHolder = (MenuViewHolder) convertView.getTag(R.id.holder);
        }

        MenuOption menuOption = getItem(position);
        menuViewHolder.setData(menuOption);
        menuViewHolder.setBaseViewListener(menuOptionListener);

        return convertView;
    }

    public void setMenuOptionList(List<MenuOption> menuOptionListData) {
        mMenuOptionList = menuOptionListData;
        notifyDataSetChanged();
    }

    public void setMenuOptionClickListener(BaseViewListener<MenuOption> listener) {
        menuOptionListener = listener;
    }

}
