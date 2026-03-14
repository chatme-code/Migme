/**
 * Copyright (c) 2013 Project Goth
 *
 * StickerStoreListAdapter.java
 * Created Dec 9, 2014, 1:45:16 PM
 */

package com.projectgoth.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.controller.StoreController;
import com.projectgoth.model.StickerStoreItem;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.StickerListViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mapet
 * 
 */
public class StickerStoreListAdapter extends BaseAdapter {

    private StickerStoreItem[]                 mStoreItemList;
    private LayoutInflater                     mInflater;
    private BaseViewListener<StickerStoreItem> storeItemListListener;

    private List<Integer> hiddenItems = new ArrayList<Integer>();

    public StickerStoreListAdapter() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    public int getListCount() {
        if (mStoreItemList != null) {
            return mStoreItemList.length;
        }
        return 0;
    }

    @Override
    public int getCount() {
        if (mStoreItemList != null) {
            return mStoreItemList.length - hiddenItems.size();
        }
        return 0;
    }

    @Override
    public StickerStoreItem getItem(int position) {
        for (Integer integer : hiddenItems) {
            if (integer <= position) {
                position++;
            }
        }
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
        StickerListViewHolder stickerListViewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_sticker_list_item, null);
            stickerListViewHolder = new StickerListViewHolder(convertView);
            convertView.setTag(R.id.holder, stickerListViewHolder);

        } else {
            stickerListViewHolder = (StickerListViewHolder) convertView.getTag(R.id.holder);
        }
        StickerStoreItem storeListItem = getItem(position);
        if (storeListItem != null) {
            stickerListViewHolder.setBaseViewListener(storeItemListListener);
            stickerListViewHolder.setData(
                    storeListItem,
                    StoreController.getInstance().isStickerPackPurchaseInProcess(
                            String.valueOf(storeListItem.getStoreItem().getId())));
        }

        return convertView;
    }

    public void setStoreItemList(StickerStoreItem[] list) {
        mStoreItemList = list;
        hiddenItems.clear();
        for (int i = 0; i < list.length; i++) {
            StickerStoreItem item = list[i];
            if (item == null) {
                hiddenItems.add(i);
            }
        }
        notifyDataSetChanged();
    }

    public void setStoreItemListListener(BaseViewListener<StickerStoreItem> listener) {
        this.storeItemListListener = listener;
    }
}
