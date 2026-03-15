/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatManagerPagerAdapter.java
 * Created Nov 26, 2014, 3:58:58 PM
 */

package com.projectgoth.ui.adapter;

import java.lang.ref.SoftReference;
import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.LruCache;

import com.projectgoth.enums.ViewPagerType;
import com.projectgoth.model.ViewPagerItem;
import com.projectgoth.ui.fragment.BaseFragment;
import com.projectgoth.ui.fragment.BaseFragment.FragmentLifecycleListener;
import com.projectgoth.util.FragmentUtils;

/**
 * @author warrenbalcos
 * 
 */
public class ChatManagerPagerAdapter extends BasePagerAdapter<ViewPagerItem> {

    private LruCache<String, SoftReference<BaseFragment>> fragmentCache;
    
    private FragmentLifecycleListener                     fragmentLifecycleListener;
    
    /**
     * @param fm
     * @param context
     */
    public ChatManagerPagerAdapter(FragmentManager fm, Context context) {
        super(fm, context);
    }

    @Override
    protected ArrayList<ViewPagerItem> createItemList() {
        ArrayList<ViewPagerItem> items = new ArrayList<ViewPagerItem>();

        ViewPagerItem item = new ViewPagerItem("Chat List", ViewPagerType.CHAT_LIST);
        items.add(item);
        
        item = new ViewPagerItem("Friend List", ViewPagerType.CONTACT_LIST);
        items.add(item);

        item = new ViewPagerItem("Chatroom List", ViewPagerType.CHATROOM_LIST);
        items.add(item);

        return items;
    }

    @Override
    public void onPositionChanged(int newPosition) {
    }

    @Override
    public Fragment getItem(int index) {
        ViewPagerItem item = items.get(index);
        Bundle args = new Bundle();
        switch (item.getType()) {
            case CHAT_LIST:
            case CHATROOM_LIST:
            case CONTACT_LIST:
                args.putBoolean(FragmentUtils.PARAM_ALLOW_FILTER, true);
                break;
            default:
                break;
        }
        item.setArgs(args);
        BaseFragment fragment = FragmentUtils.getFragmentByType(item);

        fragment.setShouldUpdateActionBarOnAttach(false);
        fragment.setFragmentLifecycleListener(fragmentLifecycleListener);
        fragmentCache.put(item.getLabel(), new SoftReference<BaseFragment>(fragment));
        return fragment;
    }

    public BaseFragment getCachedFragment(int position) {
        if (fragmentCache != null) {
            ViewPagerItem item = getPagerItem(position);
            SoftReference<BaseFragment> cachedItem = fragmentCache.get(item.getLabel());
            if (cachedItem != null) {
                return cachedItem.get();
            }
        }
        return null;
    }

    @Override
    public void setPagerItemList(ArrayList<ViewPagerItem> data) {
        super.setPagerItemList(data);
        fragmentCache = new LruCache<String, SoftReference<BaseFragment>>(data.size());
    }

    /**
     * @param FragmentLifecycleListener
     *            the FragmentLifecycleListener to set
     */
    public void setFragmentLifecycleListener(FragmentLifecycleListener listener) {
        this.fragmentLifecycleListener = listener;
    }
}
