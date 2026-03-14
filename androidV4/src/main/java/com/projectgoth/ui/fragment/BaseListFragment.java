/**
 * Copyright (c) 2013 Project Goth
 *
 * BaseListFragment.java.java
 * Created May 30, 2013, 10:27:29 AM
 */

package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.projectgoth.R;

/**
 * @author cherryv
 * 
 */
public abstract class BaseListFragment extends BaseSearchFragment implements OnRefreshListener, OnScrollListener {

    protected RelativeLayout        mMainContainer;
    protected PullToRefreshListView mPullList;
    protected ListView              mList;
    protected BaseAdapter           mAdapter;
    protected RelativeLayout        mListMessageContainer;
    protected TextView              mListMessage;
    protected FrameLayout           mEmptyViewContainer;

    protected boolean               mIsHeaderEnabled = true;
    protected boolean               isFooterEnabled                   = true;
    protected boolean               shouldDisplayListMessage          = false;
    private boolean                 mIsPullToRefreshEnabled           = true;
    protected boolean               mIsLoadingData;

    public static final String      PARAM_ENABLE_HEADER               = "PARAM_ENABLE_HEADER";
    public static final String      PARAM_ENABLE_FOOTER               = "PARAM_ENABLE_FOOTER";
    public static final String      PARAM_SHOULD_DISPLAY_LIST_MESSAGE = "PARAM_SHOULD_DISPLAY_LIST_MESSAGE";

    /**
     * @see com.projectgoth.ui.fragment.BaseFragment#readBundleArguments(android.os.Bundle)
     */
    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        mIsHeaderEnabled = args.getBoolean(PARAM_ENABLE_HEADER, true);
        isFooterEnabled = args.getBoolean(PARAM_ENABLE_FOOTER, true);
        shouldDisplayListMessage = args.getBoolean(PARAM_SHOULD_DISPLAY_LIST_MESSAGE, false);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_base_list;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMainContainer = (RelativeLayout) view.findViewById(R.id.container);
        mEmptyViewContainer = (FrameLayout) view.findViewById(R.id.empty_view_container);


        View listView = view.findViewById(R.id.list);
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
        if (headerView != null && mIsHeaderEnabled) {
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
        
        if (shouldDisplayListMessage) {
            mListMessageContainer = (RelativeLayout) view.findViewById(R.id.list_message_container);
            mListMessage = (TextView) mListMessageContainer.findViewById(R.id.list_message);
            showDisplayListMessage(true);
        }
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

    protected void setPullToRefreshComplete() {
        if (mPullList != null && mIsPullToRefreshEnabled) {
            if (mPullList.isRefreshing()) {
                mPullList.onRefreshComplete();
            }
        }
    }

    protected void setListEmptyView(View emptyView) {
        if (emptyView != null) {
            mEmptyViewContainer.removeAllViews();
            mEmptyViewContainer.addView(emptyView);
        }
    }
    
    /**
     * Sets the message to be displayed as the background of the list.
     * 
     * @param message
     *            The message to be displayed.
     */
    protected void setDisplayListMessage(final String message) {
        if (mListMessage != null && message != null) {
            mListMessage.setText(message);
        }
    }

    /**
     * Show / hides the display list message in the background.
     * 
     * @param state
     *            true to show and false to hide.
     */
    protected void showDisplayListMessage(final boolean state) {
        if (mListMessageContainer != null) {
            mListMessageContainer.setVisibility((state) ? View.VISIBLE : View.GONE);
        }
    }

    protected void showOrHideEmptyViewIfNeeded() {

    }

    protected void setLoadingData(boolean isLoadingData , boolean shouldUpdateEmptyView) {
        this.mIsLoadingData = isLoadingData;
        if (shouldUpdateEmptyView) {
            showOrHideEmptyViewIfNeeded();
        }
    }

    protected void setLoadingData(boolean isLoadingData) {
        setLoadingData(isLoadingData, false);
    }

    @Override
    protected void showLoadProgressDialog() {
        if (mAdapter != null && mAdapter.getCount() == 0) {
            super.showLoadProgressDialog();
        }
        //don't need to show load progress dialog since there is load more indicator in the list
    }
}
