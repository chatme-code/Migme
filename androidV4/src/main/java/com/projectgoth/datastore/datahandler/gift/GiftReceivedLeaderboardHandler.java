/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftReceivedLeaderboardHandler.java
 * Created Mar 17, 2015, 1:39:15 PM
 */

package com.projectgoth.datastore.datahandler.gift;

import android.os.Bundle;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.GiftReceivedLeaderboardData;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.GiftsDatastore.StatisticsPeriod;
import com.projectgoth.datastore.datahandler.DataHandler;
import com.projectgoth.datastore.datahandler.ServerHandler;
import com.projectgoth.datastore.datahandler.ServerResult;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.listeners.GiftReceivedLeaderboardListener;
import com.projectgoth.nemesis.model.MigError;

/**
 * @author mapet
 * 
 */
public class GiftReceivedLeaderboardHandler extends DataHandler<GiftReceivedLeaderboardData> {
    
    private static final String KEY_PREFIX   = "GRL";

    private static final String PARAM_USERID = "PARAM_USERID";
    private static final String PARAM_LIMIT  = "PARAM_LIMIT";
    private static final String PARAM_PERIOD = "PARAM_PERIOD";

    //@formatter:off
    private ServerHandler<GiftReceivedLeaderboardData> mServerHandler = new ServerHandler<GiftReceivedLeaderboardData>() {
        
        @Override
        public void doFetch(final Bundle params, final ServerResult<GiftReceivedLeaderboardData> callback) {
            final String userId = params.getString(PARAM_USERID);
            final String limit = params.getString(PARAM_LIMIT);
            final String period = params.getString(PARAM_PERIOD);
            
            final RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
            if (requestManager != null) {
                requestManager.getGiftReceivedLeaderboard(new GiftReceivedLeaderboardListener() {
                    
                    @Override
                    public void onGiftReceivedLeaderboardReceived(GiftReceivedLeaderboardData data) {
                        broadcastSuccessEvent(data);
                        callback.onSuccess(params, data);
                    }
                }, userId, limit, period);
            }
        }
    };
    //@formatter:on

    public GiftReceivedLeaderboardHandler() {
        useDefaultMemCache();
        setPersistenceHandler(null);
        setServerHandler(mServerHandler);
    }

    public GiftReceivedLeaderboardData getData(String userId, String limit, StatisticsPeriod period, boolean forceFetch) {
        Bundle params = createBundle(userId, limit, period);
        return getData(params, forceFetch);
    }

    private Bundle createBundle(String userId, String limit, StatisticsPeriod period) {
        Bundle result = new Bundle();
        result.putString(PARAM_USERID, userId);
        result.putString(PARAM_LIMIT, limit);
        result.putString(PARAM_PERIOD, period.toString());
        return result;
    }

    @Override
    public void broadcastSuccessEvent(GiftReceivedLeaderboardData data) {
        BroadcastHandler.Gift.sendFetchGiftReceivedLeaderboardCompleted();
    }

    @Override
    public void broadcastFailEvent(MigError error) {
    }

    @Override
    public String getKey(Bundle params) {
        return Tools.createKey(KEY_PREFIX, Constants.MINUSSTR, params.getString(PARAM_USERID), 
                params.getString(PARAM_PERIOD), params.getString(PARAM_LIMIT));
    }

}
