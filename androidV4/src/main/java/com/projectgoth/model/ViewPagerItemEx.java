/**
 * Copyright (c) 2013 Project Goth
 *
 * ViewPagerItemEx.java
 * Created Jul 31, 2014, 9:03:48 AM
 */

package com.projectgoth.model;

import com.projectgoth.enums.ViewPagerType;

/**
 * @author michael.j
 * 
 */
public class ViewPagerItemEx extends ViewPagerItem {

    private int imageId;

    public ViewPagerItemEx(String label, int imageId, ViewPagerType type) {
        super(label, type);
        this.imageId = imageId;
    }
    
    public int getImageId() {
        return imageId;
    }

}
