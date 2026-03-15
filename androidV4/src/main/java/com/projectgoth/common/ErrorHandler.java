/**
 * Copyright (c) 2013 Project Goth
 *
 * ErrorHandler.java
 * Created Aug 25, 2013, 10:22:30 PM
 */

package com.projectgoth.common;

import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.nemesis.NetworkResponseListener.ErrorEventListener;
import com.projectgoth.nemesis.model.MigError;

/**
 * All Errors pass through heres
 * 
 * @author warrenbalcos
 * 
 */
public class ErrorHandler {

    private static final String TAG = "ErrorHandler";

    public ErrorHandler() {
    }

    //@formatter:off
    /**
     * All network service errors pass through here
     */
    private ErrorEventListener errorListener = new ErrorEventListener() {
    
         @Override
         public void onError(MigError error) {

             Exception e = error.getException();
             if (e != null) {
                 Logger.error.log(TAG, error.toString(), e);
             } else {
                 Logger.error.log(TAG, error.toString());
             }
             BroadcastHandler.NetworkService.sendError(error.getType().value());
         }
    };
    //@formatter:on

    /**
     * @return the errorListener
     */
    public ErrorEventListener getErrorListener() {
        return errorListener;
    }

}
