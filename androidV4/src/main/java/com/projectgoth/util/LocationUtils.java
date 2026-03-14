/**
 * Copyright (c) 2013 Project Goth
 *
 * LocationUtils.java
 * Created Jul 7, 2014, 1:24:39 PM
 */

package com.projectgoth.util;

import android.location.Address;
import com.projectgoth.b.data.Location;
import com.projectgoth.common.TextUtils;
import com.projectgoth.controller.LocationController;
import com.projectgoth.model.LocationListItem;
import com.projectgoth.model.location.Place;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains all helper utilities related to Location
 * 
 * @author angelorohit
 * 
 */
public class LocationUtils {

    /**
     * Provides a string formatted {@link Address}.
     * 
     * @param address
     *            The {@link Address} to be formatted.
     * @return A valid String formatted address or null on failure to parse.
     */
    public static String getFormattedAddress(final Address address) {
        StringBuilder formattedAddress = new StringBuilder();
        if (address != null) {
            final int maxAddressLineIndex = address.getMaxAddressLineIndex();
            for (int i = 0; i < maxAddressLineIndex; ++i) {
                formattedAddress.append(address.getAddressLine(i)).append(", ");
            }
            
            if (maxAddressLineIndex >= 0) {
                formattedAddress.append(address.getAddressLine(maxAddressLineIndex));
            }
        }

        return formattedAddress.toString();
    }
    
    /**
     * Creates an instance of {@link com.projectgoth.b.data.Location} from a
     * given {@link LocationListItem}
     * 
     * @param locationListItem
     *            The {@link LocationListItem} whose details will be used to
     *            populate the newly created
     *            {@link com.projectgoth.b.data.Location}.
     * @return An instance of {@link com.projectgoth.b.data.Location} on success
     *         and null on failure.
     */
    public static Location makeLocationFromLocationListItem(final LocationListItem locationListItem) {
        if (locationListItem != null && !TextUtils.isEmpty(locationListItem.getFormattedLocation())) {
            final LocationListItem.Location loc = locationListItem.getLocation();
            return new Location(loc.getLatitude(), loc.getLongitude(), loc.getElevation(), locationListItem.getFormattedLocation());
        }
        
        return null;
    }

    /**
     * Factory function to create an instance of {@link LocationListItem} from a
     * given {@link Object}
     * 
     * @param data
     *            The {@link Object} data from which a {@link LocationListItem}
     *            is to be made.
     * @return An instance of {@link LocationListItem} or null if the data is an
     *         unsupported type.
     */
    public static LocationListItem makeLocationListItemFromData(final Object data) {
        if (data != null) {
            if (data instanceof Address) {
                return makeLocationListItemFromData((Address) data);
            } else if (data instanceof Place) {
                return makeLocationListItemFromData((Place) data);
            }
            // Add other supported types here...
        }

        // Unsupported type.
        return null;
    }

    /**
     * Creates an instance of {@link LocationListItem} from an object of type
     * {@link Address}.
     * 
     * @param data
     *            The {@link Address} data from which an instance of
     *            {@link LocationListItem} is to be made.
     * @return An instance of {@link LocationListItem} or null if the data
     *         parameter is null.
     */
    public static LocationListItem makeLocationListItemFromData(final Address data) {
        if (data != null) {
            LocationListItem item = new LocationListItem();
            item.setFormattedLocation(LocationUtils.getFormattedAddress(data));

            final double lat = data.getLatitude();
            final double lng = data.getLongitude();
            item.setDistance((int) LocationController.getInstance().getDistanceFromCurrentLocation(lat, lng));
            item.setLocation(new LocationListItem.Location(lat, lng));

            return item;
        }

        return null;
    }

    /**
     * Creates an instance of {@link LocationListItem} from an object of type
     * {@link Place}.
     * 
     * @param data
     *            The {@link Place} data from which an instance of
     *            {@link LocationListItem} is to be made.
     * @return An instance of {@link LocationListItem} or null if the data
     *         parameter is null.
     */
    public static LocationListItem makeLocationListItemFromData(final Place data) {
        if (data != null) {
            LocationListItem item = new LocationListItem();
            item.setFormattedLocation(data.getName());
            if (data.getGeometry() != null) {
                final Place.Location location = data.getGeometry().getLocation();
                
                final double lat = location.getLatitude();
                final double lng = location.getLongitude();
                item.setDistance((int) LocationController.getInstance().getDistanceFromCurrentLocation(lat, lng));
                item.setLocation(new LocationListItem.Location(lat, lng));
                
                return item;
            }
        }

        return null;
    }

    /**
     * Creates a {@link List} of {@link LocationListItem} from a given
     * {@link List} of {@link Object}.
     * 
     * @param dataList
     *            The {@link Object}s from which {@link LocationListItem}s are
     *            to be created.
     * @return A {@link List} of {@link LocationListItem} or null if the dataList parameter is null.
     */
    public static List<LocationListItem> makeLocationListItemsFromDataList(final List<? extends Object> dataList) {        
        if (dataList != null) {
            List<LocationListItem> result = new ArrayList<LocationListItem>();
            for (Object dataItem : dataList) {
                final LocationListItem locationListItem = makeLocationListItemFromData(dataItem);
                if (locationListItem != null) {
                    result.add(locationListItem);
                }
            }
            
            return result;
        }

        return null;
    }
}
