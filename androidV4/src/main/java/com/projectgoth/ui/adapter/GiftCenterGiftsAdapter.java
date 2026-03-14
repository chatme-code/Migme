/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftCenterGiftsAdapter.java
 * Created 9 May, 2014, 10:24:35 am
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
import com.projectgoth.ui.holder.GiftsRowViewHolder;

/**
 * @author dan
 *  to add a footer to a ListView is easy, but unfortunately, to add a footer to a GridView is very hard
 *  a workaround is just using ListView, then every item of the ListView is a LinearLayout to make it look the same as a GridView
 *  This is an Adapter of the ListView that makes it looks like a GridView 
 */
public class GiftCenterGiftsAdapter extends BaseAdapter {

    private LayoutInflater   mInflater;
    private StoreItem[]      mGiftItems;
    private BaseViewListener<StoreItem> giftViewListener;
    
    private static final int COLUMN_NUM = 4;

    public GiftCenterGiftsAdapter() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public int getCount() {
        if (mGiftItems != null) {
            int count;
            int dataNum = mGiftItems.length;
            if (dataNum % COLUMN_NUM == 0) {
                count = dataNum / COLUMN_NUM;
            } else {
                count = dataNum / COLUMN_NUM + 1;
            }
            return count;
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        StoreItem[] items = new StoreItem[COLUMN_NUM];
        int start = position * COLUMN_NUM;
        //get the items of the row, null if no more items
        for (int i = 0; i < COLUMN_NUM; i++) {
            int index = i + start;
            if (index < mGiftItems.length) {
                items[i] = mGiftItems[index];
            } else {
                items[i] = null;
            }
        }
        return items;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GiftsRowViewHolder viewHolder;
        
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_gifts_row, null);
            viewHolder = new GiftsRowViewHolder(convertView, COLUMN_NUM);
            convertView.setTag(R.id.holder, viewHolder);
        } else {
            viewHolder = (GiftsRowViewHolder) convertView.getTag(R.id.holder);
        }
        
        StoreItem[] items = (StoreItem[]) getItem(position);
        if (items != null) {
            viewHolder.setGiftViewListener(giftViewListener);
            viewHolder.setData(items);            
        }
        
        return convertView;
    }

    public void setGiftList(StoreItem[] giftItems) {
        mGiftItems = giftItems;
        notifyDataSetChanged();

    }

    public void setGiftViewListener(BaseViewListener<StoreItem> giftViewListener) {
        this.giftViewListener = giftViewListener;
    }
}
