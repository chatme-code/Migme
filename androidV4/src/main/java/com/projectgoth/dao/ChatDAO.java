/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatDAO.java
 * Created Aug 26, 2013, 11:12:38 AM
 */

package com.projectgoth.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import com.projectgoth.common.DefaultConfig;
import com.projectgoth.common.Logger;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.model.Message;
import com.projectgoth.nemesis.model.ChatData;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.CrashlyticsLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author angelorohit
 *
 */
public class ChatDAO extends BaseDAO {

    private static final String LOG_TAG                    = AndroidLogger.makeLogTag(ChatDAO.class);

    private static final String CHATCONVERSATIONS_TABLE    = "chatconversations";
    private static final String CHATMESSAGES_TABLE         = "chatmessages";
    private static final String FAILED_CHATMESSAGES_TABLE  = "failed_chatmessages";
    private static final String CHATMESSAGES_TABLE_INDEX   = "chatmessagesindex";

    private static final String COLUMN_CHATCONVERSATION_ID = "CHATCONVERSATION_ID";
    private static final String COLUMN_DATA                = "DATA";
    private static final String COLUMN_MESSAGE_ID          = "MESSAGE_ID";
    private static final String COLUMN_MESSAGE_TIMESTAMP   = "MESSAGE_TIMESTAMP";
    
    private static final String UNKNOWN_MESSAGE_ID          = COLUMN_MESSAGE_ID + " = ?";

    
    /**
     * Constructor
     * @param appCtx    Application context.
     */
    public ChatDAO(final Context appCtx) {
        super(appCtx, Arrays.asList(CHATCONVERSATIONS_TABLE, CHATMESSAGES_TABLE, FAILED_CHATMESSAGES_TABLE), null);
    }  
    
    /*
     * @see com.projectgoth.dao.BaseDAO#createTable()
     */
    @Override
    protected void createTable() {
        try {
            final String createChatConversationsTableCmd = "create table if not exists " + CHATCONVERSATIONS_TABLE + " (" +
                    COLUMN_CHATCONVERSATION_ID + " TEXT PRIMARY KEY, " +                    
                    COLUMN_DATA + " TEXT" +                          
                ");";
            
            final String createChatMessagesTableCmd = "create table if not exists " + CHATMESSAGES_TABLE + " (" + 
                    COLUMN_MESSAGE_ID + " TEXT PRIMARY KEY, " +  
                    COLUMN_CHATCONVERSATION_ID + " TEXT, " +
                    COLUMN_DATA + " TEXT, " +                 
                    COLUMN_MESSAGE_TIMESTAMP + " INTEGER" +
                ");";

            final String createFailedChatMessagesTableCmd = "create table if not exists " + FAILED_CHATMESSAGES_TABLE + " (" +
                    COLUMN_MESSAGE_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_CHATCONVERSATION_ID + " TEXT, " +
                    COLUMN_DATA + " TEXT, " +
                    COLUMN_MESSAGE_TIMESTAMP + " INTEGER" +
                    ");";

            final String createIndexOnChatMessagesTableCmd = "create index if not exists " + CHATMESSAGES_TABLE_INDEX + " on " +
                    CHATMESSAGES_TABLE + "(" + COLUMN_CHATCONVERSATION_ID + "," + COLUMN_MESSAGE_TIMESTAMP + ")";

            execSQL(createChatConversationsTableCmd);
            execSQL(createChatMessagesTableCmd);
            execSQL(createFailedChatMessagesTableCmd);

            //to create an index to help speed the select messages operation
            execSQL(createIndexOnChatMessagesTableCmd);
        } catch (SQLException ex) {
            Logger.error.log(LOG_TAG, ex);
        }
    }
    
    /**
     * Loads all {@link ChatConversation} from database.
     * @return  A List containing the ChatConversations that were loaded or 
     *          an empty List on failure to load.
     */
    @SuppressWarnings("deprecation")
    public synchronized List<ChatConversation> loadAllChatConversationsFromDatabase() {        
        List<ChatConversation> result = new ArrayList<ChatConversation>();        
        Cursor cursor = null;
        try {
            final String query = "SELECT * FROM " + CHATCONVERSATIONS_TABLE;
            SQLiteDatabase db = getDatabase();
            cursor = db.rawQuery(query, null);
            
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    final String chatDataJsonStr = cursor.getString(cursor.getColumnIndex(COLUMN_DATA));
                    if (!TextUtils.isEmpty(chatDataJsonStr)) {
                        final ChatData chatData = ChatData.fromJsonString(chatDataJsonStr);
                        if (chatData != null) {
                            final ChatConversation chatConversation = new ChatConversation(chatData);
                            result.add(chatConversation);
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
        return result;
    }
    
    /**
     * Loads Messages from database for the given {@link ChatConversation}
     * @param chatConversation  The {@link ChatConversation} whose messages are to be loaded.
     * @param noOfMessages      The last n messages to be loaded (based on timestamp). If -1 is passed then all messages are loaded.
     * @return                  A List containing the {@link Message} that were loaded.
     */
    public synchronized List<Message> loadLatestMessagesForChatConversationFromDB(final ChatConversation chatConversation, final int noOfMessages) {
        List<Message> result = new ArrayList<Message>();        
        if (chatConversation != null && !TextUtils.isEmpty(chatConversation.getId()) && noOfMessages != 0) {
            final String query = "SELECT * FROM " + CHATMESSAGES_TABLE +                         
                    " WHERE " + COLUMN_CHATCONVERSATION_ID + " = \'" + chatConversation.getId() + "\'" + 
                    " ORDER BY " + COLUMN_MESSAGE_TIMESTAMP  + " DESC" +
                    ((noOfMessages > 0) ? (" LIMIT " + noOfMessages) : "" );
            result = loadMessagesFromDB(query);
        }
        
        return result;
    }
    
    public synchronized List<Message> loadMoreMessageFromDB(ChatConversation chatConversation, long timestamp, int noOfMessages) {
        List<Message> result = new ArrayList<Message>();        
        if (chatConversation != null && !TextUtils.isEmpty(chatConversation.getId()) && noOfMessages != 0) {
            final String query = "SELECT * FROM " + CHATMESSAGES_TABLE +                         
                    " WHERE " + COLUMN_CHATCONVERSATION_ID + " = \'" + chatConversation.getId() + "\'" + 
                    " AND " + COLUMN_MESSAGE_TIMESTAMP  + " < " + timestamp + 
                    " ORDER BY " + COLUMN_MESSAGE_TIMESTAMP  + " DESC" +
                    ((noOfMessages > 0) ? (" LIMIT " + noOfMessages) : "" );
            result = loadMessagesFromDB(query);
        }
        
        return result;
    }
    
    public synchronized List<Message> loadMoreMessageFromDB(ChatConversation chatConversation, long timestampStart, long timestampEnd, int noOfMessages) {
        List<Message> result = new ArrayList<Message>();        
        if (chatConversation != null && !TextUtils.isEmpty(chatConversation.getId()) && noOfMessages != 0) {
            final String query = "SELECT * FROM " + CHATMESSAGES_TABLE +                         
                    " WHERE " + COLUMN_CHATCONVERSATION_ID + " = \'" + chatConversation.getId() + "\'" + 
                    " AND " + COLUMN_MESSAGE_TIMESTAMP  + " > " + timestampStart +  
                    " AND " + COLUMN_MESSAGE_TIMESTAMP  + " < " + timestampEnd +  
                    " ORDER BY " + COLUMN_MESSAGE_TIMESTAMP  + " DESC" +
                    ((noOfMessages > 0) ? (" LIMIT " + noOfMessages) : "" );
            result = loadMessagesFromDB(query);
        }
        
        return result;
    }
    
    @SuppressWarnings("deprecation")
    public synchronized List<Message> loadMessagesFromDB(final String query) {
        List<Message> result = new ArrayList<Message>();        
       
        Cursor cursor = null;
        try {
            SQLiteDatabase db = getDatabase();
            cursor = db.rawQuery(query, null);
                
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToLast();
                while (!cursor.isBeforeFirst()) {
                    
                    final String messageDataJsonStr = cursor.getString(cursor.getColumnIndex(COLUMN_DATA));
                    if (!TextUtils.isEmpty(messageDataJsonStr)) {
                        final Message message = Message.fromJsonString(messageDataJsonStr);
                        if (message != null) {
                            result.add(message);
                         }
                    }
                    cursor.moveToPrevious();                        
                }
           }
        }
        catch (Exception ex) {
            Logger.error.log(LOG_TAG, ex);
        } 
        finally {
            if (cursor != null) {
                cursor.deactivate();
                cursor.close();
            }
        }
        
        return result;
    }
    
    /**
     * Saves a given {@link ChatConversation} to database.
     * @param chatConversation  The {@link ChatConversation} to be saved.
     * @return                  true on success and false otherwise.
     */
    public synchronized boolean saveChatConversationToDatabase(final ChatConversation chatConversation) {
        boolean result = false;
        
        if (chatConversation != null && !TextUtils.isEmpty(chatConversation.getId())) {
            final ChatData chatData = chatConversation.makeChatData();
            if (chatData != null) {                
                result = doTransaction(new TransactionRunnable() {
                    @Override
                    public void run(SQLiteDatabase db) {
                        ContentValues tableValues = new ContentValues();
                        tableValues.put(COLUMN_CHATCONVERSATION_ID, chatConversation.getId());
                        tableValues.put(COLUMN_DATA, chatData.toJsonString());

                        if (db.update(CHATCONVERSATIONS_TABLE, tableValues, COLUMN_CHATCONVERSATION_ID + " = ?",
                                new String[] { chatConversation.getId() }) <= 0) {
                            db.insertOrThrow(CHATCONVERSATIONS_TABLE, null, tableValues);
                        }
                    }
                });
            }
        }
        
        return result;
    }
    
    /**
     * Saves a {@link Message} for a given {@link ChatConversation} to database.
     * @param chatConversationId    The id of the {@link ChatConversation}
     * @param message               The {@link Message} to be saved.
     * @return                      true on success and false otherwise.
     */
    public synchronized boolean saveChatMessageToDatabase(final String chatConversationId, final Message message) {
        boolean result = false;
        
        if (message != null && !TextUtils.isEmpty(message.getMessageId()) && !TextUtils.isEmpty(chatConversationId)) {
            result = doTransaction(new TransactionRunnable() {
                @Override
                public void run(SQLiteDatabase db) {
                    ContentValues tableValues = new ContentValues();
                    tableValues.put(COLUMN_MESSAGE_ID, message.getMessageId());
                    tableValues.put(COLUMN_DATA, message.toJsonString());
                    tableValues.put(COLUMN_CHATCONVERSATION_ID, chatConversationId);
                    tableValues.put(COLUMN_MESSAGE_TIMESTAMP, message.getLongTimestamp());

                    if (db.update(CHATMESSAGES_TABLE, tableValues, UNKNOWN_MESSAGE_ID,
                            new String[] { message.getMessageId() }) <= 0) {
                        db.insertOrThrow(CHATMESSAGES_TABLE, null, tableValues);
                    }
                }
            });
        }
        
        return result;
    }

    @SuppressWarnings("deprecation")
    public synchronized boolean hasChatMessageInDatabase(final String chatConversationId, final String msgId) {
        boolean result = false;
        
        if (!TextUtils.isEmpty(chatConversationId) && !TextUtils.isEmpty(msgId)) {
            SQLiteDatabase db = getDatabase();            
            Cursor cursor = null;
            
            if (db != null) {
                final String query = "SELECT * FROM " + CHATMESSAGES_TABLE +
                        " WHERE " + COLUMN_CHATCONVERSATION_ID + " = \'" + chatConversationId + "\'" +
                        " AND " + COLUMN_MESSAGE_ID + " = \'" + msgId + "\'" ;

                cursor = db.rawQuery(query, null);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        result = true;
                    }
                    cursor.deactivate();
                    cursor.close();
                }
            }
        }
        
        return result;
    }
    
    @SuppressWarnings("deprecation")
	public synchronized Message loadMessageFromDB(final String chatConversationId, final String msgId) {
        Message result = null;
        
        if (!TextUtils.isEmpty(chatConversationId) && !TextUtils.isEmpty(msgId)) {
            SQLiteDatabase db = getDatabase();            
            Cursor cursor = null;
            
            if (db != null) {
                final String query = "SELECT * FROM " + CHATMESSAGES_TABLE +
                        " WHERE " + COLUMN_CHATCONVERSATION_ID + " = \'" + chatConversationId + "\'" +
                        " AND " + COLUMN_MESSAGE_ID + " = \'" + msgId + "\'" ;

                cursor = db.rawQuery(query, null);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        final String messageDataJsonStr = cursor.getString(cursor.getColumnIndex(COLUMN_DATA));
                        if (!TextUtils.isEmpty(messageDataJsonStr)) {
                            final Message message = Message.fromJsonString(messageDataJsonStr);
                            result = message;
                        }
                    }

                    cursor.deactivate();
                    cursor.close();
                }
            }
        }
        
        return result;
    }
    
    @SuppressWarnings("deprecation")
    public synchronized ChatConversation loadChatConversationFromDatabase(final String chatConversationId) {
        ChatConversation chatConversation = null;
        
        if (!TextUtils.isEmpty(chatConversationId)) {
            SQLiteDatabase db = getDatabase();        
            Cursor cursor = null;
            
            if (db != null) {
                final String query = "SELECT * FROM " + CHATCONVERSATIONS_TABLE +
                        " WHERE " + COLUMN_CHATCONVERSATION_ID + " = \'" + chatConversationId + "\'";

                cursor = db.rawQuery(query, null);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        final String chatDataJsonStr = cursor.getString(cursor.getColumnIndex(COLUMN_DATA));
                        if (!TextUtils.isEmpty(chatDataJsonStr)) {
                            final ChatData chatData = ChatData.fromJsonString(chatDataJsonStr);
                            if (chatData != null) {
                                chatConversation = new ChatConversation(chatData);
                            }
                        }
                    }
                    cursor.deactivate();
                    cursor.close();
                }
            }
        }
        
        return chatConversation;
    }
    
    /**
     * Removes a ChatConversation and all its associated Messages from the database.
     * @param chatConversation  The {@link ChatConversation} to be removed.
     * @return                  true on succcess and false otherwise.
     */
    public synchronized boolean removeChatConversationFromDatabase(
            final ChatConversation chatConversation) {

        boolean result = false;
        
        if (chatConversation != null && !TextUtils.isEmpty(chatConversation.getId())) {
            SQLiteDatabase db = getDatabase();            
            
            if (db != null) {
                try {
                    //For performance sake, only do synchronized when
                    // Delete the ChatConversation from the CHATCONVERSATIONS_TABLE
                    //FIXME: do synchronized here may here performance impact, do need monitor it
                    synchronized (mTransactionLock) {
                        db.delete(CHATCONVERSATIONS_TABLE, COLUMN_CHATCONVERSATION_ID + " = ?",
                                new String[] { chatConversation.getId() });

                        // Also delete all Messages that correspond with this ChatConversation from
                        // the CHATMESSAGES_TABLE
                        db.delete(CHATMESSAGES_TABLE, COLUMN_CHATCONVERSATION_ID + " = ?",
                                new String[]{chatConversation.getId()});
                    }
                    result = true;
                } catch (SQLiteException e) {
                    CrashlyticsLog.log(e, "couldn't delete conversation due to SQLiteException");
                }
            }
        }
        
        return result;
    }

    /**
     * @param chatConversationId
     * @param messageIds
     */
    public void removeChatMessageFromDatabase(final String chatConversationId, final String[] messageIds) {
        if (!TextUtils.isEmpty(chatConversationId) && messageIds != null) {

            if (messageIds.length == 0) {
                Logger.debug.log(LOG_TAG, "Chat ", chatConversationId, " has no messages to be removed");
                return;
            }
            doTransaction(new TransactionRunnable() {
                @Override
                public void run(SQLiteDatabase db) {
                    StringBuilder whereClause = new StringBuilder(UNKNOWN_MESSAGE_ID);
                    for (int i = 1; i < messageIds.length; i++) {
                        whereClause.append(" OR ").append(UNKNOWN_MESSAGE_ID);
                    }
                    int ret = db.delete(CHATMESSAGES_TABLE, whereClause.toString(), messageIds);
                    if (ret == messageIds.length)
                        Logger.debug.log(LOG_TAG, "Removed ", ret, "message(s)");
                    else
                        Logger.warning.log(LOG_TAG, "Removed ", ret, "message(s) out of ", messageIds.length);
                }
            });
        }
    }

    @SuppressWarnings("deprecation")
    public synchronized boolean hasOlderMessageInDatabase(final String chatConversationId, final long timestamp) {
        boolean result = false;
        
        if (!TextUtils.isEmpty(chatConversationId)) {
            SQLiteDatabase db = getDatabase();
            Cursor cursor = null;

            if (db != null) {

                final String query = "SELECT * FROM " + CHATMESSAGES_TABLE +
                        " WHERE " + COLUMN_CHATCONVERSATION_ID + " = \'" + chatConversationId + "\'" +
                        " AND " + COLUMN_MESSAGE_TIMESTAMP + " < " + timestamp ;

                cursor = db.rawQuery(query, null);
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        result = true;
                    }
                    cursor.deactivate();
                    cursor.close();
                }
            }
        }
        
        return result;
    }
    
    public void clearIndex() {
        String sqlCommand = "DROP INDEX " + CHATMESSAGES_TABLE_INDEX;
        execSQL(sqlCommand);
    }

    public synchronized boolean addFailedChatMessageToDatabase(final String chatConversationId, final Message message) {
        boolean result = false;

        if (message != null && !TextUtils.isEmpty(message.getMessageId()) && !TextUtils.isEmpty(chatConversationId)) {
            result = doTransaction(new TransactionRunnable() {
                @Override
                public void run(SQLiteDatabase db) {
                    ContentValues tableValues = new ContentValues();
                    tableValues.put(COLUMN_MESSAGE_ID, message.getMessageId());
                    tableValues.put(COLUMN_DATA, message.toJsonString());
                    tableValues.put(COLUMN_CHATCONVERSATION_ID, chatConversationId);
                    tableValues.put(COLUMN_MESSAGE_TIMESTAMP, message.getLongTimestamp());

                    if (db.update(FAILED_CHATMESSAGES_TABLE, tableValues, UNKNOWN_MESSAGE_ID,
                            new String[]{message.getMessageId()}) <= 0) {
                        db.insertOrThrow(FAILED_CHATMESSAGES_TABLE, null, tableValues);
                    }
                }
            });
        }

        return result;
    }

    public synchronized List<Message> loadAllFailedMessagesFromDatabase() {

        final String query = "SELECT * FROM " + FAILED_CHATMESSAGES_TABLE;
        return loadFailedMessagesFromDatabaseInner(query);

    }

    public synchronized List<Message> loadAllFailedMessagesFromDatabase(final String chatConversationId) {

        final String query = "SELECT * FROM " + FAILED_CHATMESSAGES_TABLE +
                " WHERE " + COLUMN_CHATCONVERSATION_ID + " = \'" + chatConversationId + "\'";
        return loadFailedMessagesFromDatabaseInner(query);

    }

    @SuppressWarnings("deprecation")
    private List<Message> loadFailedMessagesFromDatabaseInner(final String sqlQueryCommand) {
        List<Message> messageList = new ArrayList<Message>();

        SQLiteDatabase db = getDatabase();
        Cursor cursor = null;

        if (db != null && sqlQueryCommand != null) {
            cursor = db.rawQuery(sqlQueryCommand, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        final String messageDataJsonStr = cursor.getString(cursor.getColumnIndex(COLUMN_DATA));
                        if (!TextUtils.isEmpty(messageDataJsonStr)) {
                            final Message message = Message.fromJsonString(messageDataJsonStr);
                            messageList.add(message);
                        }
                    } while (!cursor.isLast() && cursor.moveToNext());
                }

                cursor.deactivate();
                cursor.close();
            }
        }

        return messageList;

    }

    public synchronized void deleteAllFailedMessagesFromDatabase() {
        final String sqlCommand = "DELETE FROM " + FAILED_CHATMESSAGES_TABLE;
        try {
            execSQL(sqlCommand);
        } catch (SQLException e) {
            Logger.error.log(LOG_TAG, e);
        }
    }


    @Override
    public void upgradeTable(int oldDatabaseVersion, int newDatabaseVersion) {

        int upgradeTo = oldDatabaseVersion + 1;
        while (upgradeTo <= newDatabaseVersion) {
            switch (upgradeTo) {
                case DefaultConfig.VERSION_WITH_FAILED_MESSAGE_TABLE:
                    try {
                        final String createFailedChatMessagesTableCommand =
                                "create table if not exists " + FAILED_CHATMESSAGES_TABLE + " (" +
                                COLUMN_MESSAGE_ID + " TEXT PRIMARY KEY, " +
                                COLUMN_CHATCONVERSATION_ID + " TEXT, " +
                                COLUMN_DATA + " TEXT, " +
                                COLUMN_MESSAGE_TIMESTAMP + " INTEGER" +
                                ");";
                        execSQL(createFailedChatMessagesTableCommand);
                    } catch (SQLException e) {
                        Logger.error.log(LOG_TAG, e);
                        CrashlyticsLog.log(new SQLException(), "Chat database upgrade to version "
                                + DefaultConfig.VERSION_WITH_FAILED_MESSAGE_TABLE + " failed");
                    }
                    break;
                // Template for future database upgrade
                //case FUTURE_VERSION_NUMBER:
                //    do the upgrade things here
                //    break;
            }
            upgradeTo++;
        }

    }
}
