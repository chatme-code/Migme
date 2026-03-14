/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftCategoryAdapter.java
 * Created Dec 8, 2013, 1:09:54 PM
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
import com.projectgoth.ui.holder.GiftViewHolder;

/**
 * @author mapet
 * 
 */
public class StoreCategoryAdapter extends BaseAdapter {

    private LayoutInflater              mInflater;
    private StoreItem[]                 mStoreCategoryItems;
    private BaseViewListener<StoreItem> storeCategoryListener;

    private boolean                     isInChat = false;

    public StoreCategoryAdapter() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public int getCount() {
        if (mStoreCategoryItems != null) {
            return mStoreCategoryItems.length;
        }
        return 0;
    }

    @Override
    public StoreItem getItem(int position) {
        if (mStoreCategoryItems != null && position < mStoreCategoryItems.length) {
            return mStoreCategoryItems[position];
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GiftViewHolder giftViewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_gift_item, null);
            giftViewHolder = new GiftViewHolder(convertView);
            convertView.setTag(R.id.holder, giftViewHolder);

        } else {
            giftViewHolder = (GiftViewHolder) convertView.getTag(R.id.holder);
        }

        giftViewHolder.setGiftingInChat(isInChat);

        StoreItem giftItem = getItem(position);
        if (giftItem != null) {
            giftViewHolder.setData(giftItem);
            giftViewHolder.setBaseViewListener(storeCategoryListener);
        }

        return convertView;
    }

    public void setGiftList(StoreItem[] storeCategoryItems) {
        mStoreCategoryItems = storeCategoryItems;
        notifyDataSetChanged();
    }

    public void setStoreCategoryListener(BaseViewListener<StoreItem> storeCategoryListener) {
        this.storeCategoryListener = storeCategoryListener;
    }

    public boolean isInChat() {
        return isInChat;
    }

    public void setInChat(boolean isInChat) {
        this.isInChat = isInChat;
    }

}
