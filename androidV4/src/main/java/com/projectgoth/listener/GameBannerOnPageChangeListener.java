package com.projectgoth.listener;

import android.support.v4.view.ViewPager;

/**
 * Created by danielchen on 15/1/28.
 */
public class GameBannerOnPageChangeListener implements ViewPager.OnPageChangeListener {

    @Override
    public void onPageSelected(int position) {
        // int size = imageIdList == null ? 0 : imageIdList.size();
        // indexText.setText(new StringBuilder().append((position) % size +
        // 1).append("/")
        // .append(size));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

}
