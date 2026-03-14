/**
 * Copyright (c) 2013 Project Goth
 *
 * LocationController.java
 * Created Jul 4, 2014, 4:20:26 PM
 */

package com.projectgoth.controller;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Logger;
import com.projectgoth.controller.GooglePlacesController.GooglePlacesUpdateListener;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.model.LocationListItem;
import com.projectgoth.model.location.Place;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.LocationUtils;
import com.projectgoth.util.scheduler.JobScheduler.ScheduleListener;
import com.projectgoth.util.scheduler.ScheduledJobsHandler;
import com.projectgoth.util.scheduler.ScheduledJobsHandler.ScheduledJobKeys;

import java.util.List;

/**
 * Manages fetching, caching and manipulation related to location. Currently: -
 * Fetches the current location of the device. - Gets reverse geocoded addresses
 * near the current location.
 * 
 * @author angelorohit
 * 
 */
public class LocationController {

    private final static String             LOG_TAG                         = AndroidLogger
                                                                                    .makeLogTag(LocationController.class);

    // A lock that is obtained when working with any of the caches.
    private static final Object             CACHE_LOCK                      = new Object();

    private final static LocationController INSTANCE                        = new LocationController();

    private LocationManager                 locationManager                 = null;
    private Geocoder                        geocoder                        = null;

    /**
     * The last known location that was previously fetched. A value of null
     * indicates that the the location was either not fetched or failed to be
     * fetched. This value can expire.
     * 
     * @see {@link #LAST_KNOWN_LOCATION_EXPIRY}
     */
    private Location                        lastKnownLocation               = null;

    /**
     * A cache of last known reverse geocoded addresses that was previously
     * fetched. A value of null indicates that the last known addresses was
     * either not fetched or failed to be fetched. This cache does not expire.
     */
    private List<Address>                   reverseGeocodedAddressCache     = null;

    /** Used to indicate that data will not expire */
    private static final int                NO_EXPIRY                       = -1;

    /**
     * The time within which the data held in {@link #lastKnownLocation} will
     * expire (in ms). A value of {@link #NO_EXPIRY} indicates that the data
     * will not expire.
     */
    private static final long               LAST_KNOWN_LOCATION_EXPIRY      = 1 * 60 * 1000;

    /**
     * The timestamp when the {@link #lastKnownLocation} was last updated.
     */
    private long                            lastKnownLocationUpdateTime     = System.currentTimeMillis();
    
    /**
     * Indicates whether the last fetch for nearby google places failed or not.
     */
    private boolean                         didGooglePlacesFetchFail        = false;

    /** The minimum distance to change updates (in meters) */
    private static final long               MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    /** The minimum time beetwen updates (in milliseconds) */
    private static final long               MIN_TIME_BW_UPDATES             = 1000 * 60 * 1;

    /**
     * The max number of reverse geocoded addresses that can be fetched at a
     * time.
     */
    private static final int                MAX_NEARBY_PLACES               = 15;

    /**
     * Used for listening to location fetch and error events.
     * 
     * @author angelorohit
     * 
     */
    private interface LocationUpdateListener {

        void onReceived(final Location location);

        void onError();
    };

    /**
     * Used for listening to reverse geocode address fetch and error events.
     * 
     * @author angelorohit
     * 
     */
    private interface ReverseGeocodeUpdateListener {

        void onReceived(final List<Address> reverseGeocodedAddressList, final Location location);

        void onError(final Location location);
    }
    
    /**
     * When trying to get data via {@link #getNearbyGooglePlaces(boolean)} or
     * {@link #getReverseGeocodedAddresses(boolean)}, the last known location
     * may be unknown or expired. This listener will be used to inform of
     * location data updates data in such cases.
     */
    private LocationUpdateListener locationUpdateListener = new LocationUpdateListener() {
        
        @Override
        public void onReceived(Location location) {
            
            resetDidGooglePlacesFetchFail();
            // Send a success broadcast so that listeners can attempt to retrieve necessary data again.
            BroadcastHandler.Location.sendFetchNearbyPlacesCompleted();
        }
        
        @Override
        public void onError() {
            
            resetDidGooglePlacesFetchFail();
            // Failed to fetch the current location. We can't fetch nearby places.
            BroadcastHandler.Location.sendFetchNearbyPlacesError();
        }
    };
    
    /**
     * Used from {@link #getNearbyGooglePlaces(boolean)} as listener for success or failure.
     * On failure, an attempt is made to get nearby places by reverse geocoding the user's last known location.
     */
    private GooglePlacesUpdateListener nearbyGooglePlacesUpdateListener = new GooglePlacesUpdateListener() {
        
        @Override
        public void onReceived() {
            
            resetDidGooglePlacesFetchFail();
            // Send a success broadcast so that listeners can attempt to retrieve necessary data again.
            BroadcastHandler.Location.sendFetchNearbyPlacesCompleted();
        }
        
        @Override
        public void onError() {
            // Send a success broadcast so that listeners can attempt to retrieve necessary data again.
            // Now that google places has fetching has failed, the reverse geocoded addresses will be fetched instead.
            setDidGooglePlacesFetchFail(true);
            BroadcastHandler.Location.sendFetchNearbyPlacesCompleted();
        }
    };
    
    /**
     * Constructor
     */
    private LocationController() {
        locationManager = (LocationManager) ApplicationEx.getContext().getSystemService(
                Context.LOCATION_SERVICE);
    }

    /**
     * A single point of entry for this controller.
     * 
     * @return An instance of the controller.
     */
    public static synchronized LocationController getInstance() {
        return INSTANCE;
    }

    /**
     * Gets the current location of the device.
     * 
     * @param shouldFetch
     *            true to force fetching of the current location, false to just
     *            use the {@link #lastKnownLocation}.
     * @return The current location or null on failure.
     */
    public Location getCurrentLocation(final boolean shouldFetch) {
        return getCurrentLocation(shouldFetch, null);
    }

    /**
     * Gets the current location of the device and can optionally get reverse
     * geocoded addresses on success.
     * 
     * @param shouldFetch
     *            true to force fetching of the current location, false to just
     *            use the {@link #lastKnownLocation}.
     * @param shouldGetReverseGeocodedAddresses
     *            true to get reverse geocoded addresses on success and false
     *            otherwise.
     * @return The current location or null on failure.
     */
    private Location getCurrentLocation(final boolean shouldFetch, final LocationUpdateListener locationUpdateListener) {
        synchronized (CACHE_LOCK) {
            if (lastKnownLocation == null || shouldFetch || isLastKnownLocationExpired()) {
                fetchCurrentLocation(new LocationUpdateListener() {

                    @Override
                    public void onReceived(final Location location) {
                        Logger.debug.log(LOG_TAG, "Current location: " + location.toString());
                        setLastKnownLocation(location);

                        // The last known reverse geocoded addresses are reset
                        // because the
                        // current location has changed.
                        setReverseGeocodedAddressCache(null);

                        if (locationUpdateListener != null) {
                            locationUpdateListener.onReceived(location);
                        }

                        BroadcastHandler.Location.sendReceived();
                    }

                    @Override
                    public void onError() {
                        if (locationUpdateListener != null) {
                            locationUpdateListener.onError();
                        }
                        
                        BroadcastHandler.Location.sendFetchError();
                    }

                });
            }

            return lastKnownLocation;
        }
    }

    /**
     * Sets the {@link #lastKnownLocation} and update its
     * {@link #lastKnownLocationUpdateTime}.
     * 
     * @param location
     *            The {@link Location} to be set.
     */
    private void setLastKnownLocation(final Location location) {
        synchronized (CACHE_LOCK) {
            lastKnownLocation = location;
            lastKnownLocationUpdateTime = System.currentTimeMillis();
        }
    }

    /**
     * Sets the {@link #reverseGeocodedAddressCache}.
     * 
     * @param cache
     *            A {@link List} containing the addresses to be set.
     */
    private void setReverseGeocodedAddressCache(final List<Address> cache) {
        synchronized (CACHE_LOCK) {
            reverseGeocodedAddressCache = cache;
        }
    }
    
    /**
     * @return true if the last fetch for nearby google places failed and false otherwise.
     */
    private boolean didGooglePlacesFetchFail() {
        synchronized (CACHE_LOCK) {
            return didGooglePlacesFetchFail;
        }
    }
    
    private void setDidGooglePlacesFetchFail(final boolean state) {
        synchronized (CACHE_LOCK) {
            didGooglePlacesFetchFail = state;
        }
    }
    
    private void resetDidGooglePlacesFetchFail() {
        setDidGooglePlacesFetchFail(false);
    }

    /**
     * Gets a {@link List} of reverse geocoded addresses based on the current
     * location of the device.
     * 
     * @param shouldFetch
     *            true to force fetching of reverse geocoded addresses, false to
     *            just use the previously fetched cache.
     * @return A {@link List} of {@link Address} containing reverse geocoded
     *         addresses or null if appropriate data was not immediately found in
     *         cache.
     */
    public List<Address> getReverseGeocodedAddresses(final boolean shouldFetch) {
        synchronized (CACHE_LOCK) {
            // We don't care about expiry of cached reverse geocoded addresses
            // because those will
            // need to be recomputed if the location changes anyway.
            if (reverseGeocodedAddressCache == null || shouldFetch) {
                if (lastKnownLocation == null || isLastKnownLocationExpired()) {
                    // If the last known location was not previously fetched or
                    // has expired, we force fetch it again.
                    getCurrentLocation(true, locationUpdateListener);
                } else {
                    fetchReverseGeocodedAddresses(new ReverseGeocodeUpdateListener() {

                        @Override
                        public void onReceived(final List<Address> addressList, final Location location) {
                            setReverseGeocodedAddressCache(addressList);
                            BroadcastHandler.Location.sendFetchNearbyPlacesCompleted();
                        }

                        @Override
                        public void onError(final Location location) {
                            BroadcastHandler.Location.sendFetchNearbyPlacesError();
                        }
                    }, lastKnownLocation, MAX_NEARBY_PLACES);
                }
            }
            
            return reverseGeocodedAddressCache;
        }
    }

    /**
     * Gets nearby places using {@link GooglePlacesController}.
     * 
     * @param shouldFetch
     *            true to force fetching of nearby places, false to just use
     *            cached data.
     * @return A {@link List} of {@link Place} containing nearby places or null
     *         if appropriate data was not immediately found in cache.
     */
    public List<Place> getNearbyGooglePlaces(final boolean shouldFetch) {
        synchronized (CACHE_LOCK) {
            if (lastKnownLocation == null || isLastKnownLocationExpired()) {
                // If the last known location was not previously fetched or
                // has expired, we force fetch it again.
                getCurrentLocation(true, locationUpdateListener);
            } else {
                return GooglePlacesController.getInstance().getNearbyPlacesForLocation(lastKnownLocation.getLatitude(),
                        lastKnownLocation.getLongitude(), shouldFetch, nearbyGooglePlacesUpdateListener);
            }
        }

        return null;
    }
    
    public List<LocationListItem> getNearbyPlaces(final boolean shouldFetch) {
        if (didGooglePlacesFetchFail() == true) {
            return getNearbyPlaces(true, shouldFetch);
        } else {
            return getNearbyPlaces(false, shouldFetch);
        }
    }
    
    /**
     * Gets a list of nearby places. Currently uses
     * {@link #getNearbyGooglePlaces(boolean)}.
     * 
     * @param shouldFetch
     *            true to force fetching of nearby places, false to just use
     *            cached data.
     * @return A {@link List} of {@link LocationListItem}. This data is ready
     *         for the UI to present. If no appropriate data is available in cache, null is returned.
     */
    public List<LocationListItem> getNearbyPlaces(final boolean useReverseGeocodedAddresses, final boolean shouldFetch) {
        List<LocationListItem> result = null;
        if (useReverseGeocodedAddresses) {
            final List<Address> addressList = getReverseGeocodedAddresses(shouldFetch);
            result = LocationUtils.makeLocationListItemsFromDataList(addressList);
        } else {
            final List<Place> placeList = getNearbyGooglePlaces(shouldFetch);
            result = LocationUtils.makeLocationListItemsFromDataList(placeList);
        }
        
        if (result != null) {
            resetDidGooglePlacesFetchFail();
        }
        
        return result;
    }

    /**
     * @return true if the last known location has expired and false otherwise.
     */
    private boolean isLastKnownLocationExpired() {
        synchronized (CACHE_LOCK) {
            return (LAST_KNOWN_LOCATION_EXPIRY > NO_EXPIRY && ((System.currentTimeMillis() - lastKnownLocationUpdateTime) > LAST_KNOWN_LOCATION_EXPIRY));
        }
    }

    /**
     * Determines an appropriate location provider and fetches the current
     * location of the device.
     * 
     * @param locationUpdateListener
     *            A {@link LocationUpdateListener} that will be used to inform
     *            the caller of the results of fetching.
     */
    private void fetchCurrentLocation(final LocationUpdateListener locationUpdateListener) {
        ScheduledJobsHandler.getInstance().startJobWithKey(ScheduledJobKeys.GET_CURRENT_LOCATION,
                new ScheduleListener() {

                    @Override
                    public void processJob() {
                        Location currentLocation = null;
                        try {
                            if (locationManager != null) {
                                // Check whether GPS is enabled.
                                final boolean isGPSEnabled = locationManager
                                        .isProviderEnabled(LocationManager.GPS_PROVIDER);

                                // Check whether Network is enabled.
                                final boolean isNetworkEnabled = locationManager
                                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                                if (!isGPSEnabled && !isNetworkEnabled) {
                                    Logger.debug.log(LOG_TAG, "No location provider available!");
                                } else {

                                    // Prioritize getting location from Network
                                    // Provider.
                                    if (isNetworkEnabled) {
                                        currentLocation = requestOneTimeLocationUpdateWithProvider(LocationManager.NETWORK_PROVIDER);
                                        Logger.debug.log(LOG_TAG, "NETWORK was used to get last known location");
                                    }

                                    if (isGPSEnabled && currentLocation == null) {
                                        currentLocation = requestOneTimeLocationUpdateWithProvider(LocationManager.GPS_PROVIDER);
                                        Logger.debug.log(LOG_TAG, "GPS was used to get last known location");
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Logger.error.log(LOG_TAG, e);
                        }

                        if (currentLocation != null) {
                            locationUpdateListener.onReceived(currentLocation);
                        } else {
                            locationUpdateListener.onError();
                        }
                    }
                }, 1L, false);
    }

    /**
     * Performs a one-time request for a location update with given parameters.
     * 
     * @param locationProvider
     *            A provider to be used for fetching the location.
     * @return A {@link Location} containing the current location.
     */
    private Location requestOneTimeLocationUpdateWithProvider(final String locationProvider) {
        if (!TextUtils.isEmpty(locationProvider) && locationManager != null) {
            final LocationListener listener = new LocationListener() {

                @Override
                public void onLocationChanged(Location location) {
                    if (locationManager != null) {
                        locationManager.removeUpdates(this);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            };

            locationManager.requestLocationUpdates(locationProvider, MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);
            return locationManager.getLastKnownLocation(locationProvider);
        }

        return null;
    }

    /**
     * Performs a request to get reverse geocoded addresses with the given
     * {@link Location}
     * 
     * @param location
     *            The {@link Location} around which reverse geocoded addresses
     *            should be fetched.
     * @param maxResults
     *            The max number of reverse geocoded addresses to be fetched.
     * @return A {@link List} of {@link Address} or null on failure.
     */
    private void fetchReverseGeocodedAddresses(final ReverseGeocodeUpdateListener reverseGeocodeUpdateListener,
            final Location location, final int maxResults) {
        ScheduledJobsHandler.getInstance().startJobWithKey(ScheduledJobKeys.GET_REVERSE_GEOCODED_ADDRESSES,
                new ScheduleListener() {

                    @Override
                    public void processJob() {
                        List<Address> addressList = null;
                        if (location != null && maxResults > 0) {
                            if (geocoder == null) {
                                geocoder = new Geocoder(ApplicationEx.getContext());
                            }

                            try {
                                addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),
                                        maxResults);
                            } catch (Exception e) {
                                Logger.error.log(LOG_TAG, e);
                            }
                        }

                        if (addressList != null) {
                            reverseGeocodeUpdateListener.onReceived(addressList, location);
                        } else {
                            reverseGeocodeUpdateListener.onError(location);
                        }

                    }
                }, 1L, false);
    }
    
    /**
     * Calculates the distance between {{@link #lastKnownLocation} and the given
     * latitude and longitude.
     * 
     * @param latitude
     *            The latitude of the location from which distance is to be
     *            computed.
     * @param longitude
     *            The longitude of the location from which distance is to be
     *            computed.
     * @return The distance between {{@link #lastKnownLocation} and the given
     *         location. If the {{@link #lastKnownLocation} is not known, then
     *         -1 is returned.
     */
    public float getDistanceFromCurrentLocation(final double latitude, final double longitude) {
        synchronized (CACHE_LOCK) {
            if (lastKnownLocation != null) {
                double startLat = lastKnownLocation.getLatitude();
                double startLong = lastKnownLocation.getLongitude();

                float[] results = new float[3];
                Location.distanceBetween(startLat, startLong, latitude, longitude, results);

                return results[0];
            }

            return -1;
        }
    }

    /**
     * Check GPS enabled or not
     * @return true if GPS is enabled
     */
    public boolean isGPSEnabled() {
        if (locationManager != null &&
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true;
        } else {
            return false;
        }
    }
}
