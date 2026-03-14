package com.projectgoth.common;

import com.projectgoth.app.DebugPreference;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.SystemDatastore;

/**
 * Created by felixqk on 16/10/14.
 */
public class NUEManager {

    private static final NUEManager INSTANCE                     = new NUEManager();

    private boolean enabled = true;

    public static synchronized NUEManager getInstance() {
        return INSTANCE;
    }

    public boolean shouldShowNUE(String key) {
        if(DebugPreference.isDebuggingNUE) {
            return true;
        }
        return enabled && !SystemDatastore.getInstance().getNUEData(key + Session.getInstance().getUsername());
    }

    public void alreadyShownNUE(String key) {
        SystemDatastore.getInstance().saveNUEData(key + Session.getInstance().getUsername(), true);
    }
}
