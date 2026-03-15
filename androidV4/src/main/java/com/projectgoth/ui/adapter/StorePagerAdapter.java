/**
 * Copyright (c) 2013 Project Goth
 *
 * StorePagerAdapter.java
 * Created Nov 22, 2013, 11:58:10 AM
 */

package com.projectgoth.ui.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.projectgoth.R;
import com.projectgoth.common.Constants;
import com.projectgoth.common.WebURL;
import com.projectgoth.enums.ViewPagerType;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.StorePagerItem;
import com.projectgoth.model.ViewPagerItem;
import com.projectgoth.ui.fragment.BaseFragment;
import com.projectgoth.ui.widget.PagerSlidingTabHeader.IconTabProvider;
import com.projectgoth.util.FragmentUtils;

/**
 * @author mapet
 * 
 */
public class StorePagerAdapter extends BasePagerAdapter<ViewPagerItem> implements IconTabProvider {

    private String initialRecipient;

    public StorePagerAdapter(FragmentManager fm, Context context, String initialRecipient) {
        super(fm, context);
        this.initialRecipient = initialRecipient;
    }

    @Override
    protected ArrayList<ViewPagerItem> createItemList() {
        ArrayList<ViewPagerItem> items = new ArrayList<ViewPagerItem>();
        items.add(new ViewPagerItem(I18n.tr("Gifts"), ViewPagerType.STORE_GIFT));
        items.add(new ViewPagerItem(I18n.tr("Stickers"), ViewPagerType.STORE_STICKER));
        items.add(new ViewPagerItem(I18n.tr("Emoticons"), ViewPagerType.STORE_EMOTICON));
        items.add(new ViewPagerItem(I18n.tr("Avatar"), ViewPagerType.STORE_AVATAR));
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
        switch (item.getType()) {
            case STORE_GIFT:
                args.putString(FragmentUtils.PARAM_INITIAL_RECIPIENT, initialRecipient);
                break;
            case STORE_STICKER:
                args.putString(FragmentUtils.PARAM_MESSAGE,
                        I18n.tr("Stickers will be available for purchase on Android phones soon!"));
                break;
            case STORE_EMOTICON:
                args.putString(FragmentUtils.PARAM_URL, WebURL.URL_BUY_EMOTICON + WebURL.URL_PARAM_SHOW_NO_HEADER);
                break;
            case STORE_AVATAR:
                args.putString(FragmentUtils.PARAM_URL, WebURL.URL_BUY_AVATAR + WebURL.URL_PARAM_SHOW_NO_HEADER);
                break;
            default:
                break;
        }
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
            case STORE_GIFT:
                return R.drawable.ad_gift_grey;
            case STORE_STICKER:
                return R.drawable.ad_sticker_grey;
            case STORE_EMOTICON:
                return R.drawable.ad_emoticon_grey;
            case STORE_AVATAR:
                return R.drawable.ad_avatar_grey;
            default:
                return 0;
        }
    }
    
    @Override
    public int getPageSelectedIconResId(int position) {
        ViewPagerItem item = items.get(position);

        switch (item.getType()) {
            case STORE_GIFT:
                return R.drawable.ad_gift_white;
            case STORE_STICKER:
                return R.drawable.ad_sticker_white;
            case STORE_EMOTICON:
                return R.drawable.ad_emoticon_white;
            case STORE_AVATAR:
                return R.drawable.ad_avatar_white;
            default:
                return 0;
        }
    }

    public StorePagerItem.StorePagerType getSelectedPagerType() {
        ViewPagerItem item = items.get(getCurrentPos());

        switch (item.getType()) {
            case STORE_GIFT:
                return StorePagerItem.StorePagerType.GIFTS;
            case STORE_STICKER:
                return StorePagerItem.StorePagerType.STICKERS;
            case STORE_EMOTICON:
                return StorePagerItem.StorePagerType.EMOTICONS;
            case STORE_AVATAR:
                return StorePagerItem.StorePagerType.AVATAR;
            default:
                return null;
        }
    }
}
