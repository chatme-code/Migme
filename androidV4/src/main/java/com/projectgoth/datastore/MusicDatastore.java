/**
 * Copyright (c) 2013 Project Goth
 * MusicDatastore.java
 * Created Apr 14, 2015, 5:22:48 PM
 */

package com.projectgoth.datastore;

import android.util.SparseArray;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.MusicData;
import com.projectgoth.b.data.MusicGenreData;
import com.projectgoth.b.data.MusicItem;
import com.projectgoth.b.data.UserFavoriteData;
import com.projectgoth.common.Constants;
import com.projectgoth.enums.UserFavoriteType;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.listeners.GetMusicInfoFromDeezerListener;
import com.projectgoth.nemesis.listeners.GetMusicStationsByGenreListener;
import com.projectgoth.nemesis.listeners.GetMusicStationsDataListener;
import com.projectgoth.nemesis.listeners.SimpleResponseListener;
import com.projectgoth.nemesis.listeners.UserFavoritesListener;
import com.projectgoth.nemesis.model.MigResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * @author mapet
 */
public class MusicDatastore extends BaseDatastore {

    private static final int MAX_CACHE_SIZE = 20;

    private final static String MIGME_STATIONS_URL = "http://migme.github.io/music/data/migmestations.json";
    private final static String DEEZER_BY_GENRE_URL = "http://api.deezer.com/radio/genres";
    private final static String DEEZER_STATIONS_URL = "http://api.deezer.com/genre/0/radios";
    private final static String DEEZER_INFO = "http://api.deezer.com/infos";

    private DataCache<MusicData> mMigmeMusicItemDataCache;
    private DataCache<ArrayList<MusicGenreData>> mDeezerByGenreDataCache;
    private DataCache<ArrayList<MusicItem>> mAllDeezerStationsDataCache;
    private DataCache<ArrayList<UserFavoriteData>> mUserFavoriteDataCache;

    private SparseArray<MusicGenreData> mAllDeezerStationsByGenre;
    private HashSet<String> mFavoriteStationsList;
    private HashSet<MusicItem> mFavoriteStations;
    private boolean mIsSupportByDeezer = true;

    public enum MusicProviderType {
        deezer
    }

    public enum MusicChannelType {
        radio
    }

    private MusicDatastore() {
        super();
    }

    private static class MusicDatastoreHolder {
        static final MusicDatastore sINSTANCE = new MusicDatastore();
    }

    public static MusicDatastore getInstance() {
        return MusicDatastoreHolder.sINSTANCE;
    }

    @Override
    protected void initData() {
        try {
            mMigmeMusicItemDataCache = new DataCache<MusicData>(MAX_CACHE_SIZE);
            mDeezerByGenreDataCache = new DataCache<ArrayList<MusicGenreData>>(MAX_CACHE_SIZE);
            mAllDeezerStationsDataCache = new DataCache<ArrayList<MusicItem>>(MAX_CACHE_SIZE);
            mUserFavoriteDataCache = new DataCache<ArrayList<UserFavoriteData>>(MAX_CACHE_SIZE);
            mAllDeezerStationsByGenre = new SparseArray<MusicGenreData>();
            mFavoriteStationsList = new HashSet<String>();
            mFavoriteStations = new HashSet<MusicItem>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cacheMigmeStationsData(String key, MusicData result) {
        mMigmeMusicItemDataCache.cacheData(key, result);
    }

    private void cacheAllDeezerStations(String key, ArrayList<MusicItem> result) {
        mAllDeezerStationsDataCache.cacheData(key, result);
    }

    private void cacheAllMusicGenreData(String key, ArrayList<MusicGenreData> result) {
        mDeezerByGenreDataCache.cacheData(key, result);
    }

    public boolean isSupportByDeezer() {
        return mIsSupportByDeezer;
    }

    public MusicData getMigmeStationsData() {
        final String key = "MIGMESTATIONS";

        if (mMigmeMusicItemDataCache.isExpired(key)) {
            RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
            if (requestManager != null) {
                requestManager.getMusicStations(new GetMusicStationsDataListener() {

                    @Override
                    public void onMusicStationsDataReceived(MusicData musicData) {
                        cacheMigmeStationsData(key, musicData);
                        BroadcastHandler.NewMusic.sendFetchMigmeStationsReceived();

                    }
                }, MIGME_STATIONS_URL);
            }
        }

        return mMigmeMusicItemDataCache.getData(key);
    }

    public void getDeezerInfo() {

        RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
        if (requestManager != null)
            requestManager.getMusicInfoFromDeezer(new GetMusicInfoFromDeezerListener() {

                @Override
                public void onMusicInfoReceived(boolean isSupported) {
                    mIsSupportByDeezer = isSupported;
                }
            }, DEEZER_INFO);

    }

    public ArrayList<MusicGenreData> getAllMusicGenreData() {
        final String key = "DEEZERSTATIONS";

        if (mDeezerByGenreDataCache.isExpired(key)) {
            RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
            if (requestManager != null) {

                requestManager.getMusicStationsByGenre(new GetMusicStationsByGenreListener() {
                    @Override
                    public void onMusicStationsByGenreReceived(ArrayList<MusicGenreData> musicGenreData) {
                        cacheAllMusicGenreData(key, musicGenreData);
                        mAllDeezerStationsByGenre.clear();

                        for (MusicGenreData mgd : musicGenreData) {
                            mAllDeezerStationsByGenre.put(mgd.getId(), mgd);
                        }

                        BroadcastHandler.NewMusic.sendFetchDeezerStationsReceived();
                    }
                }, DEEZER_BY_GENRE_URL);
            }
        }

        return mDeezerByGenreDataCache.getData(key);
    }

    public ArrayList<MusicItem> getAllDeezerStations() {
        final String key = "ALLDEEZERSTATIONS";

        if (mAllDeezerStationsDataCache.isExpired(key)) {
            RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
            if (requestManager != null) {
                requestManager.getMusicStations(new GetMusicStationsDataListener() {

                    @Override
                    public void onMusicStationsDataReceived(MusicData musicData) {
                        ArrayList<MusicItem> musicItems = new ArrayList<MusicItem>(Arrays.asList(musicData.getData()));
                        cacheAllDeezerStations(key, musicItems);
                        BroadcastHandler.NewMusic.sendFetchDeezerStationsReceived();
                    }
                }, DEEZER_STATIONS_URL);
            }
        }

        return mAllDeezerStationsDataCache.getData(key);
    }

    public ArrayList<MusicItem> getMusicStationsDataByGenre(final int genreId) {
        ArrayList<MusicItem> musicItems = new ArrayList<MusicItem>();

        if (mAllDeezerStationsByGenre != null) {
            // add null check because radios could be null
            MusicGenreData musicGenreData = mAllDeezerStationsByGenre.get(genreId);
            if (musicGenreData != null && musicGenreData.getRadios() != null) {
                musicItems = new ArrayList<MusicItem>(Arrays.asList(musicGenreData.getRadios()));
            }
        }

        return musicItems;
    }

    private void cacheFavoriteMusicChannelsData(String key, ArrayList<UserFavoriteData> result) {
        mUserFavoriteDataCache.cacheData(key, result);
    }

    public ArrayList<UserFavoriteData> getFavoriteMusicChannels(final String userId) {
        final String key = "FAVORITESTATIONS" + userId;

        if (mUserFavoriteDataCache.isExpired(key)) {
            RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
            if (requestManager != null) {
                requestManager.getFavoriteItems(new UserFavoritesListener() {

                    @Override
                    public void onUserFavoritesListener(ArrayList<UserFavoriteData> data) {
                        mFavoriteStations.clear();
                        mFavoriteStationsList.clear();
                        for (UserFavoriteData userFavoriteData : data) {
                            mFavoriteStationsList.add(userFavoriteData.getUserFavorite());
                            mFavoriteStations.add(getMusicChannel(userFavoriteData.getUserFavorite()));
                        }

                        cacheFavoriteMusicChannelsData(key, data);
                        BroadcastHandler.UserFavorite.sendFetchUserFavoritesCompleted();
                    }
                }, UserFavoriteType.MUSIC_CHANNEL.name(), userId);
            }
        }

        return mUserFavoriteDataCache.getData(key);
    }

    private MusicItem getMusicChannel(final String channelId) {
        String[] id = channelId.split(Constants.SLASHSTR);
        ArrayList<MusicItem> musicItems = getAllDeezerStations();

        for (MusicItem musicItem : musicItems) {
            if (musicItem.getId().equals(id[2])) {
                return musicItem;
            }
        }

        return null;
    }

    public HashSet<MusicItem> getFavoriteStations() {
        return mFavoriteStations;
    }

    public void setFavoriteMusicChannel(final String channelId, final String userId) {
        RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
        if (requestManager != null) {
            requestManager.setFavoriteItem(new SimpleResponseListener() {

                @Override
                public void onSuccess(MigResponse response) {
                    mFavoriteStationsList.add(channelId);
                    mFavoriteStations.add(getMusicChannel(channelId));
                    BroadcastHandler.UserFavorite.sendSetFavoriteMusicChannelCompleted();
                }
            }, UserFavoriteType.MUSIC_CHANNEL.name(), userId, channelId);
        }
    }

    public void removeFavoriteMusicChannel(final String channelId, final String userId) {
        RequestManager requestManager = ApplicationEx.getInstance().getRequestManager();
        if (requestManager != null) {
            requestManager.removeFavoriteItem(new SimpleResponseListener() {

                @Override
                public void onSuccess(MigResponse response) {
                    mFavoriteStationsList.remove(channelId);
                    mFavoriteStations.remove(getMusicChannel(channelId));
                    BroadcastHandler.UserFavorite.sendRemoveFavoriteMusicChannelCompleted();
                }
            }, UserFavoriteType.MUSIC_CHANNEL.name(), userId, channelId);
        }
    }

    public boolean isFavoriteMusicChannel(final MusicProviderType provider, final MusicItem item) {
        // favorite channel format: provider/type/id
        String musicChannelId = String.format("%s/%s/%s", provider.toString(), item.getType(), item.getId());
        return (mFavoriteStationsList.contains(musicChannelId) ? true : false);
    }

    public boolean isFavoriteMusicChannel(final String channelId) {
        return (mFavoriteStationsList.contains(channelId) ? true : false);
    }

}
