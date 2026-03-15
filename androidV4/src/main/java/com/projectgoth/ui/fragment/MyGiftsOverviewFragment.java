/**
 * Copyright (c) 2013 Project Goth
 * MyGiftsOverviewFragment.java
 * Created Jan 22, 2015, 4:52:38 PM
 */

package com.projectgoth.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.b.data.GiftCount;
import com.projectgoth.b.data.GiftReceivedLeaderboardItem;
import com.projectgoth.b.data.GiftSenderLeaderboardItem;
import com.projectgoth.datastore.GiftsDatastore;
import com.projectgoth.datastore.GiftsDatastore.StatisticsPeriod;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.MyGiftsOverviewData;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.adapter.MyGiftsOverviewAdapter;
import com.projectgoth.ui.fragment.MyGiftsOverviewFilterFragment.MyGiftsOverviewFilterType;
import com.projectgoth.ui.fragment.MyGiftsOverviewFilterFragment.MyGiftsOverviewSortingListener;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.widget.Spinner;
import com.projectgoth.util.FragmentUtils;

import java.util.List;

/**
 * @param <T>
 * @author mapet
 */
public class MyGiftsOverviewFragment<T> extends BaseListFragment implements OnClickListener,
        MyGiftsOverviewSortingListener {

    private MyGiftsOverviewAdapter<T> mAdapter;

    private TextView mNewGiftsCount;
    private Spinner mCategorySpinner;
    private Spinner mPeriodSpinner;
    private View mEmptyView;

    private MyGiftsOverviewData<T> myGiftsOverviewData;
    private MyGiftsOverviewDisplayType mDisplayType = MyGiftsOverviewDisplayType.SENDER;
    private StatisticsPeriod mPeriod = StatisticsPeriod.WEEKLY;

    private int mCategoryId = 0;
    private int mPeriodId = 0;
    private String mUserId;

    private int TOP_LIST_LIMIT = 5;

    public enum MyGiftsOverviewDisplayType {
        SENDER(0), RECEIVED(1);

        private int type;

        private MyGiftsOverviewDisplayType(int type) {
            this.type = type;
        }

        public int getType() {
            return this.type;
        }

        public static MyGiftsOverviewDisplayType fromValue(int type) {
            for (MyGiftsOverviewDisplayType listType : values()) {
                if (listType.getType() == type) {
                    return listType;
                }
            }
            return SENDER;
        }
    }

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        mUserId = args.getString(FragmentUtils.PARAM_USERID);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        myGiftsOverviewData = new MyGiftsOverviewData<T>();
        updateData(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateData(true);
    }

    @Override
    protected BaseAdapter createAdapter() {
        mAdapter = new MyGiftsOverviewAdapter<T>();
        return mAdapter;
    }

    @Override
    protected View createHeaderView() {
        View header = LayoutInflater.from(getActivity()).inflate(R.layout.header_my_gifts_overview, null);

        RelativeLayout newGiftsContainer = (RelativeLayout) header.findViewById(R.id.new_gifts_container);
        mNewGiftsCount = (TextView) header.findViewById(R.id.new_gifts_count);
        TextView newGiftsLabel = (TextView) header.findViewById(R.id.new_gifts_label);

        newGiftsLabel.setText(I18n.tr("New gifts this week"));

        mCategorySpinner = (Spinner) header.findViewById(R.id.category_container);
        mPeriodSpinner = (Spinner) header.findViewById(R.id.period_container);

        mCategorySpinner.setSpinnerLabel(I18n.tr("Top 5 senders"));
        mPeriodSpinner.setSpinnerLabel(I18n.tr("This week"));

        mEmptyView = (LinearLayout) header.findViewById(R.id.empty_scenario_container);
        TextView label = (TextView) header.findViewById(R.id.label);
        TextView link = (TextView) header.findViewById(R.id.link);
        link.setMovementMethod(LinkMovementMethod.getInstance());

        label.setText(I18n.tr("Send someone a gift, you might get one back!"));
        link.setText(I18n.tr("Send a gift now."));

        mCategorySpinner.setOnClickListener(this);
        mPeriodSpinner.setOnClickListener(this);
        newGiftsContainer.setOnClickListener(this);
        link.setOnClickListener(this);

        return header;
    }

    protected void showOrHideEmptyViewIfNeeded() {
        if (myGiftsOverviewData.getListData() == null || myGiftsOverviewData.getListData().size() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            if (mEmptyView != null && mEmptyView.getParent() != null) {
                ((ViewGroup) mEmptyView.getParent()).removeView(mEmptyView);
            }
            mList.setVisibility(View.VISIBLE);
        }
    }

    private void updateData(final boolean shouldForceFetch) {
        updateNewGiftsStatsData(mPeriod, shouldForceFetch);
        setMyGiftsOverviewData(shouldForceFetch);
        showOrHideEmptyViewIfNeeded();
    }

    private void setMyGiftsOverviewData(boolean shouldForceFetch) {
        myGiftsOverviewData.setDisplayType(mDisplayType);

        if (mDisplayType == MyGiftsOverviewDisplayType.SENDER) {
            myGiftsOverviewData.setListData(updateSenderLeaderboardData(mPeriod, shouldForceFetch));
            mAdapter.setSenderListener(senderLeaderboardListener);
        } else if (mDisplayType == MyGiftsOverviewDisplayType.RECEIVED) {
            myGiftsOverviewData.setListData(updateGiftsLeaderboardData(mPeriod, shouldForceFetch));
            mAdapter.setReceivedListener(receivedLeaderboardListener);
        }

        mAdapter.setLeaderboardData(myGiftsOverviewData);
    }

    @SuppressWarnings("unchecked")
    private List<T> updateSenderLeaderboardData(StatisticsPeriod period, boolean forceFetch) {
        return (List<T>) GiftsDatastore.getInstance().getSenderLeaderboards(mUserId,
                TOP_LIST_LIMIT, period, forceFetch);
    }

    @SuppressWarnings("unchecked")
    private List<T> updateGiftsLeaderboardData(StatisticsPeriod period, boolean forceFetch) {
        return (List<T>) GiftsDatastore.getInstance().getReceivedLeaderboards(mUserId,
                TOP_LIST_LIMIT, period, forceFetch);
    }

    private void updateNewGiftsStatsData(StatisticsPeriod period, boolean forceFetch) {
        GiftCount giftCount = GiftsDatastore.getInstance().getNewGiftsStats(mUserId, period,
                forceFetch);
        if (giftCount != null) {
            mNewGiftsCount.setText(String.valueOf(giftCount.getResponse()));
        }
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.Gift.FETCH_NEW_GIFTS_COUNT_COMPLETED);
        registerEvent(Events.Gift.FETCH_GIFT_SENDER_LEADERBOARD_COMPLETED);
        registerEvent(Events.Gift.FETCH_GIFT_RECEIVED_LEADERBOARD_COMPLETED);
        registerEvent(Events.Emoticon.RECEIVED);
        registerEvent(Events.Profile.RECEIVED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(Events.Gift.FETCH_NEW_GIFTS_COUNT_COMPLETED)) {
            updateNewGiftsStatsData(mPeriod, false);
        } else if (action.equals(Events.Gift.FETCH_GIFT_SENDER_LEADERBOARD_COMPLETED)
                || action.equals(Events.Gift.FETCH_GIFT_RECEIVED_LEADERBOARD_COMPLETED)
                || action.equals(Events.Emoticon.RECEIVED) || action.equals(Events.Profile.RECEIVED)) {
            setMyGiftsOverviewData(false);
            showOrHideEmptyViewIfNeeded();
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        switch (viewId) {
            case R.id.new_gifts_container:
                ActionHandler.getInstance().displayMyGiftsCardListFragment(getActivity(),
                        I18n.tr("This month"), GiftsDatastore.Category.ALL.ordinal(), true, mUserId);
                break;
            case R.id.category_container:
                ActionHandler.getInstance().displayMyGiftsOverviewFilterFragment(getActivity(),
                        MyGiftsOverviewFilterType.CATEGORY, mCategoryId, this, mUserId);
                break;
            case R.id.period_container:
                ActionHandler.getInstance().displayMyGiftsOverviewFilterFragment(getActivity(),
                        MyGiftsOverviewFilterType.PERIOD, mPeriodId, this, mUserId);
                break;
            case R.id.link:
                ActionHandler.getInstance().displayStore(getActivity(), null);
                break;
        }
    }

    @Override
    public void onCategorySelected(int categoryId, String categoryName) {
        this.mCategoryId = categoryId;
        mCategorySpinner.setSpinnerLabel(categoryName);
        mDisplayType = MyGiftsOverviewDisplayType.fromValue(categoryId);
        setMyGiftsOverviewData(false);
    }

    @Override
    public void onPeriodSelected(int periodId, StatisticsPeriod period, String periodName) {
        this.mPeriodId = periodId;
        this.mPeriod = period;
        mPeriodSpinner.setSpinnerLabel(periodName);
        setMyGiftsOverviewData(false);
    }

    private BaseViewListener<GiftSenderLeaderboardItem> senderLeaderboardListener =
            new BaseViewListener<GiftSenderLeaderboardItem>() {

                @Override
                public void onItemClick(View v, GiftSenderLeaderboardItem data) {
                    ActionHandler.getInstance().displayStore(getActivity(), data.getSenderUserName());
                }

                @Override
                public void onItemLongClick(View v, GiftSenderLeaderboardItem data) {
                }
            };

    private BaseViewListener<GiftReceivedLeaderboardItem> receivedLeaderboardListener =
            new BaseViewListener<GiftReceivedLeaderboardItem>() {

                @Override
                public void onItemClick(View v, GiftReceivedLeaderboardItem data) {
                    ActionHandler.getInstance().displayGiftItem(getActivity(), String.valueOf(data.getStoreItemId()), null);
                }

                @Override
                public void onItemLongClick(View v, GiftReceivedLeaderboardItem data) {
                }
            };

}
