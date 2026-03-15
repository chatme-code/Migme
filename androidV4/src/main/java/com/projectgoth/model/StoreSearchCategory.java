package com.projectgoth.model;

import com.projectgoth.model.StorePagerItem.StorePagerType;

/**
 * Created by houdangui on 9/12/14.
 */

public class StoreSearchCategory<T> {

    private String         label;
    private StorePagerType type;
    private int totalNum;
    private T[]    storeItems;
    private String requestKey;
    private boolean noResult;

    public StoreSearchCategory(String label, StorePagerType type) {
        this.label = label;
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    public T[] getStoreItems() {
        return storeItems;
    }

    public void setStoreItems(T[] storeItems) {
        this.storeItems = storeItems;
    }

    public boolean hasMoreItems() {
        int itemsCount = getItemsCount();

        if (itemsCount < totalNum) {
            return true;
        }

        return false;
    }

    public int getItemsCount() {
        int count = 0;

        if (storeItems != null) {
            count = storeItems.length;
        }

        return count;
    }

    public StorePagerType getType() {
        return type;
    }

    public boolean hasNoResult() {
        return noResult;
    }

    public void setNoResult(boolean noResult) {
        this.noResult = noResult;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }
}
