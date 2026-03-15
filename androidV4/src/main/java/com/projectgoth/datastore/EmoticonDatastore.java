/**
 * Copyright (c) 2013 Project Goth
 * 
 * EmoticonDatastore.java
 * Jun 10, 2013 10:51:00 AM
 */

package com.projectgoth.datastore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Config;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.controller.EmoticonsController;
import com.projectgoth.dao.EmoticonDAO;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.model.BaseEmoticonPackData;
import com.projectgoth.model.UsedChatItem;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.listeners.GetEmoticonsListener;
import com.projectgoth.nemesis.listeners.GetGiftListener;
import com.projectgoth.nemesis.listeners.GetStickerPackListListener;
import com.projectgoth.nemesis.listeners.GetStickerPackListener;
import com.projectgoth.nemesis.model.BaseEmoticon;
import com.projectgoth.nemesis.model.BaseEmoticonPack;
import com.projectgoth.nemesis.model.Emoticon;
import com.projectgoth.nemesis.model.GiftCategory;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.nemesis.model.Sticker;
import com.projectgoth.nemesis.model.VirtualGift;
import com.projectgoth.service.NetworkService;
import com.projectgoth.util.AndroidLogger;
import com.projectgoth.util.LogUtils;

/**
 * Manages caching and persistent storage of emoticons. This includes regular
 * emoticons, virtual gifts and stickers.
 * 
 * Note: We use the term hotkey to refer to both main and alternate hotkeys in
 * general.
 * 
 * @author angelorohit
 */
public class EmoticonDatastore extends BaseDatastore {

    private static final String                LOG_TAG                              = AndroidLogger
                                                                                            .makeLogTag(EmoticonDatastore.class);

    // A lock that is obtained when working with any of the caches.
    private static final Object                CACHE_LOCK                           = new Object();

    // A DAO for saving alerts to persistent storage.
    private EmoticonDAO                        mEmoticonDAO                         = null;

    /**
     * A cache of all main hotkeys that the user owns.
     */
    private Set<String>                        mOwnMainHotkeyCache;

    /**
     * A cache of all alternate hotkeys mapped to main hotkeys. Note: The
     * alternate hotkeys and their main hotkey mappings need not be owned by the
     * user. This means that {@link #mOwnMainHotkeyCache} will not contain all
     * the main hotkey values in this map. This will make the lookup of
     * BaseEmoticons with a given alternate key more time efficient. The key of
     * this map is the alternate hotkey.
     */
    private Map<String, String>                mAltHotkeyCache;

    /**
     * A cache of all BaseEmoticon. This can include BaseEmoticon that the user
     * does not own. The key is the main hotkey of the BaseEmoticon.
     */
    private Map<String, BaseEmoticon>          mBaseEmotionCache;

    /**
     * A cache of all Pack that the user owns. The key is the id of the Pack.
     * 
     * @see BaseEmoticonPack#getId()
     */
    private Map<Integer, BaseEmoticonPackData> mBaseEmoticonPackCache;

    /**
     * We maintain a cache of all the emoticon hotkeys because
     * {@link #getAllOwnEmoticonHotkeys()} will be called very frequently. The
     * cache is internally invalidated by setting
     * {@link #mIsAllEmoticonHotkeysCacheInvalid}
     */
    private Set<String>                        mAllOwnEmoticonHotkeysSet;
    private boolean                            mIsAllOwnEmoticonHotkeysCacheInvalid = true;

    /**
     * A cache of Virtual Gift categories mapped to a Set containing the main
     * hotkeys of the VirtualGifts in that GiftCategory. The key is the
     * GiftCategory. This will make lookup of all VirtualGifts in a GiftCategory
     * more efficient.
     */
    private HashMap<GiftCategory, Set<String>> mVirtualGiftCategoryCache;

    /**
     * A cache to hold the recently used emoticons.
     */
    private List<String>                       mRecentEmoticonsCache;

    /**
     * A cache to hold the recently used stickers.
     */
    // private Stack<String> mRecentStickersCache;

    /**
     * A cache to hold the recently used stickers and gifts
     */
    private List<UsedChatItem>                 mUsedChatItemsCache;

    /**
     * The maximum number of gifts that can be requested for at a time.
     * 
     * @see #requestGiftsForCategory(GiftCategory)
     */
    private final static int                   GET_GIFT_LIMIT                       = 20;

    // The maximum sizes for each of the recently used cache.
    private final static int                   MAX_RECENT_CACHE_SIZE                = 24;
    private final static int                   MAX_RECENT_EMOTICON_CACHE_SIZE       = 36;

    /**
     * Constructor
     * 
     * @param appCtx
     *            Application Context.
     */
    @SuppressLint("UseSparseArrays")
    private EmoticonDatastore() {
        super();

        final Context appCtx = ApplicationEx.getContext();
        if (appCtx != null) {
            mEmoticonDAO = new EmoticonDAO(appCtx);
        }

        loadFromPersistentStorage();
    }

    private static class EmoticonDatastoreHolder {
        static final EmoticonDatastore sINSTANCE = new EmoticonDatastore();
    }

    /**
     * A single point of access for this class.
     * 
     * @return An instance of this class.
     */
    public static EmoticonDatastore getInstance() {
        return EmoticonDatastoreHolder.sINSTANCE;
    }

    @SuppressLint("UseSparseArrays")
    @Override
    protected void initData() {
        synchronized (CACHE_LOCK) {
            mOwnMainHotkeyCache = new LinkedHashSet<String>();
            mAltHotkeyCache = new HashMap<String, String>();
            mBaseEmotionCache = new HashMap<String, BaseEmoticon>();
            mVirtualGiftCategoryCache = new HashMap<GiftCategory, Set<String>>();

            mRecentEmoticonsCache = new ArrayList<String>();
            // mRecentStickersCache = new Stack<String>();
            mUsedChatItemsCache = new ArrayList<UsedChatItem>();

            // We prefer a HashMap over a SparseArray here because we need
            // to be able to get all the keys in the packCache efficiently.
            mBaseEmoticonPackCache = new HashMap<Integer, BaseEmoticonPackData>();

            mAllOwnEmoticonHotkeysSet = null;
            mIsAllOwnEmoticonHotkeysCacheInvalid = true;
        }
    }

    @Override
    public void clearData() {
        super.clearData();

        if (mEmoticonDAO != null) {
            mEmoticonDAO.clearTables();
        }
    }

    /**
     * Gets the recently used emoticons.
     * 
     * @return A list containing the relevant emoticons.
     */
    public List<Emoticon> getRecentlyUsedEmoticons() {
        synchronized (CACHE_LOCK) {
            final List<Emoticon> resultList = new ArrayList<Emoticon>();
            final List<String> recentlyUsedMainHotkeysSet = mRecentEmoticonsCache;

            for (String mainHotkey : recentlyUsedMainHotkeysSet) {
                final Emoticon emoticon = getEmoticonWithHotkey(mainHotkey);
                if (emoticon != null) {
                    resultList.add(emoticon);
                }
            }

            return resultList;
        }
    }

    /**
     * Gets the recently used stickers.
     * 
     * @return A list containing the relevant stickers.
     */
    /*
     * public List<Sticker> getRecentlyUsedStickers() { final List<Sticker>
     * resultList = new ArrayList<Sticker>(); final Set<String>
     * recentlyUsedMainHotkeysSet =
     * getRecentlyUsedMainHotkeysFromCache(mRecentStickersCache);
     * 
     * for (String mainHotkey : recentlyUsedMainHotkeysSet) { final Sticker
     * sticker = getStickerWithHotkey(mainHotkey); if (sticker != null) {
     * resultList.add(sticker); } }
     * 
     * return resultList; }
     */

    public List<UsedChatItem> getRecentlyUsedChatItems() {
        synchronized (CACHE_LOCK) {
            return mUsedChatItemsCache;
        }
    }

    public boolean isRecentStickersAndGiftsEmpty() {
        List<UsedChatItem> list = getRecentlyUsedChatItems();
        if (list == null || list.size() == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get a BaseEmoticonPack in cache with the given id.
     * 
     * @param id
     *            The id of the Pack to be retrieved.
     * @return The relevant Pack if it could be found in cache and null
     *         otherwise.
     */
    public BaseEmoticonPackData getBaseEmoticonPackWithId(final Integer id) {
        if (id != null) {
            synchronized (CACHE_LOCK) {
                return mBaseEmoticonPackCache.get(id);
            }
        }

        return null;
    }
    
    public void clearBaseEmoticonPackCache() {
        synchronized (CACHE_LOCK) {
            mBaseEmoticonPackCache.clear();
        }
    }

    /**
     * Returns the main hot key of a BaseEmoticon given a main or alt hotkey.
     * 
     * @param hotkey
     *            A main or alternate hotkey.
     * @return The associated main hotkey if it could be found and null
     *         otherwise
     */
    public String getMainHotkeyFromHotkey(final String hotkey) {
        synchronized (CACHE_LOCK) {
            final BaseEmoticon baseEmoticon = mBaseEmotionCache.get(hotkey);
            // If a BaseEmoticon for the given hotkey could not be found,
            // then this hotkey is not a main hotkey. It might be an alternate
            // hotkey.
            return (baseEmoticon != null) ? hotkey : mAltHotkeyCache.get(hotkey);
        }
    }

    /**
     * Gets any BaseEmoticon (Emoticon, VirtualGift, Sticker etc.) with a given
     * hotkey. This hotkey can be a main or alternate hotkey.
     * 
     * @param hotkey
     *            The hotkey that uniquely identifies the BaseEmoticon. Can be a
     *            main or alternate hotkey.
     * @param typeParameterClass
     *            A Class of the type of BaseEmoticon that must match the
     *            hotkey.
     * @return The associated BaseEmoticon on success and null if the
     *         BaseEmoticon could not be found in cache.
     */
    private <T extends BaseEmoticon> T getBaseEmoticonWithHotkeyAndType(final String hotkey,
            final Class<T> typeParameterClass) {
        T result = null;
        final String mainHotkey = getMainHotkeyFromHotkey(hotkey);
        if (mainHotkey != null) {
            result = getBaseEmoticonWithMainHotkeyAndType(mainHotkey, typeParameterClass);
        }

        return result;
    }

    /**
     * Gets any BaseEmoticon (Emoticon, VirtualGift, Sticker etc.) with a given
     * main hotkey.
     * 
     * @param mainHotkey
     *            The main hotkey that uniquely identifies the BaseEmoticon.
     * @param typeParameterClass
     *            A Class of the type of BaseEmoticon that must match the
     *            hotkey.
     * @return The associated BaseEmoticon on success and null if the
     *         BaseEmoticon could not be found in cache.
     */
    private <T extends BaseEmoticon> T getBaseEmoticonWithMainHotkeyAndType(final String mainHotkey,
            final Class<T> typeParameterClass) {
        synchronized (CACHE_LOCK) {
            T result = null;
            final BaseEmoticon baseEmoticon = mBaseEmotionCache.get(mainHotkey);

            if (baseEmoticon != null && baseEmoticon.getClass().equals(typeParameterClass)) {
                result = typeParameterClass.cast(baseEmoticon);
            }

            return result;
        }
    }

    /**
     * Gets a BaseEmoticon with a given hotkey. The hotkey can be main or
     * alternate.
     * 
     * @param hotkey
     *            The hotkey to be matched.
     * @return The associated BaseEmoticon on success and null otherwise.
     */
    public BaseEmoticon getBaseEmoticonWithHotkey(final String hotkey) {
        if (hotkey == null) {
            return null;
        }
        String mainHotkey = getMainHotkeyFromHotkey(hotkey);
        if (TextUtils.isEmpty(mainHotkey)) {
            mainHotkey = hotkey;
        }
        return getBaseEmoticonWithMainHotkey(mainHotkey);
    }

    /**
     * Gets a BaseEmoticon with a given main hotkey.
     * 
     * @param mainHotkey
     *            The main hotkey to be matched.
     * @return The associated BaseEmoticon on success and null otherwise.
     */
    private BaseEmoticon getBaseEmoticonWithMainHotkey(final String mainHotkey) {
        synchronized (CACHE_LOCK) {
            Logger.debug.log(LOG_TAG, "getBaseEmoticonWithMainHotkey: key: ", mainHotkey);
            return mBaseEmotionCache.get(mainHotkey);
        }
    }

    /**
     * Gets an Emoticon from cache with the given hotkey. The hotkey can be a
     * main or alternate hotkey.
     * 
     * @param hotkey
     *            The hotkey that uniquely identifies the BaseEmoticon.
     * @return The associated Emoticon on success and null if the Emoticon could
     *         not be found in cache.
     */
    // Angelo: This function has been set to private because we don't
    // use it externally at the moment.
    private Emoticon getEmoticonWithHotkey(final String hotkey) {
        Logger.debug.log(LOG_TAG, "Getting Emoticon with hotkey: ", hotkey);
        return getBaseEmoticonWithHotkeyAndType(hotkey, Emoticon.class);
    }

    /**
     * Gets a VirtualGift from cache with the given hotkey. The hotkey can be a
     * main or alternate hotkey.
     * 
     * @param hotkey
     *            The hotkey that uniquely identifies the VirtualGift.
     * @return The associated VirtualGift on success and null if the VirtualGift
     *         could not be found in cache.
     */
    // Angelo: This function has been set to private because we don't
    // use it externally at the moment.
    private VirtualGift getVirtualGiftWithHotkey(final String hotkey) {
        return getBaseEmoticonWithHotkeyAndType(hotkey, VirtualGift.class);
    }

    /**
     * Gets a Sticker from cache with the given hotkey. The hotkey can be a main
     * or alternate hotkey.
     * 
     * @param hotkey
     *            The hotkey that uniquely identifies the Sticker.
     * @return The associated Sticker on success and null if the Sticker could
     *         not be found in cache.
     */
    public Sticker getStickerWithHotkey(final String hotkey) {
        Logger.debug.log(LOG_TAG, "Getting Sticker with hotkey: ", hotkey);
        return getBaseEmoticonWithHotkeyAndType(hotkey, Sticker.class);
    }

    /**
     * Returns all own main hotkeys and alternate hotkeys. This includes regular
     * emoticons, stickers and virtual gifts that must exist in the cache.
     * 
     * @param tyoeParameterClass
     *            The type of BaseEmoticon whose own hotkeys are to be fetched.
     * @param shouldOnlyGetMainHotkeys
     *            Indicates that the return Set should only contain main
     *            hotkeys.
     * @return A set containing all main hotkeys and alternate hotkeys of the
     *         BaseEmoticon in the cache.
     */
    private <T extends BaseEmoticon> Set<String> getAllOwnBaseEmoticonHotkeysForType(Class<T> typeParameterClass,
            final boolean shouldOnlyGetMainHotkeys) {
        synchronized (CACHE_LOCK) {
            Set<String> resultSet = new HashSet<String>();

            for (final String mainHotkey : mOwnMainHotkeyCache) {
                final BaseEmoticon baseEmoticon = getBaseEmoticonWithMainHotkeyAndType(mainHotkey, typeParameterClass);
                if (baseEmoticon != null) {
                    resultSet.add(mainHotkey);

                    if (!shouldOnlyGetMainHotkeys) {
                        final Set<String> altHotkeySet = baseEmoticon.getAltHotkeys();
                        if (altHotkeySet != null && altHotkeySet.size() > 0) {
                            resultSet.addAll(altHotkeySet);
                        }
                    }
                }
            }

            return resultSet;
        }
    }

    /**
     * Returns all own Emoticon main hotkeys and alternate keys.
     * 
     * @return A Set containing all own main hotkeys and altkeys of the
     *         Emoticons in the cache.
     */
    public Set<String> getAllOwnEmoticonHotkeys() {
        if (mIsAllOwnEmoticonHotkeysCacheInvalid || mAllOwnEmoticonHotkeysSet == null) {
            mAllOwnEmoticonHotkeysSet = getAllOwnBaseEmoticonHotkeysForType(Emoticon.class, false);
        }

        return mAllOwnEmoticonHotkeysSet;
    }

    /**
     * Returns all own Emoticon main hotkeys. Note: This function does not
     * verify if the actual emoticons are available in the cache.
     * 
     * @return An immutable Set containing all own main hotkeys of the Emoticons
     *         in the cache.
     */
    public Set<String> getAllOwnEmoticonMainHotkeys() {
        return Collections.unmodifiableSet(mOwnMainHotkeyCache);
    }

    /**
     * Returns all own emoticons
     * 
     * @return A list containing the own emoticon hotkeys in cache.
     */
    public List<Emoticon> getAllOwnEmoticons() {
        final List<Emoticon> resultList = new ArrayList<Emoticon>();
        final Set<String> ownEmoticonMainHotkeys = getAllOwnEmoticonMainHotkeys();

        for (String mainHotkey : ownEmoticonMainHotkeys) {
            final Emoticon emoticon = getEmoticonWithHotkey(mainHotkey);
            if (emoticon != null) {
                resultList.add(emoticon);
            }
        }

        return resultList;
    }

    /**
     * Returns all main hotkeys for a BaseEmoticonPack with given id.
     * 
     * @param packId
     *            The id of the BaseEmoticonPack whose main hotkeys are to be
     *            retrieved.
     * @return A Set containing all main hotkeys of the Stickers in the
     *         BaseEmoticonPack.
     */
    // Angelo: This function has been set to private because we don't
    // use it externally at the moment.
    private Set<String> getAllMainHotkeysForStickerPackWithId(final Integer packId) {
        final BaseEmoticonPackData pack = getBaseEmoticonPackWithId(packId);
        if (pack != null) {
            return pack.getBaseEmoticonPack().getHotkeys();
        }

        return null;
    }

    /**
     * Get all VirtualGifts belonging to a particular category.
     * 
     * @param category
     *            The GiftCategory for which all VirtualGifts must be retrieved.
     * @return A List containing all the relevant VirtualGifts.
     */
    public List<VirtualGift> getAllVirtualGiftsForCategory(final GiftCategory category) {
        synchronized (CACHE_LOCK) {
            List<VirtualGift> resultList = new ArrayList<VirtualGift>();
            final Set<String> mainHotkeySet = mVirtualGiftCategoryCache.get(category);
            if (mainHotkeySet != null) {
                for (final String mainHotkey : mainHotkeySet) {
                    final VirtualGift virtualGift = getVirtualGiftWithHotkey(mainHotkey);

                    if (virtualGift != null) {
                        resultList.add(virtualGift);
                    } else {
                        EmoticonsController.getInstance().fetchEmoticonDataFromFusion(mainHotkey);
                    }
                }
            }

            return resultList;
        }
    }

    /**
     * Gets all the stickers in a given pack.
     * 
     * @param packId
     *            The id of the pack whose stickers are to be retrieved.
     * @return A list containing all stickers for a given pack. If any one of
     *         the stickers in the pack could not be found, all of the stickers
     *         in the pack are refetched.
     */
    public List<Sticker> getAllStickersInPack(final Integer packId) {
        List<Sticker> resultList = new ArrayList<Sticker>();
        final BaseEmoticonPackData pack = getBaseEmoticonPackWithId(packId);
        if (pack != null) {
            final Set<String> hotkeySet = pack.getBaseEmoticonPack().getHotkeys();

            for (String hotkey : hotkeySet) {
                final Sticker sticker = getStickerWithHotkey(hotkey);

                if (sticker == null) {
                    resultList.add(new Sticker(hotkey));
                } else {
                    resultList.add(sticker);
                }
            }
        }

        return resultList;
    }

    /**
     * Gets all the BaseEmoticonPack for Stickers.
     * 
     * @return A List containing all the relevant BaseEmoticonPack.
     */
    public List<BaseEmoticonPack> getAllStickerPacks() {
        synchronized (CACHE_LOCK) {
            List<BaseEmoticonPackData> emoticonPackDataList = new ArrayList<BaseEmoticonPackData>(
                    mBaseEmoticonPackCache.values());
            List<BaseEmoticonPack> baseEmoticonPacks = new ArrayList<BaseEmoticonPack>();

            for (BaseEmoticonPackData pack : emoticonPackDataList) {
                baseEmoticonPacks.add(pack.getBaseEmoticonPack());
            }

            return baseEmoticonPacks;
        }
    }

    public List<BaseEmoticonPack> getOwnStickerPacks() {
        synchronized (CACHE_LOCK) {
            List<BaseEmoticonPackData> emoticonPackDataList = new ArrayList<BaseEmoticonPackData>(
                    mBaseEmoticonPackCache.values());
            List<BaseEmoticonPack> baseEmoticonPacks = new ArrayList<BaseEmoticonPack>();

            for (BaseEmoticonPackData pack : emoticonPackDataList) {
                if (pack.isOwnPack()) {
                    baseEmoticonPacks.add(pack.getBaseEmoticonPack());
                }
            }

            return baseEmoticonPacks;
        }
    }

    public List<BaseEmoticonPackData> getOwnStickerPackDataList() {
        synchronized (CACHE_LOCK) {
            List<BaseEmoticonPackData> emoticonPackDataList = new ArrayList<BaseEmoticonPackData>(mBaseEmoticonPackCache.values());
            List<BaseEmoticonPackData> myPackDataList = new ArrayList<BaseEmoticonPackData>();

            for (BaseEmoticonPackData pack : emoticonPackDataList) {
                if (pack.isOwnPack()) {
                    myPackDataList.add(pack);
                }
            }

            return myPackDataList;
        }
    }

    public List<BaseEmoticonPack> getMyEnabledStickerPacks() {
        synchronized (CACHE_LOCK) {
            List<BaseEmoticonPackData> emoticonPackDataList = new ArrayList<BaseEmoticonPackData>(mBaseEmoticonPackCache.values());
            List<BaseEmoticonPack> baseEmoticonPacks = new ArrayList<BaseEmoticonPack>();

            for (BaseEmoticonPackData pack : emoticonPackDataList) {
                if (pack.isOwnPack() && pack.isEnable()) {
                    baseEmoticonPacks.add(pack.getBaseEmoticonPack());
                }
            }

            return baseEmoticonPacks;
        }
    }

    public BaseEmoticonPackData getStickerPackWithId(final int referenceId) {
        synchronized (CACHE_LOCK) {
            return mBaseEmoticonPackCache.get(referenceId);
        }
    }

    public boolean isStickerPackEnabled(final int referenceId) {
        synchronized (CACHE_LOCK) {
            BaseEmoticonPackData data = getStickerPackWithId(referenceId);
            if (data != null) {
                return data.isEnable();
            }

            return true;
        }
    }

    /**
     * Adds a main hotkey to a given recently used cache.
     * 
     * @param mainHotkey
     *            The main hotkey to be added.
     * @param cache
     *            The recently used cache to be added to.
     * @param shouldPersist
     *            Indicates whether the cache should be persisted or not.
     */
    private void addMainHotkeyToRecentlyUsedCache(final String mainHotkey, List<String> cache,
            final boolean shouldPersist) {
        synchronized (CACHE_LOCK) {
            // Remove the existing mainhotkey if it is present in the stack.
            // Then push the mainhotkey on top.
            for (String hotkey : cache) {
                if (hotkey.equals(mainHotkey)) {
                    cache.remove(hotkey);
                    break;
                }
            }

            cache.add(0, mainHotkey);

            // Limit the size of the recent used cache.
            while (cache.size() > MAX_RECENT_EMOTICON_CACHE_SIZE) {
                cache.remove(cache.size() - 1);
            }
        }
    }

    /**
     * Adds an emoticon hotkey to the recently used cache.
     * 
     * @param hotkey
     *            The hotkey to be added. Can be a main or alternate key.
     */
    public void addEmoticonHotkeyToRecentlyUsedCache(final String hotkey) {
        final String mainHotkey = getMainHotkeyFromHotkey(hotkey);
        if (mainHotkey != null && getEmoticonWithHotkey(mainHotkey) != null) {
            addMainHotkeyToRecentlyUsedCache(mainHotkey, mRecentEmoticonsCache, true);

            if (!saveRecentlyUsedEmoticonHotkeysToPersistentStorage(mRecentEmoticonsCache)) {
                Logger.error.log(LOG_TAG, "Failed to persist with saveOwnMainHotkeysToPersistentStorage");
            }
        }
    }

    /**
     * Adds a sticker hotkey to the recently used cache.
     * 
     * @param hotkey
     *            The hotkey to be added. Can be a main or alternate key.
     */
    /*
     * public void addStickerHotkeyToRecentlyUsedCache(final String hotkey) {
     * final String mainHotkey = getMainHotkeyFromHotkey(hotkey); if (mainHotkey
     * != null && getStickerWithHotkey(mainHotkey) != null) {
     * addMainHotkeyToRecentlyUsedCache(mainHotkey, mRecentStickersCache, true);
     * 
     * if
     * (!saveRecentlyUsedStickerHotkeysToPersistentStorage(mRecentStickersCache
     * )) { LogUtils.LOGE(LOG_TAG,
     * "Failed to persist with saveOwnMainHotkeysToPersistentStorage"); } } }
     */

    public void addUsedChatItemToUsedCache(final UsedChatItem usedChatItem) {
        synchronized (CACHE_LOCK) {
            // Remove the existing one if it is present in the stack.
            // Then push it on top.

            for (UsedChatItem item : mUsedChatItemsCache) {
                if (item.getHotkey().equals(usedChatItem.getHotkey())) {
                    mUsedChatItemsCache.remove(item);
                    break;
                }
            }

            mUsedChatItemsCache.add(0, usedChatItem);

            // Limit the size of the recent used cache.
            while (mUsedChatItemsCache.size() > MAX_RECENT_CACHE_SIZE) {
                mUsedChatItemsCache.remove(mUsedChatItemsCache.size() - 1);
            }

            if (!saveUsedChatItemsToPersistentStorage(mUsedChatItemsCache)) {
                Logger.error.log(LOG_TAG, "Failed to persist with saveOwnMainHotkeysToPersistentStorage");
            }
        }
    }

    /**
     * Adds a main hotkey of a VirtualGift to the GiftCategory mapping in cache.
     * 
     * @param mainHotkey
     *            The main hotkey to be associated with the GiftCategory.
     * @param category
     *            The GiftCategory whose mapping is to be updated.
     * @param shouldPersist
     *            Indicates whether the cache should be persisted or not.
     */
    private void addMainHotkeysForVirtualGiftCategory(final Set<String> mainHotkeySet, final GiftCategory category) {
        if (category != null && mainHotkeySet != null && !mainHotkeySet.isEmpty()) {
            synchronized (CACHE_LOCK) {
                Set<String> existingMainHotkeySet = mVirtualGiftCategoryCache.get(category);
                if (existingMainHotkeySet == null) {
                    existingMainHotkeySet = new HashSet<String>();
                }

                existingMainHotkeySet.addAll(mainHotkeySet);
                mVirtualGiftCategoryCache.put(category, existingMainHotkeySet);

                // Note: We don't persist mVirtualGiftCategoryCache.
            }
        }
    }

    /**
     * Add the user's own main hot keys to the cache. If any of the
     * BaseEmoticons associated with the main hotkeys cannot be found in the
     * cache, a request is sent to fetch them from server.
     * 
     * @param mainHotkeySet
     *            A set containing the main hotkeys to be cached.
     * @oparam shouldPersist Indicates whether the cached own hotkeys should
     *         immediately be persisted to data storage.
     */
    public void addOwnHotkeys(final Set<String> mainHotkeySet, final boolean shouldPersist) {
        synchronized (CACHE_LOCK) {
            mOwnMainHotkeyCache.addAll(mainHotkeySet);
            if (shouldPersist) {
                if (!saveOwnMainHotkeysToPersistentStorage(mOwnMainHotkeyCache)) {
                    Logger.error.log(LOG_TAG, "Failed to persist with saveOwnMainHotkeysToPersistentStorage");
                }
            }
        }

        checkAndFetchBaseEmoticonForHotkeys(mainHotkeySet);
        mIsAllOwnEmoticonHotkeysCacheInvalid = true;
    }

    /**
     * If any of the BaseEmoticons associated with the hotkeys cannot be found
     * in the cache, a request is sent to fetch them from the server.
     * 
     * @param hotkeySet
     *            A Set containing the hotkeys to be cached. Can contain both
     *            main and alternate hotkeys.
     */
    private void checkAndFetchBaseEmoticonForHotkeys(final Set<String> hotkeySet) {
        // Go through each of the hotkeys and see whether there is
        // already a BaseEmoticon associated with that hotkey.
        for (final String hotkey : hotkeySet) {
            if (getBaseEmoticonWithHotkey(hotkey) == null) {
                Logger.debug.log("Emoticons", "checkAndFetchBaseEmoticonForHotkeys: Emoticon not found. Fetching: ",
                        hotkey);
                EmoticonsController.getInstance().fetchEmoticonDataFromFusion(hotkey);
            }
        }

        // TODO: Fix hard-coded paintwars hotkey (requires server change)
        if (getEmoticonWithHotkey(Constants.PAINTWARS_EMOTICON_HOTKEY) == null) {
            EmoticonsController.getInstance().fetchEmoticonDataFromFusion(Constants.PAINTWARS_EMOTICON_HOTKEY);
        }
    }

    /**
     * Checks the relevant caches for Packs that match the given set of pack
     * ids. Sends a request to fetch the packs which were not in cache.
     * 
     * @param packIdSet
     *            A Set of ids that need to be checked for and fetched if
     *            necessary.
     * @param forceFetch
     *            Indicates that all of the ids in packIdSet should be
     *            force-fetched irrespective of whether they are in the cache or
     *            not.
     */
    public void checkAndFetchStickerPacksForIds(final Set<Integer> packIdSet, final boolean getOwnPack,
            final boolean forceFetch) {
        Set<Integer> packIdsToFetchSet = new HashSet<Integer>();
        
        // this is for checking if update sticker set
        if(getOwnPack){
            boolean shouldUpdate = false;
            for (final Integer packId : packIdSet) {
                
                BaseEmoticonPackData localData = getBaseEmoticonPackWithId(packId);
                if (localData == null) {
                    packIdsToFetchSet.add(packId);
                    
                    // local data don't content this id, that means all sticker set should be updated.
                    if(mBaseEmoticonPackCache.size() > 0){
                        shouldUpdate = true;
                    }
                }
            }
            
            if(shouldUpdate){
                // clear memory cache and database
                clearBaseEmoticonPackCache();
                mEmoticonDAO.clearStickers();
            }
        }
        
        if (forceFetch) {
            packIdsToFetchSet.addAll(packIdSet);
        } else {
            // Go through each of the pack ids and see whether there is
            // already a Pack associated with that id.
            
            for (final Integer packId : packIdSet) {
                if (getBaseEmoticonPackWithId(packId) == null) {
                    packIdsToFetchSet.add(packId);
                }
            }
        }

        if (packIdsToFetchSet.size() > 0) {
            requestStickerPacksForIds(packIdsToFetchSet, getOwnPack);
        }
    }

    /**
     * Checks if the BaseEmoticon object for the sticker hotkeys in this pack
     * has already been retrieved. Note that unlike
     * {@link #checkAndFetchBaseEmoticonForHotkeys(Set)} method, this does not
     * check if the bitmap image for the hotkey already exists in the
     * {@link FusionImageWorker} as stickers use a different
     * {@link ImageFetcher} to retrieve the image.
     * 
     * @param packId
     *            Id of the sticker pack whose hotkeys will check
     */
    private void checkAndFetchStickersInPack(final Integer packId) {
        Set<String> stickerIds = getAllMainHotkeysForStickerPackWithId(packId);
        Logger.debug.log(LogUtils.TAG_FUSION_IMAGE_FETCHER, "Checking the hotkeys of the stickers in pack id: ", packId);
        for (String hotkey : stickerIds) {
            if (getBaseEmoticonWithHotkey(hotkey) == null) {
                EmoticonsController.getInstance().fetchEmoticonDataFromFusion(hotkey);
            }
        }
    }

    /**
     * Adds a BaseEmoticon to the relevant caches.
     * 
     * @param baseEmoticon
     *            The BaseEmoticon to be cached.
     * @param shouldPersist
     *            Indicates whether the cached BaseEmoticon should immediately
     *            be persisted to data storage.
     */
    private void addBaseEmoticon(final BaseEmoticon baseEmoticon, final boolean shouldPersist) {
        synchronized (CACHE_LOCK) {
            if (baseEmoticon != null) {
                final String mainHotkey = baseEmoticon.getMainHotkey();

                if (mainHotkey != null) {
                    Logger.debug.log("Emoticons", "addBaseEmoticon: mainHotkey: ", mainHotkey);
                    // Cache the base emoticon with the main hot key.
                    mBaseEmotionCache.put(mainHotkey, baseEmoticon);

                    // Map all alternate hotkeys of the base emoticon with its
                    // main hotkey.
                    final Set<String> altHotkeySet = baseEmoticon.getAltHotkeys();
                    if (altHotkeySet != null && altHotkeySet.size() > 0) {
                        for (String altHotkey : altHotkeySet) {
                            mAltHotkeyCache.put(altHotkey, mainHotkey);
                        }
                    }

                    // Add the pack id to the pack cache if this is a sticker.
                    if (baseEmoticon instanceof Sticker) {
                        final Sticker sticker = (Sticker) baseEmoticon;

                        final Integer packId = sticker.getPackId();
                        if (packId != null) {
                            BaseEmoticonPackData packData = getBaseEmoticonPackWithId(packId);
                            if (packData != null) {
                                BaseEmoticonPack pack = packData.getBaseEmoticonPack();
                                if (pack == null) {
                                    pack = new BaseEmoticonPack(sticker.getPackId());
                                }
                                pack.addHotkey(sticker.getMainHotkey());
                            }
                        }
                    }

                    if (shouldPersist) {
                        if (!saveBaseEmoticonToPersistentStorage(baseEmoticon)) {
                            Logger.error.log(LOG_TAG, "Failed to persist with saveBaseEmoticonToPersistentStorage");
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds a Pack to cache.
     * 
     * @param pack
     *            The Pack to be cached.
     * @param shouldPersist
     *            Indicates whether the cached BaseEmoticon should immediately
     *            be persisted to data storage.
     */
    public void addStickerPack(final BaseEmoticonPack pack, final boolean getOwnPack, final boolean isEnabled, final boolean shouldPersist) {
        synchronized (CACHE_LOCK) {
            if (pack != null) {
                BaseEmoticonPackData packData = new BaseEmoticonPackData(pack, getOwnPack);
                packData.setEnable(isEnabled);
                addStickerPack(packData, shouldPersist);
            }
        }
    }

    public void addStickerPack(final BaseEmoticonPackData packData, final boolean shouldPersist) {
        synchronized (CACHE_LOCK) {
            if (packData != null) {
                mBaseEmoticonPackCache.put(packData.getBaseEmoticonPack().getId(), packData);

                if (shouldPersist) {
                    if (!saveBaseEmoticonPackToPersistentStorage(packData)) {
                        Logger.error.log(LOG_TAG, "Failed to persist with saveBaseEmoticonPackToPersistentStorage");
                    }
                }
            }
        }
    }

    // Network requests.
    /**
     * Sends a request to download all emoticons for the given hotkeys. This
     * includes Emoticons and Stickers only.
     * 
     * @param hotkeySet
     *            A set containing the main hotkeys of the emoticons to be
     *            downloaded.
     */
    public void requestEmoticonsForHotkeys(final Set<String> hotkeySet) {
        if (hotkeySet != null && hotkeySet.size() > 0) {
            final ApplicationEx appEx = ApplicationEx.getInstance();
            if (appEx != null) {
                RequestManager requestManager = appEx.getRequestManager();
                if (requestManager != null) {
                    // Make a space separated string of the hotkeys.
                    final String hotkeyStr = TextUtils.join(Constants.SPACESTR, hotkeySet);

                    if (hotkeyStr != null && hotkeyStr.length() > 0) {
                        requestManager.sendGetEmoticons(new GetEmoticonsListener() {

                            @Override
                            public void onEmoticonReceived(BaseEmoticon emoticon) {
                                
                                // Check if there is a VirtualGift already in
                                // the cache
                                // that matches the hotkey of the Emoticon.
                                // If yes, then just update
                                // that VirtualGift. This is because
                                // VirtualGifts are actually received via
                                // the GIFT_HOTKEYS packet.
                                VirtualGift virtualGift = getVirtualGiftWithHotkey(emoticon.getMainHotkey());
                                if (virtualGift != null) {
                                    virtualGift.fromBaseEmoticon(emoticon);
                                    emoticon = virtualGift;
                                }
                                addBaseEmoticon(emoticon, true);
                                
                                EmoticonsController.getInstance().setEmoticonDataReceived(emoticon.getMainHotkey());
                                BroadcastHandler.Emoticon.sendReceived(emoticon.getMainHotkey());
                            }

                            @Override
                            public void onGetCompleteEmoticonsReceived(final String hotkeysStr) {
                                // Broadcast that Get emoticons has been
                                // compeleted.
                                BroadcastHandler.Emoticon.sendFetchAllCompleted();
                            }

                            @Override
                            public void onError(MigError error) {
                                super.onError(error);
                                BroadcastHandler.Emoticon.sendFetchAllError(error);
                            }

                        }, hotkeyStr, Config.getInstance().isEnableEmoticonJsonData());
                    }
                }
            }
        }
    }

    /**
     * Sends a request to get all sticker pack ids. When the sticker pack ids
     * are received, all the sticker packs are fetched from server.
     */
    public void requestStickerPackList() {
        requestStickerPackList(ApplicationEx.getInstance().getNetworkService());
    }

    /**
     * Sends a request to get all sticker pack ids. When the sticker pack ids
     * are received, all the sticker packs are fetched from server.
     */
    public void requestStickerPackList(NetworkService service) {
        if (Session.getInstance().getIsStickerSupported()) {
            RequestManager requestManager = service == null ? null : service.getRequestManager();
            if (requestManager != null) {
                requestManager.sendGetStickerPackList(new GetStickerPackListListener() {

                    @Override
                    public void onStickerPackListReceived(final Set<Integer> idSet) {
                        checkAndFetchStickerPacksForIds(idSet, true, true);
                        BroadcastHandler.Sticker.sendPackListReceived();
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                        BroadcastHandler.Sticker.sendFetchPackListError(error);
                    }
                });
            }
        }
    }

    /**
     * Sends a request to get all sticker packs for the given pack ids. When the
     * sticker packs are received, the stickers for the hotkeys are fetched from
     * the server.
     * 
     * @param packIdSet
     *            A Set containing the pack ids to be fetched.
     */
    private void requestStickerPacksForIds(final Set<Integer> packIdSet, final boolean getOwnPack) {
        if (packIdSet != null && packIdSet.size() > 0) {
            final ApplicationEx appEx = ApplicationEx.getInstance();
            if (appEx != null) {
                RequestManager requestManager = appEx.getRequestManager();
                if (requestManager != null) {
                    String[] strArray = new String[packIdSet.size()];
                    int i = 0;
                    for (Integer integer : packIdSet) {
                        strArray[i++] = integer.toString();
                    }

                    requestManager.sendGetStickerPack(new GetStickerPackListener() {

                        @Override
                        public void onStickerPackReceived(final BaseEmoticonPack pack) {
                            //the enable/disable of my sticker only works on client side, so when receiving the
                            // sticker pack here, need to keep the status if it was already disabled
                            boolean isEnable = EmoticonDatastore.getInstance().isStickerPackEnabled(pack.getId());
                            addStickerPack(pack, getOwnPack, isEnable, true);
                            checkAndFetchStickersInPack(pack.getId());
                            BroadcastHandler.Sticker.sendPackReceived(pack.getId());
                        }

                        @Override
                        public void onEndStickerPackReceived(final String[] packIdStr) {
                            BroadcastHandler.Sticker.sendFetchPacksCompleted();
                        }

                        @Override
                        public void onError(MigError error) {
                            super.onError(error);
                            BroadcastHandler.Sticker.sendFetchPackError(error);
                        }

                    }, strArray);
                }
            }
        }
    }

    /**
     * Sends a request to get all VirtualGift hotkeys for a given category.
     * 
     * @param category
     *            The GiftCategory for which gifts are to be received.
     */
    public void requestGiftsForCategory(final GiftCategory category) {
        requestGiftsForCategory(ApplicationEx.getInstance().getNetworkService(), category);
    }

    /**
     * Sends a request to get all VirtualGift hotkeys for a given category.
     * 
     * @param category
     *            The GiftCategory for which gifts are to be received.
     */
    public void requestGiftsForCategory(NetworkService service, final GiftCategory category) {
        if (category != null) {
            RequestManager requestManager = service == null ? null : service.getRequestManager();
            if (requestManager != null) {
                requestManager.sendGetGift(new GetGiftListener() {

                    @Override
                    public void onGiftHotkeysReceived(final List<VirtualGift> virtualGiftList,
                            final GiftCategory category) {
                        final Set<String> receivedVirtualGiftHotkeys = new HashSet<String>();
                        for (VirtualGift virtualGift : virtualGiftList) {
                            
                            VirtualGift temp = getVirtualGiftWithHotkey(virtualGift.getMainHotkey());
                            if (temp != null) {
                                temp.fromBaseEmoticon(virtualGift);
                                virtualGift = temp;
                            }
                            
                            // Add the virtual gift to cache. Other details
                            // will be fetched subsequently via
                            addBaseEmoticon(virtualGift, false);
                            receivedVirtualGiftHotkeys.add(virtualGift.getMainHotkey());
                        }

                        if (receivedVirtualGiftHotkeys.size() > 0) {
                            // Map the virtual gift main hotkeys to the
                            // GiftCategory.
                            addMainHotkeysForVirtualGiftCategory(receivedVirtualGiftHotkeys, category);
                        }
                    }

                    @Override
                    public void onError(MigError error) {
                        super.onError(error);
                    }

                }, GET_GIFT_LIMIT, category);
            }
        }
    }

    /**
     * Loads all emoticon related data from persistent storage into the relevant
     * caches.
     */
    private void loadFromPersistentStorage() {
        if (mEmoticonDAO != null) {
            Logger.debug.log("Emoticons", "loadFromPersistentStorage: START!!!");

            // Load recently used emoticons.
            final List<String> recentlyUsedEmoticonHotkeys = mEmoticonDAO.loadRecentlyUsedEmoticonHotkeysFromDatabase();
            if (recentlyUsedEmoticonHotkeys != null) {
                mRecentEmoticonsCache.clear();
                mRecentEmoticonsCache.addAll(recentlyUsedEmoticonHotkeys);
            }

            /*
             * // Load recently used stickers. final List<String>
             * recentlyUsedStickerHotkeys =
             * mEmoticonDAO.loadRecentlyUsedStickerHotkeysFromDatabase(); if
             * (recentlyUsedStickerHotkeys != null) {
             * mRecentStickersCache.clear();
             * mRecentStickersCache.addAll(recentlyUsedStickerHotkeys); }
             */

            // Load used chat items.
            final List<UsedChatItem> recentlyUsedChatItems = mEmoticonDAO.loadRecentlyUsedChatItemsFromDatabase();
            if (recentlyUsedChatItems != null) {
                mUsedChatItemsCache.clear();
                mUsedChatItemsCache.addAll(recentlyUsedChatItems);
            }

            // Load own main hotkeys.
            final List<String> ownMainHotkeys = mEmoticonDAO.loadOwnMainHotkeysFromDatabase();
            if (ownMainHotkeys != null) {
                mOwnMainHotkeyCache.clear();
                mOwnMainHotkeyCache.addAll(ownMainHotkeys);
            }

            // Load all base emoticon packs.
            final List<BaseEmoticonPackData> packDataList = mEmoticonDAO.loadAllBaseEmoticonPacksFromDatabase();

            if (packDataList != null && !packDataList.isEmpty()) {
                for (BaseEmoticonPackData packData : packDataList) {
                    addStickerPack(packData, false);
                }
            }

            // Load all base emoticons.
            final List<BaseEmoticon> baseEmoticonList = mEmoticonDAO.loadAllBaseEmoticonsFromDatabase();
            if (baseEmoticonList != null && !baseEmoticonList.isEmpty()) {
                for (BaseEmoticon baseEmoticon : baseEmoticonList) {
                    addBaseEmoticon(baseEmoticon, false);
                }
            }
            Logger.debug.log("Emoticons", "loadFromPersistentStorage: Based emoticons: ", mBaseEmotionCache.size());
        }
    }

    /**
     * Saves a stack of recently used emoticons to persistent storage.
     * 
     * @param recentEmoticonsCache
     *            A Stack containing the recently used emoticon main hotkeys to
     *            be persisted.
     * @return true on success and false otherwise.
     */
    private boolean saveRecentlyUsedEmoticonHotkeysToPersistentStorage(final List<String> recentEmoticons) {
        if (mEmoticonDAO != null) {
            return mEmoticonDAO.saveRecentlyUsedEmoticonHotkeysToDatabase(recentEmoticons);
        }

        return false;
    }

    private boolean saveUsedChatItemsToPersistentStorage(final List<UsedChatItem> usedChatItems) {
        if (mEmoticonDAO != null) {
            return mEmoticonDAO.saveUsedChatItemsToDatabase(usedChatItems);
        }

        return false;
    }

    /**
     * Saves a Set of own main hotkeys to persistent storage.
     * 
     * @param ownMainHotkeys
     *            A Set containing the own main hotkeys to be persisted.
     * @return true on success and false otherwise.
     */
    private boolean saveOwnMainHotkeysToPersistentStorage(final Set<String> ownMainHotkeys) {
        if (mEmoticonDAO != null) {
            return mEmoticonDAO.saveOwnMainHotkeysToDatabase(ownMainHotkeys);
        }

        return false;
    }

    /**
     * Saves a BaseEmoticonPack to persistent storage.
     * 
     * @param pack
     *            The BaseEmoticonPack ot be persisted.
     * @return true on success and false otherwise.
     */
    private boolean saveBaseEmoticonPackToPersistentStorage(final BaseEmoticonPackData pack) {
        if (mEmoticonDAO != null) {
            return mEmoticonDAO.saveBaseEmoticonPackToDatabase(pack);
        }

        return false;
    }

    private boolean saveBaseEmoticonToPersistentStorage(final BaseEmoticon baseEmoticon) {
        if (mEmoticonDAO != null) {
            return mEmoticonDAO.saveBaseEmoticonToDatabase(baseEmoticon);
        }

        return false;
    }

}
