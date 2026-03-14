/**
 * Copyright (c) 2013 Project Goth
 *
 * InfiniteViewPagerAdapter.java
 * Created Sep 3, 2014, 2:08:11 PM
 */

package com.projectgoth.ui.adapter;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import com.mig33.diggle.common.StringUtils;
import com.projectgoth.ui.fragment.BaseFragment;
import com.projectgoth.ui.widget.InfiniteViewPager;

import java.util.List;


/**
 * A simple pager adapter that provides the necessary Fragments to the
 * {@link InfiniteViewPager} in sequence.
 * 
 * @author angelorohit
 */
public class InfiniteViewPagerAdapter extends FragmentStatePagerAdapter {

    private List<BaseFragment> pagerFragments;
    
    private static final String FIRST_FRAGMENT_CLASS_NAME = "_firstFragmentId";  // Id can't start with "f"
    
    public InfiniteViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }
    
    public List<BaseFragment> getPagerFragments() {
        return this.pagerFragments;
    }
    
    public void setPagerFragments(final List<BaseFragment> pagerFragments) {
        this.pagerFragments = pagerFragments;
        
        // Force parent lists to have the same lengths
        final int count = pagerFragments.size(); 
        while (mFragments.size() < count) {
            mFragments.add(null);
        }
        while (mSavedState.size() < count) {
            mSavedState.add(null);
        }
    }

    @Override
    public Fragment getItem(int position) {
        return (pagerFragments != null && pagerFragments.size() > position) ? 
                pagerFragments.get(position) : null;
    }

    @Override
    public int getCount() {
        return (pagerFragments != null) ? pagerFragments.size() : 0;
    }
    
    @Override
    public int getItemPosition(Object object){
        if (object != null) {
            final int pos = pagerFragments.indexOf(object);
            if (pos != -1) {
                return pos;
            }
        }
        
        return PagerAdapter.POSITION_NONE;
    }

    /**
     * Cycles the items depending on the current position that is passed in.
     * 
     * @param position The position of the currently selected item.
     * @return -1 if the items were cycled to the right, 
     *          1 if the items were cycled to the left, 
     *          0 if the items were not cycled.
     */
    public int cycle(int position) {
        int cycleResult = 0;
        final int count = pagerFragments.size();
        if (count > 2) {
            final int lastPosition = count - 1;
            if (position == lastPosition) {
                pagerFragments.add(pagerFragments.remove(0));
                mFragments.add(mFragments.remove(0));
                mSavedState.add(mSavedState.remove(0));
                cycleResult = -1;
            } else if (position == 0) {
                pagerFragments.add(0, pagerFragments.remove(lastPosition));
                mFragments.add(0, mFragments.remove(lastPosition));
                mSavedState.add(0, mSavedState.remove(lastPosition));
                cycleResult = 1;
            } else {
                return 0;
            }
            notifyDataSetChanged();
        }
        return cycleResult;
    }
    
    @Override
    public Parcelable saveState() {
        Parcelable state = super.saveState();
        if (state == null) {
            state = new Bundle();
        }
        if (state instanceof Bundle) {
            if (pagerFragments.size() > 0) {
                Bundle bundle = (Bundle) state;
                bundle.putString(FIRST_FRAGMENT_CLASS_NAME, pagerFragments.get(0).getClass().getName());
            }
        }
        return state;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        if (state != null) {
            Bundle bundle = (Bundle) state;
            String fragmentClassName = bundle.getString(FIRST_FRAGMENT_CLASS_NAME);
            if (!StringUtils.isEmpty(fragmentClassName)) {
                final int count = pagerFragments.size();
                for (int i = 0; i < count; i++) {
                    BaseFragment fragment = pagerFragments.get(0);
                    if (fragment.getClass().getName().equals(fragmentClassName)) {
                        break;
                    }
                    pagerFragments.add(pagerFragments.remove(0));
                }
            }
        }

        super.restoreState(state, loader);
    }
}
