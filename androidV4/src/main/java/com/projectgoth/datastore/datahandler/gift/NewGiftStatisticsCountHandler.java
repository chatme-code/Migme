/**
 * Copyright (c) 2013 Project Goth
 *
 * NewGiftStatisticsCountHandler.java
 * Created Mar 16, 2015, 11:59:11 AM
 */

package com.projectgoth.datastore.datahandler.gift;

import android.os.Bundle;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.GiftCount;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.GiftsDatastore.StatisticsPeriod;
import com.projectgoth.datastore.GiftsDatastore.StatisticsType;
import com.projectgoth.datastore.datahandler.DataHandler;
import com.projectgoth.datastore.datahandler.ServerHandler;
import com.projectgoth.datastore.datahandler.ServerResult;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.listeners.GetNewGiftStatisticsCountListener;
import com.projectgoth.nemesis.model.MigError;

/**
 * @author mapet
 * 
 */
public class NewGiftStatisticsCountHandler extends DataHandler<GiftCount> {

    private static final String KEY_PREFIX     = "NGSC";

    private static final String PARAM_USERID   = "PARAM_USERID";
    private static final String PARAM_TYPE     = "PARAM_TYPE";
    private static final String PARAM_PERIOD   = "PARAM_PERIOD";
    
    //@formatter:off
    private ServerHandler<GiftCount> mServerHandler = new ServerHandler<GiftCount>() {
        
        @Override
        public void doFetch(final Bundle params, final ServerResult<GiftCount> callback) {
            final String userId = params.getString(PARAM_USERID);
            final String type = params.getString(PARAM_TYPE);
            final String period = params.getString(PARAM_PERIOD);
            
            final RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
            if (requestManager != null) {
                requestManager.getNewGiftStatisticsCount(new GetNewGiftStatisticsCountListener() {

                    @Override
                    public void onNewGiftStatisticsCountReceived(GiftCount data) {
                        broadcastSuccessEvent(data);
                        callback.onSuccess(params, data);
                    }
                        
                }, userId, type, period);
            }
        }
    };
    //@formatter:on

    public NewGiftStatisticsCountHandler() {
        useDefaultMemCache();
        setPersistenceHandler(null);
        setServerHandler(mServerHandler);
    }

    public GiftCount getData(String userId, StatisticsType type, StatisticsPeriod period, boolean forceFetch) {
        Bundle params = createBundle(userId, type, period);
        return getData(params, forceFetch);
    }

    private Bundle createBundle(String userId, StatisticsType type, StatisticsPeriod period) {
        Bundle result = new Bundle();
        result.putString(PARAM_USERID, userId);
        result.putString(PARAM_TYPE, type.toString());
        result.putString(PARAM_PERIOD, period.toString());
        return result;
    }

    @Override
    public void broadcastSuccessEvent(GiftCount data) {
        BroadcastHandler.Gift.sendFetchNewGiftsCountCompleted();
    }

    @Override
    public void broadcastFailEvent(MigError error) {
    }

    @Override
    public String getKey(Bundle params) {
        return Tools.createKey(KEY_PREFIX, Constants.MINUSSTR, params.getString(PARAM_USERID), 
                params.getString(PARAM_TYPE), params.getString(PARAM_PERIOD));
    }

}
