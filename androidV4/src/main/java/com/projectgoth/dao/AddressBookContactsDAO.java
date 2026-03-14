/**
 * Copyright (c) 2013 Project Goth
 *
 * AddressBookContactsDAO.java
 * Created Aug 27, 2013, 3:59:37 PM
 */

package com.projectgoth.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Logger;
import com.projectgoth.datastore.Session;
import com.projectgoth.model.AddressBookContact;
import com.projectgoth.util.AndroidLogger;

/**
 * @author angelorohit
 */
public class AddressBookContactsDAO extends BaseDAO {

    // A logging tag for this particular class.
    private final static String LOG_TAG                          = AndroidLogger.makeLogTag(AddressBookContactsDAO.class);
    
    // The name of this table
    private final static String TABLE_NAME                       = "addressbookcontacts";
    
    // The various fields used in the table that are saved to db.
    private final static String COLUMN_ID                        = "ID";
    private final static String COLUMN_NAME                      = "NAME";
    private final static String COLUMN_PHONE_NUMBERS_WAS_SYNCED  = "WAS_PHONE_NUMBERS_SYNCED";
    private final static String COLUMN_EMAILS_WAS_SYNCED         = "WAS_EMAIL_SYNCED";
    private final static String COLUMN_PHONE_NUMBERS             = "PHONE_NUMBERS";
    private final static String COLUMN_EMAIL                     = "EMAIL";
    private final static String SHARED_PREFS_FILE_NAME_PREFIX    = "AddressBookSharedPrefs";
    private final static String SHARED_PREFS_TABLE_VERSION       = "AddressBookTableVersion";
    private final static int    TABLE_VERSION                    = 1;
    
    /**
     * Constructor
     * @param appCtx    Application context.
     */
    public AddressBookContactsDAO(final Context appCtx) {
        super(appCtx, TABLE_NAME, null);
    }  
    
    /**
     * @see com.projectgoth.dao.BaseDAO#createTable()
     */
    @Override
    protected void createTable() {
        try {
            if (getSharedPrefsTableVersion() != TABLE_VERSION) {
                final String dropSqlCoomand = "Drop table if exists " + TABLE_NAME;
                execSQL(dropSqlCoomand);
            }

            final String createSqlCommand = "create table if not exists " + TABLE_NAME + " (" + 
                    COLUMN_ID + " TEXT PRIMARY KEY," +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_PHONE_NUMBERS_WAS_SYNCED + " INTEGER," +
                    COLUMN_EMAILS_WAS_SYNCED + " INTEGER," +
                    COLUMN_PHONE_NUMBERS + " TEXT," +
                    COLUMN_EMAIL + " TEXT" +
                ");";
                        
            execSQL(createSqlCommand);
            updateSharedPrefsTableVersion();
        }        
        catch (Exception ex) {
            Logger.error.log(LOG_TAG, ex);
        }
    }

    private int getSharedPrefsTableVersion() {
        SharedPreferences sharedPref = ApplicationEx.getInstance().getSharedPreferences(
                getSharedPrefsFileName(), Context.MODE_PRIVATE);
        return sharedPref.getInt(SHARED_PREFS_TABLE_VERSION, 0);
    }

    private void updateSharedPrefsTableVersion() {
        SharedPreferences sharedPref = ApplicationEx.getInstance().getSharedPreferences(
                getSharedPrefsFileName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(SHARED_PREFS_TABLE_VERSION, TABLE_VERSION);
        editor.commit();
    }

    private String getSharedPrefsFileName() {
        final String userName = Session.getInstance().getUsername();
        if (userName != null) {
            return SHARED_PREFS_FILE_NAME_PREFIX + userName;
        }
        return SHARED_PREFS_FILE_NAME_PREFIX;
    }

    /**
     * Queries the db and returns a map containing all the contacts that were stored in it.
     * @return A map with key as contact id and value as AddressBookContact.
     * @see com.projectgoth.model.AddressBookContact
     */
    @SuppressWarnings("deprecation")
    public synchronized Map<String, AddressBookContact> loadAllAddressBookContacts() {
        final String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " ASC";

        Map<String, AddressBookContact> contactDataMap = new HashMap
                <String, AddressBookContact>();
        Cursor cursor = null;
        try {            
            SQLiteDatabase db = getDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    final String id = cursor.getString(cursor.getColumnIndex(COLUMN_ID));
                    final String displayName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));                    
                    final int wasNumberSynced = cursor.getInt(cursor.getColumnIndex(COLUMN_PHONE_NUMBERS_WAS_SYNCED));
                    final int wasEmailSynced = cursor.getInt(cursor.getColumnIndex(COLUMN_EMAILS_WAS_SYNCED));
                    final String jsonContactNumbersStr = cursor.getString(cursor.getColumnIndex(COLUMN_PHONE_NUMBERS));
                    final String jsonContactEmailList = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL));

                    if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(displayName)) {
                        final ArrayList<String> contactNumbersArr = new Gson().fromJson(jsonContactNumbersStr, ArrayList.class);
                        final ArrayList<String> contactEmailList = new Gson().fromJson(jsonContactEmailList, ArrayList.class);

                        if (contactNumbersArr != null || contactEmailList != null) {
                            AddressBookContact contact = new AddressBookContact(id, displayName);
                            contact.setName(displayName);                       
                            contact.setWasNumberSynced((wasNumberSynced == 0) ? false : true);
                            contact.setWasEmailSynced((wasEmailSynced == 0) ? false : true);

                            if (contactNumbersArr != null && contactNumbersArr.size() > 0) {
                                contact.setNumberList(contactNumbersArr);
                                if (contactEmailList == null || (contactEmailList != null && contactEmailList.size() == 0)) {
                                    contact.setDataRowOnlyStoredPhoneNumbers(true);
                                }
                            }
                            if (contactEmailList != null && contactEmailList.size() > 0) {
                                contact.setEmailList(contactEmailList);
                            }
                            contactDataMap.put(contact.getId(), contact);
                        }
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
        
        Logger.debug.log(LOG_TAG, query + " Queried " + contactDataMap.size() + " address book contacts");        
        return contactDataMap;
    }

    /**
     * Inserts a new contact or updates an existing contact in the db. 
     * @param contact   The {@link AddressBookContact} to be added or updated.
     * @return          true on success and false otherwise.
     */
    public synchronized boolean saveAddressBookContactToDatabase(final AddressBookContact contact) {
        boolean result = false;

        if (contact != null) {
            result = doTransaction(new TransactionRunnable() {
                @Override
                public void run(SQLiteDatabase db) {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_ID, contact.getId());
                    values.put(COLUMN_NAME, contact.getName());
                    values.put(COLUMN_PHONE_NUMBERS_WAS_SYNCED, contact.getWasNumberSynced());
                    values.put(COLUMN_EMAILS_WAS_SYNCED, contact.getWasEmailSynced());
                    // Store the phone numbers as a json array
                    if (contact.getNumbers() != null) {
                        final String jsonContactNumbersStr = new Gson().toJson(contact.getNumbers().toArray());
                        values.put(COLUMN_PHONE_NUMBERS, jsonContactNumbersStr);
                    }
                    if (contact.getEmailList() != null) {
                        final String jsonContactEmailList = new Gson().toJson(contact.getEmailList().toArray());
                        values.put(COLUMN_EMAIL, jsonContactEmailList);
                    }

                    if (db.update(TABLE_NAME, values, COLUMN_ID + " = ?",
                            new String[] { contact.getId() }) <= 0) {
                        db.insertOrThrow(TABLE_NAME, null, values);
                    }
                }
            });
        }
        
        return result;        
    }
}
