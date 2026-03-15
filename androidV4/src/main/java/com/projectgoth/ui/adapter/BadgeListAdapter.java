/**
 * Copyright (c) 2013 Project Goth
 *
 * BadgeGridAdapter.java
 * Created Aug 22, 2013, 4:07:20 PM
 */

package com.projectgoth.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Badge;
import com.projectgoth.ui.holder.BadgeViewHolder;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author dangui
 * 
 */
public class BadgeListAdapter extends BaseAdapter {

    private LayoutInflater          mInflater;
    private List<Badge>             badgeList = new ArrayList<Badge>();
    private BaseViewListener<Badge> bagdeViewListener;

    public BadgeListAdapter() {
        super();
        mInflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    @Override
    public int getCount() {
        if (badgeList != null) {
            return badgeList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int pos) {
        if (badgeList != null && pos < getCount()) {
            return badgeList.get(pos);
        }
        return null;
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        BadgeViewHolder badgeViewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.holder_badge_item, null);
            badgeViewHolder = new BadgeViewHolder(convertView);
            convertView.setTag(R.id.holder, badgeViewHolder);
        } else {
            badgeViewHolder = (BadgeViewHolder) convertView.getTag(R.id.holder);
        }

        Badge badge = (Badge) getItem(pos);

        badgeViewHolder.setData(badge);
        badgeViewHolder.setBaseViewListener(bagdeViewListener);

        return convertView;
    }

    /**
     * @param badges
     */
    public void setBadges(List<Badge> badges) {
        this.badgeList = badges;
        sortBadges(badgeList);
        notifyDataSetChanged();
    }

    /**
     * Returns an immutable list of the badges contained in this adapter for
     * display.
     * 
     * @return A List of {@link Badge}
     */
    public List<Badge> getBadges() {
        return Collections.unmodifiableList(this.badgeList);
    }

    /**
     * sort badges base on the unlock time stamp
     * 
     * @param badgeList
     */
    private void sortBadges(List<Badge> badgeList) {
        Collections.sort(badgeList, new Comparator<Badge>() {

            @Override
            public int compare(Badge lhs, Badge rhs) {
                Long lhsUnlockTimestamp = lhs.getUnlockedTimestamp();
                Long rhsUnlockTimestamp = rhs.getUnlockedTimestamp();

                // time stamp null means not unlocked yet
                if (lhsUnlockTimestamp == null && rhsUnlockTimestamp == null) {
                    return 0;
                } else if (lhsUnlockTimestamp == null && rhsUnlockTimestamp != null) {
                    return 1;
                } else if (lhsUnlockTimestamp != null && rhsUnlockTimestamp == null) {
                    return -1;
                } else if (lhsUnlockTimestamp != null && rhsUnlockTimestamp != null) {
                    if (lhsUnlockTimestamp.longValue() > rhsUnlockTimestamp.longValue()) {
                        return -1;
                    } else if (lhsUnlockTimestamp.longValue() < rhsUnlockTimestamp.longValue()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }

                return 0;
            }
        });
    }

    public void setBagdeViewListener(BaseViewListener<Badge> bagdeViewListener) {
        this.bagdeViewListener = bagdeViewListener;
    }
}
