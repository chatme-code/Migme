/**
 * Copyright (c) 2013 Project Goth
 * AlertsDAO.java
 * 
 * Jun 19, 2013 1:52:45 PM
 */
package com.projectgoth.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.projectgoth.b.data.Alert;
import com.projectgoth.common.Logger;
import com.projectgoth.nemesis.utils.JsonParseUtils;
import com.projectgoth.util.AndroidLogger;
import java.util.ArrayList;
import java.util.List;


/**
 * Manages persistence and loading of all alerts related data.
 * Used by the AlertsDatastore.
 * @author angelorohit
 */
public class AlertsDAO extends BaseDAO {

    private static final String          LOG_TAG                          = AndroidLogger.makeLogTag(AlertsDAO.class);
    
    // The name of the table.
    private static final String          TABLE_NAME                       = "alerts";
    
    // Columns in the table.
    private static final String          COLUMN_ID                        = "ID";    
    private static final String          COLUMN_DATA                      = "DATA";

    /**
     * Constructor
     * @param appCtx    Application context.
     */
    public AlertsDAO(final Context appCtx) {
        super(appCtx, TABLE_NAME, null);
    }   
    
    /**
     * @see com.projectgoth.dao.BaseDAO#createTable()
     */
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
    
    /**
     * Loads all Alert from database and constructs them. 
     * @return  A list containing all the Alert loaded from persistent storage.
     */
    @SuppressWarnings("deprecation")
    public synchronized List<Alert> loadAlertsFromDatabase() {
        final String query = "SELECT * FROM " + TABLE_NAME;
        
        List<Alert> alertList = new ArrayList<Alert>();        
        Cursor cursor = null;
        try {            
            SQLiteDatabase db = getDatabase();
            cursor = db.rawQuery(query, null);
            
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {                                                         
                    final String alertJsonStr = 
                            cursor.getString(cursor.getColumnIndex(COLUMN_DATA));                                        
                    
                    if (alertJsonStr != null) {                                                
                        final Alert alert = JsonParseUtils.deserializeAlert(alertJsonStr);                        
                        alertList.add(alert);
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
        
        Logger.debug.log(LOG_TAG, query + " Retrieved " + alertList.size() + " alerts");        
        return alertList;
    }

    /**
     * Saves a List of Alert to database
     * @param alertList     The List of Alert to be saved.
     * @return              true on success and false otherwise.
     */
    public synchronized boolean saveAlertsToDatabase(final List<Alert> alertList) {
        boolean result = false;
        
        if (alertList != null && !alertList.isEmpty()) {
            result = doTransaction(new TransactionRunnable() {
                @Override
                public void run(SQLiteDatabase db) {
                    for (Alert alert : alertList) {
                        final String jsonStr = JsonParseUtils.serializeAlert(alert);
                        ContentValues values = new ContentValues();
                        values.put(COLUMN_ID, alert.getId());
                        values.put(COLUMN_DATA, jsonStr);

                        if (db.update(TABLE_NAME, values, COLUMN_ID + " = ?",
                                new String[] { alert.getId() }) <= 0) {
                            db.insertOrThrow(TABLE_NAME, null, values);
                        }
                    }
                }
            });
        }
        
        return result;
    }
}

