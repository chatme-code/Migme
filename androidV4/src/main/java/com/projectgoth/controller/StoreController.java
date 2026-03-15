/**
 * Copyright (c) 2013 Project Goth
 *
 * StoreController.java
 * Created Nov 8, 2013, 12:05:26 PM
 */

package com.projectgoth.controller;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.StoreCategory;
import com.projectgoth.b.data.StoreItem;
import com.projectgoth.b.data.StoreItemReferenceData;
import com.projectgoth.b.data.StoreItems;
import com.projectgoth.b.data.StoreUnlockedItem;
import com.projectgoth.common.Constants;
import com.projectgoth.datastore.DataCache;
import com.projectgoth.datastore.EmoticonDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.BaseEmoticonPackData;
import com.projectgoth.model.StickerStoreItem;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.enums.RequestTypeEnum;
import com.projectgoth.nemesis.listeners.GetStoreCategoriesListener;
import com.projectgoth.nemesis.listeners.GetStoreCategoryItemsListener;
import com.projectgoth.nemesis.listeners.GetStoreItemListener;
import com.projectgoth.nemesis.listeners.GetUnlockedGiftsListener;
import com.projectgoth.nemesis.listeners.SearchStoreItemsListener;
import com.projectgoth.nemesis.model.BaseEmoticonPack;
import com.projectgoth.nemesis.model.MigError;

import java.util.HashSet;
import java.util.Set;

/**
 * @author mapet
 * 
 */
public class StoreController {

    private final static StoreController   INSTANCE             = new StoreController();
    private static final Object            CACHE_LOCK           = new Object();

    private DataCache<StoreItem>           storeItemCache;
    private DataCache<StoreItems>          storeItemsCache;
    private DataCache<StoreCategory[]>     storeCategoriesCache;
    private DataCache<StoreUnlockedItem[]> unlockedGiftsCache;

    private String                         localCurrency        = Constants.BLANKSTR;

    private static final int               STORE_MAX_CACHE_SIZE = 20;

    public static final float              DEFAULT_MIN_PRICE    = -1;
    public static final float              DEFAULT_MAX_PRICE    = -1;
    public static final String             SORT_BY_NUMSOLD      = "numsold";
    public static final String             SORT_BY_DATELISTED   = "datelisted";
    public static final String             SORT_BY_NAME         = "name";
    public static final String             SORT_BY_PRICE        = "price";
    public static final String             SORT_ORDER_ASC       = "asc";
    public static final String             SORT_ORDER_DESC      = "desc";
    public static final String             FEATURED             = "true";
    public static final String             NOT_FEATURED         = "false";

    public static final int                newCategoryId        = -1;
    public static final int                featuredCategoryId   = -2;
    public static final int                allCategoryId        = -3;
    
    private Set<String>                    purchaseStickerPackInProgress;
    
    public static String[] sortByStringArr = {
        I18n.tr("Popularity"),
        I18n.tr("Price low to high"),
        I18n.tr("Price high to low"),
        I18n.tr("Name A to Z"),
        I18n.tr("Name Z to A") };
    
    public static enum StoreItemFilterType {
        GENERAL(0),
        POPULAR(1),
        FEATURED(2),
        NEW(3),
        CATEGORY(4),
        PRICE_ASC(5),
        PRICE_DESC(6),
        NAME_ASC(7),
        NAME_DESC(6);

        private int mValue;

        private StoreItemFilterType(final int value) {
            mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }

        public static StoreItemFilterType fromValue(int value) {
            for (StoreItemFilterType type : values()) {
                if (type.mValue == value) {
                    return type;
                }
            }
            return null;
        }
    }

    private StoreController() {
        try {
            storeItemCache = new DataCache<StoreItem>(STORE_MAX_CACHE_SIZE);
            storeItemsCache = new DataCache<StoreItems>(STORE_MAX_CACHE_SIZE);
            storeCategoriesCache = new DataCache<StoreCategory[]>(STORE_MAX_CACHE_SIZE);
            unlockedGiftsCache = new DataCache<StoreUnlockedItem[]>(STORE_MAX_CACHE_SIZE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        purchaseStickerPackInProgress = new HashSet<String>();
    }

    public static synchronized StoreController getInstance() {
        return INSTANCE;
    }

    private void cacheStoreItem(String key, StoreItem result) {
        synchronized (CACHE_LOCK) {
            storeItemCache.cacheData(key, result);
        }
    }

    private void cacheSearchStoreResults(String key, StoreItems result) {
        synchronized (CACHE_LOCK) {
            storeItemsCache.cacheData(key, result);
        }
    }

    private void cacheStoreCategories(String key, StoreCategory[] result) {
        synchronized (CACHE_LOCK) {
            storeCategoriesCache.cacheData(key, result);
        }
    }
    
    private void cacheUnlockedGifts(String key, StoreUnlockedItem[] result) {
        synchronized (CACHE_LOCK) {
            unlockedGiftsCache.cacheData(key, result);
        }
    }
    
    //@formatter:off
    private SearchStoreItemsListener searchStoreItemsListener = new SearchStoreItemsListener() {
        @Override
        public void onSearchStoreItemsReceived(String key, StoreItems storeItems) {
            cacheSearchStoreResults(key, storeItems);
            BroadcastHandler.MigStore.Item.sendFetchForSearchCompleted(key);
        }
        
        @Override
        public void onGetStoreCategoryReceived(String key, StoreItems storeItems) {
            cacheSearchStoreResults(key, storeItems);
            BroadcastHandler.MigStore.Item.sendFetchForSubCategoryCompleted();
        }
    };
    
    private GetStoreCategoryItemsListener getStoreCategoryItemsListener = new GetStoreCategoryItemsListener() {
        @Override
        public void onGetStoreCategoryPopularItemsReceived(String key, StoreItems storeItems) {
            cacheSearchStoreResults(key, storeItems);
            BroadcastHandler.MigStore.Item.sendFetchForPopularCompleted();
        }
        
        @Override
        public void onGetStoreCategoryFeaturedItemsReceived(String key, StoreItems storeItems) {
            cacheSearchStoreResults(key, storeItems);
            BroadcastHandler.MigStore.Item.sendFetchForFeaturedCompleted();
        }
        
        @Override
        public void onGetStoreCategoryNewItemsReceived(String key, StoreItems storeItems) {
            cacheSearchStoreResults(key, storeItems);
            BroadcastHandler.MigStore.Item.sendFetchForNewCompleted();
        }
        
        @Override
        public void onGetStoreCategoryItemsReceived(String key, StoreItems storeItems) {
            cacheSearchStoreResults(key, storeItems);
            BroadcastHandler.MigStore.Item.sendFetchForMainCategoryCompleted();
        }

        @Override
        public void onGetStoreCategoryReceived(String key, StoreItems storeItems) {
            cacheSearchStoreResults(key, storeItems);
            BroadcastHandler.MigStore.Item.sendFetchForSubCategoryCompleted();
        }
    };
    
    private GetUnlockedGiftsListener getStoreUnlockedItemsListener = new GetUnlockedGiftsListener() {

        @Override
        public void onUnlockedGiftsReceived(String key, StoreUnlockedItem[] storeUnlockedItems) {
            cacheUnlockedGifts(key, storeUnlockedItems);
            BroadcastHandler.MigStore.Item.sendFetchUnlockedItems();
        }
    };
    
    private GetStoreItemListener getStoreItemListener = new GetStoreItemListener() {
        @Override
        public void onStoreItemReceived(String key, StoreItem storeItem) {
            cacheStoreItem(key, storeItem);
            BroadcastHandler.MigStore.Item.sendReceived();
        }

        @Override
        public void onStoreItemPurchased(String key, StoreItem storeItem) {
            cacheStoreItem(key, storeItem);
            stickerPackPurchaseDone(String.valueOf(storeItem.getId()));
            BroadcastHandler.MigStore.Item.sendPurchased(storeItem.getId());
        }

        @Override
        public void onStoreItemPurchaseError(final MigError error) {
            BroadcastHandler.MigStore.Item.sendPurchaseError(error);
        }
        
        @Override
        public void onStoreUnlockedItemSent(String key, StoreItem storeItem) {
            BroadcastHandler.MigStore.Item.sendUnlockedItem();
        };
        
    };
    
    private GetStoreCategoriesListener getStoreCategoriesListener = new GetStoreCategoriesListener() {
        @Override
        public void onStoreCategoriesReceived(String key, StoreCategory[] storeCategories) {
            cacheStoreCategories(key, storeCategories);
            BroadcastHandler.MigStore.SubCategory.sendFetchAllCompleted();
        }
    };
    //@formatter:on

    public StoreItems getMainCategories(int storeType, int limit, int offset, StoreItemFilterType filterType) {
        switch (filterType) {
            case POPULAR:
                return getStoreCategoryPopularItems(SORT_BY_NUMSOLD, SORT_ORDER_DESC, NOT_FEATURED, limit, offset,
                        Integer.toString(storeType));
            case FEATURED:
                return getStoreCategoryFeaturedItems(SORT_BY_NAME, SORT_ORDER_ASC, FEATURED, limit, offset,
                        Integer.toString(storeType));
            case NEW:
                return getStoreCategoryNewItems(SORT_BY_DATELISTED, SORT_ORDER_DESC, NOT_FEATURED, limit, offset,
                        Integer.toString(storeType));
            case NAME_ASC:
                return getStoreCategoryItems(SORT_BY_NAME, SORT_ORDER_ASC, NOT_FEATURED, limit, offset,
                        Integer.toString(storeType));
            case NAME_DESC:
                return getStoreCategoryItems(SORT_BY_NAME, SORT_ORDER_DESC, NOT_FEATURED, limit, offset,
                        Integer.toString(storeType));
            case PRICE_ASC:
                return getStoreCategoryItems(SORT_BY_PRICE, SORT_ORDER_ASC, NOT_FEATURED, limit, offset,
                        Integer.toString(storeType));
            case PRICE_DESC:
                return getStoreCategoryItems(SORT_BY_PRICE, SORT_ORDER_DESC, NOT_FEATURED, limit, offset,
                        Integer.toString(storeType));
            default:
                return null;
        }
    }

    public StoreItems searchStoreItems(int storeType, String searchString, float minPrice, float maxPrice,
            String sortBy, String sortOrder, String featured, int limit, int offset, RequestTypeEnum requestType) {

        if (requestType == null) {
            requestType = RequestTypeEnum.STORE_SEARCH_ITEMS;
        }

        String cacheKey = generateSearchStoreRequestKey(storeType, searchString, minPrice, maxPrice, sortBy, sortOrder,
                featured, limit, offset);

        if (storeItemsCache.isExpired(cacheKey)) {
            final ApplicationEx appEx = ApplicationEx.getInstance();

            if (appEx != null) {
                final RequestManager requestManager = appEx.getRequestManager();
                if (requestManager != null) {
                    requestManager.searchForStoreItems(searchStoreItemsListener, storeType, searchString,
                            Float.toString(minPrice), Float.toString(maxPrice), sortBy, sortOrder, featured, limit,
                            offset, Session.getInstance().getUserId(), requestType);

                    BroadcastHandler.MigStore.Item.sendBeginFetchStoreItems(cacheKey);
                }
            }
        }

        return storeItemsCache.getData(cacheKey);
    }

    public String generateSearchStoreRequestKey(int storeType, String searchString, float minPrice, float maxPrice,
                                                String sortBy, String sortOrder, String featured, int limit, int offset) {
        String key = "SS_" + storeType + searchString + Float.toString(minPrice) + Float.toString(maxPrice)
                + sortBy + sortOrder + featured + limit + offset;

        return  key;

    }

    /*
     * Returns the a store category's categories (e.g. Sale, Animals)
     */
    public StoreCategory[] getStoreCategories(String parentId) {
        if (storeCategoriesCache.isExpired(parentId)) {
            final ApplicationEx appEx = ApplicationEx.getInstance();

            if (appEx != null) {
                final RequestManager requestManager = appEx.getRequestManager();
                if (requestManager != null) {
                    requestManager.getStoreCategories(getStoreCategoriesListener, Session.getInstance().getUserId(),
                            parentId);
                }
            }
        }

        return storeCategoriesCache.getData(parentId);
    }

    public StoreItems getStoreCategory(String categoryId, String sortBy, String sortOrder, String featured, int limit,
            int offset) {

        String cacheKey = "SCI_" + categoryId + sortBy + sortOrder + featured + limit + offset;

        if (storeItemsCache.isExpired(cacheKey)) {
            final ApplicationEx appEx = ApplicationEx.getInstance();

            if (appEx != null) {
                final RequestManager requestManager = appEx.getRequestManager();
                if (requestManager != null) {
                    requestManager.getStoreCategory(searchStoreItemsListener, Session.getInstance().getUserId(),
                            categoryId, sortBy, sortOrder, featured, limit, offset);

                    BroadcastHandler.MigStore.Item.sendBeginFetchStoreItems(cacheKey);
                }
            }
        }

        return storeItemsCache.getData(cacheKey);
    }
    
    /*
     * Returns the items in a particular store category
     */
    public StoreItems getStoreCategoryItems(String sortBy, String sortOrder, String featured, int limit, int offset,
            String storeType) {

        String cacheKey = "SCI_" + storeType + sortBy + sortOrder + featured + limit + offset;

        if (storeItemsCache.isExpired(cacheKey)) {
            final ApplicationEx appEx = ApplicationEx.getInstance();

            if (appEx != null) {
                final RequestManager requestManager = appEx.getRequestManager();
                if (requestManager != null) {
                    requestManager.getStoreCategoryItems(getStoreCategoryItemsListener, sortBy, sortOrder, featured,
                            limit, offset, Session.getInstance().getUserId(), storeType,
                            RequestTypeEnum.STORE_CATEGORY_ITEMS);
                    BroadcastHandler.MigStore.Item.sendBeginFetchStoreItems(cacheKey);
                }
            }
        }

        return storeItemsCache.getData(cacheKey);
    }

    public StoreItems getStoreCategoryFeaturedItems(String sortBy, String sortOrder, String featured, int limit,
            int offset, String storeType) {

        String cacheKey = "SCI_" + storeType + sortBy + sortOrder + featured + limit + offset;

        if (storeItemsCache.isExpired(cacheKey)) {
            final ApplicationEx appEx = ApplicationEx.getInstance();

            if (appEx != null) {
                final RequestManager requestManager = appEx.getRequestManager();
                if (requestManager != null) {
                    requestManager.getStoreCategoryFeaturedItems(getStoreCategoryItemsListener, sortBy, sortOrder,
                            featured, limit, offset, Session.getInstance().getUserId(), storeType);

                    BroadcastHandler.MigStore.Item.sendBeginFetchStoreItems(cacheKey);
                }
            }
        }

        return storeItemsCache.getData(cacheKey);
    }

    public StoreItems getStoreCategoryNewItems(String sortBy, String sortOrder, String featured, int limit, int offset,
            String storeType) {

        String cacheKey = "SCI_" + storeType + sortBy + sortOrder + featured + limit + offset;

        if (storeItemsCache.isExpired(cacheKey)) {
            final ApplicationEx appEx = ApplicationEx.getInstance();

            if (appEx != null) {
                final RequestManager requestManager = appEx.getRequestManager();
                if (requestManager != null) {
                    requestManager.getStoreCategoryNewItems(getStoreCategoryItemsListener, sortBy, sortOrder, featured,
                            limit, offset, Session.getInstance().getUserId(), storeType);
                    BroadcastHandler.MigStore.Item.sendBeginFetchStoreItems(cacheKey);
                }
            }
        }

        return storeItemsCache.getData(cacheKey);
    }

    public StoreItems getStoreCategoryPopularItems(String sortBy, String sortOrder, String featured, int limit,
            int offset, String storeType) {

        String cacheKey = "SCI_" + storeType + sortBy + sortOrder + featured + limit + offset;

        if (storeItemsCache.isExpired(cacheKey)) {
            final ApplicationEx appEx = ApplicationEx.getInstance();

            if (appEx != null) {
                final RequestManager requestManager = appEx.getRequestManager();
                if (requestManager != null) {
                    requestManager.getStoreCategoryPopularItems(getStoreCategoryItemsListener, sortBy, sortOrder,
                            featured, limit, offset, Session.getInstance().getUserId(), storeType);

                    BroadcastHandler.MigStore.Item.sendBeginFetchStoreItems(cacheKey);
                }
            }
        }

        return storeItemsCache.getData(cacheKey);
    }

    /*
     * Return the store item's data
     */
    public StoreItem getStoreItem(String itemId) {
        if (storeItemCache.isExpired(itemId)) {
            final ApplicationEx appEx = ApplicationEx.getInstance();

            if (appEx != null) {
                final RequestManager requestManager = appEx.getRequestManager();
                if (requestManager != null) {
                    requestManager.getStoreItem(getStoreItemListener, Session.getInstance().getUserId(), itemId);
                }
            }
        }

        return storeItemCache.getData(itemId);
    }

    public void purchaseGift(String itemId, String recipients, String message, String privateGift, String postToMiniblog, String counterId) {
        final ApplicationEx appEx = ApplicationEx.getInstance();

        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.purchaseGiftItem(getStoreItemListener, Session.getInstance().getUserId(), itemId,
                        recipients, message, privateGift, postToMiniblog, counterId);
            }
        }
    }

    public void purchaseStickerPack(final String packId) {
        final ApplicationEx appEx = ApplicationEx.getInstance();

        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                
                final GetStoreItemListener storeItemListener = new GetStoreItemListener() {
                    @Override
                    public void onStoreItemReceived(String key, StoreItem storeItem) {
                    }

                    @Override
                    public void onStoreItemPurchased(String key, StoreItem storeItem) {
                        cacheStoreItem(key, storeItem);
                        stickerPackPurchaseDone(packId);
                        BroadcastHandler.MigStore.Item.sendPurchased(storeItem.getId());
                    }

                    @Override
                    public void onStoreItemPurchaseError(final MigError error) {
                        stickerPackPurchaseDone(packId);
                        BroadcastHandler.MigStore.Item.sendPurchaseError(error);
                    }
                    
                    @Override
                    public void onStoreUnlockedItemSent(String key, StoreItem storeItem) {
                    };
                };
                
                beginStickerPackPurchase(packId);
                BroadcastHandler.MigStore.Item.sendBeginPurchaseItem(Integer.parseInt(packId));
                requestManager.purchaseStickerPack(storeItemListener, Session.getInstance().getUserId(), packId);
            }
        }
    }
    
    public StoreUnlockedItem[] getUnlockedGifts(String userId, boolean shouldForceFetch) {
        if (unlockedGiftsCache.isExpired(userId) || shouldForceFetch) {
            final ApplicationEx appEx = ApplicationEx.getInstance();

            if (appEx != null) {
                final RequestManager requestManager = appEx.getRequestManager();
                if (requestManager != null) {
                    requestManager.getUnlockedGifts(getStoreUnlockedItemsListener, userId);
                }
            }
        }

        return unlockedGiftsCache.getData(userId);
    }
    
    public void sendUnlockedItem(String itemId, String recipients, String message, String privateGift, String postToMiniblog) {
        final ApplicationEx appEx = ApplicationEx.getInstance();

        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.sendUnlockedGiftItem(getStoreItemListener, Session.getInstance().getUserId(), itemId,
                        recipients, message, privateGift, postToMiniblog);
            }
        }
    }
    
    public StoreUnlockedItem getUnlockedGift(String userId, String itemId) {
        if (unlockedGiftsCache != null) {
            StoreUnlockedItem[] unlockedItemArr = unlockedGiftsCache.getData(userId);
            for (StoreUnlockedItem storeUnlockedItem : unlockedItemArr) {
                if (itemId.equals(storeUnlockedItem.getStoreItemData().getId().toString())) {
                    return storeUnlockedItem;
                }
            }
        } 
        
        return null;
    }

    public String getLocalCurrency() {
        return localCurrency;
    }

    public void setLocalCurrency(String localCurrency) {
        this.localCurrency = localCurrency;
    }

    public boolean canPurchaseItem(float itemPrice) {
        String balance = Session.getInstance().getAccountBalance();
        balance = balance.replaceAll("[^\\.\\d]", Constants.BLANKSTR);

        float accountBalance = Float.valueOf(balance);
        if (accountBalance > itemPrice) {
            return true;
        }

        return false;
    }

    public StickerStoreItem[] fetchStickerPacks(StoreItem[] storeItem) {
        if (storeItem != null && storeItem.length > 0) {
            Set<Integer> packIds = new HashSet<Integer>();
            StickerStoreItem[] stickerStoreItems = new StickerStoreItem[storeItem.length];

            StickerStoreItem stickerStoreItem = null;
            BaseEmoticonPackData packData = null;
            BaseEmoticonPack pack = null;
            StoreItemReferenceData referenceData = null;
            for (int i = 0; i < storeItem.length; i++) {
                referenceData = storeItem[i].getReferenceData();
                if (referenceData != null) {
                    packData = EmoticonDatastore.getInstance().getBaseEmoticonPackWithId(
                            referenceData.getId());
                    if (packData == null) {
                        pack = new BaseEmoticonPack(referenceData.getId());
                        packData = new BaseEmoticonPackData();
                        packData.setBaseEmoticonPack(pack);
                    }

                    stickerStoreItem = new StickerStoreItem(storeItem[i], packData);
                    stickerStoreItems[i] = stickerStoreItem;

                    packIds.add(Integer.valueOf(referenceData.getId()));
                }
            }
            EmoticonDatastore.getInstance().checkAndFetchStickerPacksForIds(packIds, false, false);
            return stickerStoreItems;
        }
        return null;
    }

    //- TODO: Workaround to update the status of the pack after purchase
    //- Remove after server sends the correct value of owned field.
    public boolean updateStickerStoreItem(StickerStoreItem[] stickerStoreItems, int packId) {
        StickerStoreItem ssi = null;
        int idx = 0;

        if (stickerStoreItems != null && stickerStoreItems.length > 0) {
            for (int i = 0; i < stickerStoreItems.length; i++) {

                if (stickerStoreItems[i].getStoreItem() != null) {
                    if (stickerStoreItems[i].getStoreItem().getId() == packId) {
                        ssi = stickerStoreItems[i];
                        idx = i;
                        break;
                    }
                }
            }
        }

        if (ssi != null) {
            ssi.getStoreItem().getReferenceData().setOwned(true);
            ssi.getPackData().setIsOwnPack(true);
            EmoticonDatastore.getInstance().addStickerPack(ssi.getPackData(), true);
            stickerStoreItems[idx] = ssi;
            return true;
        }

        return false;
    }
    
    public void beginStickerPackPurchase(final String packId) {
        purchaseStickerPackInProgress.add(packId);
    }
    
    public void stickerPackPurchaseDone(final String packId) {
        purchaseStickerPackInProgress.remove(packId);
    }
    
    public boolean isStickerPackPurchaseInProcess(final String packId) {
        return (purchaseStickerPackInProgress.contains(packId));
    }
    
}
