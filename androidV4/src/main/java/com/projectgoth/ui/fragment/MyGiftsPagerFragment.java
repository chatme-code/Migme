/**
 * Copyright (c) 2013 Project Goth
 * MyGiftsPagerFragment.java
 * Created Jan 21, 2015, 1:50:37 PM
 */

package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;

import com.projectgoth.R;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.adapter.MyGiftsPagerAdapter;
import com.projectgoth.ui.widget.PagerSlidingTabHeader;
import com.projectgoth.util.FragmentUtils;

/**
 * @author mapet
 */
public class MyGiftsPagerFragment extends BaseViewPagerFragment implements OnPageChangeListener {

    protected ViewPager mViewPager;
    private MyGiftsPagerAdapter mAdapter;
    private PagerSlidingTabHeader mTabs;
    private int mSelectedTab = 0;
    private String[] mPageTitles;
    private String mUserId;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_my_gifts_pager;
    }

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        mUserId = args.getString(FragmentUtils.PARAM_USERID);
    }

    @Override
    protected FragmentStatePagerAdapter createAdapter(FragmentManager fragmentManager) {
        mAdapter = new MyGiftsPagerAdapter(fragmentManager, getActivity());
        mAdapter.setUserId(mUserId);
        return mAdapter;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setPageTitles();

        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mViewPager.setAdapter(mAdapter);

        mTabs = (PagerSlidingTabHeader) view.findViewById(R.id.tabs);
        mTabs.setViewPager(mViewPager);

        mViewPager.setBackgroundColor(Theme.getColor(ThemeValues.LIGHT_BACKGROUND_COLOR));
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setCurrentItem(mSelectedTab);
    }

    private void setPageTitles() {
        mPageTitles = new String[2];
        mPageTitles[0] = I18n.tr("My gifts");
        mPageTitles[1] = I18n.tr("Overview");
    }

    @Override
    protected String getTitle() {
        return I18n.tr("My gifts");
    }

    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_gift_white;
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = super.getActionBarConfig();
        config.setNavigationButtonState(NavigationButtonState.BACK);
        return config;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        mSelectedTab = position;
        mAdapter.setCurrentPos(position);
        setTitle(mPageTitles[position]);
        mTabs.notifyDataSetChanged();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

}
