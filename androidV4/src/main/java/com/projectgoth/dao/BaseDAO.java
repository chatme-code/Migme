/**
 * Copyright (c) 2013 Project Goth
 * BaseDAO.java
 * 
 * Jun 19, 2013 1:28:01 PM
 */
package com.projectgoth.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.projectgoth.common.Logger;
import com.projectgoth.util.AndroidLogger;

/**
 * Serves as the base class for all DAO implementations.
 * @author angelorohit
 */
public abstract class BaseDAO {
    
    private static final String LOG_TAG = AndroidLogger.makeLogTag(BaseDAO.class);

    static final Object mTransactionLock = new Object();

    // Set mDatabaseHelper to be static to be shared with all DAOs
    static protected DatabaseHelper mDatabaseHelper = null;

    // An list containing all the table names for this DAO. A DAO can have one or more tables.
    protected List<String>   mTableNameList = new ArrayList<String>();
    protected Context        mAppCtx;

    /**
     * Abstract function called to create the table
     */
    protected abstract void createTable(); 
    
    /**
     * Constructor
     * @param appCtx        Application context.
     * @param tableName     The name of the table for this DAO.
     * @param dbHelper      The {@link DatabaseHelper}. Can be null.
     */
    public BaseDAO(final Context appCtx, final String tableName, final DatabaseHelper dbHelper) {
        this(appCtx, tableName, dbHelper, false);
    }

    /**
     * Constructor
     * @param appCtx        Application context.
     * @param tableName     The name of the table for this DAO.
     * @param dbHelper      The {@link DatabaseHelper}. Can be null.
     * @param canUpgrade    Whether this DAO can drop the table in case of a database upgrade.
     */
    public BaseDAO(final Context appCtx, final String tableName, final DatabaseHelper dbHelper, final boolean canUpgrade) {        
        this(appCtx, Arrays.asList(tableName), dbHelper, canUpgrade);        
    }
    
    /**
     * Constructor
     * @param appCtx            Application context.
     * @param tableNameList     A list containing the names of the tables for this DAO.
     * @param dbHelper          The {@link DatabaseHelper}. Can be null.
     */
    public BaseDAO(final Context appCtx, final List<String> tableNameList, final DatabaseHelper dbHelper) {        
        this(appCtx, tableNameList, dbHelper, false);        
    }
    
    /**
     * Constructor
     * @param appCtx                       Application context.
     * @param tableNameList                A list containing the names of the tables for this DAO.
     * @param dbHelper                     The {@link DatabaseHelper}. Can be null.
     * @param onUpgradeCanDropTable        Whether this DAO can drop the table in case of a database upgrade.
     */
    public BaseDAO(final Context appCtx, final List<String> tableNameList, final DatabaseHelper dbHelper, final boolean onUpgradeCanDropTable) {
        mAppCtx = appCtx;
        if (tableNameList != null) {
            mTableNameList = tableNameList;
        }

        if (dbHelper != null) {
            mDatabaseHelper = dbHelper;
        } else if (mDatabaseHelper == null) {
            mDatabaseHelper = new DatabaseHelper(appCtx);
        }

        // Call DatabaseHelper#getDatabase() intentionally to launch SQLite database
        // This should be done before DatabaseHelper#shouldUpgrade() is called
        // Otherwise the DatabaseHelper#onUpgrade() would not be called,
        // and the DAOs would not know whether it should upgrade the database tables
        if (mDatabaseHelper != null && !mDatabaseHelper.isDatabaseLaunched()) {
            mDatabaseHelper.getDatabase();
            mDatabaseHelper.setDatabaseLaunched(true);
        }

        if (mDatabaseHelper.shouldUpgrade() && onUpgradeCanDropTable) {
            for (String tableName : mTableNameList) {
                final String query = "DROP TABLE " + tableName + ";";             
                try {                    
                    execSQL(query);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } 
            }
        }

        createTable();

        // Upgrade table
        if (mDatabaseHelper.shouldUpgrade()) {
            upgradeTable(mDatabaseHelper.getOldVersion(), mDatabaseHelper.getNewVersion());
        }

    }

    /**
     * Get the database
     * @return  The database retrieved via the DatabaseHelper.
     */
    protected SQLiteDatabase getDatabase() {
        try {
            return this.mDatabaseHelper.getDatabase();
        }
        catch (Exception ex) {
            Logger.error.log(LOG_TAG, ex);
        }
        
        return null;
    }
    
    /**
     * Clears the tables for this DAO.
     */
    public void clearTables() {
        doTransaction(new TransactionRunnable() {
            @Override
            public void run(SQLiteDatabase db) {
                for (String tableName : BaseDAO.this.mTableNameList) {
                    db.delete(tableName, null, null);
                }
            }
        });
    }
    
    /**
     * Executes an exclusive SQL statement that is NOT a SELECT or any other SQL statement that returns data.
     * @param sqlCommand    The SQL command to be executed.
     * @throws SQLException
     */
    public void execSQL(final String sqlCommand) {
        doTransaction(new TransactionRunnable() {
            @Override
            public void run(SQLiteDatabase db) {
                db.execSQL(sqlCommand);
            }
        });
    }

    protected boolean doTransaction(TransactionRunnable runnable) {
        boolean result = false;
        if (runnable == null) {
            throw new IllegalArgumentException("TransactionRunnable can't be null");
        }

        final SQLiteDatabase db = getDatabase();
        if (db != null) {
            synchronized (mTransactionLock) {
                try {
                    db.beginTransaction();
                    runnable.run(db);
                    db.setTransactionSuccessful();
                    result = true;
                }
                catch (SQLException ex) {
                    Logger.error.log(LOG_TAG, ex);
                }
                finally {
                    db.endTransaction();
                }
            }
        }
        return result;
    }

    /**
     * run database operation in transaction
     */
    public interface TransactionRunnable {
        public void run(SQLiteDatabase db);
    }

    /*
     * A template method for DAOs which have the need to upgrade tables when database is upgrading
     * Those DAOs should override this method
     * This method will be called when the version number of the database is increased
     */
    protected void upgradeTable(int oldDatabaseVersion, int newDatabaseVersion) {
    }
}

