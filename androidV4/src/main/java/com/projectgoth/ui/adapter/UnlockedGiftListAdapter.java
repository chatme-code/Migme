/**
 * Copyright (c) 2013 Project Goth
 *
 * UnlockedGiftListAdapter.java
 * Created Jan 6, 2015, 11:55:21 AM
 */

package com.projectgoth.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.StoreUnlockedItem;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.UnlockedGiftViewHolder;

/**
 * @author mapet
 * 
 */
public class UnlockedGiftListAdapter extends BaseAdapter {

    private StoreUnlockedItem[]                 mUnlockedItemList;
    private LayoutInflater                      mInflater;
    private BaseViewListener<StoreUnlockedItem> unlockedItemListListener;

    public UnlockedGiftListAdapter() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public int getCount() {
        if (mUnlockedItemList != null) {
            return mUnlockedItemList.length;
        }
        return 0;
    }

    @Override
    public StoreUnlockedItem getItem(int position) {
        if (mUnlockedItemList != null && position < mUnlockedItemList.length) {
            return mUnlockedItemList[position];
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UnlockedGiftViewHolder unlockedGiftViewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_gift_list_item, null);
            unlockedGiftViewHolder = new UnlockedGiftViewHolder(convertView);
            convertView.setTag(R.id.holder, unlockedGiftViewHolder);

        } else {
            unlockedGiftViewHolder = (UnlockedGiftViewHolder) convertView.getTag(R.id.holder);
        }

        StoreUnlockedItem unlockedItem = (StoreUnlockedItem) getItem(position);
        if (unlockedItem != null) {
            unlockedGiftViewHolder.setBaseViewListener(unlockedItemListListener);
            unlockedGiftViewHolder.setData(unlockedItem);
        }

        return convertView;
    }

    public void setUnlockedItemList(StoreUnlockedItem[] itemList) {
        mUnlockedItemList = itemList;
        notifyDataSetChanged();
    }

    public void setUnlockedItemListListener(BaseViewListener<StoreUnlockedItem> listener) {
        this.unlockedItemListListener = listener;
    }

}
