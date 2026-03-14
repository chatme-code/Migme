package com.projectgoth.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.StoreItem;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.SendGiftItemViewHolder;

/**
 * Created by houdangui on 18/6/15.
 */
public class HorizontalGiftListAdapter extends BaseAdapter {

    private StoreItem[] mGiftItems = new StoreItem[0];
    private LayoutInflater mInflater;

    private BaseViewListener<StoreItem> mItemClickListener;

    private StoreItem mSelectedItem;

    public HorizontalGiftListAdapter() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public int getCount() {
        return mGiftItems.length;
    }

    @Override
    public Object getItem(int position) {
        return mGiftItems[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SendGiftItemViewHolder viewHolder = null;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_send_gift_item, null);
            viewHolder = new SendGiftItemViewHolder(convertView);
            convertView.setTag(R.id.holder, viewHolder);
        } else {
            viewHolder = (SendGiftItemViewHolder) convertView.getTag(R.id.holder);
        }

        StoreItem storeItem = (StoreItem) getItem(position);
        viewHolder.setData(storeItem);

        viewHolder.setIsSelected(mSelectedItem == storeItem);

        viewHolder.setBaseViewListener(mItemClickListener);

        return convertView;
    }

    public void setGiftItems(StoreItem[] giftItems) {
        mGiftItems = giftItems;
    }

    public void setItemClickListener(BaseViewListener<StoreItem> itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    public void setSelectedItem(StoreItem selectedItem) {
        this.mSelectedItem = selectedItem;
    }
}
