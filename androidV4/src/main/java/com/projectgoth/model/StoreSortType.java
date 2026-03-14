package com.projectgoth.model;

/**
 * Created by felixqk on 17/12/14.
 */
public enum StoreSortType {
    POPULARITY(0), PRICE_ASC(1), PRICE_DESC(2), NAME_ASC(3), NAME_DESC(4);

    private int type;

    private StoreSortType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public static StoreSortType fromValue(int type) {
        for (StoreSortType listType : values()) {
            if (listType.getType() == type) {
                return listType;
            }
        }
        return POPULARITY;
    }
}