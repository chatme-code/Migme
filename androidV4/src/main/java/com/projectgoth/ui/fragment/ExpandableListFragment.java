/**
 * Copyright (c) 2013 Project Goth
 *
 * ExpandableListFragment.java
 * Created Jun 3, 2013, 2:42:49 PM
 */

package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
import com.projectgoth.R;
import com.projectgoth.common.Tools;
import com.projectgoth.ui.widget.PinnedHeaderExpandableListView;

/**
 * This abstract class represents a fragment that contains a Pull-To-Refresh
 * Expandable List View. The AdapterType for this class is the type of
 * BaseExpandableListAdapter to be used for this fragment.
 * 
 * @author angelorohit
 */
public abstract class ExpandableListFragment<AdapterType extends BaseExpandableListAdapter>
        extends BaseSearchFragment
        implements OnRefreshListener, OnScrollListener, ExpandableListView.OnGroupClickListener {

    protected PullToRefreshExpandableListView mPullListView           = null;
    // main data listView
    protected PinnedHeaderExpandableListView  mExpandableListView     = null;
    protected AdapterType                     mAdapter                = null;

    protected boolean                         mIsPullToRefreshEnabled = true;

    /**
     * Forces all types of ExpandableListFragment to implement a function to
     * create an adapter for the fragment.
     * 
     * @return A {@link BaseExpandableListAdapter}
     */
    protected abstract AdapterType createAdapter();
    protected View headerView;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPullListView = (PullToRefreshExpandableListView) view.findViewById(R.id.expandable_list);
        if (mPullListView != null) {
            mPullListView.setShowViewWhileRefreshing(true);
            mPullListView.setOnRefreshListener(this);
            setPullToRefreshEnabled(mIsPullToRefreshEnabled);

            mExpandableListView = mPullListView.getRefreshableView();
            if (mExpandableListView != null) {
                mAdapter = createAdapter();
                addListViewHeader();

                headerView = mAdapter.getGroupView(0, true, null, (ViewGroup) view);
                mExpandableListView.setPinnedHeaderView(headerView);
                mExpandableListView.setAdapter(mAdapter);
                mExpandableListView.setOnScrollListener(this);
                mExpandableListView.setOnGroupClickListener(this);
            }
        }
    }

    public void addListViewHeader() {
    }

    protected void setListEmptyView(View emptyView) {
        emptyView.setVisibility(View.GONE);
        if (emptyView.getParent() == null) {
            ((ViewGroup) mPullListView.getParent()).addView(emptyView);
        }
    }
    
    /**
     * Checks whether pull to refresh is enabled for this fragment.
     * 
     * @return true if enabled and false otherwise.
     */
    protected boolean isPullToRefreshEnabled() {
        return mIsPullToRefreshEnabled;
    }

    /**
     * Enables / disables pull-to-refresh for this fragment.
     * 
     * @param state
     *            true enables pull-to-refresh, false disables pull-to-refresh
     */
    protected void setPullToRefreshEnabled(final boolean state) {
        mIsPullToRefreshEnabled = state;
        if (mPullListView != null) {
            mPullListView.setPullToRefreshEnabled(state);
        }
    }

    /**
     * Expands a group in the {@link ExpandableListView}
     * 
     * @param position
     *            The position of the group to be expanded.
     */
    protected void expandGroup(int position) {
        if (mExpandableListView != null) {
            mExpandableListView.expandGroup(position);
        }
    }

    /**
     * Collapses a group in the {@link ExpandableListView}
     * 
     * @param position
     *            The position of the group to be collapsed.
     */
    protected void collapseGroup(int position) {
        if (mExpandableListView != null) {
            mExpandableListView.collapseGroup(position);
        }
    }

    protected void expandAllGroups() {
        final int groupCount = mAdapter.getGroupCount();
        for (int groupPos = 0; groupPos < groupCount; ++groupPos) {
            expandGroup(groupPos);
        }
    }

    protected void collapseAllGroups() {
        final int groupCount = mAdapter.getGroupCount();
        for (int groupPos = 0; groupPos < groupCount; ++groupPos) {
            collapseGroup(groupPos);
        }
    }

    protected void setPullToRefreshAsRefreshing() {
        if (mPullListView != null && mIsPullToRefreshEnabled && !mPullListView.isRefreshing()) {
            mPullListView.setRefreshing(true);
        }
    }

    /**
     * Sets the pull-to-refresh list as completed refreshing.
     */
    protected void setPullToRefreshComplete() {
        if (mPullListView != null && isPullToRefreshEnabled()) {
            mPullListView.onRefreshComplete();
        }
    }

    /**
     * Override this routine for those fragments that set pull to refresh as
     * enabled. This routine is meant to take action when the user pulls to
     * refresh the view.
     * 
     * @see com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener#onRefresh()
     */
    @Override
    public void onRefresh() {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        if (view instanceof PinnedHeaderExpandableListView) {
            ((PinnedHeaderExpandableListView) view).configureHeaderView(firstVisibleItem);
        }
    }

    @Override
    public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
        return Tools.hideVirtualKeyboard(getActivity());
    }
}
