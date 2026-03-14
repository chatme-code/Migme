/**
 * Copyright (c) 2013 Project Goth
 *
 * ViewPagerItem.java
 * Created Mar 6, 2014, 4:48:48 PM
 */

package com.projectgoth.model;

import android.os.Bundle;

import com.projectgoth.enums.ViewPagerType;
import com.projectgoth.ui.adapter.ProfilePagerAdapter.PagerScrollListener;

/**
 * @author mapet
 * 
 */
public class ViewPagerItem {

    private int                 postion;
    
    private String              label;
    private ViewPagerType       type;
    private Bundle              args = new Bundle();

    private boolean             hasHeaderPlaceHolder;

    private PagerScrollListener pagerScrollListener;

    public ViewPagerItem(String label, ViewPagerType type) {
        super();
        this.label = label;
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ViewPagerType getType() {
        return type;
    }

    public void setFragment(ViewPagerType type) {
        this.type = type;
    }

    /**
     * @return the args
     */
    public Bundle getArgs() {
        return args;
    }

    /**
     * @param args
     *            the args to set
     */
    public void setArgs(Bundle args) {
        this.args = args;
    }

    /**
     * @return the hasHeaderPlaceHolder
     */
    public boolean hasHeaderPlaceHolder() {
        return hasHeaderPlaceHolder;
    }

    /**
     * @param hasHeaderPlaceHolder
     *            the hasHeaderPlaceHolder to set
     */
    public void setHasHeaderPlaceHolder(boolean hasHeaderPlaceHolder) {
        this.hasHeaderPlaceHolder = hasHeaderPlaceHolder;
    }

    /**
     * @return the pagerScrollListener
     */
    public PagerScrollListener getPagerScrollListener() {
        return pagerScrollListener;
    }

    /**
     * @param pagerScrollListener the pagerScrollListener to set
     */
    public void setPagerScrollListener(PagerScrollListener pagerScrollListener) {
        this.pagerScrollListener = pagerScrollListener;
    }

    /**
     * @return the postion
     */
    public int getPostion() {
        return postion;
    }

    /**
     * @param postion the postion to set
     */
    public void setPostion(int postion) {
        this.postion = postion;
    }

}
