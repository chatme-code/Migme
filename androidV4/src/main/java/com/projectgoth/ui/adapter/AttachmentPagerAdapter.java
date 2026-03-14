/**
 * Copyright (c) 2013 Project Goth
 *
 * AttachmentPagerAdapter.java
 * Created 20 Mar, 2014, 5:33:22 pm
 */

package com.projectgoth.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.projectgoth.controller.AttachmentPagerCalculator;
import com.projectgoth.enums.ViewPagerType;
import com.projectgoth.model.ViewPagerItem;
import com.projectgoth.ui.fragment.AttachmentFragment;
import com.projectgoth.ui.fragment.AttachmentPagerFragment;
import com.projectgoth.ui.fragment.AttachmentPagerFragment.BaseAttachmentFragmentListener;
import com.projectgoth.util.FragmentUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dan
 * 
 */
public class AttachmentPagerAdapter extends BasePagerAdapter<Object> {

    int packId;
    List<? extends Object> list;
    private BaseAttachmentFragmentListener mAttachmentListener;
    AttachmentPagerCalculator pagerCalculator;
    
    /**
     * @param fm
     */
    public AttachmentPagerAdapter(FragmentManager fm, Context context, int packId) {
        super(fm, context);
        
        this.packId = packId;
        pagerCalculator = new AttachmentPagerCalculator(packId);

    }

    @Override
    public Fragment getItem(int position) {
        
        ViewPagerItem item = new ViewPagerItem(null, ViewPagerType.ATTACHMENT_GRID);

        //put args in bundle
        Bundle args = new Bundle();
        args.putInt(AttachmentPagerFragment.PARAM_PACK_ID, packId);
        args.putInt(AttachmentPagerFragment.PARAM_PAGER_COLUMNS, pagerCalculator.getNumOfColumn());
        item.setArgs(args);
        
        AttachmentFragment pagerFragment = (AttachmentFragment) FragmentUtils.getFragmentByType(item);

        if (list != null) {
            int[] dataRange = pagerCalculator.getDataRangeOfPage(position, list.size());
            if (dataRange != null) {
                pagerFragment.setData(list.subList(dataRange[0], dataRange[1]));
            }
        }
        
        //set click listener
        pagerFragment.setAttachmentListener(mAttachmentListener);
        
        return pagerFragment;
    }

    @Override
    public int getCount() {
        if (list != null) {
            int numOfPage = pagerCalculator.getNumOfPage(list.size());
            return numOfPage;
        }
        
        return 0;
    }

    /**
     * @return the packId
     */
    public int getPackId() {
        return packId;
    }
    
    /**
     * @param packId the packId to set
     */
    public void setPackId(int packId) {
        this.packId = packId;
    }

    /**
     * @param list
     */
    public void setData(List<? extends Object> list) {
        this.list = list;
    }
    
    public void setAttachmentListener(BaseAttachmentFragmentListener attachmentListener) {
        this.mAttachmentListener = attachmentListener;
    }

    @Override
    protected ArrayList<Object> createItemList() {
        // NOT NEEDED
        return null;
    }
    
    @Override
    public void onPositionChanged(int newPosition) {
    }
}
