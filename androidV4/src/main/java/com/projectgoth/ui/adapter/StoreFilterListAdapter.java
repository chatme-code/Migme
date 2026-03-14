package com.projectgoth.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.model.StoreFilterItem;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.FilterListViewHolder;

import java.util.List;

public class StoreFilterListAdapter extends BaseAdapter {

    private List<StoreFilterItem>               mDataList;
    private LayoutInflater                      mInflater;
    private BaseViewListener<StoreFilterItem>   giftListListener;

    public StoreFilterListAdapter() {
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
    public StoreFilterItem getItem(int position) {
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
        FilterListViewHolder filterListViewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_store_filter_item, null);
            filterListViewHolder = new FilterListViewHolder(convertView);
            convertView.setTag(R.id.holder, filterListViewHolder);

        } else {
            filterListViewHolder = (FilterListViewHolder) convertView.getTag(R.id.holder);
        }

        StoreFilterItem giftListItem = (StoreFilterItem) getItem(position);
        if (giftListItem != null) {
            filterListViewHolder.setBaseViewListener(giftListListener);
            filterListViewHolder.setData(giftListItem);
        }

        return convertView;
    }

    public void setList(List<StoreFilterItem> dataList) {
        mDataList = dataList;
        notifyDataSetChanged();
    }

    public void setGiftListListener(BaseViewListener<StoreFilterItem> giftListListener) {
        this.giftListListener = giftListListener;
    }

}
