/**
 * Copyright (c) 2013 Project Goth
 *
 * Place.java
 * Created Jul 23, 2014, 3:07:35 PM
 */

package com.projectgoth.model.location;

/**
 * A model that represents a single Place in the results of a Google places search.
 * 
 * @see <a
 *      href="https://developers.google.com/places/documentation/search#PlaceSearchResults">Google
 *      Place Search Results</a>
 * @author angelorohit
 **/
public class Place {

    /**
     * A unique identifier for a place.
     */
    private String   place_id;

    /**
     * Contains the human-readable name for a place.
     */
    private String   name;

    /**
     * Contains the URL of a recommended icon which may be displayed to the user
     * when indicating this place.
     */
    private String   icon;

    /**
     * Contains a feature name of a nearby location.
     */
    private String   vicinity;

    /**
     * Contains geometry information about the place, generally including the location (geocode) of the place.
     */
    private Geometry geometry;

    public String getPlaceId() {
        return place_id;
    }

    public void setPlaceId(String id) {
        this.place_id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(final String icon) {
        this.icon = icon;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    @Override
    public String toString() {
        return name + " - " + place_id;
    }

    public static class Geometry {

        private Location location;

        public Location getLocation() {
            return this.location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }
    }

    public static class Location {

        private double lat;

        private double lng;

        public double getLatitude() {
            return lat;
        }

        public void setLatitude(double latitude) {
            this.lat = latitude;
        }

        public double getLongitude() {
            return lng;
        }

        public void setLongitude(double longitude) {
            this.lng = longitude;
        }
    }

}
