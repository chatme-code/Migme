/**
 * Copyright (c) 2013 Project Goth
 * MyGiftsOverviewFilterAdapter.java
 * Created Jan 26, 2015, 2:15:46 PM
 */

package com.projectgoth.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.model.MyGiftsFilterItem;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.MyGiftsOverviewFilterViewHolder;

import java.util.List;

/**
 * @author mapet
 */
public class MyGiftsOverviewFilterAdapter extends BaseAdapter {

    private List<MyGiftsFilterItem> mDataList;
    private LayoutInflater mInflater;
    private BaseViewListener<MyGiftsFilterItem> mListener;

    public MyGiftsOverviewFilterAdapter() {
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
    public MyGiftsFilterItem getItem(int position) {
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
        MyGiftsOverviewFilterViewHolder filterListViewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_store_filter_item, null);
            filterListViewHolder = new MyGiftsOverviewFilterViewHolder(convertView);
            convertView.setTag(R.id.holder, filterListViewHolder);

        } else {
            filterListViewHolder = (MyGiftsOverviewFilterViewHolder) convertView.getTag(R.id.holder);
        }

        MyGiftsFilterItem myGiftsFilterItem = (MyGiftsFilterItem) getItem(position);
        if (myGiftsFilterItem != null) {
            filterListViewHolder.setBaseViewListener(mListener);
            filterListViewHolder.setData(myGiftsFilterItem);
        }

        return convertView;
    }

    public void setList(List<MyGiftsFilterItem> dataList) {
        mDataList = dataList;
        notifyDataSetChanged();
    }

    public void setMyGiftsOverviewListener(BaseViewListener<MyGiftsFilterItem> listener) {
        this.mListener = listener;
    }

}
