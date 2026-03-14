/**
 * Copyright (c) 2013 Project Goth
 *
 * MenuGroup.java.java
 * Created Jun 10, 2013, 5:08:34 PM
 */

package com.projectgoth.model;

import com.projectgoth.b.data.MenuConfigItem;

/**
 * @author cherryv
 * 
 */
public class MenuGroup {

    private String title;
    private MenuConfigItem[] items;

    public MenuGroup(String title) {
        this.setTitle(title);
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the items
     */
    public MenuConfigItem[] getItems() {
        return items;
    }

    /**
     * @param items
     *            the items to set
     */
    public void setItems(MenuConfigItem[] items) {
        this.items = items;
    }

}
