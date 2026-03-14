/**
 * Copyright (c) 2013 Project Goth
 *
 * HomeViewPagerFragment.java.java
 * Created May 31, 2013, 2:36:27 PM
 */
package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import com.projectgoth.R;


/**
 * @author cherryv
 *
 */
public abstract class BaseViewPagerFragment extends BaseFragment {
	
	protected ViewPager viewPager;
	private FragmentStatePagerAdapter mAdapter;
	
	@Override
    protected int getLayoutId() {
        return R.layout.fragment_base_view_pager;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
	    viewPager = (ViewPager) view.findViewById(R.id.pager);
	    mAdapter = createAdapter(getChildFragmentManager());
		viewPager.setAdapter(mAdapter);
	}
	
	protected abstract FragmentStatePagerAdapter createAdapter(FragmentManager fragmentManager);

}
