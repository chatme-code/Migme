/**
 * Copyright (c) 2013 Project Goth
 *
 * MyGiftsOverviewData.java
 * Created Jan 28, 2015, 10:37:02 AM
 */

package com.projectgoth.model;

import java.util.List;

import com.projectgoth.ui.fragment.MyGiftsOverviewFragment.MyGiftsOverviewDisplayType;

/**
 * @author mapet
 * @param <T>
 * 
 */
public class MyGiftsOverviewData<T> {

    private MyGiftsOverviewDisplayType displayType;
    private List<T>                    listData;

    /**
     * @return the displayType
     */
    public MyGiftsOverviewDisplayType getDisplayType() {
        return displayType;
    }

    /**
     * @param displayType
     *            the displayType to set
     */
    public void setDisplayType(MyGiftsOverviewDisplayType displayType) {
        this.displayType = displayType;
    }

    /**
     * @return the listData
     */
    public List<T> getListData() {
        return listData;
    }

    /**
     * @param listData
     *            the listData to set
     */
    public void setListData(List<T> listData) {
        this.listData = listData;
    }

}
