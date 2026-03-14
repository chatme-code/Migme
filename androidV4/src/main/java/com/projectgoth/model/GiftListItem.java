/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftListItem.java
 * Created Nov 27, 2013, 2:31:44 PM
 */

package com.projectgoth.model;

import com.projectgoth.b.data.StoreItem;
import com.projectgoth.b.data.StoreItems;
import com.projectgoth.controller.StoreController.StoreItemFilterType;
import com.projectgoth.ui.adapter.StoreCategoryAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;

/**
 * @author mapet
 * 
 */
public class GiftListItem {

    private StoreItemFilterType         filterType;
    private String                      filterName;
    private StoreItems                  storeItems;
    private StoreCategoryAdapter         giftItemAdapter;
    private BaseViewListener<StoreItem> giftItemListener;

    public StoreItemFilterType getFilterType() {
        return filterType;
    }

    public void setFilterType(StoreItemFilterType filterType) {
        this.filterType = filterType;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public StoreItems getGiftItems() {
        return storeItems;
    }

    public void setStoreItems(StoreItems giftItems) {
        this.storeItems = giftItems;
    }

    public StoreCategoryAdapter getGiftItemAdapter() {
        return giftItemAdapter;
    }

    public void setGiftItemAdapter(StoreCategoryAdapter giftItemAdapter) {
        this.giftItemAdapter = giftItemAdapter;
    }

    public BaseViewListener<StoreItem> getStoreItemListener() {
        return giftItemListener;
    }

    public void setGiftItemListener(BaseViewListener<StoreItem> giftItemListener) {
        this.giftItemListener = giftItemListener;
    }

}
