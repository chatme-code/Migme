/**
 * Copyright (c) 2013 Project Goth
 *
 * ContactGroupsDAO.java
 * Created Jul 15, 2013, 3:12:59 PM
 */
package com.projectgoth.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import com.projectgoth.common.Logger;
import com.projectgoth.nemesis.model.ContactGroup;
import com.projectgoth.util.AndroidLogger;

/**
 * @author angelorohit
 */
public class ContactGroupsDAO extends BaseDAO {

    private static final String LOG_TAG          = AndroidLogger.makeLogTag(ContactGroupsDAO.class);

    // The name of the table.
    private static final String TABLE_NAME       = "contactgroups";

    // Columns in the table.
    private static final String COLUMN_GROUPID   = "GROUPID";
    private static final String COLUMN_GROUPNAME = "GROUPNAME";
    
    /**
     * Constructor
     * @param appCtx    Application context.
     */
    public ContactGroupsDAO(final Context appCtx) {
        super(appCtx, TABLE_NAME, null);
    }   
    
    /**
     * @see com.projectgoth.dao.BaseDAO#createTable()
     */
    @Override
    protected void createTable() {
        try {
            final String createSqlCommand = "create table if not exists " + TABLE_NAME + " (" + 
                    COLUMN_GROUPID + " INTEGER PRIMARY KEY, " +                    
                    COLUMN_GROUPNAME + " TEXT" +           
                ");";
                        
            execSQL(createSqlCommand);            
        }        
        catch (Exception ex) {
            Logger.error.log(LOG_TAG, ex);
        }
    }

    /**
     * Loads all ContactGroups from database and constructs them.
     * @return A SparseArray with ContactGroup groupId as key and the ContactGroup as value.
     */
    @SuppressWarnings("deprecation")
    public synchronized SparseArray<ContactGroup> loadContactGroupsFromDatabase() { 
        final String query = "SELECT * FROM " + TABLE_NAME;
        SparseArray<ContactGroup> contactGroupsSparseArray = new SparseArray<ContactGroup>();
        
        Cursor cursor = null;
        try {
            SQLiteDatabase db = getDatabase();
            cursor = db.rawQuery(query, null);
            
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {                                                         
                    final int groupId = cursor.getInt(cursor.getColumnIndex(COLUMN_GROUPID));
                    final String groupName = cursor.getString(cursor.getColumnIndex(COLUMN_GROUPNAME));
                    
                    final ContactGroup contactGroup = new ContactGroup(groupId, groupName);
                    contactGroupsSparseArray.put(contactGroup.getGroupID(), contactGroup);
                    
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
        
        Logger.debug.log(LOG_TAG, query + " Retrieved " + contactGroupsSparseArray.size() + " contact groups");   
        return contactGroupsSparseArray;
    }
    
    /**
     * Saves a ContactGroup to database.
     * @param contactGroup  The ContactGroup to be saved.
     * @return              true if the ContactGroup was successfully saved and false otherwise.
     */
    public synchronized boolean saveContactGroupToDatabase(final ContactGroup contactGroup) {
        boolean result = false;
        
        if (contactGroup != null) {
            result = doTransaction(new TransactionRunnable() {
                @Override
                public void run(SQLiteDatabase db) {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_GROUPID, contactGroup.getGroupID());
                    values.put(COLUMN_GROUPNAME, contactGroup.getGroupName());

                    if (db.update(TABLE_NAME, values, COLUMN_GROUPID + " = ?",
                            new String[] { String.valueOf(contactGroup.getGroupID()) }) <= 0) {
                        db.insertOrThrow(TABLE_NAME, null, values);
                    }
                }
            });
        }
        
        return result;
    }
}
