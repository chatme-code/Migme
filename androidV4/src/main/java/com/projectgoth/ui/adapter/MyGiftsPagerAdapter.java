/**
 * Copyright (c) 2013 Project Goth
 * MyGiftsPagerAdapter.java
 * Created Jan 21, 2015, 2:47:55 PM
 */

package com.projectgoth.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.projectgoth.R;
import com.projectgoth.common.Constants;
import com.projectgoth.enums.ViewPagerType;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.ViewPagerItem;
import com.projectgoth.ui.fragment.BaseFragment;
import com.projectgoth.ui.widget.PagerSlidingTabHeader.IconTabProvider;
import com.projectgoth.util.FragmentUtils;

import java.util.ArrayList;

/**
 * @author mapet
 */
public class MyGiftsPagerAdapter extends BasePagerAdapter<ViewPagerItem> implements IconTabProvider {

    private String mUserId;

    public MyGiftsPagerAdapter(FragmentManager fm, Context context) {
        super(fm, context);
    }

    @Override
    protected ArrayList<ViewPagerItem> createItemList() {
        ArrayList<ViewPagerItem> items = new ArrayList<ViewPagerItem>();
        items.add(new ViewPagerItem(I18n.tr("My gifts"), ViewPagerType.MY_GIFTS));
        items.add(new ViewPagerItem(I18n.tr("Overview"), ViewPagerType.MY_GIFTS_OVERVIEW));
        return items;
    }

    @Override
    public void setPagerItemList(ArrayList<ViewPagerItem> data) {
        super.setPagerItemList(data);
    }

    @Override
    public Fragment getItem(int position) {
        ViewPagerItem item = items.get(position);
        Bundle args = new Bundle();
        args.putString(FragmentUtils.PARAM_USERID, mUserId);
        item.setArgs(args);
        BaseFragment fragment = FragmentUtils.getFragmentByType(item);
        fragment.setShouldUpdateActionBarOnAttach(false);
        return fragment;
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = Constants.BLANKSTR;
        ViewPagerItem item = (ViewPagerItem) items.get(position);
        title = item.getLabel();
        return title;
    }

    @Override
    public void onPositionChanged(int newPosition) {
    }

    @Override
    public int getPageIconResId(int position) {
        ViewPagerItem item = items.get(position);

        switch (item.getType()) {
            case MY_GIFTS:
                return R.drawable.ad_gift_gallery_grey;
            case MY_GIFTS_OVERVIEW:
                return R.drawable.ad_stats_grey;
            default:
                return 0;
        }
    }

    @Override
    public int getPageSelectedIconResId(int position) {
        ViewPagerItem item = items.get(position);

        switch (item.getType()) {
            case MY_GIFTS:
                return R.drawable.ad_gift_gallery_white;
            case MY_GIFTS_OVERVIEW:
                return R.drawable.ad_stats_white;
            default:
                return 0;
        }
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

}
