package com.projectgoth.ui.adapter;

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.projectgoth.R;
import com.projectgoth.b.data.mime.GiftMimeData;
import com.projectgoth.ui.fragment.MyGiftsListFragment;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.MyGiftsAllListViewHolder;

import java.util.List;

/**
 * Created by lopenny on 1/23/15.
 */
public class MyGiftsAllListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<GiftMimeData> mItemList;
    private BaseViewListener<GiftMimeData> mHolderListener;

    public MyGiftsAllListAdapter(FragmentActivity fragment) {
        super();
        mInflater = LayoutInflater.from(fragment);
    }

    @Override
    public int getCount() {
        if (mItemList != null) {
            return mItemList.size() > MyGiftsListFragment.DISPLAY_ALL_COUNT ?
                    MyGiftsListFragment.DISPLAY_ALL_COUNT : mItemList.size();
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
        MyGiftsAllListViewHolder viewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_my_gift_all, null);
            viewHolder = new MyGiftsAllListViewHolder(convertView);
            convertView.setTag(R.id.holder, viewHolder);

        } else {
            viewHolder = (MyGiftsAllListViewHolder) convertView.getTag(R.id.holder);
        }

        GiftMimeData giftListItem = (GiftMimeData) getItem(position);
        if (giftListItem != null) {
            viewHolder.setBaseViewListener(mHolderListener);
            viewHolder.setData(giftListItem);
        }

        return convertView;
    }

    public void setItemList(List<GiftMimeData> giftList) {
        mItemList = giftList;
        notifyDataSetChanged();
    }

    public void setItemListListener(BaseViewListener<GiftMimeData> listener) {
        this.mHolderListener = listener;
    }

}
