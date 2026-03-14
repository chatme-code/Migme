/**
 * Copyright (c) 2013 Project Goth
 *
 * LocationListAdapter.java
 * Created Jul 11, 2014, 5:25:51 PM
 */

package com.projectgoth.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Checkable;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.TextUtils;
import com.projectgoth.model.LocationListItem;
import com.projectgoth.ui.fragment.LocationListFragment;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.holder.LocationItemViewHolder;
import com.projectgoth.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the adapter for the {@link LocationListFragment}
 * @author angelorohit
 * 
 */
public class LocationListAdapter extends BaseAdapter {

    private List<? extends Checkable>          data           = new ArrayList<Checkable>();
    private List<? extends Checkable>          unfilteredData = new ArrayList<Checkable>();
    private LayoutInflater                     inflater;
    private BaseViewListener<LocationListItem> listener;

    public LocationListAdapter() {
        super();
        inflater = LayoutInflater.from(ApplicationEx.getContext());
    }

    /**
     * Sets the data to be used for each item in this adapter. Can be null.
     * 
     * @param data
     *            A {@link List} of {@link Checkable} to be used as the data.
     */
    public void setData(final List<? extends Checkable> data) {
        if (data != null) {
            this.unfilteredData = data;
            this.data = data;

            notifyDataSetChanged();
        }
    }
    
    public void setFilteredData(final List<? extends Checkable> data) {
        if (data != null) {
            this.data = data;

            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        if (data != null) {
            return data.size();
        }

        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (data != null && position < getCount()) {
            return data.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LocationItemViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.holder_location_list_item, null);
            holder = new LocationItemViewHolder(convertView);
            convertView.setTag(R.id.holder, holder);
        } else {
            holder = (LocationItemViewHolder) convertView.getTag(R.id.holder);
        }

        final LocationListItem data = (LocationListItem) getItem(position);
        holder.setData(data);
        holder.setBaseViewListener(listener);

        return convertView;
    }

    /**
     * Sets an external {@link BaseViewListener} to be informed of clicks and
     * long clicks on each item in the adapter's list.
     * 
     * @param listener
     *            The {@link BaseViewListener} to be set.
     */
    public void setUserClickListener(BaseViewListener<LocationListItem> listener) {
        this.listener = listener;
    }

    /**
     * Unchecks all the {@link Checkable} in data.
     */
    public void uncheckAllItems() {
        if (unfilteredData != null && !unfilteredData.isEmpty()) {
            for (Checkable item : unfilteredData) {
                item.setChecked(false);
            }
        }
    }

    public void filterAndRefresh(final String filterText) {
        if (TextUtils.isEmpty(filterText)) {
            setFilteredData(unfilteredData);

        } else {
            final List<LocationListItem> filteredData = getFilteredData(filterText);
            setFilteredData(filteredData);
        }
    }

    private List<LocationListItem> getFilteredData(final String filterText) {
        List<LocationListItem> resultList = new ArrayList<LocationListItem>();

        if (unfilteredData != null) {
            for (Checkable item : unfilteredData) {
                if (item instanceof LocationListItem) {
                    LocationListItem locationListItem = (LocationListItem) item;
                    final String locationName = locationListItem.getFormattedLocation();
                    if (StringUtils.containsIgnoreCase(locationName, filterText)) {
                        resultList.add(locationListItem);
                    }
                }
            }
        }

        return resultList;
    }
}
