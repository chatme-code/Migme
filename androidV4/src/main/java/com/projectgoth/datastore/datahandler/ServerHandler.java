/**
 * Copyright (c) 2013 Project Goth
 *
 * ServerHandler.java
 * Created Jan 28, 2015, 1:45:11 PM
 */

package com.projectgoth.datastore.datahandler;

import android.os.Bundle;

/**
 * @author warrenbalcos
 * 
 */
public abstract class ServerHandler<T> {

    /**
     * Implement the data fetching logic in this method
     * 
     * @param params
     * @param callback
     *            - *important* it is required to send back the result of the
     *            server fetch to this callback
     */
    public abstract void doFetch(final Bundle params, final ServerResult<T> callback);
}
