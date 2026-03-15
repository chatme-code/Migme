/**
 * Copyright (c) 2013 Project Goth
 * GiftsDatastore.java
 * Created Jan 22, 2015, 6:26:27 PM
 */

package com.projectgoth.datastore;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.GiftCategoryData;
import com.projectgoth.b.data.GiftCount;
import com.projectgoth.b.data.GiftReceivedLeaderboardData;
import com.projectgoth.b.data.GiftReceivedLeaderboardItem;
import com.projectgoth.b.data.GiftSenderLeaderboardData;
import com.projectgoth.b.data.GiftSenderLeaderboardItem;
import com.projectgoth.b.data.UserFavoriteData;
import com.projectgoth.b.data.UserGiftListData;
import com.projectgoth.b.data.UserGiftStatData;
import com.projectgoth.b.data.mime.GiftMimeData;
import com.projectgoth.common.Constants;
import com.projectgoth.datastore.datahandler.gift.GiftCategoriesHandler;
import com.projectgoth.datastore.datahandler.gift.GiftReceivedLeaderboardHandler;
import com.projectgoth.datastore.datahandler.gift.GiftSenderLeaderboardHandler;
import com.projectgoth.datastore.datahandler.gift.GiftsReceivedHandler;
import com.projectgoth.datastore.datahandler.gift.NewGiftStatisticsCountHandler;
import com.projectgoth.datastore.datahandler.gift.UserGiftStatHandler;
import com.projectgoth.enums.UserFavoriteType;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.listeners.SimpleResponseListener;
import com.projectgoth.nemesis.listeners.UserFavoritesListener;
import com.projectgoth.nemesis.model.MigResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author warrenbalcos
 */
public class GiftsDatastore extends BaseDatastore {


    private NewGiftStatisticsCountHandler mNewGiftStatisticsCountHandler;
    private GiftSenderLeaderboardHandler mGiftSenderLeaderboardHandler;
    private GiftReceivedLeaderboardHandler mGiftReceivedLeaderboardHandler;
    private GiftCategoriesHandler mGiftCategoriesHandler;
    private UserGiftStatHandler mUserGiftStatHandler;
    private GiftsReceivedHandler mGiftsReceivedHandler;
    private DataCache<ArrayList<UserFavoriteData>> mUserFavoriteDataCache;
    private HashSet<String> mFavoriteGiftsList;

    private static final int MAX_CACHE_SIZE = 20;

    public enum Category {
        ALL("This month"), FAVORITE("Favorite"), PREMIUM("Premium"), FROM_CELEBRITIES("From celebrities");

        private String type;

        private Category(String type) {
            this.type = type;
        }

        private String getType() {
            return this.type;
        }

        public static Category fromValue(int type) {
            for (Category category : values()) {
                if (category.ordinal() == type) {
                    return category;
                }
            }
            return ALL;
        }

        public static String fromType(String type) {
            for (Category category : values()) {
                if (category.toString().equals(type)) {
                    return category.getType();
                }
            }
            return Constants.BLANKSTR;
        }
    }

    private static class GiftsDatastoreHolder {
        static final GiftsDatastore sINSTANCE = new GiftsDatastore();
    }

    public static GiftsDatastore getInstance() {
        return GiftsDatastoreHolder.sINSTANCE;
    }

    public enum SortOrder {
        ASC("asc"), DESC("desc");

        private String value;

        private SortOrder(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum StatisticsType {
        GIFT_SENT, GIFT_RECEIVED
    }

    public enum StatisticsPeriod {
        DAILY, WEEKLY, MONTHLY, YEARLY, ALL_TIME
    }

    public enum OrderType {
        COUNT, DATE, SENDER, RECEIVER, MIMETYPEID
    }

    private GiftsDatastore() {
        super();
    }

    @Override
    protected void initData() {
        mNewGiftStatisticsCountHandler = new NewGiftStatisticsCountHandler();
        mGiftSenderLeaderboardHandler = new GiftSenderLeaderboardHandler();
        mGiftReceivedLeaderboardHandler = new GiftReceivedLeaderboardHandler();
        mGiftCategoriesHandler = new GiftCategoriesHandler();
        mUserGiftStatHandler = new UserGiftStatHandler();
        mGiftsReceivedHandler = new GiftsReceivedHandler();
        mUserFavoriteDataCache = new DataCache<ArrayList<UserFavoriteData>>(MAX_CACHE_SIZE);
        mFavoriteGiftsList = new HashSet<String>();
    }

    public UserGiftStatData getUserGiftStatistics(String userId, String year, boolean forceFetch) {
        return mUserGiftStatHandler.getData(userId, year, forceFetch);
    }

    public GiftCategoryData getUserGiftCategories(String userId, boolean forceFetch) {
        return mGiftCategoriesHandler.getData(userId, forceFetch);
    }

    public GiftCount getNewGiftsStats(String userId, StatisticsPeriod period, boolean forceFetch) {
        return mNewGiftStatisticsCountHandler.getData(userId, StatisticsType.GIFT_RECEIVED,
                period, forceFetch);
    }

    public GiftSenderLeaderboardData getSenderLeaderboardData(String userId, int limit,
                                                              StatisticsPeriod period, boolean forceFetch) {
        return mGiftSenderLeaderboardHandler.getData(userId, Integer.toString(limit), period, forceFetch);
    }

    public List<GiftSenderLeaderboardItem> getSenderLeaderboards(String userId, int limit,
                                                                 StatisticsPeriod period, boolean forceFetch) {

        ArrayList<GiftSenderLeaderboardItem> result = null;
        GiftSenderLeaderboardData data = getSenderLeaderboardData(userId, limit, period, forceFetch);
        if (data != null && data.getResponse() != null) {
            result = new ArrayList<GiftSenderLeaderboardItem>(Arrays.asList(data.getResponse()));
        }
        return result;
    }

    public GiftReceivedLeaderboardData getReceivedLeaderboardData(String userId, int limit,
                                                                  StatisticsPeriod period, boolean forceFetch) {
        return mGiftReceivedLeaderboardHandler.getData(userId, Integer.toString(limit), period, forceFetch);
    }

    public List<GiftReceivedLeaderboardItem> getReceivedLeaderboards(String userId, int limit,
                                                                     StatisticsPeriod period, boolean forceFetch) {

        ArrayList<GiftReceivedLeaderboardItem> result = null;
        GiftReceivedLeaderboardData data = getReceivedLeaderboardData(userId, limit, period, forceFetch);
        if (data != null && data.getResponse() != null) {
            result = new ArrayList<GiftReceivedLeaderboardItem>(Arrays.asList(data.getResponse()));
        }
        return result;
    }

    public UserGiftListData getGiftsReceivedList(String userId, GiftsDatastore.Category category, String month, String year,
                                                 GiftsDatastore.OrderType orderType, GiftsDatastore.SortOrder sortOrder,
                                                 int offset, int limit, boolean forceFetch) {
        return mGiftsReceivedHandler.getData(userId, category, month, year, orderType, sortOrder, offset, limit, forceFetch);
    }

    public void setFavoriteGift(final String giftId, final String userId) {
        RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
        if (requestManager != null) {
            requestManager.setFavoriteItem(new SimpleResponseListener() {

                @Override
                public void onSuccess(MigResponse response) {
                    mFavoriteGiftsList.add(giftId);
                    BroadcastHandler.UserFavorite.sendSetFavoriteGiftCompleted();
                }
            }, UserFavoriteType.VIRTUAL_GIFT.name(), userId, giftId);
        }
    }

    public void removeFavoriteGift(final String giftId, final String userId) {
        RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
        if (requestManager != null) {
            requestManager.removeFavoriteItem(new SimpleResponseListener() {

                @Override
                public void onSuccess(MigResponse response) {
                    mFavoriteGiftsList.remove(giftId);
                    BroadcastHandler.UserFavorite.sendRemoveFavoriteGiftCompleted();
                }
            }, UserFavoriteType.VIRTUAL_GIFT.name(), userId, giftId);
        }
    }

    public boolean isFavoriteGift(final GiftMimeData giftMimeData) {
        String giftId = String.valueOf(giftMimeData.getMimeTypeId());
        return (mFavoriteGiftsList.contains(giftId) ? true : false);
    }

    public ArrayList<UserFavoriteData> getFavoriteGiftData(final String userId) {
        final String key = "FAVORITEGIFTS" + userId;

        if (mUserFavoriteDataCache.isExpired(key)) {
            RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
            if (requestManager != null) {
                requestManager.getFavoriteItems(new UserFavoritesListener() {

                    @Override
                    public void onUserFavoritesListener(ArrayList<UserFavoriteData> data) {
                        mFavoriteGiftsList.clear();
                        for (UserFavoriteData userFavoriteData : data) {
                            String giftId = String.valueOf(userFavoriteData.getUserFavorite());
                            mFavoriteGiftsList.add(giftId);
                        }
                        cacheFavoriteGiftData(key, data);
                        BroadcastHandler.UserFavorite.sendFetchUserFavoritesCompleted();
                    }
                }, UserFavoriteType.VIRTUAL_GIFT.name(), userId);
            }
        }

        return mUserFavoriteDataCache.getData(key);
    }

    private void cacheFavoriteGiftData(String key, ArrayList<UserFavoriteData> result) {
        mUserFavoriteDataCache.cacheData(key, result);
    }

}
