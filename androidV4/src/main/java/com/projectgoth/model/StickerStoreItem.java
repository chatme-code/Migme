/**
 * Copyright (c) 2013 Project Goth
 *
 * StickerStoreItem.java
 * Created Dec 11, 2014, 5:47:48 PM
 */

package com.projectgoth.model;

import com.projectgoth.b.data.StoreItem;

/**
 * @author mapet
 * 
 */
public class StickerStoreItem {

    private StoreItem            storeItem;
    private BaseEmoticonPackData packData;

    public StickerStoreItem() {
    }

    public StickerStoreItem(final StoreItem storeItem, final BaseEmoticonPackData packData) {
        this.storeItem = storeItem;
        this.packData = packData;
    }

    public StoreItem getStoreItem() {
        return storeItem;
    }

    public void setStoreItem(StoreItem storeItem) {
        this.storeItem = storeItem;
    }

    public BaseEmoticonPackData getPackData() {
        return packData;
    }

    public void setPackData(BaseEmoticonPackData packData) {
        this.packData = packData;
    }

}
