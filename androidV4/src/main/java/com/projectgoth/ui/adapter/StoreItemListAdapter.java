package com.projectgoth.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.StoreItem;
import com.projectgoth.model.StickerStoreItem;
import com.projectgoth.model.StorePagerItem;
import com.projectgoth.ui.holder.BaseViewHolder;
import com.projectgoth.ui.holder.GiftListViewHolder;
import com.projectgoth.ui.holder.StickerListViewHolder;

/**
 * Created by houdangui on 12/12/14.
 */
public class StoreItemListAdapter<T> extends BaseAdapter {
    private T[] mStoreItemList;
    private LayoutInflater mInflater;
    private BaseViewHolder.BaseViewListener<StoreItem> storeItemListener;
    private BaseViewHolder.BaseViewListener<StickerStoreItem> stickerItemListener;

    private StorePagerItem.StorePagerType storeType;

    public StoreItemListAdapter() {
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
    public T getItem(int position) {
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

        Object storeItem = getItem(position);

        switch (storeType) {
            case GIFTS:
                convertView = getGiftItemView(convertView, (StoreItem)storeItem);
                break;
            case STICKERS:
                convertView = getStickerItemView(convertView, (StickerStoreItem)storeItem);
                break;
        }

        return convertView;

    }

    public void setStoreItemList(T[] storeItemList) {
        mStoreItemList = storeItemList;
        notifyDataSetChanged();
    }

    public T[] getStoreItemList() {
        return mStoreItemList;
    }

    public void setStoreItemListener(BaseViewHolder.BaseViewListener<StoreItem> storeItemListener) {
        this.storeItemListener = storeItemListener;
    }

    private View getGiftItemView(View convertView, StoreItem storeItem) {
        GiftListViewHolder giftListViewHolder = null;
        if (convertView == null ) {
            convertView = mInflater.inflate(R.layout.holder_gift_list_item, null);
            giftListViewHolder = new GiftListViewHolder(convertView);
            convertView.setTag(R.id.holder, giftListViewHolder);
        } else {
            giftListViewHolder = (GiftListViewHolder) convertView.getTag(R.id.holder);
        }

        giftListViewHolder.setBaseViewListener(storeItemListener);
        giftListViewHolder.setData(storeItem);

        return convertView;
    }

    private View getStickerItemView(View convertView, StickerStoreItem storeItem) {
        StickerListViewHolder stickerListViewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_sticker_list_item, null);
            stickerListViewHolder = new StickerListViewHolder(convertView);
            convertView.setTag(R.id.holder, stickerListViewHolder);

        } else {
            stickerListViewHolder = (StickerListViewHolder) convertView.getTag(R.id.holder);
        }

        if (storeItem != null) {
            stickerListViewHolder.setBaseViewListener(stickerItemListener);
            stickerListViewHolder.setData(storeItem);
        }

        return convertView;
    }

    public void setStoreType(StorePagerItem.StorePagerType storeType) {
        this.storeType = storeType;
    }

    public void setStickerItemListener(BaseViewHolder.BaseViewListener<StickerStoreItem> stickerItemListener) {
        this.stickerItemListener = stickerItemListener;
    }
}
