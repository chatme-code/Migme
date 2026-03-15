/**
 * Copyright (c) 2013 Project Goth
 * PostsDAO.java
 * 
 * Jun 19, 2013 6:01:40 PM
 */

package com.projectgoth.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.projectgoth.b.data.Post;
import com.projectgoth.common.Logger;
import com.projectgoth.datastore.VersionedData;
import com.projectgoth.model.PostCategory;
import com.projectgoth.util.AndroidLogger;
import java.util.Arrays;
import java.util.List;

/**
 * Manages persistence and loading of all post-related data. Used by the
 * PostsDatastore.
 * 
 * @author angelorohit
 */
public class PostsDAO extends BaseDAO {

    private static final String LOG_TAG                      = AndroidLogger.makeLogTag(PostsDAO.class);

    // The name of the table.
    private static final String POSTS_TABLE_NAME             = "posts";
    private static final String POSTCATEGORIES_TABLE_NAME    = "postcategories";

    // Columns in the tables.
    private static final String COLUMN_ID                    = "ID";
    private static final String COLUMN_DATA                  = "DATA";
    private static final String COLUMN_CATEGORY              = "CATEGORY";
    private static final String COLUMN_ISREMOVED             = "ISREMOVED";
    private static final String COLUMN_ISALTERED             = "ISALTERED";
    private static final String COLUMN_LAST_UPDATE_TIMESTAMP = "LAST_UPDATE_TIMESTAMP";

    /**
     * Constructor
     * 
     * @param appCtx
     *            Application context.
     */
    public PostsDAO(final Context appCtx) {
        super(appCtx, Arrays.asList(POSTS_TABLE_NAME, POSTCATEGORIES_TABLE_NAME), null);
    }

    /**
     * @see com.projectgoth.dao.BaseDAO#createTable()
     */
    @Override
    protected void createTable() {
        try {
            final String createPostsTableCommand = String
                    .format("create table if not exists %s (%s TEXT PRIMARY KEY, %s TEXT, %s INTEGER, %s INTEGER, %s INTEGER);",
                            POSTS_TABLE_NAME, COLUMN_ID, COLUMN_DATA, COLUMN_ISREMOVED, COLUMN_ISALTERED,
                            COLUMN_LAST_UPDATE_TIMESTAMP);

            final String createPostCategoriesTableCommand = String.format(
                    "create table if not exists %s (%s TEXT PRIMARY KEY, %s TEXT, %s INTEGER, %s INTEGER, %s INTEGER)",
                    POSTCATEGORIES_TABLE_NAME, COLUMN_CATEGORY, COLUMN_DATA, COLUMN_ISREMOVED, COLUMN_ISALTERED,
                    COLUMN_LAST_UPDATE_TIMESTAMP);

            execSQL(createPostsTableCommand);
            execSQL(createPostCategoriesTableCommand);
        } catch (Exception ex) {
            Logger.error.log(LOG_TAG, ex);
        }
    }

    /**
     * Loads a list of post ids matching a given key from database.
     * 
     * @param key
     *            The key to be matched.
     * @return A list of post ids that match the given key or null if no match
     *         is found.
     */
    @SuppressWarnings("deprecation")
    public synchronized VersionedData<List<String>> loadPostIdsForCategoryFromDatabase(final String key) {
        VersionedData<List<String>> result = null;
        List<String> data = null;

        Cursor cursor = null;
        try {
            final String query = String.format("SELECT * FROM %s WHERE %s=\'%s\'", POSTCATEGORIES_TABLE_NAME,
                    COLUMN_CATEGORY, key);
            
            SQLiteDatabase db = getDatabase();
            cursor = db.rawQuery(query, null);
            
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                if (!cursor.isAfterLast()) {
                    final String postIdJsonArrStr = cursor.getString(cursor.getColumnIndex(COLUMN_DATA));
                    if (postIdJsonArrStr != null) {
                        final String[] postIdArr = new Gson().fromJson(postIdJsonArrStr, String[].class);
                        if (postIdArr != null) {
                            data = Arrays.asList(postIdArr);
                            result = new VersionedData<List<String>>(data);
                            final int isRemoved = cursor.getInt(cursor.getColumnIndex(COLUMN_ISREMOVED));
                            result.setIsRemoved((isRemoved == 0) ? false : true);
                            final int isAltered = cursor.getInt(cursor.getColumnIndex(COLUMN_ISALTERED));
                            result.setIsAltered((isAltered == 0) ? false : true);
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

    /**
     * Loads a post with the given id from database.
     * 
     * @param postId
     *            The id of the post to be loaded.
     * @return The loaded DirtyData Post or null if a post matching the given id
     *         could not be found in database.
     */
    @SuppressWarnings("deprecation")
    public synchronized VersionedData<Post> loadPostWithIdFromDatabase(String postId) {
        VersionedData<Post> result = null;
        Cursor cursor = null;
        try {
            final String query = String.format("SELECT * FROM %s WHERE %s=\'%s\'", POSTS_TABLE_NAME, COLUMN_ID, postId);
            
            SQLiteDatabase db = getDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.getCount() > 0) {
                Gson gson = new Gson();
                cursor.moveToFirst();
                if (!cursor.isAfterLast()) {
                    final String postJsonStr = cursor.getString(cursor.getColumnIndex(COLUMN_DATA));

                    if (postJsonStr != null) {
                        final Post post = gson.fromJson(postJsonStr, Post.class);
                        if (post != null) {
                            result = new VersionedData<Post>(post);
                            final int isRemoved = cursor.getInt(cursor.getColumnIndex(COLUMN_ISREMOVED));
                            result.setIsRemoved((isRemoved == 0) ? false : true);
                            final int isAltered = cursor.getInt(cursor.getColumnIndex(COLUMN_ISALTERED));
                            result.setIsAltered((isAltered == 0) ? false : true);
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

    public synchronized void deleteOldPosts(final long expiry) {

        final long currentTime = System.currentTimeMillis();
        final long deletePostThresholdTime = currentTime - expiry;

        final String sqlCommand = String.format("DELETE FROM %s WHERE %s<\'%s\'", POSTS_TABLE_NAME, COLUMN_LAST_UPDATE_TIMESTAMP, deletePostThresholdTime);

        try {
            execSQL(sqlCommand);
        } catch (SQLException e) {
            Logger.error.log(LOG_TAG, e);
        }
    }


    /**
     * Saves a given dirty data post to the database.
     * 
     * @param dirtyDataPost
     *            The dirty data post to be persisted.
     * @return true on success, false otherwise.
     */
    public synchronized boolean savePostToDatabase(final VersionedData<Post> dirtyDataPost) {
        boolean result = false;

        if (dirtyDataPost != null) {
            result = doTransaction(new TransactionRunnable() {
                @Override
                public void run(SQLiteDatabase db) {
                    // Save to POSTS_TABLE_NAME
                    final Post post = dirtyDataPost.getData();
                    if (post != null) {
                        ContentValues postsTablevalues = getContentValuesFromDirtyDataPost(dirtyDataPost);
                        if (db.update(POSTS_TABLE_NAME, postsTablevalues, COLUMN_ID + " = ?", new String[] { post.getId() }) <= 0) {
                            db.insertOrThrow(POSTS_TABLE_NAME, null, postsTablevalues);
                        }
                    }
                }
            });
        }

        return result;
    }

    /**
     * Saves a list of posts for a category to database.
     * 
     * @param key
     *            The key corresponding to the category of the posts to be
     *            persisted.
     * @param postList
     *            A list containing the posts to be persisted.
     * @return true on success, false otherwise.
     */
    public synchronized boolean savePostsForCategoryToDatabase(final String key, final VersionedData<PostCategory> data) {
        boolean result = false;

        if (key != null && data != null) {
            PostCategory category = data.getData();
            final List<String> postIdList = category.getPostIds();
            
            if (postIdList != null && !postIdList.isEmpty()) {
                result = doTransaction(new TransactionRunnable() {
                    @Override
                    public void run(SQLiteDatabase db) {
                        // Save to POSTCATEGORIES_TABLE_NAME
                        final String jsonPostIdsArrStr = new Gson().toJson(postIdList.toArray());
                        ContentValues postCategoriesTableValues = new ContentValues();
                        postCategoriesTableValues.put(COLUMN_CATEGORY, key);
                        postCategoriesTableValues.put(COLUMN_DATA, jsonPostIdsArrStr);
                        postCategoriesTableValues.put(COLUMN_ISREMOVED, data.isRemoved());
                        postCategoriesTableValues.put(COLUMN_ISALTERED, data.isAltered());
                        postCategoriesTableValues.put(COLUMN_LAST_UPDATE_TIMESTAMP, data.getLastUpdateTimestamp());

                        if (db.update(POSTCATEGORIES_TABLE_NAME, postCategoriesTableValues, COLUMN_CATEGORY + " = ?",
                                new String[] { key }) <= 0) {
                            db.insertOrThrow(POSTCATEGORIES_TABLE_NAME, null, postCategoriesTableValues);
                        }
                    }
                });
            }
        }

        return result;
    }

    /**
     * Saves a list of dirty data posts to database.
     * 
     * @param dirtyDataPostList
     *            The list containing dirty data posts to be persisted.
     * @return true on success, false otherwise.
     */
    public synchronized boolean savePostListToDatabase(final List<VersionedData<Post>> dirtyDataPostList) {
        boolean result = false;

        if (dirtyDataPostList != null && !dirtyDataPostList.isEmpty()) {
            result = doTransaction(new TransactionRunnable() {
                @Override
                public void run(SQLiteDatabase db) {
                    // Save to POSTS_TABLE_NAME
                    for (VersionedData<Post> dirtyDataPost : dirtyDataPostList) {
                        final Post post = dirtyDataPost.getData();
                        if (post != null) {
                            ContentValues postsTablevalues = getContentValuesFromDirtyDataPost(dirtyDataPost);
                            if (db.update(POSTS_TABLE_NAME, postsTablevalues, COLUMN_ID + " = ?",
                                    new String[] { post.getId() }) <= 0) {
                                db.insertOrThrow(POSTS_TABLE_NAME, null, postsTablevalues);
                            }
                        }
                    }
                }
            });
        }

        return result;
    }

    private ContentValues getContentValuesFromDirtyDataPost(final VersionedData<Post> dirtyDataPost) {
        ContentValues postsTablevalues = new ContentValues();
        if (dirtyDataPost != null && dirtyDataPost.getData() != null) {
            final Post post = dirtyDataPost.getData();
            final String jsonPostStr = post.toJsonString();

            postsTablevalues.put(COLUMN_ID, post.getId());
            postsTablevalues.put(COLUMN_DATA, jsonPostStr);
            postsTablevalues.put(COLUMN_ISREMOVED, dirtyDataPost.isRemoved());
            postsTablevalues.put(COLUMN_ISALTERED, dirtyDataPost.isAltered());
            postsTablevalues.put(COLUMN_LAST_UPDATE_TIMESTAMP, dirtyDataPost.getLastUpdateTimestamp());
        }

        return postsTablevalues;
    }
}
