/**
 * Copyright (c) 2013 Project Goth
 *
 * LocationListItem.java
 * Created Jul 15, 2014, 1:50:48 PM
 */

package com.projectgoth.model;

import android.widget.Checkable;

/**
 * Represents a Location model solely for UI purposes.
 * 
 * @author angelorohit
 */
public class LocationListItem implements Checkable {

    /**
     * Represents the location for this item.
     * 
     * @author angelorohit
     */
    public static class Location {

        private double latitude;
        private double longitude;
        private double elevation;

        public Location(final double latitude, final double longitude) {
            this(latitude, longitude, 0.0);
        }

        public Location(final double latitude, final double longitude, final double elevation) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.elevation = elevation;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(final double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(final double longitude) {
            this.longitude = longitude;
        }

        public double getElevation() {
            return elevation;
        }

        public void setElevation(final double elevation) {
            this.elevation = elevation;
        }
    }

    private String   formattedLocation;
    private int      distance;
    private boolean  isChecked;
    private Location location;

    public LocationListItem() {
        this(null, -1, false, null);
    }

    /**
     * Constructor.
     * 
     * @param formattedLocation
     *            A String representation of a formatted address location.
     * @param distance
     *            The distance (in meters) between this location and the current
     *            location.
     * @param isChecked
     *            Whether this {@link LocationListItem} was selected by the
     *            user.
     */
    public LocationListItem(final String formattedLocation, final int distance, final boolean isChecked,
            final Location location) {
        this.formattedLocation = formattedLocation;
        this.distance = distance;
        this.isChecked = isChecked;
        this.location = location;
    }

    /**
     * A String representation of a formatted address location.
     * 
     * @return the formattedLocation
     */
    public String getFormattedLocation() {
        return formattedLocation;
    }

    /**
     * @param formattedLocation
     *            the formattedLocation to set
     */
    public void setFormattedLocation(final String formattedLocation) {
        this.formattedLocation = formattedLocation;
    }

    /**
     * The distance (in meters) between this location and the current location.
     */
    public int getDistance() {
        return distance;
    }

    /**
     * @param distance
     *            the distance to set
     */
    public void setDistance(final int distance) {
        this.distance = distance;
    }

    /**
     * @return The {@link LocationListItem.Location} for this item. Can be null.
     */
    public Location getLocation() {
        return this.location;
    }

    /**
     * @param location
     *            The {@link LocationListItem.Location} to be set for this item.
     *            Can be null.
     */
    public void setLocation(final Location location) {
        this.location = location;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((formattedLocation == null) ? 0 : formattedLocation.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof LocationListItem)) {
            return false;
        }
        LocationListItem other = (LocationListItem) obj;
        if (formattedLocation == null) {
            if (other.formattedLocation != null) {
                return false;
            }
        } else if (!formattedLocation.equals(other.formattedLocation)) {
            return false;
        }
        return true;
    }

    /**
     * @see android.widget.Checkable#setChecked(boolean)
     */
    @Override
    public void setChecked(final boolean checked) {
        isChecked = checked;
    }

    /**
     * @see android.widget.Checkable#isChecked()
     */
    @Override
    public boolean isChecked() {
        return isChecked;
    }

    /**
     * @see android.widget.Checkable#toggle()
     */
    @Override
    public void toggle() {
        // Do nothing
    }
}
