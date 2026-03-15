/**
 * Copyright (c) 2013 Project Goth
 *
 * StoreListAdapter.java
 * Created Nov 27, 2013, 11:26:39 AM
 */

package com.projectgoth.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.StoreItem;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.GiftListViewHolder;

/**
 * @author mapet
 * 
 */
public class GiftStoreListAdapter extends BaseAdapter {

    private StoreItem[]                 mStoreItemList;
    private LayoutInflater              mInflater;
    private BaseViewListener<StoreItem> storeItemListListener;

    public GiftStoreListAdapter() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public int getCount() {
        if (mStoreItemList != null) {
            return mStoreItemList.length;
        }
        return 0;
    }

    @Override
    public StoreItem getItem(int position) {
        if (mStoreItemList != null && position < mStoreItemList.length) {
            return mStoreItemList[position];
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GiftListViewHolder giftListViewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_gift_list_item, null);
            giftListViewHolder = new GiftListViewHolder(convertView);
            convertView.setTag(R.id.holder, giftListViewHolder);

        } else {
            giftListViewHolder = (GiftListViewHolder) convertView.getTag(R.id.holder);
        }

        StoreItem giftListItem = (StoreItem) getItem(position);
        if (giftListItem != null) {
            giftListViewHolder.setBaseViewListener(storeItemListListener);
            giftListViewHolder.setData(giftListItem);
        }

        return convertView;
    }

    public void setStoreItemList(StoreItem[] giftList) {
        mStoreItemList = giftList;
        notifyDataSetChanged();
    }

    public void setStoreItemListListener(BaseViewListener<StoreItem> listener) {
        this.storeItemListListener = listener;
    }

}
