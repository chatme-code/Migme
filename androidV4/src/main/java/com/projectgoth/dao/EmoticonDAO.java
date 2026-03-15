/**
 * Copyright (c) 2013 Project Goth
 *
 * EmoticonDAO.java
 * Created Aug 2, 2013, 12:00:19 PM
 */

package com.projectgoth.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.projectgoth.common.Logger;
import com.projectgoth.model.BaseEmoticonPackData;
import com.projectgoth.model.UsedChatItem;
import com.projectgoth.nemesis.model.BaseEmoticon;
import com.projectgoth.nemesis.model.BaseEmoticon.EmoticonType;
import com.projectgoth.nemesis.model.BaseEmoticonPack;
import com.projectgoth.nemesis.model.Emoticon;
import com.projectgoth.nemesis.model.GiftCategory;
import com.projectgoth.nemesis.model.Sticker;
import com.projectgoth.nemesis.model.VirtualGift;
import com.projectgoth.util.AndroidLogger;


/**
 * @author angelorohit
 */
public class EmoticonDAO extends BaseDAO {

    private static final String LOG_TAG                           = AndroidLogger.makeLogTag(EmoticonDAO.class);

    // This table will store the recently used emoticon hotkeys, recently used
    // sticker hotkeys,
    // and own emoticon hotkeys
    private static final String MAINHOTKEYS_TABLE                 = "mainhotkeys";
    // This table will store all the base emoticon data.
    private static final String BASEEMOTICONS_TABLE               = "baseemoticons";
    // This table will store all the base emoticon pack data.
    private static final String BASEEMOTICONPACKS_TABLE           = "baseemoticonpackdata";

    // Columns in the tables.
    private static final String COLUMN_CATEGORY                   = "CATEGORY";
    private static final String COLUMN_DATA                       = "DATA";

    // Columns for BaseEmoticon data and BaseEmoticonPack Data.
    private static final String COLUMN_CLASSTYPE                  = "CLASSTYPE";
    private static final String COLUMN_TYPE                       = "TYPE";
    private static final String COLUMN_MAINHOTKEY                 = "MAINHOTKEY";
    private static final String COLUMN_ALTHOTKEYS                 = "ALTHOTKEYS";
    private static final String COLUMN_URL                        = "URL";
    private static final String COLUMN_PRICE                      = "PRICE";
    private static final String COLUMN_NAME                       = "NAME";
    private static final String COLUMN_GIFTCATEGORIES             = "GIFTCATEGORIES";
    private static final String COLUMN_ALIAS                      = "ALIAS";
    private static final String COLUMN_PACKID                     = "PACKID";
    private static final String COLUMN_OWNED                      = "OWN_PACK";
    private static final String COLUMN_ENABLED                    = "ENABLED";
    private static final String COLUMN_JSON                       = "ENABLED";

    private static final String COLUMN_ID                         = "ID";
    private static final String COLUMN_MAINHOTKEYS                = "MAINHOTKEYS";
    private static final String COLUMN_ICONURL                    = "ICONURL";
    private static final String COLUMN_VERSION                    = "VERSION";

    // The category values for the MAINHOTKEYS_TABLE
    private static final String RECENTLY_USED_EMOTICONS_CATEGORY  = "RECENTLY_USED_EMOTICONS";
    private static final String RECENTLY_USED_STICKERS_CATEGORY   = "RECENTLY_USED_STICKERS";
    private static final String RECENTLY_USED_CHAT_ITEMS_CATEGORY = "RECENTLY_USED_CHAT_ITEMS";
    private static final String OWN_MAINHOTKEYS_CATEGORY          = "OWN_MAINHOTKEYS";
    
    /**
     * Constructor
     * @param appCtx    Application context.
     */
    public EmoticonDAO(final Context appCtx) {
        super(appCtx, Arrays.asList(MAINHOTKEYS_TABLE, BASEEMOTICONS_TABLE, BASEEMOTICONPACKS_TABLE), null);
    }   
    
    /**
     * @see com.projectgoth.dao.BaseDAO#createTable()
     */
    @Override
    protected void createTable() {
        try {
            final String createMainHotkeysTableCommand = "create table if not exists " + MAINHOTKEYS_TABLE + " (" + 
                    COLUMN_CATEGORY + " TEXT PRIMARY KEY, " +                    
                    COLUMN_DATA + " TEXT" +                          
                ");";
            
            final String createBaseEmoticonsTableCommand = "create table if not exists " + BASEEMOTICONS_TABLE + " (" +
                    COLUMN_MAINHOTKEY + " TEXT PRIMARY KEY, " +
                    COLUMN_CLASSTYPE + " TEXT, " +
                    COLUMN_TYPE + " INTEGER, " +
                    COLUMN_ALTHOTKEYS + " TEXT, " +
                    COLUMN_URL + " TEXT, " +
                    COLUMN_PRICE + " TEXT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_GIFTCATEGORIES + " TEXT, " +
                    COLUMN_ALIAS + " TEXT, " +
                    COLUMN_PACKID + " INTEGER," +
                    COLUMN_JSON + " TEXT" +
                ")"; 
            
            final String createBaseEmoticonPacksTableCommand = "create table if not exists " + BASEEMOTICONPACKS_TABLE + " (" +
                    COLUMN_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_MAINHOTKEYS + " TEXT, " +
                    COLUMN_ICONURL + " TEXT, " +
                    COLUMN_VERSION +  " TEXT, " +
                    COLUMN_OWNED +  " INTEGER, " +
                    COLUMN_ENABLED + " INTEGER" +
                ")";
                    
            execSQL(createMainHotkeysTableCommand);
            execSQL(createBaseEmoticonsTableCommand);
            execSQL(createBaseEmoticonPacksTableCommand);
        }        
        catch (Exception ex) {
            Logger.error.log(LOG_TAG, ex);
        }
    }
    
    /**
     * Loads hotkeys from the MAINHOTKEYS_TABLE matching a specific category.
     * @param categoryName  The name of the category of hotkeys to be loaded.
     * @return              A List containing the hotkeys that were successfully loaded from the table 
     *                      and null if hotkeys matching the category were not present in the table.
     */
    @SuppressWarnings("deprecation")
    private synchronized List<String> loadHotkeysForCategoryFromDatabase(final String tableName, final String categoryName) {        
        List<String> result = null;
        
        Cursor cursor = null;
        try {
            final String query = "SELECT * FROM " + tableName + 
                    " WHERE " + COLUMN_CATEGORY + "=\'" + categoryName + "\'";
            
            SQLiteDatabase db = getDatabase();
            cursor = db.rawQuery(query, null);
            
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                if (!cursor.isAfterLast()) {                         
                    final String mainHotkeysJsonArrStr = 
                            cursor.getString(cursor.getColumnIndex(COLUMN_DATA));
                    if (mainHotkeysJsonArrStr != null) {
                        final String[] mainHotkeysArr = new Gson().fromJson(
                                mainHotkeysJsonArrStr, String[].class);                    
                        if (mainHotkeysArr != null) {
                            result = Arrays.asList(mainHotkeysArr);
                        }                    
                    }                    
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
     * Loads the recently used emoticon hotkeys from database
     * @return  A List containing the hotkeys that were successfully loaded from database and 
     *          null if the recently used emoticon hotkeys were not stored in database.
     */
    public List<String> loadRecentlyUsedEmoticonHotkeysFromDatabase() {
        return loadHotkeysForCategoryFromDatabase(MAINHOTKEYS_TABLE, RECENTLY_USED_EMOTICONS_CATEGORY);
    }      
    
    /**
     * Loads the recently used sticker hotkeys from database
     * @return  A List containing the hotkeys that were successfully loaded from database and 
     *          null if the recently used sticker hotkeys were not stored in database.
     */
    public List<String> loadRecentlyUsedStickerHotkeysFromDatabase() {
        return loadHotkeysForCategoryFromDatabase(MAINHOTKEYS_TABLE, RECENTLY_USED_STICKERS_CATEGORY);        
    }
    
    @SuppressWarnings("deprecation")
    private synchronized List<UsedChatItem> loadRecentlyUsedChatItemsFromDatabase(final String tableName, final String categoryName) {        
        List<UsedChatItem> result = null;
        
        Cursor cursor = null;
        try {
            final String query = "SELECT * FROM " + tableName + 
                    " WHERE " + COLUMN_CATEGORY + "=\'" + categoryName + "\'";
            
            SQLiteDatabase db = getDatabase();
            cursor = db.rawQuery(query, null);
            
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                if (!cursor.isAfterLast()) {                         
                    final String usedItemsJsonArrStr = 
                            cursor.getString(cursor.getColumnIndex(COLUMN_DATA));
                    if (usedItemsJsonArrStr != null) {
                        final UsedChatItem[] usedItemsArr = new Gson().fromJson(
                                usedItemsJsonArrStr, UsedChatItem[].class);                    
                        if (usedItemsArr != null) {
                            result = Arrays.asList(usedItemsArr);
                        }                    
                    }                    
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
    
    public List<UsedChatItem> loadRecentlyUsedChatItemsFromDatabase() {
        return loadRecentlyUsedChatItemsFromDatabase(MAINHOTKEYS_TABLE, RECENTLY_USED_CHAT_ITEMS_CATEGORY);
    }
    
    /**
     * Loads the user's own main hotkeys from database.
     * @return  A List containing the hotkeys that were successfully loaded from database and 
     *          null if the own main hotkeys were not stored in database.
     */
    public List<String> loadOwnMainHotkeysFromDatabase() {
        return loadHotkeysForCategoryFromDatabase(MAINHOTKEYS_TABLE, OWN_MAINHOTKEYS_CATEGORY);
    }
    
    public void clearStickers() {       
        doTransaction(new TransactionRunnable() {
            @Override
            public void run(SQLiteDatabase db) {
                db.delete(BASEEMOTICONPACKS_TABLE, null, null);
            }
        });
    }
    
    /**
     * Loads all stored BaseEmoticonPack
     * @return  A List containing all the stored BaseEmoticonPack in the database or an empty list on failure to load.
     */
    @SuppressWarnings("deprecation")
    public synchronized List<BaseEmoticonPackData> loadAllBaseEmoticonPacksFromDatabase() {        
        List<BaseEmoticonPackData> result = new ArrayList<BaseEmoticonPackData>();
        
        Cursor cursor = null;
        try {
            final String query = "SELECT * FROM " + BASEEMOTICONPACKS_TABLE;
            SQLiteDatabase db = getDatabase();
            cursor = db.rawQuery(query, null);
            
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {        
                    final int id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
                    final String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));                    
                    final String packHotkeysJsonArrStr = 
                            cursor.getString(cursor.getColumnIndex(COLUMN_MAINHOTKEYS));
                    final String iconUrl = cursor.getString(cursor.getColumnIndex(COLUMN_ICONURL));
                    final String version = cursor.getString(cursor.getColumnIndex(COLUMN_VERSION));
                    final boolean isOwnPack = (cursor.getInt(cursor.getColumnIndex(COLUMN_OWNED)) == 1) ? true : false;
                    final boolean isEnabled = (cursor.getInt(cursor.getColumnIndex(COLUMN_ENABLED)) == 1) ? true : false;

                    Logger.debug.log(LOG_TAG, "name:" + name + " isOwnPack:" + isOwnPack + " isEnabled:" + isEnabled);

                    if (packHotkeysJsonArrStr != null) {
                        final String[] packHotkeysArr = new Gson().fromJson(
                                packHotkeysJsonArrStr, String[].class);                    
                        if (packHotkeysArr != null) {
                            BaseEmoticonPack pack = new BaseEmoticonPack(id);
                            pack.setName(name);
                            pack.setIconUrl(iconUrl);
                            pack.setVersion(version);
                            
                            Set<String> packHotkeySet = new HashSet<String>();
                            packHotkeySet.addAll(Arrays.asList(packHotkeysArr));                            
                            pack.setHotkeys(packHotkeySet);
                            
                            BaseEmoticonPackData packData = new BaseEmoticonPackData(pack, isOwnPack);
                            packData.setEnable(isEnabled);
                            result.add(packData);
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
    
    @SuppressWarnings("deprecation")
    public synchronized List<BaseEmoticon> loadAllBaseEmoticonsFromDatabase() {
        List<BaseEmoticon> result = new ArrayList<BaseEmoticon>();
        
        Cursor cursor = null;
        try {
            final String query = "SELECT * FROM " + BASEEMOTICONS_TABLE;
            SQLiteDatabase db = getDatabase();
            cursor = db.rawQuery(query, null);
            
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {  
                    // First load all the BaseEmoticon values.
                    final String mainHotkey = cursor.getString(cursor.getColumnIndex(COLUMN_MAINHOTKEY));
                    final EmoticonType emoticonType = EmoticonType.fromValue((byte) cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE)));
                    final String baseEmoticonAltHotkeysJsonArrStr = cursor.getString(cursor.getColumnIndex(COLUMN_ALTHOTKEYS));
                    String url = null;
                    if (cursor.getColumnIndex(COLUMN_URL) > -1) {
                        url = cursor.getString(cursor.getColumnIndex(COLUMN_URL));
                    }
                    String jsonData = null;
                    if (cursor.getColumnIndex(COLUMN_JSON) > -1) {
                        jsonData = cursor.getString(cursor.getColumnIndex(COLUMN_JSON));
                    }
                    
                    final String classTypeStr = cursor.getString(cursor.getColumnIndex(COLUMN_CLASSTYPE));
                    
                    // Now construct the appropriate BaseEmoticon. 
                    if (!TextUtils.isEmpty(mainHotkey) && 
                        emoticonType != null && 
                        baseEmoticonAltHotkeysJsonArrStr != null && 
                        !TextUtils.isEmpty(classTypeStr)) {
                        
                        final String[] baseEmoticonAltHotkeysArr = new Gson().fromJson(
                                baseEmoticonAltHotkeysJsonArrStr, String[].class);    
                        
                        if (baseEmoticonAltHotkeysArr != null) {                            
                            BaseEmoticon baseEmoticon = null;
                            
                            // Create the right object based on the class type that was saved to db.
                            if (classTypeStr.equals(Emoticon.class.getSimpleName())) {
                                baseEmoticon = new Emoticon(mainHotkey);
                            } 
                            else if (classTypeStr.equals(VirtualGift.class.getSimpleName())) {
                                final String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                                final String price = cursor.getString(cursor.getColumnIndex(COLUMN_PRICE));
                                final String giftCategoriesJsonArrStr = cursor.getString(cursor.getColumnIndex(COLUMN_GIFTCATEGORIES));
                                Set<GiftCategory> giftCategorySet = new HashSet<GiftCategory>();
                                if (giftCategoriesJsonArrStr != null) {
                                    final String[] giftCategoriesArr = new Gson().fromJson(giftCategoriesJsonArrStr, String[].class);
                                    if (giftCategoriesArr != null) {
                                        List<String> giftCategoryNamesList = Arrays.asList(giftCategoriesArr);
                                        for (String giftCategoryName : giftCategoryNamesList) {
                                            giftCategorySet.add(GiftCategory.fromValue(giftCategoryName));
                                        }
                                    }
                                }
                                
                                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(price) && !giftCategorySet.isEmpty()) {
                                    VirtualGift virtualGift = new VirtualGift(mainHotkey, name, price, giftCategorySet);
                                    baseEmoticon = virtualGift;
                                }
                            } 
                            else if (classTypeStr.equals(Sticker.class.getSimpleName())) {
                                Sticker sticker = new Sticker(mainHotkey);
                                final String alias = cursor.getString(cursor.getColumnIndex(COLUMN_ALIAS));
                                final int packId = cursor.getInt(cursor.getColumnIndex(COLUMN_PACKID));
                                                                
                                sticker.setAlias(alias);
                                sticker.setPackId(packId);
                                baseEmoticon = sticker;
                            }
                            
                            // Set all the BaseEmoticon values.
                            if (baseEmoticon != null) {
                                Set<String> baseEmoticonAltHotkeys = new HashSet<String>();
                                baseEmoticonAltHotkeys.addAll(Arrays.asList(baseEmoticonAltHotkeysArr));
                                baseEmoticon.setAltHotkeys(baseEmoticonAltHotkeys);
                                baseEmoticon.setType(emoticonType);
                                baseEmoticon.setUrl(url);
                                baseEmoticon.setJsonData(jsonData);
                            }
                            
                            result.add(baseEmoticon);
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
     * Saves the hotkeys for a given category in either the MAINHOTKEYS_TABLE or VIRTUALGIFT_CATEGORIES_TABLE.
     * @param mainHotkeysArr    An array containing the hotkeys to be saved.
     * @param categoryName      The category of hotkeys to be saved.
     * @return                  true on success and false otherwise.
     */
    private synchronized boolean saveHotkeysForCategoryToDatabase(final String tableName, final Object[] mainHotkeysArr, final String categoryName) {
        boolean result = false;
        
        if (mainHotkeysArr != null && mainHotkeysArr.length > 0 && !TextUtils.isEmpty(tableName) && !TextUtils.isEmpty(categoryName)) {
            result = doTransaction(new TransactionRunnable() {
                @Override
                public void run(SQLiteDatabase db) {
                    // Save to MAINHOTKEYS_TABLE
                    final String jsonMainHotkeysArrStr = new Gson().toJson(mainHotkeysArr);
                    ContentValues mainHotkeysTableValues = new ContentValues();
                    mainHotkeysTableValues.put(COLUMN_CATEGORY, categoryName);
                    mainHotkeysTableValues.put(COLUMN_DATA, jsonMainHotkeysArrStr);

                    if (db.update(tableName, mainHotkeysTableValues, COLUMN_CATEGORY + " = ?",
                            new String[] { categoryName }) <= 0) {
                        db.insertOrThrow(tableName, null, mainHotkeysTableValues);
                    }
                }
            });
        }
        
        return result;
    }      
    
    /**
     * Saves the user's recently used emoticon hotkeys to database.
     * @param recentEmoticons   A Stack containing the user's recently used emoticon hotkeys.
     * @return                  true on succcess and false otherwise.
     */
    public boolean saveRecentlyUsedEmoticonHotkeysToDatabase(final List<String> recentEmoticons) {                
        if (recentEmoticons != null && !recentEmoticons.isEmpty()) {
            return saveHotkeysForCategoryToDatabase(MAINHOTKEYS_TABLE, recentEmoticons.toArray(), RECENTLY_USED_EMOTICONS_CATEGORY);
        }
        
        return false;
    }
    
    /**
     * Saves the user's recently used sticker hotkeys to database.
     * @param recentStickers    A Stack containing the user's recently used sticker hotkeys.
     * @return                  true on success and false otherwise.
     */
    public boolean saveRecentlyUsedStickerHotkeysToDatabase(final Stack<String> recentStickers) {
        if (recentStickers != null && !recentStickers.isEmpty()) {
            return saveHotkeysForCategoryToDatabase(MAINHOTKEYS_TABLE, recentStickers.toArray(), RECENTLY_USED_STICKERS_CATEGORY);
        }
        
        return false;
    }
    
    /**
     * @param usedChatItems
     * @return
     */
    public boolean saveUsedChatItemsToDatabase(List<UsedChatItem> usedChatItems) {
        if (usedChatItems != null && !usedChatItems.isEmpty()) {
            return saveHotkeysForCategoryToDatabase(MAINHOTKEYS_TABLE, usedChatItems.toArray(), RECENTLY_USED_CHAT_ITEMS_CATEGORY);
        }
        
        return false;
    }
    
    /**
     * Saves the user's own main hotkeys to database.
     * @param ownMainHotkeys    A Set containing the user's own main hotkeys.
     * @return                  true on success and false otherwise.
     */
    public boolean saveOwnMainHotkeysToDatabase(final Set<String> ownMainHotkeys) {
        if (ownMainHotkeys != null && !ownMainHotkeys.isEmpty()) {
            return saveHotkeysForCategoryToDatabase(MAINHOTKEYS_TABLE, ownMainHotkeys.toArray(), OWN_MAINHOTKEYS_CATEGORY);
        }
        
        return false;
    }
    
    /**
     * Saves a BaseEmoticonPack to database.
     * @param pack  The BaseEmoticonPack to be saved.
     * @return      true on success and false otherwise.
     */
    public synchronized boolean saveBaseEmoticonPackToDatabase(final BaseEmoticonPackData pack) {
        boolean result = false;
        if (pack != null) {
            result = doTransaction(new TransactionRunnable() {
                @Override
                public void run(SQLiteDatabase db) {
                    // Save to BASEEMOTICONPACKS_TABLE
                    final String jsonPackHotkeysArrStr = new Gson().toJson(pack.getBaseEmoticonPack().getHotkeys().toArray());
                    ContentValues tableValues = new ContentValues();
                    tableValues.put(COLUMN_ID, String.valueOf(pack.getBaseEmoticonPack().getId()));
                    tableValues.put(COLUMN_NAME, pack.getBaseEmoticonPack().getName());
                    tableValues.put(COLUMN_MAINHOTKEYS, jsonPackHotkeysArrStr);
                    tableValues.put(COLUMN_ICONURL, pack.getBaseEmoticonPack().getIconUrl());
                    tableValues.put(COLUMN_VERSION, pack.getBaseEmoticonPack().getVersion());
                    tableValues.put(COLUMN_OWNED, (pack.isOwnPack() ? 1 : 0));
                    tableValues.put(COLUMN_ENABLED, (pack.isEnable() ? 1 : 0));

                    Logger.debug.log(LOG_TAG, "name:" + pack.getBaseEmoticonPack().getName() +
                            " isOwnPack:" + pack.isOwnPack() + " isEnabled:" + pack.isEnable());

                    if (db.update(BASEEMOTICONPACKS_TABLE, tableValues, COLUMN_ID + " = ?",
                            new String[] { String.valueOf(pack.getBaseEmoticonPack().getId()) }) <= 0) {
                        db.insertOrThrow(BASEEMOTICONPACKS_TABLE, null, tableValues);
                    }
                }
            });
        }
        
        return result;
    }
    
    public synchronized boolean saveBaseEmoticonToDatabase(final BaseEmoticon baseEmoticon) {
        boolean result = false;
        if (baseEmoticon != null) {
            result = doTransaction(new TransactionRunnable() {
                @Override
                public void run(SQLiteDatabase db) {
                    ContentValues tableValues = new ContentValues();
                    tableValues.put(COLUMN_MAINHOTKEY, baseEmoticon.getMainHotkey());
                    tableValues.put(COLUMN_CLASSTYPE, baseEmoticon.getClass().getSimpleName());
                    tableValues.put(COLUMN_TYPE, baseEmoticon.getType().value());
                    tableValues.put(COLUMN_ALTHOTKEYS, new Gson().toJson(baseEmoticon.getAltHotkeys().toArray()));
                    tableValues.put(COLUMN_JSON, baseEmoticon.getJsonData());

                    String url = baseEmoticon.getUrl();
                    if (!TextUtils.isEmpty(url)) {
                        tableValues.put(COLUMN_URL, url);
                    }

                    if (baseEmoticon instanceof VirtualGift) {
                        VirtualGift virtualGift = (VirtualGift) baseEmoticon;
                        tableValues.put(COLUMN_PRICE, virtualGift.getPrice());
                        tableValues.put(COLUMN_NAME, virtualGift.getName());

                        List<String> giftCategoriesStrList = new ArrayList<String>();
                        for (GiftCategory giftCategory : virtualGift.getGiftCategories()) {
                            giftCategoriesStrList.add(giftCategory.getValue());
                        }
                        tableValues.put(COLUMN_GIFTCATEGORIES, new Gson().toJson(giftCategoriesStrList));
                    } else if (baseEmoticon instanceof Sticker) {
                        Sticker sticker = (Sticker) baseEmoticon;
                        tableValues.put(COLUMN_ALIAS, sticker.getAlias());
                        tableValues.put(COLUMN_PACKID, sticker.getPackId());
                    }

                    if (db.update(BASEEMOTICONS_TABLE, tableValues, COLUMN_MAINHOTKEY + " = ?",
                            new String[] { baseEmoticon.getMainHotkey() }) <= 0) {
                        db.insertOrThrow(BASEEMOTICONS_TABLE, null, tableValues);
                    }
                }
            });
        }
        
        return result;
    }

}
