/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftCategoryListAdapter.java
 * Created 27 May, 2014, 9:28:32 am
 */

package com.projectgoth.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.StoreCategory;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.GiftCategoryViewHolder;


/**
 * @author Dan
 *
 */
public class GiftCategoryListAdapter extends BaseAdapter {
    
    private final Object                lock = new Object();
    private LayoutInflater              mInflater;
    StoreCategory[] mGiftCategories;
    private BaseViewListener<StoreCategory> giftCategoryListener;
        
    public GiftCategoryListAdapter() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public int getCount() {
        synchronized (lock) {
            if (mGiftCategories != null) {
                return mGiftCategories.length;
            }
        }
        return 0;
    }

    @Override
    public StoreCategory getItem(int position) {
        synchronized (lock) {
            if (mGiftCategories != null && position < mGiftCategories.length) {
                return mGiftCategories[position];
            }
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GiftCategoryViewHolder giftCategoryViewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_gift_category_item, null);
            giftCategoryViewHolder = new GiftCategoryViewHolder(convertView);
            convertView.setTag(R.id.holder, giftCategoryViewHolder);

        } else {
            giftCategoryViewHolder = (GiftCategoryViewHolder) convertView.getTag(R.id.holder);
        }
        
        StoreCategory giftCategoryItem = getItem(position);
        if (giftCategoryItem != null) {
            giftCategoryViewHolder.setData(giftCategoryItem);
            giftCategoryViewHolder.setBaseViewListener(giftCategoryListener);
        }

        return convertView;
    }
    
    public void setGiftCategoryListener(BaseViewListener<StoreCategory> giftCategoryListener) {
        this.giftCategoryListener = giftCategoryListener;
    }

    public void setGiftCategories(StoreCategory[] giftCategories) {
        synchronized (lock) {
            mGiftCategories = giftCategories;
            notifyDataSetChanged();
        }
    }

}
