/**
 * Copyright (c) 2013 Project Goth
 *
 * GamesDAO.java
 * Created Jan 23, 2015, 11:30:13 AM
 */

package com.projectgoth.dao;

import java.util.ArrayList;
import java.util.List;

import com.projectgoth.b.data.GameItem;
import com.projectgoth.common.Logger;
import com.projectgoth.nemesis.utils.JsonParseUtils;
import com.projectgoth.util.AndroidLogger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


/**
 * @author shiyukun
 *
 */
public class GamesDAO extends BaseDAO{
    
    private static final String          LOG_TAG                          = AndroidLogger.makeLogTag(GamesDAO.class);
    
    // The name of the table.
    private static final String          TABLE_NAME                       = "games";
    
    // Columns in the table.
    private static final String          COLUMN_ID                        = "ID";    
    private static final String          COLUMN_DATA                      = "DATA";
    
    /**
     * Constructor
     * @param appCtx    Application context.
     */
    public GamesDAO(final Context appCtx) {
        super(appCtx, TABLE_NAME, null);
    }


    @Override
    protected void createTable() {
        try {
            final String createSqlCommand = "create table if not exists " + TABLE_NAME + " (" + 
                    COLUMN_ID + " TEXT PRIMARY KEY, " +                    
                    COLUMN_DATA + " TEXT" +                                       
                ");";
                        
            execSQL(createSqlCommand);            
        }        
        catch (Exception ex) {
            Logger.error.log(LOG_TAG, ex);
        }
    }  
    
    public synchronized boolean insertGamesToDatabase(final GameItem game){
        boolean result = false;
        if(game == null) return result;

        result = doTransaction(new TransactionRunnable() {
            @Override
            public void run(SQLiteDatabase db) {
                final String jsonStr = JsonParseUtils.serializeGame(game);
                ContentValues values = new ContentValues();
                values.put(COLUMN_ID, game.getGameId());
                values.put(COLUMN_DATA, jsonStr);

                if (db.update(TABLE_NAME, values, COLUMN_ID + " = ?",
                        new String[] { game.getGameId() }) <= 0) {
                    db.insertOrThrow(TABLE_NAME, null, values);
                }
            }
        });

        return result;
    }
    
    public synchronized boolean saveGamesToDatabase(final List<GameItem> gameList) {
        boolean result = false;
        
        if (gameList != null && !gameList.isEmpty()) {
            result = doTransaction(new TransactionRunnable() {
                @Override
                public void run(SQLiteDatabase db) {
                    for (GameItem game : gameList) {
                        final String jsonStr = JsonParseUtils.serializeGame(game);
                        ContentValues values = new ContentValues();
                        values.put(COLUMN_ID, game.getGameId());
                        values.put(COLUMN_DATA, jsonStr);

                        if (db.update(TABLE_NAME, values, COLUMN_ID + " = ?",
                                new String[] { game.getGameId() }) <= 0) {
                            db.insertOrThrow(TABLE_NAME, null, values);
                        }
                    }
                }
            });
        }
        
        return result;
    }    
    
    @SuppressWarnings("deprecation")
    public synchronized GameItem loadGameFromDatabase(String gameId){
        final String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = " + gameId; 
        Cursor cursor = null;
        GameItem game = null;
        try {            
            SQLiteDatabase db = getDatabase();
            cursor = db.rawQuery(query, null);
            
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                final String gameJsonStr = cursor.getString(cursor.getColumnIndex(COLUMN_DATA));                                        
                if (gameJsonStr != null) {                                                
                    game = JsonParseUtils.deserializeGame(gameJsonStr);                        
                }
            }
        }
        catch (Exception ex) {
            Logger.error.log(LOG_TAG, ex);          
        } finally {
            if (cursor != null) {
                cursor.deactivate();
                cursor.close();
            }
        }
        
        Logger.debug.log(LOG_TAG, query + " Retrieved game " + gameId);        
        return game;
    }
    
    @SuppressWarnings("deprecation")
    public synchronized List<GameItem> loadGamesFromDatabase() {
        final String query = "SELECT * FROM " + TABLE_NAME;
        
        List<GameItem> gameList = new ArrayList<GameItem>();        
        Cursor cursor = null;
        try {            
            SQLiteDatabase db = getDatabase();
            cursor = db.rawQuery(query, null);
            
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {                                                         
                    final String gameJsonStr = 
                            cursor.getString(cursor.getColumnIndex(COLUMN_DATA));                                        
                    
                    if (gameJsonStr != null) {                                                
                        final GameItem game = JsonParseUtils.deserializeGame(gameJsonStr);                        
                        gameList.add(game);
                    }
                    cursor.moveToNext();
                }
            }
        }
        catch (Exception ex) {
            Logger.error.log(LOG_TAG, ex);          
        } finally {
            if (cursor != null) {
                cursor.deactivate();
                cursor.close();
            }
        }
        
        Logger.debug.log(LOG_TAG, query + " Retrieved " + gameList.size() + " games");        
        return gameList;
    }


}
