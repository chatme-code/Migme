/**
 * Copyright (c) 2013 Project Goth
 *
 * ServerResult.java
 * Created Jan 29, 2015, 11:25:08 AM
 */

package com.projectgoth.datastore.datahandler;

import android.os.Bundle;

import com.projectgoth.nemesis.model.MigError;

/**
 * Use this callback to send back the result of the server fetch
 * 
 * @author warrenbalcos
 */
public interface ServerResult<T> {

    public void onSuccess(Bundle params, T data);

    public void onFail(MigError error);

}
