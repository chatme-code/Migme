/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftSenderLeaderboardHandler.java
 * Created Mar 17, 2015, 1:38:12 PM
 */

package com.projectgoth.datastore.datahandler.gift;

import android.os.Bundle;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.GiftSenderLeaderboardData;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.GiftsDatastore.StatisticsPeriod;
import com.projectgoth.datastore.datahandler.DataHandler;
import com.projectgoth.datastore.datahandler.ServerHandler;
import com.projectgoth.datastore.datahandler.ServerResult;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.listeners.GiftSenderLeaderboardListener;
import com.projectgoth.nemesis.model.MigError;

/**
 * @author mapet
 * 
 */
public class GiftSenderLeaderboardHandler extends DataHandler<GiftSenderLeaderboardData> {
    
    private static final String KEY_PREFIX   = "GSL";

    private static final String PARAM_USERID = "PARAM_USERID";
    private static final String PARAM_LIMIT  = "PARAM_LIMIT";
    private static final String PARAM_PERIOD = "PARAM_PERIOD";

    //@formatter:off
    private ServerHandler<GiftSenderLeaderboardData> mServerHandler = new ServerHandler<GiftSenderLeaderboardData>() {
        
        @Override
        public void doFetch(final Bundle params, final ServerResult<GiftSenderLeaderboardData> callback) {
            final String userId = params.getString(PARAM_USERID);
            final String limit = params.getString(PARAM_LIMIT);
            final String period = params.getString(PARAM_PERIOD);
            
            final RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
            if (requestManager != null) {
                requestManager.getGiftSenderLeaderboard(new GiftSenderLeaderboardListener() {
                        
                    @Override
                    public void onGiftSenderLeaderboardReceived(GiftSenderLeaderboardData data) {
                        broadcastSuccessEvent(data);
                        callback.onSuccess(params, data);
                    }
                }, userId, limit, period);
            }
        }
    };
    //@formatter:on

    public GiftSenderLeaderboardHandler() {
        useDefaultMemCache();
        setPersistenceHandler(null);
        setServerHandler(mServerHandler);
    }

    public GiftSenderLeaderboardData getData(String userId, String limit, StatisticsPeriod period, boolean forceFetch) {
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
    public void broadcastSuccessEvent(GiftSenderLeaderboardData data) {
        BroadcastHandler.Gift.sendFetchGiftSenderLeaderboardCompleted();
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
