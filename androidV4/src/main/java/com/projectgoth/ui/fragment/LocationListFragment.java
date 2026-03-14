/**
 * Copyright (c) 2013 Project Goth
 *
 * LocationListFragment.java
 * Created Jul 11, 2014, 5:13:49 PM
 */

package com.projectgoth.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.TextUtils;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.LocationController;
import com.projectgoth.events.AppEvents;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.LocationListItem;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.adapter.LocationListAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.util.AnimUtils;

import java.util.List;

/**
 * Represents a {@link BaseListSearchFragment} for displaying a list of
 * {@link LocationListItem}.
 * 
 * @author angelorohit
 */
public class LocationListFragment extends BaseListFragment 
                                                implements BaseViewListener<LocationListItem>, View.OnClickListener {

    /**
     * An interface for events related to selection and deselection of the list
     * items.
     * 
     * @author angelorohit
     */
    public interface EventListener {

        void onLocationListItemSelected(final LocationListItem item);

        void onLocationListItemRemoved();
    }

    /**
     * The {@link LocationListItem} that is currently selected by the user.
     */
    private LocationListItem    selectedLocationListItem = null;
    
    /**
     * The adapter for this list.
     */
    private LocationListAdapter adapter;
    private EventListener       listener;

    /**
     * A floating footer view that will be used by the user to deselect an already selected item.
     */
    private View                floatingFooterView       = null;
    
    /**
     * The footer view that will contain the "powered by google" logo.
     */
    private View                footerView               = null;

    private boolean mGPSSettingShown = false;

    public LocationListFragment(final EventListener listener, final LocationListItem selectedLocationListItem) {
        setEventListener(listener);
        setSelectedLocationListItem(selectedLocationListItem);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mMainContainer != null) {

            // Take the focus away from the search box.
            mMainContainer.setFocusable(true);
            mMainContainer.setFocusableInTouchMode(true);

            // Create the floating footer view and add it to the bottom of the main container.
            final LayoutInflater inflater = LayoutInflater.from(ApplicationEx.getContext());
            if (inflater != null) {
                floatingFooterView = inflater.inflate(R.layout.locationlist_floatingfooter, null);
                if (floatingFooterView != null) {
                    mMainContainer.addView(floatingFooterView);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    floatingFooterView.setLayoutParams(params);
                    floatingFooterView.setOnClickListener(this);
                }

                showFloatingFooter(Constants.BLANKSTR, false);
            }
        }

        setDisplayListMessage(I18n.tr("Finding nearby places"));
    }

    @Override
    protected boolean isPullToRefreshEnabled() {
        // Pull-to-refresh is disabled for the Location list.
        return false;
    }

    @Override
    protected BaseAdapter createAdapter() {
        adapter = new LocationListAdapter();
        adapter.setUserClickListener(this);

        return adapter;
    }

    @Override
    public void onRefresh() {
        refreshData(true);
    }

    @Override
    protected void updateListData() {
        refreshData(false);
    }

    private void refreshData(final boolean shouldRefresh) {
        showPoweredByGoogleLogo(false);
        final LocationController locationController = LocationController.getInstance();

        if (locationController != null) {
            final List<LocationListItem> locationListItems = locationController.getNearbyPlaces(false);
            
            if (locationListItems != null) {
                if (!locationListItems.isEmpty()) {
                    for (LocationListItem item : locationListItems) {
                        final boolean isLocationListItemSelected = item.equals(selectedLocationListItem);
                        if (isLocationListItemSelected) {
                            showFloatingFooter(item.getFormattedLocation(), true);
                            item.setChecked(isLocationListItemSelected);
                        }
                    }
                    
                    showDisplayListMessage(false);
                    showPoweredByGoogleLogo(true);
                } else {
                    setDisplayListMessage(I18n.tr("No results"));
                }
                
                
                updateAdapterData(locationListItems);
            }
        }
    }

    private void updateAdapterData(final List<LocationListItem> data) {
        if (adapter != null) {
            adapter.setData(data);
            adapter.notifyDataSetChanged();
        }
    }

    private void showFloatingFooter(final String formattedLocationText, final boolean shouldShow) {
        if (floatingFooterView != null) {
            TextView txtFooter = (TextView) floatingFooterView.findViewById(R.id.txt_location_footer);
            if (txtFooter != null) {
                if (!TextUtils.isEmpty(formattedLocationText)) {
                    txtFooter.setText(formattedLocationText);
                }
            }
            
            AnimUtils.doFadeAnimation(floatingFooterView, shouldShow, null);
            floatingFooterView.setVisibility((shouldShow) ? View.VISIBLE : View.GONE);            
        }
    }

    @Override
    protected void registerReceivers() {
        registerEvent(AppEvents.Location.FETCH_NEARBY_PLACES_COMPLETED);
        registerEvent(AppEvents.Location.FETCH_NEARBY_PLACES_ERROR);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(AppEvents.Location.FETCH_NEARBY_PLACES_COMPLETED)) {
            refreshData(false);
        } else if (action.equals(AppEvents.Location.FETCH_NEARBY_PLACES_ERROR)) {
            final LocationController locationController = LocationController.getInstance();
            if (locationController.isGPSEnabled()) {
                setDisplayListMessage(I18n.tr("No results"));
                Tools.showToastForIntent(context, intent);
            } else {
                if (mGPSSettingShown) {
                    //  if user don't enable GPS and go back app again,
                    //  just finish the search nearby activity
                    getActivity().finish();
                } else {
                    Intent settingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(settingIntent);
                    mGPSSettingShown = true;
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.locationlist_floatingfooter_container:
                onFloatingFooterClicked();
                break;
        }
    }

    @Override
    public void onItemClick(View v, LocationListItem data) {
        switch (v.getId()) {
            case R.id.holder_container:
                onLocationItemClick(data);
                break;
        }
    }

    @Override
    public void onItemLongClick(View v, LocationListItem data) {
        // Nothing to do here
    }

    public void setSelectedLocationListItem(final LocationListItem item) {
        selectedLocationListItem = item;
    }

    public void setEventListener(final EventListener listener) {
        this.listener = listener;
    }

    private void onFloatingFooterClicked() {
        if (adapter != null) {
            adapter.uncheckAllItems();
            adapter.notifyDataSetChanged();
        }

        selectedLocationListItem = null;
        if (listener != null) {
            listener.onLocationListItemRemoved();
        }

        showFloatingFooter(Constants.BLANKSTR, false);
    }

    private void onLocationItemClick(final LocationListItem item) {
        if (item != null) {
            selectedLocationListItem = item;

            if (listener != null) {
                listener.onLocationListItemSelected(selectedLocationListItem);
            }

            // No need to call notifyDataSetChanged on the adapter since
            // the fragment is closed anyway.
            closeFragment();
        }
    }   
    
    public void performFilter(final String filterString) {
        super.performFilter(filterString);
        adapter.filterAndRefresh(filterString);
    }
    
    
    @Override
    protected View createFooterView() {
        LayoutInflater inflater = LayoutInflater.from(ApplicationEx.getContext());   
        footerView = inflater.inflate(R.layout.powered_by_google_footer, null, false);
        return footerView;
    }
    
    private void showPoweredByGoogleLogo(final boolean shouldShow) {
        if (footerView != null) {
            footerView.setVisibility((shouldShow) ? View.VISIBLE : View.GONE);
        }
    }
    
    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = super.getActionBarConfig();
        config.setNavigationButtonState(NavigationButtonState.BACK);
        return config;
    }
    
    @Override
    protected String getTitle() {
        return I18n.tr("Nearby places");
    }
    
    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_location;
    }
}
