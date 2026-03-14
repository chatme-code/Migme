package com.projectgoth.ui.adapter;

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.b.data.UserGiftStat;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewPositionListener;
import com.projectgoth.ui.holder.MyGiftsTimeListViewHolder;
import com.projectgoth.ui.widget.TimelineView;

import java.util.List;

/**
 * Created by lopenny on 1/26/15.
 */
public class MyGiftsTimeListAdapter extends BaseExpandableListAdapter {

    private LayoutInflater           mInflater;

    private List<UserGiftStat>       mYearList;
    private List<List<UserGiftStat>> mItemList;
    private BaseViewPositionListener<UserGiftStat> mItemListener;

    public MyGiftsTimeListAdapter(FragmentActivity fragment) {
        super();
        mInflater = LayoutInflater.from(fragment);
    }

    @Override
    public UserGiftStat getGroup(int groupPosition) {
        //year
        if (mYearList != null && groupPosition < mYearList.size()) {
            return mYearList.get(groupPosition);
        }
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (mItemList != null && groupPosition < mItemList.size()) {
            List<UserGiftStat> items = mItemList.get(groupPosition);
            if (items != null && childPosition < items.size()) {
                return items.get(childPosition);
            }
        }
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                             ViewGroup parent) {
        MyGiftsTimeListViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_my_gift_time, null);
            viewHolder = new MyGiftsTimeListViewHolder(convertView);
            convertView.setTag(R.id.holder, viewHolder);
        } else {
            viewHolder = (MyGiftsTimeListViewHolder) convertView.getTag(R.id.holder);
        }
        UserGiftStat statData = (UserGiftStat) getChild(groupPosition, childPosition);
        if (statData != null) {
            TimelineView.Type type = TimelineView.Type.CENTRAL;
            if (childPosition == 0) {
                if (getChildrenCount(groupPosition) == 1) {
                    type = TimelineView.Type.SINGLE;
                } else {
                    type = TimelineView.Type.UPPER;
                }
            } else if (childPosition == getChildrenCount(groupPosition) - 1) {
                type = TimelineView.Type.BOTTOM;
            }
            viewHolder.setData(groupPosition, childPosition, type, statData);
            viewHolder.setBaseViewPositionListener(mItemListener);
        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (mItemList != null) {
            if (groupPosition < mItemList.size() && mItemList.get(groupPosition) != null) {
                return mItemList.get(groupPosition).size();
            }
        }
        return 0;
    }

    @Override
    public int getGroupCount() {
        if (mYearList != null) {
            return mYearList.size();
        }
        return 0;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_my_gift_time_category, null);
        }
        TextView title = (TextView) convertView.findViewById(R.id.time_title);
        title.setText(getGroup(groupPosition).getTitle());
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void setYearList(List<UserGiftStat> yearList) {
        mYearList = yearList;
    }

    public void setAllItemStatsList(List<List<UserGiftStat>> allItemStatsList) {
        mItemList = allItemStatsList;
        notifyDataSetChanged();
    }

    public void setItemListListener(BaseViewPositionListener<UserGiftStat> listener) {
        this.mItemListener = listener;
    }
}
