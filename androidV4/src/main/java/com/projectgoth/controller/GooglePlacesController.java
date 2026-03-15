/**
 * Copyright (c) 2013 Project Goth
 *
 * GooglePlacesController.java
 * Created Jul 24, 2014, 11:35:44 AM
 */

package com.projectgoth.controller;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.projectgoth.common.Logger;
import com.projectgoth.datastore.DataCache;
import com.projectgoth.model.location.Place;
import com.projectgoth.model.location.PlacesList;
import com.projectgoth.util.AndroidLogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

/**
 * The purpose of this class is to:
 * - manage requests to the Google Places API and deserialize responses.
 * - manage caching and expiry of data
 * @author angelorohit
 */
public class GooglePlacesController {

    private final static String                 LOG_TAG                  = AndroidLogger
                                                                                 .makeLogTag(GooglePlacesController.class);

    // A lock that is obtained when working with any of the caches.
    private static final Object                 CACHE_LOCK               = new Object();

    private final static GooglePlacesController INSTANCE                 = new GooglePlacesController();

    // Will be used to build a search places request.
    private final static String                 PLACES_API_BASE          = "https://maps.googleapis.com/maps/api/place";
    private final static String                 TYPE_NEARBY_SEARCH       = "/nearbysearch";
    private final static String                 OUT_JSON                 = "/json";
    private final static String                 PLACE_TYPES              = "food|bar|store|museum|art_gallery";
    private final static String                 URL_ENCODING_CHARSET     = "UTF-8";
    
    private final static String                 API_KEY                  = "AIzaSyC8WpGahk7KjeZTtk9S0jNq0O8ABgADqOw";

    private final static String                 NEARBY_PLACES_URL        = String.format(
                                                                                 "%s%s%s?key=%s&rankby=distance",
                                                                                 PLACES_API_BASE, TYPE_NEARBY_SEARCH,
                                                                                 OUT_JSON, API_KEY);
    
    /**
     * Used to update external callers on success or failure of fetching.
     * @author angelorohit
     */
    public interface GooglePlacesUpdateListener {
        void onReceived();
        
        void onError();
    }

    /**
     * A {@link DataCache} of nearby places for locations. The key for this
     * cache is determined by {@link #getPlacesCacheKey(double, double)}
     */
    DataCache<PlacesList>                       nearbyPlacesCache        = null;

    /**
     * The max size of {@link #nearbyPlacesCache}
     */
    private final static int                    NEARBY_PLACES_CACHE_SIZE = 10;

    /**
     * Private Constructor
     */
    private GooglePlacesController() {
        try {
            nearbyPlacesCache = new DataCache<PlacesList>(NEARBY_PLACES_CACHE_SIZE, false);
        } catch (Exception ex) {
            Logger.error.log(LOG_TAG, ex);
        }
    }

    /**
     * A single point of entry for this controller.
     * 
     * @return An instance of the controller.
     */
    public static synchronized GooglePlacesController getInstance() {
        return INSTANCE;
    }

    /**
     * Async Task that makes Google Places Searches.
     * 
     * @author angelorohit
     * 
     */
    private class FetchTask extends AsyncTask<String, Void, String> {

        private double latitude;
        private double longitude;
        private GooglePlacesUpdateListener listener;

        public FetchTask(final double latitude, final double longitude, 
                final GooglePlacesUpdateListener listener) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.listener = listener;
        }

        /**
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected String doInBackground(String... placesURL) {            

            // Process parameter strings.
            for (String placeSearchURL : placesURL) {
                if (!TextUtils.isEmpty(placeSearchURL)) {
                    HttpClient placesClient = new DefaultHttpClient();

                    try {
                        HttpGet placesGet = new HttpGet(placeSearchURL);
                        HttpResponse placesResponse = placesClient.execute(placesGet);
                        StatusLine placeSearchStatus = placesResponse.getStatusLine();

                        if (placeSearchStatus.getStatusCode() == 200) {
                            // OK response received.
                            // Buffer response into a String.
                            
                            StringBuilder placesBuilder = new StringBuilder();
                            
                            HttpEntity placesEntity = placesResponse.getEntity();
                            InputStream placesContent = placesEntity.getContent();
                            InputStreamReader placesInput = new InputStreamReader(placesContent);
                            BufferedReader placesReader = new BufferedReader(placesInput);
                            try {
                                String lineIn;
                                while ((lineIn = placesReader.readLine()) != null) {
                                    placesBuilder.append(lineIn);
                                }
                            } finally {
                                placesReader.close();
                            }

                            return placesBuilder.toString();
                        }
                    } catch (Exception ex) {
                        Logger.error.log(LOG_TAG, ex);
                    }
                }
            }

            return null;
        }

        /**
         * @see android.os.AsyncTask#onPostExecute(String)
         */
        @Override
        protected void onPostExecute(final String result) {
            boolean didSucceed = false;
            if (!TextUtils.isEmpty(result)) {
                try {
                    // Deserialize JSON String result.
                    final PlacesList placesList = new Gson().fromJson(result, PlacesList.class);
                    didSucceed = cacheNearbyPlacesForLocation(placesList, latitude, longitude);                    
                } catch (Exception ex) {
                    Logger.error.log(LOG_TAG, ex);
                }
            }
            
            if (listener != null) {
                if (didSucceed) {
                    listener.onReceived();
                } else {
                    listener.onError();
                }
            }
        }
    }

    /**
     * Caches a {@link PlacesList} for nearby locations.
     * 
     * @param placesList
     *            A {@link PlacesList} to be cached.
     * @param latitude
     *            The latitude of the location from which the {@link PlacesList}
     *            was fetched.
     * @param longitude
     *            The longitude of the location from which the
     *            {@link PlacesList} was fetched.
     * @return true on successful caching and false otherwise.
     */
    private boolean cacheNearbyPlacesForLocation(final PlacesList placesList, final double latitude,
            final double longitude) {
        if (placesList != null) {
            synchronized (CACHE_LOCK) {
                if (nearbyPlacesCache != null && placesList.getResults().length > 0
                        && placesList.getStatus() == PlacesList.ResponseStatus.OK) {
                    final String key = getPlacesCacheKey(latitude, longitude);
                    if (!TextUtils.isEmpty(key)) {
                        nearbyPlacesCache.cacheData(key, placesList);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Generates a key for any google places cache that stores data based on
     * location.
     * 
     * @param latitude
     *            The latitude of the location for which the cached data is to
     *            be uniquely identified.
     * @param longitude
     *            The longitude of the location for which the cached data is to
     *            be uniquely identified.
     * @return A String key.
     */
    private static String getPlacesCacheKey(final double latitude, final double longitude) {
        // We uniquely identify the nearby places list based on
        // latitude-longitude upto six decimal places each.
        return String.format("%.6f-%.6f", latitude, longitude);
    }

    /**
     * Get a List of Places for a given latitude and longitude.
     * 
     * @param latitude
     *            The latitude of the location for which the results are to be
     *            retrieved.
     * @param longitude
     *            The longitude of the location for which the results are to be
     *            retrieved.
     * @param shouldForceFetch
     *            true to force fetching of the results. Cached results (if
     *            available) will still be returned.
     * @param listener
     *            A {@link GooglePlacesUpdateListener} that will be informed of
     *            the results of fetching.
     * @return A List of {@link Place} that represents the nearby search results
     *         and null if an appropriate entry was not found in cache.
     */
    public List<Place> getNearbyPlacesForLocation(final double latitude, final double longitude,
            final boolean shouldForceFetch, final GooglePlacesUpdateListener listener) {
        synchronized (CACHE_LOCK) {
            final String key = getPlacesCacheKey(latitude, longitude);
            if (nearbyPlacesCache != null) {
                final PlacesList result = nearbyPlacesCache.getData(key);
                // If the data is not present or if it has expired.
                if (nearbyPlacesCache.isExpired(key) || shouldForceFetch) {
                    fetchNearbyPlacesForLocation(latitude, longitude, listener);
                }

                if (result != null) {
                    return Arrays.asList(result.getResults());
                }
            }
        }

        return null;
    }

    /**
     * Sends an asynchronous request to fetch nearby places for the given
     * latitude and longitude.
     * 
     * @param latitude
     *            The latitude of the location for which nearby places are to be
     *            fetched.
     * @param longitude
     *            The longitude of the location for which nearby places are to
     *            be fetched.
     * @param listener
     *            A {@link GooglePlacesUpdateListener} that will be informed of
     *            the results of fetching.
     */
    private void fetchNearbyPlacesForLocation(final double latitude, final double longitude,
            final GooglePlacesUpdateListener listener) {
        final String fetchURL = buildNearbySearchUrl(latitude, longitude);

        FetchTask fetchTask = new FetchTask(latitude, longitude, listener);
        fetchTask.execute(new String[] { fetchURL });
    }
    
    /**
     * Constructs the nearby search places URL from the given parameters.
     * 
     * @param latitude
     *            The latitude around which to retrieve place information
     * @param longitude
     *            The longitude around which to retrieve place information
     * @return A valid URL on success and null on failure to construct the URL.
     */
    private String buildNearbySearchUrl(final double latitude, final double longitude) {
        try {
            return String.format("%s&location=%f,%f&types=%s", NEARBY_PLACES_URL, latitude, longitude,
                    URLEncoder.encode(PLACE_TYPES, URL_ENCODING_CHARSET));
        } catch (UnsupportedEncodingException ex) {
            // Exception can be thrown by URLEncoder.encode()
            Logger.error.log(LOG_TAG, ex);
        }

        return null;
    }
}
