/**
 * Copyright (c) 2013 Project Goth
 *
 * DrawerFragment.java
 * Created Jul 16, 2013, 6:13:14 PM
 */

package com.projectgoth.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.enums.AttachmentType;
import com.projectgoth.nemesis.model.BaseEmoticonPack;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.adapter.AttachmentTabAdapter;
import com.projectgoth.ui.fragment.AttachmentPagerFragment.BaseAttachmentFragmentListener;
import com.projectgoth.ui.widget.DrawerTabs;
import com.projectgoth.ui.widget.DrawerTabs.OnTabClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays the attachment panel which includes the tab scroller
 * and the grid menu
 * 
 * @author mapet
 * @author dan
 * 
 */
public class EmoticonDrawerFragment extends BaseFragment implements OnTabClickListener {

    private DrawerTabs                     mDrawerTabs;
    private AttachmentTabAdapter           mDrawerTabsAdapter;
    private List<BaseEmoticonPack>         mDrawerTabsData       = new ArrayList<BaseEmoticonPack>();

    private int                            mSelectedTab          = 0;

    private BaseAttachmentFragmentListener mAttachmentListener;

    private int                            mDrawerHeight = -1;

    private static final String            PARAM_ATTACHMENT_TYPE = "ATTACHMENT_TYPE";

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_emoticon_drawer;
    }
    
    @Override
    public void onAttach(Activity activity) {
        setShouldUpdateActionBarOnAttach(false);
        super.onAttach(activity);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mDrawerHeight > 0) {
            LayoutParams params = view.getLayoutParams();
            params.height = mDrawerHeight;
            view.requestLayout();
        }

        mDrawerTabs = (DrawerTabs) view.findViewById(R.id.tabs);
        mDrawerTabs.setOnTabClickListener(this);
        mDrawerTabsAdapter = new AttachmentTabAdapter();
        mDrawerTabsAdapter.setPadding(ApplicationEx.getDimension(R.dimen.medium_margin));

        refreshTabData();

        if (!mDrawerTabsData.isEmpty()) {
            refreshDrawerFragment(mDrawerTabsData.get(mSelectedTab).getId());
        }
    }

    public void setDrawerTabData(List<BaseEmoticonPack> drawerTabsData) {
        mDrawerTabsData = drawerTabsData;
    }

    public void setSelectedTab(int selectedTab) {
        mSelectedTab = selectedTab;
    }

    private void refreshTabData() {
        mDrawerTabsAdapter.setAttachmentTabList(mDrawerTabsData);
        mDrawerTabsAdapter.setSelected(mSelectedTab);
        mDrawerTabs.setAdapter(mDrawerTabsAdapter);
    }

    private void refreshDrawerFragment(int tabType) {


        AttachmentPagerFragment mDrawerGrid = new AttachmentPagerFragment();
        mDrawerGrid.setAttachmentListener(mAttachmentListener);

        Bundle args = new Bundle();
        args.putInt(PARAM_ATTACHMENT_TYPE, tabType);
        mDrawerGrid.setArguments(args);

        addChildFragment(R.id.grid, mDrawerGrid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.projectgoth.ui.widget.DrawerTabs.OnTabClickListener#onTabClicked(
     * android.view.View, int, long)
     */
    @Override
    public void onTabClicked(View view, int position, long id) {
        mSelectedTab = position;

        if (mSelectedTab >= 0 && mSelectedTab < mDrawerTabsData.size()) {
            int tabId = mDrawerTabsData.get(mSelectedTab).getId();

            if (tabId == AttachmentType.STORE_EMOTICON.value) {
                ActionHandler.getInstance().displayStore(getActivity(), 2, null);
            } else {
                mDrawerTabsAdapter.setSelected(mSelectedTab);
                mDrawerTabs.setAdapter(mDrawerTabsAdapter);
                refreshDrawerFragment(tabId);
            }
        }
    }

    public void setAttachmentListener(BaseAttachmentFragmentListener mAttachmentListener) {
        this.mAttachmentListener = mAttachmentListener;
    }

    public void setDrawerHeight(int drawerHeight) {
        this.mDrawerHeight = drawerHeight;
    }
}
