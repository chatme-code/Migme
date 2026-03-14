/**
 * Copyright (c) 2013 Project Goth
 *
 * BasePagerAdapter.java
 * Created Nov 21, 2013, 5:29:15 PM
 */

package com.projectgoth.ui.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.ui.widget.PagerTabView;
import com.projectgoth.ui.widget.PagerTabView.CaptionPagerTabView;
import com.projectgoth.ui.widget.PagerTabView.ImagePagerTabView;

/**
 * @author mapet
 * @param <T>
 * 
 */
public abstract class BasePagerAdapter<T> extends FragmentStatePagerAdapter {

    private Context      context;
    protected ArrayList<T> items = new ArrayList<T>();
    private int          currentPos;

    public BasePagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
        items = createItemList();
        setPagerItemList(items);
    }

    protected abstract ArrayList<T> createItemList();

    public void setPagerItemList(ArrayList<T> data) {
        items = data;
    }

    public PagerTabView createPageOption(String name, int counter, int notifCtr, int position) {
        return createPageOption(name, 0, counter, notifCtr, position, false);
    }
    
    public PagerTabView createPageOption(String name, int counter, int notifCtr, int position, boolean useSmallTextSize) {
        return createPageOption(name, 0, counter, notifCtr, position, useSmallTextSize); 
    }

    public PagerTabView createPageOption(int imageId, int counter, int notifCtr, int position, boolean useSmallTextSize) {
        return createPageOption(null, imageId, counter, notifCtr, position, useSmallTextSize); 
    }

    public PagerTabView createPageOption(String name, int imageId, int counter, int notifCtr, int position, boolean useSmallTextSize) {
        PagerTabView pagerTab = null; 
        
        if (imageId > 0) {
            ImagePagerTabView imagePagerTab = new ImagePagerTabView(context);
            imagePagerTab.setImageResource(imageId);
            pagerTab = imagePagerTab;
        } else {
            CaptionPagerTabView captionPagerTab = new CaptionPagerTabView(context);
            captionPagerTab.setCaption(name);
            pagerTab = captionPagerTab;
        }

        if (counter > 0) {
            pagerTab.setCounter(Tools.formatCounters(counter, Constants.MAX_COUNT_DISPLAY_PROFILE));
            pagerTab.setCounterVisible(true);
        }

        if (notifCtr > 0) {
            if (notifCtr > Constants.MAX_COUNT_DISPLAY_NOTIFICATIONS) {
                pagerTab.setNotificationCounter(Constants.MAX_COUNT_DISPLAY_NOTIFICATIONS + Constants.PLUSSTR);
            } else {
                pagerTab.setNotificationCounter(String.valueOf(notifCtr));
            }

            pagerTab.setNotificationCtrVisible(true);
        }

        if (useSmallTextSize) {
            pagerTab.displaySmallTextSize();
        }
        
        return pagerTab;
    }

    public int getCurrentPos() {
        return currentPos;
    }

    public void setCurrentPos(int currentPos) {
        if (this.currentPos != currentPos) {
            this.currentPos = currentPos;
            onPositionChanged(currentPos);
        }
    }

    public T getPagerItem(int index) {
        return items.get(index);
    }

    @Override
    public Fragment getItem(int index) {
        return null;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    /**
     * Called when the current position switches to a new position
     * 
     * @param newPosition
     */
    public abstract void onPositionChanged(int newPosition);
}
