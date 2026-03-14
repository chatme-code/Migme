/**
 * Copyright (c) 2013 Project Goth
 *
 * BadgesFragment.java
 * Created Aug 22, 2013, 3:11:48 PM
 */

package com.projectgoth.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.b.data.Badge;
import com.projectgoth.common.TextUtils;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.events.AppEvents;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.adapter.BadgeListAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;

import java.util.List;

/**
 * @author dangui
 * 
 */
public class BadgesFragment extends BaseListFragment implements OnRefreshListener, BaseViewListener<Badge> {

    public static final String PARAM_USERNAME = "username";

    private BadgeListAdapter   mAdapter;
    private String             username;

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        username = args.getString(PARAM_USERNAME);
        updateActionBar(getActivity());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_badges;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateData(false);
    }

    @Override
    protected BaseAdapter createAdapter() {
        mAdapter = new BadgeListAdapter();
        mAdapter.setBagdeViewListener(this);
        return mAdapter;
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.User.FETCH_BADGES_COMPLETED);
        registerEvent(Events.User.FETCH_BADGES_ERROR);
        registerEvent(AppEvents.NetworkService.ERROR);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(Events.User.FETCH_BADGES_COMPLETED)) {
            Bundle data = intent.getExtras();
            String username = data.getString(Events.User.Extra.USERNAME);
            if (this.username.equalsIgnoreCase(username)) {
                updateData(false);
            }
            mPullList.onRefreshComplete();
        } else if (action.equals(Events.User.FETCH_BADGES_ERROR)
                && (mAdapter.getBadges() == null || mAdapter.getBadges().isEmpty())) {
            Tools.showToastForIntent(context, intent);
        } else if (action.equals(AppEvents.NetworkService.ERROR)) {
            mPullList.onRefreshComplete();
        }
    }

    private void updateData(final boolean shouldForceFetch) {
        List<Badge> badges = UserDatastore.getInstance().getBadgesForUserWithName(username, shouldForceFetch);
        if (badges != null) {
            mAdapter.setBadges(badges);
        }
    }

    @Override
    public void onRefresh() {
        if(Session.getInstance().isNetworkConnected()) {
            updateData(true);
        } else {
            mPullList.onRefreshComplete();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        updateData(false);
    }

    @Override
    public void onItemClick(View v, Badge data) {
        ActionHandler.getInstance().displayBadgeInfoFragment(getActivity(), username, data.getId());
    }

    @Override
    public void onItemLongClick(View v, Badge data) {
    }

    @Override
    protected String getTitle() {
        if (!TextUtils.isEmpty(username)) {
            return String.format(I18n.tr("Badges (%d)"), 
                    UserDatastore.getInstance().getUnlockedBadgesCounter(username));
        } else {
            return I18n.tr("Badges");
        }
    }

    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_badge_white;
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = super.getActionBarConfig();
        config.setNavigationButtonState(NavigationButtonState.BACK);
        return config;
    }

}
