package com.projectgoth.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.UserGiftListData;
import com.projectgoth.b.data.UserGiftStat;
import com.projectgoth.b.data.mime.GiftMimeData;
import com.projectgoth.common.Constants;
import com.projectgoth.datastore.GiftsDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.UIUtils;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.adapter.MyGiftsAllListAdapter;
import com.projectgoth.ui.adapter.MyGiftsFavoriteListAdapter;
import com.projectgoth.ui.holder.BaseViewHolder;
import com.projectgoth.ui.widget.HorizontalListViewEx;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lopenny on 1/22/15.
 */
public class MyGiftsListFragment extends BaseListFragment implements View.OnClickListener,
        HorizontalListViewEx.OnItemClickListener,
        BaseViewHolder.BaseViewPositionListener<UserGiftStat> {

    public final static int DISPLAY_FAVORITE_COUNT = 3;
    public final static int DISPLAY_ALL_COUNT = 10;

    private RelativeLayout mFavoriteHeader, mAllHeader;
    private TextView mFavoriteTitleText;
    private TextView mAllTitleText;
    private MyGiftsFavoriteListAdapter mFavoriteGiftsListAdapter;
    private HorizontalListViewEx mFavoriteList;

    private MyGiftsAllListAdapter mGiftsAdapter;

    private String mUserId;
    private GiftsDatastore mGiftsDatastore = GiftsDatastore.getInstance();

    private List<GiftMimeData> mFavoriteGiftItems;
    private List<GiftMimeData> mAllGiftItems;


    @Override
    protected BaseAdapter createAdapter() {
        mGiftsAdapter = new MyGiftsAllListAdapter(getActivity());
        return mGiftsAdapter;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setPullToRefreshEnabled(false);
        int margin = ApplicationEx.getDimension(R.dimen.normal_margin);
        UIUtils.setMargins(mList, margin, 0, margin, margin);

        mUserId = Session.getInstance().getUserId();
        retrieveData();
    }

    @Override
    public void onResume() {
        super.onResume();
        retrieveData();
    }

    @Override
    protected View createHeaderView() {
        View headerView;

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        headerView = inflater.inflate(R.layout.header_my_gifts_list, null);
        mFavoriteTitleText = (TextView) headerView.findViewById(R.id.favorite_title);
        mFavoriteHeader = (RelativeLayout) headerView.findViewById(R.id.favorite_gifts_header_container);
        mFavoriteHeader.setOnClickListener(this);

        mFavoriteList = (HorizontalListViewEx) headerView.findViewById(R.id.favorite_list);
        mFavoriteList.setOnItemClickListener(this);
        mFavoriteList.setScrollContainer(false);

        setupFavoriteContainer();

        mAllTitleText = (TextView) headerView.findViewById(R.id.all_title);
        mAllTitleText.setText(I18n.tr("This month"));
        mAllHeader = (RelativeLayout) headerView.findViewById(R.id.all_gifts_header_container);
        mAllHeader.setOnClickListener(this);

        return headerView;
    }

    private void retrieveData() {
        refreshAllGiftsData(true);
        refreshFavoriteGiftsData(true);
        GiftsDatastore.getInstance().getUserGiftCategories(mUserId, false);
        GiftsDatastore.getInstance().getFavoriteGiftData(mUserId);
    }

    private void refreshAllGiftsData(final boolean forceFetch) {
        UserGiftListData allGiftListData = mGiftsDatastore.getGiftsReceivedList(mUserId,
                GiftsDatastore.Category.ALL, Constants.BLANKSTR, Constants.BLANKSTR,
                GiftsDatastore.OrderType.DATE, GiftsDatastore.SortOrder.DESC, 0, DISPLAY_ALL_COUNT, forceFetch);
        if (allGiftListData != null && allGiftListData.getResponse() != null) {
            GiftMimeData[] giftMimeData = allGiftListData.getResponse();
            mAllGiftItems = Arrays.asList(giftMimeData);
            mGiftsAdapter.setItemList(mAllGiftItems);
        }
    }

    private void refreshFavoriteGiftsData(final boolean forceFetch) {
        UserGiftListData favoriteGiftListData = mGiftsDatastore.getGiftsReceivedList(mUserId,
                GiftsDatastore.Category.FAVORITE, Constants.BLANKSTR, Constants.BLANKSTR,
                GiftsDatastore.OrderType.DATE, GiftsDatastore.SortOrder.DESC, 0, DISPLAY_FAVORITE_COUNT, forceFetch);
        if (favoriteGiftListData != null && favoriteGiftListData.getResponse() != null) {
            GiftMimeData[] giftMimeData = favoriteGiftListData.getResponse();
            mFavoriteGiftItems = Arrays.asList(giftMimeData);
        }
    }

    private void setupFavoriteContainer() {
        if (mFavoriteGiftsListAdapter == null) {
            mFavoriteTitleText.setText(I18n.tr("Favorites"));
            mFavoriteList.setDisplayMaxCount(DISPLAY_FAVORITE_COUNT);
            mFavoriteGiftsListAdapter = new MyGiftsFavoriteListAdapter(getActivity());
            updateFavoriteContainer();
        }
        mFavoriteGiftsListAdapter.setItemList(mFavoriteGiftItems);
        mFavoriteList.setAdapter(mFavoriteGiftsListAdapter);
    }

    private void updateFavoriteContainer() {
        int height = 0;
        if (mFavoriteGiftItems != null && mFavoriteGiftItems.size() > 0) {
            height = ApplicationEx.getDimension(R.dimen.favorite_gifts_cell_height);
        }
        HorizontalScrollView.LayoutParams lp = new HorizontalScrollView.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, height);
        mFavoriteList.setItemLayoutParams(lp);
        mFavoriteList.setDisplayMaxCount(DISPLAY_FAVORITE_COUNT);
        mFavoriteGiftsListAdapter = new MyGiftsFavoriteListAdapter(getActivity());
        mFavoriteGiftsListAdapter.setItemList(mFavoriteGiftItems);
        mFavoriteList.setAdapter(mFavoriteGiftsListAdapter);
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.Gift.FETCH_GIFT_LIST_RECEIVED_COMPLETED);
        registerEvent(Events.Emoticon.RECEIVED);
        registerEvent(Events.Emoticon.FETCH_ALL_COMPLETED);
        registerEvent(Events.Profile.RECEIVED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(Events.Gift.FETCH_GIFT_LIST_RECEIVED_COMPLETED) ||
                action.equals(Events.Emoticon.RECEIVED) ||
                action.equals(Events.Emoticon.FETCH_ALL_COMPLETED) ||
                action.equals(Events.Profile.RECEIVED)) {
            refreshAllGiftsData(false);
            refreshFavoriteGiftsData(false);
            updateFavoriteContainer();
        }
    }

    private void displayFavoriteCardList() {
        ActionHandler.getInstance().displayMyGiftsCardListFragment(getActivity(), I18n.tr("Favorites"),
                GiftsDatastore.Category.FAVORITE.ordinal(), false, mUserId);
    }

    private void displayAllCardList() {
        ActionHandler.getInstance().displayMyGiftsCardListFragment(getActivity(), I18n.tr("This month"),
                GiftsDatastore.Category.ALL.ordinal(), true, mUserId);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.favorite_gifts_header_container:
                displayFavoriteCardList();
                break;
            case R.id.all_gifts_header_container:
                displayAllCardList();
                break;
            default:
                //do nothing
        }
    }

    @Override
    public void onItemClicked(HorizontalListViewEx adapterView, View view, int position, long id) {
        displayFavoriteCardList();
    }

    @Override
    public void onItemClick(View v, int groupPosition, UserGiftStat data) {
    }
}
