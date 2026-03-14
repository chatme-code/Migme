/**
 * Copyright (c) 2013 Project Goth
 *
 * BaseListFragment.java.java
 * Created May 30, 2013, 10:27:29 AM
 */

package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.projectgoth.R;
import com.projectgoth.ui.widget.tooltip.ToolTipRelativeLayout;
import com.projectgoth.ui.widget.tooltip.ToolTipView;

/**
 * @author cherryv
 * 
 */
public abstract class BaseProfileFragment extends BaseFragment implements OnRefreshListener, AbsListView.OnScrollListener {

    protected PullToRefreshListView mPullList;
    protected ListView              mList;
    protected BaseAdapter           mAdapter;

    protected boolean               isHeaderEnabled                   = true;
    protected boolean               isFooterEnabled                   = true;
    protected boolean               shouldDisplayListMessage          = false;
    private boolean                 mIsPullToRefreshEnabled           = true;

    public static final String      PARAM_ENABLE_HEADER               = "PARAM_ENABLE_HEADER";
    public static final String      PARAM_ENABLE_FOOTER               = "PARAM_ENABLE_FOOTER";
    public static final String      PARAM_SHOULD_DISPLAY_LIST_MESSAGE = "PARAM_SHOULD_DISPLAY_LIST_MESSAGE";

    protected ToolTipView mToolTipView;
    protected ToolTipRelativeLayout mToolTipFrameLayout;
    private View listView;

    /**
     * @see BaseFragment#readBundleArguments(android.os.Bundle)
     */
    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        isHeaderEnabled = args.getBoolean(PARAM_ENABLE_HEADER, true);
        isFooterEnabled = args.getBoolean(PARAM_ENABLE_FOOTER, true);
        shouldDisplayListMessage = args.getBoolean(PARAM_SHOULD_DISPLAY_LIST_MESSAGE, false);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_navigation_drawer;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = view.findViewById(R.id.list);
        if(listView instanceof ListView) {
            mPullList = null;
            mList = (ListView) listView;
            mList.setOnScrollListener(this);
        } else {
            mPullList = (PullToRefreshListView) listView;
            mPullList.setShowViewWhileRefreshing(true);
            mPullList.setOnRefreshListener(this);
            mPullList.setPullToRefreshEnabled(isPullToRefreshEnabled());
            mList = (ListView) mPullList.getRefreshableView();
            mList.setOnScrollListener(this);
        }

        View headerView = createHeaderView();
        if (headerView != null && isHeaderEnabled) {
            mList.addHeaderView(headerView);
        }

        View footerView = createFooterView();
        if (footerView != null && isFooterEnabled) {
            mList.addFooterView(footerView);
        }

        mAdapter = createAdapter();
        // the header view must be set before set adapter, so make setAdapter
        // not set so early
        mList.setAdapter(mAdapter);

        mToolTipFrameLayout = (ToolTipRelativeLayout) view.findViewById(R.id.container);
    }

    protected View createHeaderView() {
        return null;
    }

    protected View createFooterView() {
        return null;
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
        if (mPullList != null) {
            mPullList.setPullToRefreshEnabled(state);
        }
    }

    /**
     * Sets the pull-to-refresh as currently refreshing. This will only be
     * effected if the pull list is not already refreshing.
     */
    protected void setPullToRefreshAsRefreshing() {
        if (mPullList != null && mIsPullToRefreshEnabled && !mPullList.isRefreshing()) {
            mPullList.setRefreshing(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateListData();
    }

    protected void updateListData() {

    }

    protected abstract BaseAdapter createAdapter();



    @Override
    public void onRefresh() {
    }
}
