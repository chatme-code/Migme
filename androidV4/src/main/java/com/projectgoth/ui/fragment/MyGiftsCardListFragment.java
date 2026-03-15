package com.projectgoth.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.GiftCategoryItem;
import com.projectgoth.b.data.UserGiftListData;
import com.projectgoth.b.data.mime.GiftMimeData;
import com.projectgoth.common.Constants;
import com.projectgoth.common.ShareManager;
import com.projectgoth.common.TextUtils;
import com.projectgoth.datastore.GiftsDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.BaseCustomFragmentActivity;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.adapter.MyGiftsCardListAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.widget.Spinner;
import com.projectgoth.util.FragmentUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lopenny on 1/22/15.
 */
public class MyGiftsCardListFragment extends BaseListFragment implements View.OnClickListener,
        BaseViewListener<GiftMimeData>, MyGiftsCategoryFragment.CategoryListener {

    public static final String PARAM_TITLE = "PARAM_TITLE";
    public static final String PARAM_SHOW_FILTER = "PARAM_SHOW_FILTER";
    public static final String PARAM_CATEGORY = "PARAM_CATEGORY";

    private static final int GIFT_RECEIVED_LIMIT = 30;

    private MyGiftsCardListAdapter mGiftsCardAdapter;

    private String mCurrentTitle;
    private List<GiftMimeData> mGiftItems;
    private View mEmptyView;
    private Spinner mCategorySpinner;

    private boolean mIsFilterMode = true;
    private int mCategoryIdx = 0;
    private GiftsDatastore.Category mCategory;
    private String mUserId;

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        mCurrentTitle = args.getString(PARAM_TITLE);
        mIsFilterMode = args.getBoolean(PARAM_SHOW_FILTER, false);
        mCategory = GiftsDatastore.Category.fromValue(args.getInt(PARAM_CATEGORY));
        mUserId = args.getString(FragmentUtils.PARAM_USERID);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((BaseCustomFragmentActivity) this.getActivity()).setFragment(this);
        this.setPullToRefreshEnabled(false);

        updateListData();
    }

    @Override
    public void onRefresh() {
        refreshData(false);
    }

    @Override
    protected BaseAdapter createAdapter() {
        mGiftsCardAdapter = new MyGiftsCardListAdapter(getActivity());
        mGiftsCardAdapter.setShowActionButtons(Session.getInstance().getUserId().equals(mUserId));
        return mGiftsCardAdapter;
    }

    @Override
    protected void updateListData() {
        refreshData(false);
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.Gift.FETCH_GIFT_LIST_RECEIVED_COMPLETED);
        registerEvent(Events.Emoticon.RECEIVED);
        registerEvent(Events.Emoticon.FETCH_ALL_COMPLETED);
        registerEvent(Events.Profile.RECEIVED);
        registerEvent(Events.UserFavorite.FETCH_USER_FAVORITES_COMPLETED);
        registerEvent(Events.UserFavorite.SET_FAVORITE_GIFT_COMPLETED);
        registerEvent(Events.UserFavorite.REMOVE_FAVORITE_GIFT_COMPLETED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Events.Gift.FETCH_GIFT_LIST_RECEIVED_COMPLETED) ||
                action.equals(Events.UserFavorite.SET_FAVORITE_GIFT_COMPLETED) ||
                action.equals(Events.Emoticon.RECEIVED) ||
                action.equals(Events.Emoticon.FETCH_ALL_COMPLETED) ||
                action.equals(Events.Profile.RECEIVED) ||
                action.equals(Events.UserFavorite.FETCH_USER_FAVORITES_COMPLETED) ||
                action.equals(Events.UserFavorite.SET_FAVORITE_GIFT_COMPLETED) ||
                action.equals(Events.UserFavorite.REMOVE_FAVORITE_GIFT_COMPLETED)) {
            refreshData(false);
        }
    }

    private void refreshData(final boolean shouldForceFetch) {
        updateDataWithGiftList(shouldForceFetch);
    }

    private void updateDataWithGiftList(final boolean shouldForceFetch) {
        UserGiftListData giftListData = GiftsDatastore.getInstance().getGiftsReceivedList(mUserId,
                mCategory, Constants.BLANKSTR, Constants.BLANKSTR, GiftsDatastore.OrderType.DATE,
                GiftsDatastore.SortOrder.DESC, 0, GIFT_RECEIVED_LIMIT, shouldForceFetch);
        updateAdapterData(giftListData);
    }

    private void updateAdapterData(UserGiftListData data) {
        if (data != null) {
            mGiftsCardAdapter.setItemList(mGiftItems);
            mGiftsCardAdapter.setItemListListener(this);

            GiftMimeData[] giftMimeData = data.getResponse();
            if (giftMimeData != null) {
                mGiftItems = Arrays.asList(giftMimeData);
            }
        }

        showOrHideEmptyViewIfNeeded();
    }

    private void updateCategoryInfo(GiftCategoryItem category) {
        if (category != null && !TextUtils.isEmpty(category.getTitle())) {
            mCategorySpinner.setSpinnerLabel(GiftsDatastore.Category.fromType(category.getTitle()));
        }
    }

    protected void showOrHideEmptyViewIfNeeded() {
        if (mGiftsCardAdapter.getCount() == 0) {
            // show emptyView
            if (mEmptyView == null) {
                mEmptyView = createEmptyView();
            }
            // prevent it from being added multiple times
            hideEmptyView();

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            if (mIsFilterMode) {
                params.topMargin = ApplicationEx.getDimension(R.dimen.xxlarge_margin);
            }
            ((ViewGroup) mMainContainer).addView(mEmptyView, params);
            mEmptyView.setVisibility(View.VISIBLE);

        } else {
            // hide emptyView
            hideEmptyView();
            mList.setVisibility(View.VISIBLE);
        }
    }

    private void hideEmptyView() {
        if (mEmptyView != null && mEmptyView.getParent() != null) {
            ((ViewGroup) mEmptyView.getParent()).removeView(mEmptyView);
        }
    }

    @Override
    protected View createHeaderView() {
        if (mIsFilterMode) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View headerView = inflater.inflate(R.layout.header_my_gifts_card, null);
            RelativeLayout headerContainer = (RelativeLayout) headerView.findViewById(R.id.category_container);

            mCategorySpinner = (Spinner) headerView.findViewById(R.id.category_container);
            if (mCategory == GiftsDatastore.Category.ALL) {
                mCategorySpinner.setSpinnerLabel(I18n.tr("This month"));
            } else {
                mCategorySpinner.setSpinnerLabel(I18n.tr("Favorites"));
            }

            headerContainer.setOnClickListener(this);
            return headerView;
        }
        return null;
    }

    private View createEmptyView() {
        View emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_view_my_gifts, mList, false);
        ImageView icon = (ImageView) emptyView.findViewById(R.id.empty_list_icon);
        TextView label = (TextView) emptyView.findViewById(R.id.empty_list_label);
        TextView hint = (TextView) emptyView.findViewById(R.id.empty_list_hint);

        if (mCategory == GiftsDatastore.Category.ALL) {
            label.setText(I18n.tr("No gifts yet."));
            hint.setText(I18n.tr("Make the first move, send a gift now."));
            icon.setImageResource(R.drawable.ad_illustgift);
        } else if (mCategory == GiftsDatastore.Category.FAVORITE) {
            label.setText(I18n.tr("No favorites yet."));
            hint.setText(I18n.tr("Tip: Add a gift to favorites by tapping the heart under the gift."));
            icon.setImageResource(R.drawable.ad_illustfavourite);
        }

        return emptyView;
    }

    private void sentGiftBack(GiftMimeData data) {
        ActionHandler.getInstance().displayGiftItem(getActivity(),
                String.valueOf(data.getStoreItemId()), data.getSender());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.category_container:
                ActionHandler.getInstance().displayMyGiftsCategoryFragment(getActivity(), mCategoryIdx,
                        this, mUserId);
                break;
            default:
                //do nothing
                break;
        }
    }

    @Override
    public void onItemClick(View v, GiftMimeData data) {
        switch (v.getId()) {
            case R.id.favorite_icon:
                String giftId = String.valueOf(data.getMimeTypeId());
                if (!GiftsDatastore.getInstance().isFavoriteGift(data)) {
                    GiftsDatastore.getInstance().setFavoriteGift(giftId, mUserId);
                } else {
                    GiftsDatastore.getInstance().removeFavoriteGift(giftId, mUserId);
                }

                mGiftsCardAdapter.notifyDataSetChanged();
                refreshData(true);
                break;
            case R.id.share_icon:
                ShareManager.shareToPost(getActivity(), data);
                break;
            case R.id.giftback_icon:
                sentGiftBack(data);
                break;
            default:
                //do nothing
                break;
        }
    }

    @Override
    public void onItemLongClick(View v, GiftMimeData data) {
    }

    @Override
    public void onCategorySelected(int position, GiftCategoryItem data) {
        refreshData(false);
        updateCategoryInfo(data);
        mCategory = GiftsDatastore.Category.fromValue(position);
        updateDataWithGiftList(true);
        mCategoryIdx = position;
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = new CustomActionBarConfig();
        config.setNavigationButtonState(CustomActionBarConfig.NavigationButtonState.BACK);
        return config;
    }

    @Override
    protected String getTitle() {
        return I18n.tr(mCurrentTitle);
    }

    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_gift_white;
    }
}
