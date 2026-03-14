/**
 * Copyright (c) 2013 Project Goth
 *
 * MyGiftsFilterItem.java
 * Created Jan 26, 2015, 2:14:05 PM
 */

package com.projectgoth.model;

import com.projectgoth.datastore.GiftsDatastore.StatisticsPeriod;

/**
 * @author mapet
 * 
 */
public class MyGiftsFilterItem {

    private int              id;
    private String           name;
    private boolean          isSelected;
    private StatisticsPeriod period;
    
    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
    
    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return the isSelected
     */
    public boolean isSelected() {
        return isSelected;
    }
    
    /**
     * @param isSelected the isSelected to set
     */
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
    
    /**
     * @return the period
     */
    public StatisticsPeriod getPeriod() {
        return period;
    }
    
    /**
     * @param period the period to set
     */
    public void setPeriod(StatisticsPeriod period) {
        this.period = period;
    }
    
}
