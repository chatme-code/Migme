/**
 * Copyright (c) 2013 Project Goth
 * DatabaseHelper.java
 * 
 * Jun 14, 2013 11:29:07 AM
 */
package com.projectgoth.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.projectgoth.common.DefaultConfig;


/**
 * @author angelorohit
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int      CURRENT_DATABASE_VERSION_NUMBER   = DefaultConfig.VERSION_WITH_FAILED_MESSAGE_TABLE;

    protected static final String DATABASE_NAME                     = "mig33.db";
    protected int                 mOldVersion                       = CURRENT_DATABASE_VERSION_NUMBER;
    protected int                 mNewVersion                       = CURRENT_DATABASE_VERSION_NUMBER;

    boolean                       mIsDatabaseLaunched               = false;

    /**
     * Constructor
     * @param appCtx The application context.
     */
    public DatabaseHelper(Context appCtx) {
        super(appCtx, DATABASE_NAME, null, CURRENT_DATABASE_VERSION_NUMBER);
    }
    
    /**
     * Destructor
     */
    public void finalize() {
        this.close();
        try {
            super.finalize();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /* Implementation of Base class
     * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Do nothing        
    }

    /*
     * Implementation of Base class
     * This method will be called when the version number of the database is increased
     * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        this.mNewVersion = newVersion;
        this.mOldVersion = oldVersion;

        onCreate(db);
    }

    /**
     * Get the database.
     * @return A database that can be written to.
     */
    public SQLiteDatabase getDatabase() {
        return this.getWritableDatabase();
    }
    
    /**
     * Indicates whether the database has just been upgraded.
     * @return true if the database has been upgraded and false otherwise.
     */
    public boolean shouldUpgrade() {
        return (mNewVersion > mOldVersion);
    }
    
    /**
     * Get the old version of the database before it was upgraded.
     * @return The old database version
     */
    public int getOldVersion() {
        return mOldVersion;
    }
    
    /**
     * Get the new version of the database after it got upgraded.
     * @return The new database version
     */
    public int getNewVersion() {
        return mNewVersion;
    }

    public boolean isDatabaseLaunched() {
        return mIsDatabaseLaunched;
    }

    public void setDatabaseLaunched(boolean launched) {
        mIsDatabaseLaunched = launched;
    }
}

