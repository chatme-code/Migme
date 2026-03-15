package com.projectgoth.ui.adapter;

import java.util.List;

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.projectgoth.R;
import com.projectgoth.b.data.GiftCategoryItem;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewPositionListener;
import com.projectgoth.ui.holder.MyGiftsCategoryListViewHolder;

/**
 * Created by lopenny on 1/27/15.
 */
public class MyGiftsCategoryListAdapter extends BaseAdapter {

    private LayoutInflater           mInflater;

    private int                      mSelectedIdx = 0;
    private List<GiftCategoryItem>   mItemList;
    private BaseViewPositionListener<GiftCategoryItem> mHolderListener;

    public MyGiftsCategoryListAdapter(FragmentActivity fragment) {
        super();
        mInflater = LayoutInflater.from(fragment);
    }

    @Override
    public int getCount() {
        if (mItemList != null) {
            return mItemList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mItemList != null && position < mItemList.size()) {
            return mItemList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyGiftsCategoryListViewHolder viewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_my_gift_category, null);
            viewHolder = new MyGiftsCategoryListViewHolder(convertView);
            convertView.setTag(R.id.holder, viewHolder);

        } else {
            viewHolder = (MyGiftsCategoryListViewHolder) convertView.getTag(R.id.holder);
        }

        GiftCategoryItem giftListItem = (GiftCategoryItem) getItem(position);
        if (giftListItem != null) {
            viewHolder.setBaseViewPositionListener(mHolderListener);
            viewHolder.setData(position, giftListItem, (mSelectedIdx == position));
        }

        return convertView;
    }

    public void setItemList(int selectedIdx, List<GiftCategoryItem> giftList) {
        mSelectedIdx = selectedIdx;
        mItemList = giftList;
        notifyDataSetChanged();
    }

    public void setItemListListener(BaseViewPositionListener<GiftCategoryItem> listener) {
        this.mHolderListener = listener;
    }
}
