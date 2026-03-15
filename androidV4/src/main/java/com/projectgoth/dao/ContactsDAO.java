/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatsDAO.java
 * Created Jul 15, 2013, 1:29:25 PM
 */

package com.projectgoth.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import com.google.gson.Gson;
import com.projectgoth.b.data.Profile;
import com.projectgoth.blackhole.enums.ImType;
import com.projectgoth.blackhole.enums.PresenceType;
import com.projectgoth.common.Logger;
import com.projectgoth.datastore.VersionedData;
import com.projectgoth.nemesis.model.Friend;
import com.projectgoth.nemesis.model.User;
import com.projectgoth.util.AndroidLogger;
import java.util.Arrays;

/**
 * Manages persistence and loading of all Contact related data.
 * 
 * @author angelorohit
 */
public class ContactsDAO extends BaseDAO {

    private static final String LOG_TAG                      = AndroidLogger.makeLogTag(ContactsDAO.class);

    // TODO: refactor. store friend and profile in a single table

    // The name of the table.
    private static final String FRIEND_TABLE_NAME            = "contacts";
    private static final String PROFILE_TABLE_NAME           = "user_profile";

    // Columns in the table.
    private static final String COLUMN_CONTACTID             = "CONTACTID";
    private static final String COLUMN_CONTACTGROUPID        = "CONTACTGROUPID";
    private static final String COLUMN_DISPLAYNAME           = "DISPLAYNAME";
    private static final String COLUMN_MOBILENUMBER          = "MOBILENUMBER";
    private static final String COLUMN_USERNAME              = "USERNAME";
    private static final String COLUMN_STATUSMESSAGE         = "STATUSMESSAGE";
    private static final String COLUMN_GUID                  = "GUID";

    // Columns in user profile table.
    private static final String COLUMN_DATA                  = "DATA";
    private static final String COLUMN_ISREMOVED             = "ISREMOVED";
    private static final String COLUMN_ISALTERED             = "ISALTERED";
    private static final String COLUMN_LAST_UPDATE_TIMESTAMP = "LAST_UPDATE_TIMESTAMP";

    public ContactsDAO(final Context appCtx) {
        super(appCtx, Arrays.asList(FRIEND_TABLE_NAME, PROFILE_TABLE_NAME), null);
    }

    /**
     * @see com.projectgoth.dao.BaseDAO#createTable()
     */
    @Override
    protected void createTable() {
        try {
            final String createSqlCommand = "create table if not exists " + FRIEND_TABLE_NAME + " (" + COLUMN_CONTACTID
                    + " INTEGER PRIMARY KEY, " + COLUMN_CONTACTGROUPID + " INTEGER," + COLUMN_DISPLAYNAME + " TEXT,"
                    + COLUMN_MOBILENUMBER + " TEXT," + COLUMN_USERNAME + " TEXT," + COLUMN_STATUSMESSAGE + " TEXT,"
                    + COLUMN_GUID + " TEXT" + ");";

            final String userProfileTableCommand = String
                    .format("create table if not exists %s (%s TEXT PRIMARY KEY, %s TEXT, %s INTEGER, %s INTEGER, %s INTEGER);",
                            PROFILE_TABLE_NAME, COLUMN_USERNAME, COLUMN_DATA, COLUMN_ISREMOVED, COLUMN_ISALTERED,
                            COLUMN_LAST_UPDATE_TIMESTAMP);

            execSQL(createSqlCommand);
            execSQL(userProfileTableCommand);
        } catch (Exception ex) {
            Logger.error.log(LOG_TAG, ex);
        }
    }

    /**
     * Loads all Friends from the database and constructs them.
     * 
     * @return A SparseArray with Friend contact id as key and value as Friend.
     */
    @SuppressWarnings("deprecation")
    public synchronized SparseArray<Friend> loadFriends() {
        final String query = "SELECT * FROM " + FRIEND_TABLE_NAME;

        SparseArray<Friend> friendsSparseArray = new SparseArray<Friend>();

        Cursor cursor = null;
        try {
            SQLiteDatabase db = getDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    final int contactId = cursor.getInt(cursor.getColumnIndex(COLUMN_CONTACTID));
                    final int contactGroupId = cursor.getInt(cursor.getColumnIndex(COLUMN_CONTACTGROUPID));
                    final String displayName = cursor.getString(cursor.getColumnIndex(COLUMN_DISPLAYNAME));
                    final String mobileNumber = cursor.getString(cursor.getColumnIndex(COLUMN_MOBILENUMBER));
                    final String username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
                    final String statusMessage = cursor.getString(cursor.getColumnIndex(COLUMN_STATUSMESSAGE));
                    final String guid = cursor.getString(cursor.getColumnIndex(COLUMN_GUID));

                    final Friend friend = new Friend(contactId, displayName, username, mobileNumber, statusMessage,
                            guid, contactGroupId, PresenceType.OFFLINE, ImType.FUSION);

                    friendsSparseArray.put(friend.getContactID(), friend);

                    cursor.moveToNext();
                }
            }
        } catch (Exception ex) {
            Logger.error.log(LOG_TAG, ex);
        } finally {
            if (cursor != null) {
                cursor.deactivate();
                cursor.close();
            }
        }

        Logger.debug.log(LOG_TAG, query + " Retrieved " + friendsSparseArray.size() + " contacts");
        return friendsSparseArray;
    }

    /**
     * Saves a Friend to database.
     * 
     * @param friend
     *            The Friend to be saved.
     * @return true if the Friend was successfully persisted and false
     *         otherwise.
     */
    public synchronized boolean saveFriend(final Friend friend) {
        boolean result = false;

        if (friend != null) {
            result = doTransaction(new TransactionRunnable() {
                @Override
                public void run(SQLiteDatabase db) {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_CONTACTID, friend.getContactID());
                    values.put(COLUMN_CONTACTGROUPID, friend.getGroupID());
                    values.put(COLUMN_DISPLAYNAME, friend.getDisplayName());
                    values.put(COLUMN_MOBILENUMBER, friend.getMobileNumber());
                    values.put(COLUMN_USERNAME, friend.getUsername());
                    values.put(COLUMN_STATUSMESSAGE, friend.getStatusMessage());
                    values.put(COLUMN_GUID, friend.getGUID());

                    if (db.update(FRIEND_TABLE_NAME, values, COLUMN_CONTACTID + " = ?",
                            new String[] { String.valueOf(friend.getContactID()) }) <= 0) {
                        db.insertOrThrow(FRIEND_TABLE_NAME, null, values);
                    }
                }
            });
        }

        return result;
    }

    /**
     * Load {@link Friend} from the database
     * 
     * @param username
     * @return
     */
    @SuppressWarnings("deprecation")
    public synchronized Friend loadFriend(String username) {
        Friend result = null;
        Cursor cursor = null;
        try {
            final String query = String.format("SELECT * FROM %s WHERE %s=\'%s\'", FRIEND_TABLE_NAME, COLUMN_USERNAME,
                    username);

            SQLiteDatabase db = getDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                if (!cursor.isAfterLast()) {
                    final int contactId = cursor.getInt(cursor.getColumnIndex(COLUMN_CONTACTID));
                    final int contactGroupId = cursor.getInt(cursor.getColumnIndex(COLUMN_CONTACTGROUPID));
                    final String displayName = cursor.getString(cursor.getColumnIndex(COLUMN_DISPLAYNAME));
                    final String mobileNumber = cursor.getString(cursor.getColumnIndex(COLUMN_MOBILENUMBER));
                    final String statusMessage = cursor.getString(cursor.getColumnIndex(COLUMN_STATUSMESSAGE));
                    final String guid = cursor.getString(cursor.getColumnIndex(COLUMN_GUID));

                    result = new Friend(contactId, displayName, username, mobileNumber, statusMessage, guid,
                            contactGroupId, PresenceType.OFFLINE, ImType.FUSION);
                }
            }
        } catch (Exception ex) {
            Logger.error.log(LOG_TAG, ex);
        } finally {
            if (cursor != null) {
                cursor.deactivate();
                cursor.close();
            }
        }

        return result;
    }

    /**
     * load the {@link User} data
     * 
     * @param username
     * @return
     */
    public synchronized VersionedData<User> loadUser(String username) {
        VersionedData<User> result = null;
        User user = null;

        Friend friend = loadFriend(username);
        if (friend != null) {
            user = new User(friend);
        }

        VersionedData<Profile> profile = loadProfile(username);
        if (profile != null) {
            if (user == null) {
                user = new User(profile.getData());
            } else {
                user.setProfile(profile.getData());
            }

            if (result == null) {
                result = new VersionedData<User>();
            }

            result.setIsAltered(profile.isAltered());
            result.setIsRemoved(profile.isRemoved());
            result.setLastUpdateTimestamp(profile.getLastUpdateTimestamp());
        }
        
        if (user != null) {
            if (result == null) {
                result = new VersionedData<User>();
            }
            result.setData(user);
        }

        return result;
    }

    /**
     * save the {@link User} data
     * 
     * @param user
     * @return
     */
    public synchronized boolean saveUser(VersionedData<User> user) {
        boolean friendResult = false;
        boolean profileResult = false;

        User data = user.getData();

        Friend friend = data.getFriend();
        if (friend != null) {
            friendResult = saveFriend(friend);
        }

        Profile profileData = data.getProfile();
        if (profileData != null) {
            VersionedData<Profile> profile = new VersionedData<Profile>();
            profile.setIsAltered(user.isAltered());
            profile.setIsRemoved(user.isRemoved());
            profile.setLastUpdateTimestamp(user.getLastUpdateTimestamp());
            profileResult = saveProfile(profile);
        }

        return friendResult || profileResult;
    }

    /**
     * Loads a post with the given id from database.
     * 
     * @param id
     *            The id of the profile to be loaded.
     * @return the {@link Profile} {@link VersionedData} data
     */
    @SuppressWarnings("deprecation")
    public synchronized VersionedData<Profile> loadProfile(String username) {
        VersionedData<Profile> result = null;
        Cursor cursor = null;
        try {
            final String query = String.format("SELECT * FROM %s WHERE %s=\'%s\'", PROFILE_TABLE_NAME, COLUMN_USERNAME,
                    username);

            SQLiteDatabase db = getDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.getCount() > 0) {
                Gson gson = new Gson();
                cursor.moveToFirst();
                if (!cursor.isAfterLast()) {
                    final String profileJsonStr = cursor.getString(cursor.getColumnIndex(COLUMN_DATA));

                    if (profileJsonStr != null) {
                        final Profile profile = gson.fromJson(profileJsonStr, Profile.class);
                        if (profile != null) {
                            result = new VersionedData<Profile>(profile);
                            final int isRemoved = cursor.getInt(cursor.getColumnIndex(COLUMN_ISREMOVED));
                            result.setIsRemoved(isRemoved > 0);
                            final int isAltered = cursor.getInt(cursor.getColumnIndex(COLUMN_ISALTERED));
                            result.setIsAltered(isAltered > 0);
                            final long lastUpdate = cursor.getLong(cursor.getColumnIndex(COLUMN_LAST_UPDATE_TIMESTAMP));
                            result.setLastUpdateTimestamp(lastUpdate);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.error.log(LOG_TAG, ex);
        } finally {
            if (cursor != null) {
                cursor.deactivate();
                cursor.close();
            }
        }

        return result;
    }

    public synchronized boolean saveProfile(final VersionedData<Profile> data) {
        boolean result = false;

        if (data != null) {
            result = doTransaction(new TransactionRunnable() {
                @Override
                public void run(SQLiteDatabase db) {
                    final Profile profile = data.getData();
                    if (profile != null) {
                        ContentValues profileTablevalues = getContentValuesFromProfile(data);
                        if (db.update(PROFILE_TABLE_NAME, profileTablevalues, COLUMN_USERNAME + " = ?",
                                new String[] { profile.getUsername() }) <= 0) {
                            db.insertOrThrow(PROFILE_TABLE_NAME, null, profileTablevalues);
                        }
                    }
                }
            });
        }

        return result;
    }

    private ContentValues getContentValuesFromProfile(final VersionedData<Profile> data) {
        ContentValues tablevalues = new ContentValues();
        if (data != null && data.getData() != null) {
            final Profile profile = data.getData();
            final String profileStr = new Gson().toJson(profile);

            tablevalues.put(COLUMN_USERNAME, profile.getUsername());
            tablevalues.put(COLUMN_DATA, profileStr);
            tablevalues.put(COLUMN_ISREMOVED, data.isRemoved());
            tablevalues.put(COLUMN_ISALTERED, data.isAltered());
            tablevalues.put(COLUMN_LAST_UPDATE_TIMESTAMP, data.getLastUpdateTimestamp());
        }

        return tablevalues;
    }
}
