/**
 * Copyright (c) 2013 Project Goth
 *
 * GridItem.java
 * Created Jul 19, 2013, 4:24:13 PM
 */

package com.projectgoth.model;

/**
 * @author mapet
 * 
 */
public class GridItem {

    private int    id;
    private String title;
    private int    resId;

    public GridItem(int id, String title, int resId) {
        this.id = id;
        this.title = title;
        this.resId = resId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

}
