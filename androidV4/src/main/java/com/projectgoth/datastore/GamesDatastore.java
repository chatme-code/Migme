/**
 * Copyright (c) 2013 Project Goth
 *
 * GamesDatastore.java
 * Created Jan 23, 2015, 11:29:53 AM
 */

package com.projectgoth.datastore;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mig33.diggle.common.StringUtils;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Banner;
import com.projectgoth.b.data.GameItem;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.dao.GamesDAO;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.nemesis.utils.JsonParseUtils;
import com.projectgoth.util.AndroidLogger;


/**
 * @author shiyukun
 *
 */
public class GamesDatastore extends BaseDatastore {

    private static final String             LOG_TAG                 = AndroidLogger.makeLogTag(GamesDatastore.class);
    
    // A lock that is obtained when working with any of the caches.
    private static final Object             CACHE_LOCK              = new Object();
    
    // A DAO for saving games to persistent storage.
    private GamesDAO                        mGamesDAO               = null;

    // A cache of all games. The key for this cache is the id of the game.
    private Map<String, GameItem>           mGamesCache;
    public HashMap<Integer, ArrayList<GameItem>> gameMap           = new HashMap<Integer, ArrayList<GameItem>>();

    // The max number of games to be fetched at a time.
    private final static int                DEFAULT_FETCH_LIMIT     = 5;
    
    public static final String              GAME_PREFERENCES_NAME   = "Mig33GamePreferences";
    public static final String              BANNER_PREFERENCES_NAME = "Mig33BannerPreferences";
    
    public static final String              GAME_LIST_KEY           = "Mig33GameList";
    public static final String              GAME_LIST_TIMESTAMP     = "Mig33GameTimestamp";
    public static final String              BANNER_KEY              = "Mig33BannerKey";
    public static final String              BANNER_TIMESTAMP        = "Mig33BannerTimestamp";
    
    
    private SharedPreferences               gamePreferences;
    private SharedPreferences               bannerPreferences;
    
    private static final String             DATA_URL                = "http://migme.github.io/games/data/data.json";
    
    /**
     * Constructor
     * 
     * @param appCtx Application context.
     */
    private GamesDatastore() {
        super();
        final Context appCtx = ApplicationEx.getContext();
        if (appCtx != null) {
            mGamesDAO = new GamesDAO(appCtx);
            gamePreferences = appCtx.getSharedPreferences(GAME_PREFERENCES_NAME, Context.MODE_PRIVATE);
            bannerPreferences = appCtx.getSharedPreferences(BANNER_PREFERENCES_NAME, Context.MODE_PRIVATE);
        }
        loadFromPersistentStorage();
    }

    private static class GamesDatastoreHolder {
        static final GamesDatastore sINSTANCE = new GamesDatastore();
    }
    
    /**
     * A singleton point of access for this class.
     * 
     * @return An instance of GamesDatastore.
     */
    public static GamesDatastore getInstance() {
        return GamesDatastoreHolder.sINSTANCE;
    }

    @Override
    protected void initData() {
        synchronized (CACHE_LOCK) {
            mGamesCache = new HashMap<String, GameItem>();
        }
    }
    
    @Override
    public void clearData() {
        super.clearData();
        if (mGamesDAO != null) {
            mGamesDAO.clearTables();
        }
    }

    //Gets all games from cache. if cache is empty, fetch from server and store it
    public HashMap<Integer, ArrayList<GameItem>> getGameList(final boolean shouldForceFetch) {

        String games = "";
        HashMap<Integer, ArrayList<GameItem>> gameMap = new HashMap<Integer, ArrayList<GameItem>>();

        if (shouldForceFetch)
            getGameData();

        try {
            //fetch from store
            games = gamePreferences.getString(GAME_LIST_KEY, Constants.BLANKSTR);
            if (TextUtils.isEmpty(games)) {
                
//                //if empty, fetch from server and store it
//                long timestamp = gamePreferences.getLong(GAME_LIST_TIMESTAMP, 0);
//                requestGetGamesIndex(Session.getInstance().getUserId(), new ArrayList<Integer>(), DEFAULT_FETCH_LIMIT, 0, timestamp, "");
                //TODO
                getGameData();
            } else {
                gameMap = JsonParseUtils.deserializeGameMap(games);
                this.gameMap = gameMap;
            }
        } catch (Exception e) {
            Logger.error.log(LOG_TAG, e);
        }
        return gameMap;
    }

    //Get all banners from cache, if cache is empty, fetch from server and store it
    public ArrayList<Banner> getBannerList(final boolean shouldForceFetch) {

        ArrayList<Banner> bannersArr = new ArrayList<Banner>();

        if (shouldForceFetch)
            getGameData();

        String banners = "";

        try {
            banners = bannerPreferences.getString(BANNER_KEY, Constants.BLANKSTR);
            if (TextUtils.isEmpty(banners)) {
                //fetch from store
//                long timestamp = bannerPreferences.getLong(BANNER_TIMESTAMP, 0);
//                requestGetGameBanners(Session.getInstance().getUserId(), DEFAULT_FETCH_LIMIT, 0, timestamp, "");
                //TODO
                getGameData();
            } else {
                bannersArr = JsonParseUtils.deserializeBanners(banners);
            }
        } catch (Exception e) {
            Logger.error.log(LOG_TAG, e);
        }

        return bannersArr;
    }
    
    //TODO
    //Gets an game with the given id from cache. if cache is empty, fetch from server
    public GameItem getGameItemWithId(final String gameId) {
        synchronized (CACHE_LOCK) {
            GameItem game = mGamesCache.get(gameId);
            if (game == null) {
                //fetch game from server
                game = requestGetGameInfo(Session.getInstance().getUserId(), gameId);
            }
            return game;
        }
    }
    
    //TODO
    public void requestGetGameBanners(String userId, int limit, int offset, long lastModifiedTimestamp, String osVersion){
        
        //fetch banner list from server
        
        //send broadcast to game page
        
        //store banner list and timestamp to cache and database
    }
    
    //TODO
    public void requestGetGamesIndex(String userId, List<Integer> gameTypes, int limit, int offset, long lastModifiedTimestamp, String osVersion){
        
        //fetch game list from server
        
        //send broadcast to game page
        
        //store game list and timestamp to cache and database
    }
    
    //TODO
    public GameItem requestGetGameInfo(String userId, String gameId) {
        
        if (StringUtils.isEmpty(gameId)) return null;
        GameItem gameItem = null;
        //fetch full info of game from server
        //TODO
        ArrayList<GameItem> gameList1 = gameMap.get(GameItem.GAME_SINGLE);
        if (gameList1 != null) {
            for (GameItem item : gameList1) {
                if (gameId.equals(item.getGameId())) {
                    gameItem = item;
                    break;
                }
            }
        }

        ArrayList<GameItem> gameList2 = gameMap.get(GameItem.GAME_MULTIPLY);
        if (gameList1 != null && gameItem == null) {
            for (GameItem item : gameList2) {
                if (gameId.equals(item.getGameId())) {
                    gameItem = item;
                    break;
                }
            }
        }
        //store it to cache and database
        mGamesDAO.insertGamesToDatabase(gameItem);
        return gameItem;
    }
   
    //Loads all related data from persistent storage into cache.
    private void loadFromPersistentStorage() {
        //load all games to cache
        if(mGamesDAO != null){
            final List<GameItem> games = mGamesDAO.loadGamesFromDatabase();
            if(games != null){
                synchronized (CACHE_LOCK) {
                    mGamesCache.clear();
                    for(GameItem game : games){
                        mGamesCache.put(game.getGameId(), game);
                    }
                }
            }
        }
    }

    // a private AsyncTask class
    private class GetJsonTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String response = "";
            for (String url : urls) {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                try {
                    HttpResponse execute = client.execute(httpGet);
                    InputStream content = execute.getEntity().getContent();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            if(StringUtils.isEmpty(result)) return;
            
            HashMap<Integer, ArrayList<GameItem>> gameMap = new HashMap<Integer, ArrayList<GameItem>>();
            ArrayList<GameItem> singleGameList = new ArrayList<GameItem>();
            ArrayList<GameItem> multGameList = new ArrayList<GameItem>();
            ArrayList<Banner> banners = new ArrayList<Banner>();
            
            JsonParser parser = new JsonParser();
            JsonObject jsonObj = parser.parse(result).getAsJsonObject();

            JsonArray hotGameArray = jsonObj.getAsJsonArray("androidGameCenterBannerGames");
            Set<Integer> hotGameIdSet = new HashSet<Integer>();
            for (int i = 0; i < hotGameArray.size(); i++) {
                hotGameIdSet.add(hotGameArray.get(i).getAsInt());
            }

            JsonArray gameArr = jsonObj.getAsJsonArray("data");
            for (int i = 0; i < gameArr.size(); i++) {
                JsonObject gameObj = gameArr.get(i).getAsJsonObject();
                int type = gameObj.get("type").getAsInt();
                boolean shownOnLandingPage = gameObj.get("shownOnLanding").getAsBoolean();

                if ((shownOnLandingPage == false) || (type != GameItem.GAME_SINGLE && type != GameItem.GAME_MULTIPLY)) continue;

                String gameId = gameObj.get("gameId").getAsString();
                String name = gameObj.get("name").getAsString();
                String description = gameObj.get("description").getAsString();
                String howToPlay = gameObj.get("howToPlay").getAsString();
                String about = gameObj.get("aboutDeveloper").getAsString();
                String thumbnail = gameObj.get("thumbnail_mobile").getAsString();
                String actionUrl = gameObj.get("actionUrl").getAsString();
                JsonArray imageArr = new JsonArray();
                if (isDisplayBig() && gameObj.get("images") != null) {
                    imageArr = gameObj.get("images").getAsJsonArray();
                } else {
                    imageArr = gameObj.get("images_mobile").getAsJsonArray();
                }
                String images[] = new String[imageArr.size()];
                for (int imageIdx = 0; imageIdx < imageArr.size(); imageIdx ++) {
                    images[imageIdx] = imageArr.get(imageIdx).getAsString();
                }

                GameItem game = new GameItem(gameId, type, images, thumbnail, name, actionUrl);
                game.setDescriptionInfo(description);
                game.setAboutInfo(about);
                game.setHowToPlayInfo(howToPlay);

                try {
                    Integer gameIdInt = Integer.parseInt(gameId);
                    if (hotGameIdSet.contains(gameIdInt)) {
                        Banner banner = new Banner();
                        banner.setImageUrl(images[0]);
                        banner.setUrl("game:" + gameId);
                        banners.add(banner);
                    }
                } catch (Exception e) {
                    Logger.error.log(LOG_TAG, e);
                }

                if (type == GameItem.GAME_SINGLE) {
                    singleGameList.add(game);
                } else if (type == GameItem.GAME_MULTIPLY) {
                    multGameList.add(game);
                }
            }
            gameMap.put(GameItem.GAME_SINGLE, singleGameList);
            gameMap.put(GameItem.GAME_MULTIPLY, multGameList);
            
            GamesDatastore.getInstance().gameMap = gameMap;
            
            //send broadcast to game page
            BroadcastHandler.Game.sendFetchGamesCompleted();
            
            //store game list and timestamp to cache and database
            String toStoreGames = JsonParseUtils.serializeGames(gameMap);
            SharedPreferences.Editor gameEditor = gamePreferences.edit();
            gameEditor.putString(GAME_LIST_KEY, toStoreGames);
            gameEditor.commit();
            Logger.debug.log(LOG_TAG, "game data: ", toStoreGames);

            //send broadcast to game page
            BroadcastHandler.Game.sendFetchBannersCompleted();
            
            //store banners and timestamp to cache and database
            String toStoreBanner = JsonParseUtils.serializeBanner(banners);
            SharedPreferences.Editor bannerEditor = bannerPreferences.edit();
            bannerEditor.putString(BANNER_KEY, toStoreBanner);
            bannerEditor.commit();
            Logger.debug.log(LOG_TAG, "banner data: ", toStoreBanner);
            
        }
    }

    public void getGameData() {
        GetJsonTask task = new GetJsonTask();
        task.execute(new String[] {DATA_URL});
    }

    private boolean isDisplayBig() {
        DisplayMetrics metrics = ApplicationEx.getInstance().getDisplayMetrics();
        if (metrics != null) {
            switch (metrics.densityDpi) {
                case DisplayMetrics.DENSITY_LOW:
                    return false;
                case DisplayMetrics.DENSITY_MEDIUM:
                    return false;
                case DisplayMetrics.DENSITY_HIGH:
                    return true;
                case DisplayMetrics.DENSITY_XHIGH:
                case DisplayMetrics.DENSITY_XXHIGH:
                    return true;
            }
        }
        return false;
    }
    
}
