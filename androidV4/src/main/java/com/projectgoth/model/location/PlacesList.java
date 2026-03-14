/**
 * Copyright (c) 2013 Project Goth
 *
 * PlacesList.java
 * Created Jul 23, 2014, 3:16:50 PM
 */

package com.projectgoth.model.location;

import com.projectgoth.common.TextUtils;

/**
 * A model used to store the results of a Google places search.
 * @author angelorohit
 */
public class PlacesList {
    
    /**
     * The possible response statuses that can be received when doing a Google places search.
     * @author angelorohit
     */
    public enum ResponseStatus {
        OK("OK"),
        REQUEST_DENIED("REQUEST_DENIED"),
        INVALID_REQUEST("INVALID_REQUEST"),
        UNKNOWN("UNKNOWN");
        
        private String value;
        private ResponseStatus(final String statusStr) {
            this.value = statusStr;
        }
        
        public static ResponseStatus fromValue(final String statusStr) {
            if (!TextUtils.isEmpty(statusStr)) {
                for (ResponseStatus responseStatus : values()) {
                    if (statusStr.equalsIgnoreCase(responseStatus.toString())) {
                        return responseStatus;
                    }
                }
            }
            
            return ResponseStatus.UNKNOWN;
        }
        
        @Override
        public String toString() {
            return value;
        } 
    };    

    /**
     * A string representation of the status of the result.
     * Can be used to provide additional information on the state of the request.
     */
    private String      status;

    /**
     * An array of {@link Place} results.
     */
    private Place[]     results;
    
    public ResponseStatus getStatus() {
        return ResponseStatus.fromValue(this.status);
    }

    public void setStatus(ResponseStatus responseStatus) {
        if (status != null) {
            this.status = responseStatus.toString();
        }
    }
    
    public Place[] getResults() {
        return results;
    }
    
    public void setResults(Place[] places) {
        this.results = places;
    }
}