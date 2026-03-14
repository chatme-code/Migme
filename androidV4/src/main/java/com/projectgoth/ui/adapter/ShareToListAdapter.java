/**
 * Copyright (c) 2013 Project Goth
 *
 * ShareToListAdapter.java
 * Created Feb 27, 2015, 2:12:27 PM
 */

package com.projectgoth.ui.adapter;

import java.util.List;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.model.ShareToItem;
import com.projectgoth.ui.holder.ShareToItemHolder;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


/**
 * @author shiyukun
 *
 */
public class ShareToListAdapter extends BaseAdapter{

    private List<ShareToItem>                   mDataList;
    private LayoutInflater                      mInflater;
    private BaseViewListener<ShareToItem>       mListener;
    
    public ShareToListAdapter() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public int getCount() {
        if (mDataList != null) {
            return mDataList.size();
        }
        return 0;
    }

    @Override
    public ShareToItem getItem(int position) {
        if (mDataList != null && position < mDataList.size()) {
            return mDataList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ShareToItemHolder holder;
        
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_share_to_item, null);
            holder = new ShareToItemHolder(convertView);
            convertView.setTag(R.id.holder, holder);

        } else {
            holder = (ShareToItemHolder) convertView.getTag(R.id.holder);
        }

        ShareToItem shareToItem = (ShareToItem) getItem(position);
        if (shareToItem != null) {
            holder.setData(shareToItem);
            holder.setBaseViewListener(mListener);
        }
        return convertView;
    }
    
    public void setList(List<ShareToItem> dataList) {
        mDataList = dataList;
        notifyDataSetChanged();
    }
    
    public void setListener(BaseViewListener<ShareToItem> listener) {
        this.mListener = listener;
    }

}
