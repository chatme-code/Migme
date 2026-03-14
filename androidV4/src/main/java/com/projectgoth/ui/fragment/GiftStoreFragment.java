/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftListFragment.java
 * Created Nov 25, 2013, 3:06:05 PM
 */

package com.projectgoth.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.b.data.StoreItem;
import com.projectgoth.b.data.StoreItems;
import com.projectgoth.b.data.StoreUnlockedItem;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.StoreController;
import com.projectgoth.controller.StoreController.StoreItemFilterType;
import com.projectgoth.datastore.Session;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.StorePagerItem.StorePagerType;
import com.projectgoth.model.StoreSortType;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.adapter.GiftStoreListAdapter;
import com.projectgoth.ui.fragment.StoreFilterFragment.StoreFilterType;
import com.projectgoth.ui.fragment.StoreFilterFragment.StoreSortingListener;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;

/**
 * @author mapet
 * 
 */
public class GiftStoreFragment extends BaseListFragment implements BaseViewListener<StoreItem>, OnClickListener,
        StoreSortingListener {

    private GiftStoreListAdapter giftListAdapter;
    private StoreItems           storeItems;
    private int                  storeType                = StorePagerType.GIFTS.getValue();
    private StoreItemFilterType  filterType               = StoreItemFilterType.NEW;
    private String               selectedFilterTypeString = Constants.BLANKSTR;

    private TextView             categoryLabel;
    private TextView             sortByLabel;

    private StoreUnlockedItem[]  storeUnlockedItemArr;
    private TextView             headerLabel;
    private int                  unlockedItemCount = 0;

    private int                  categoryId               = StoreController.newCategoryId;

    private String               categorySorting          = StoreController.SORT_BY_NAME;
    private String               categoryOrderBy          = StoreController.SORT_ORDER_ASC;
    private String               categoryFeatured         = StoreController.NOT_FEATURED;

    private static final int     LOAD_MORE_INCREMENT      = 15;
    private int                  currentLoadMoreLimit     = LOAD_MORE_INCREMENT;

    private String               mInitialRecipient;
    public static final String   PARAM_INITIAL_RECIPIENT  = "PARAM_INITIAL_RECIPIENT";

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        mInitialRecipient = args.getString(PARAM_INITIAL_RECIPIENT);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentLoadMoreLimit = LOAD_MORE_INCREMENT;
        initDataCache();
        refreshData();
    }

    private void initDataCache() {
        // New gifts are displayed by default
        storeItems = StoreController.getInstance().getMainCategories(storeType, currentLoadMoreLimit, 0,
                StoreItemFilterType.NEW);
    }

    @Override
    protected BaseAdapter createAdapter() {
        giftListAdapter = new GiftStoreListAdapter();

        if (storeItems != null && storeItems.getListData().length > 0) {
            giftListAdapter.setStoreItemList(storeItems.getListData());
            giftListAdapter.setStoreItemListListener(this);
        }

        return giftListAdapter;
    }

    @Override
    protected View createHeaderView() {
        View header = LayoutInflater.from(getActivity()).inflate(R.layout.header_gift_store, null);

        RelativeLayout unlockedGiftsContainer = (RelativeLayout) header.findViewById(R.id.unlocked_gifts_container);
        headerLabel = (TextView) header.findViewById(R.id.label);
        
        refreshUnlockedLabel();
        
        unlockedGiftsContainer.setOnClickListener(this);

        RelativeLayout categoryContainer = (RelativeLayout) header.findViewById(R.id.category_container);
        RelativeLayout sortByContainer = (RelativeLayout) header.findViewById(R.id.sortby_container);

        categoryContainer.setOnClickListener(this);
        sortByContainer.setOnClickListener(this);

        categoryLabel = (TextView) header.findViewById(R.id.category_label);
        sortByLabel = (TextView) header.findViewById(R.id.sortby_label);

        categoryLabel.setText(I18n.tr("Category"));
        sortByLabel.setText(I18n.tr("Sort by"));

        header.setOnClickListener(this);
        return header;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUnlockedLabel();
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.MigStore.Item.BEGIN_FETCH_STORE_ITEMS);
        registerEvent(Events.MigStore.Item.FETCH_FOR_POPULAR_COMPLETED);
        registerEvent(Events.MigStore.Item.FETCH_FOR_NEW_COMPLETED);
        registerEvent(Events.MigStore.Item.FETCH_FOR_FEATURED_COMPLETED);
        registerEvent(Events.MigStore.Item.FETCH_FOR_SUBCATEGORY_COMPLETED);
        registerEvent(Events.MigStore.Item.FETCH_FOR_SEARCH_COMPLETED);
        registerEvent(Events.MigStore.SubCategory.FETCH_ALL_COMPLETED);
        registerEvent(Events.Emoticon.RECEIVED);
        registerEvent(Events.Emoticon.FETCH_ALL_COMPLETED);
        registerEvent(Events.MigStore.Item.FETCH_UNLOCKED_COMPLETED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Events.MigStore.Item.BEGIN_FETCH_STORE_ITEMS)) {
            if (!isShowingLoadingMore()) {
                showLoadProgressDialog();
            }
        } else if (action.equals(Events.Emoticon.FETCH_ALL_COMPLETED)
                || action.equals(Events.Emoticon.RECEIVED)) {
            refreshData();
        
        } else if (action.equals(Events.MigStore.Item.FETCH_UNLOCKED_COMPLETED)) {
            refreshData();
            refreshUnlockedLabel();
            
        } else {
            refreshData();
            setRefreshDone();
        }
    }

    @Override
    public void onRefresh() {
        if (Session.getInstance().isNetworkConnected()) {
            currentLoadMoreLimit = LOAD_MORE_INCREMENT;
            refreshData();
        } else {
            refreshData();
            setRefreshDone();
        }
    }

    @Override
    protected void onListEndReached() {
        super.onListEndReached();
        if (currentLoadMoreLimit <= giftListAdapter.getCount()) {
            showLoadingMore();
            currentLoadMoreLimit += LOAD_MORE_INCREMENT;
        }
        refreshData();
    }

    private void refreshData() {
        switch (filterType) {
            case POPULAR:
            case FEATURED:
            case NEW:
            case NAME_ASC:
            case NAME_DESC:
            case PRICE_ASC:
            case PRICE_DESC:
                storeItems = getGiftsByFilter(filterType);
                break;
            case CATEGORY:
                storeItems = StoreController.getInstance().getStoreCategory(Integer.toString(categoryId),
                        categorySorting, categoryOrderBy, StoreController.NOT_FEATURED, currentLoadMoreLimit, 0);
                break;
            case GENERAL:
                storeItems = StoreController.getInstance().searchStoreItems(storeType, Constants.BLANKSTR,
                        StoreController.DEFAULT_MIN_PRICE, StoreController.DEFAULT_MAX_PRICE, categorySorting,
                        categoryOrderBy, categoryFeatured, currentLoadMoreLimit, 0, null);
                break;
            default:
                break;
        }

        if (giftListAdapter != null && storeItems != null) {
            storeItems.filterListToLevel(Session.getInstance().getMigLevel());
            giftListAdapter.setStoreItemList(storeItems.getListData());
            giftListAdapter.setStoreItemListListener(this);
        }
    }
    
    private void refreshUnlockedLabel() {
        storeUnlockedItemArr = StoreController.getInstance().getUnlockedGifts(Session.getInstance().getUsername(),
                false);
        if (storeUnlockedItemArr != null) {
            unlockedItemCount = storeUnlockedItemArr.length;
        }
        headerLabel.setText(String.format(I18n.tr("My unlocked gifts (%d)"), unlockedItemCount));
    }

    protected void setRefreshDone() {
        setPullToRefreshComplete();
        hideLoadingMore();
        hideLoadProgressDialog();
    }

    private StoreItems getGiftsByFilter(StoreItemFilterType filterType) {
        StoreItems storeItems = StoreController.getInstance().getMainCategories(storeType, currentLoadMoreLimit, 0,
                filterType);

        if (storeItems != null && storeItems.getListData().length > 0) {
            storeItems.filterListToLevel(Session.getInstance().getMigLevel());
        }

        return storeItems;
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = new CustomActionBarConfig();
        config.setNavigationButtonState(NavigationButtonState.BACK);
        return config;
    }

    @Override
    protected View createFooterView() {
        return createLoadingView();
    }

    @Override
    public void onItemClick(View v, StoreItem data) {

        GAEvent.Store_SendGift.send();

        if (!data.isAvailableForLevel(Session.getInstance().getMigLevel())) {
            Tools.showToast(getActivity(),
                    String.format(I18n.tr("Unlock this gift when you get to level %s!"), data.getMigLevelMin()));
        } else if (data.isGroupOnly()) {
            Tools.showToast(getActivity(),
                    String.format(I18n.tr("Unlock this gift by joining the %s group!"), data.getGroupName()));
        } else {
            ActionHandler.getInstance().displayGiftItem(getActivity(), data.getId().toString(), mInitialRecipient);
        }
    }

    @Override
    public void onItemLongClick(View v, StoreItem data) {
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.category_container:
                GAEvent.Store_GiftCategoryList.send();
                ActionHandler.getInstance().displayStoreFilterFragment(getActivity(), StoreFilterType.CATEGORY,
                        storeType, this, categoryId, selectedFilterTypeString);
                break;
            case R.id.sortby_container:
                GAEvent.Store_GiftSortList.send();
                ActionHandler.getInstance().displayStoreFilterFragment(getActivity(), StoreFilterType.SORT_BY,
                        storeType, this, categoryId, selectedFilterTypeString);
                break;
            case R.id.unlocked_gifts_container:
                GAEvent.Store_ClickUnlockGiftPage.send();
                ActionHandler.getInstance().displayUnlockedGiftListFragment(getActivity());
                break;
        }
    }

    @Override
    public void onCategorySelected(int categoryId, String categoryName) {
        this.categoryId = categoryId;
        currentLoadMoreLimit = LOAD_MORE_INCREMENT;

        if (categoryId == StoreController.newCategoryId) {
            filterType = StoreItemFilterType.NEW;
        } else if (categoryId == StoreController.featuredCategoryId) {
            filterType = StoreItemFilterType.FEATURED;
        } else if (categoryId == StoreController.allCategoryId) {
            filterType = StoreItemFilterType.POPULAR;
        } else {
            filterType = StoreItemFilterType.CATEGORY;
        }

        refreshData();
        categoryLabel.setText(categoryName);

    }

    @Override
    public void onSortingSelected(StoreSortType selectedItem, String sortingName) {
        if (categoryId == 0) {
            switch (selectedItem) {
                case POPULARITY:
                    GAEvent.Store_GiftSortPopularity.send();
                    filterType = StoreItemFilterType.POPULAR;
                    selectedFilterTypeString = StoreController.sortByStringArr[0];
                    break;
                case PRICE_ASC:
                    GAEvent.Store_GiftSortLowToHigh.send();
                    filterType = StoreItemFilterType.PRICE_ASC;
                    selectedFilterTypeString = StoreController.sortByStringArr[1];
                    break;
                case PRICE_DESC:
                    GAEvent.Store_GiftSortHighToLow.send();
                    filterType = StoreItemFilterType.PRICE_DESC;
                    selectedFilterTypeString = StoreController.sortByStringArr[2];
                    break;
                case NAME_ASC:
                    GAEvent.Store_GiftSortAToZ.send();
                    filterType = StoreItemFilterType.NAME_ASC;
                    selectedFilterTypeString = StoreController.sortByStringArr[3];
                    break;
                case NAME_DESC:
                    GAEvent.Store_GiftSortZToA.send();
                    filterType = StoreItemFilterType.NAME_DESC;
                    selectedFilterTypeString = StoreController.sortByStringArr[4];
                    break;
            }

        } else {

            if (categoryId == StoreController.newCategoryId || categoryId == StoreController.allCategoryId) {
                filterType = StoreItemFilterType.GENERAL;
                categoryFeatured = StoreController.NOT_FEATURED;
            } else if (categoryId == StoreController.featuredCategoryId) {
                filterType = StoreItemFilterType.GENERAL;
                categoryFeatured = StoreController.FEATURED;
            } else {
                filterType = StoreItemFilterType.CATEGORY;
                categoryFeatured = StoreController.NOT_FEATURED;
            }

            switch (selectedItem) {
                case POPULARITY:
                    GAEvent.Store_GiftSortPopularity.send();
                    categorySorting = StoreController.SORT_BY_NUMSOLD;
                    categoryOrderBy = StoreController.SORT_ORDER_DESC;
                    selectedFilterTypeString = StoreController.sortByStringArr[0];
                    break;
                case PRICE_ASC:
                    GAEvent.Store_GiftSortLowToHigh.send();
                    categorySorting = StoreController.SORT_BY_PRICE;
                    categoryOrderBy = StoreController.SORT_ORDER_ASC;
                    selectedFilterTypeString = StoreController.sortByStringArr[1];
                    break;
                case PRICE_DESC:
                    GAEvent.Store_GiftSortHighToLow.send();
                    categorySorting = StoreController.SORT_BY_PRICE;
                    categoryOrderBy = StoreController.SORT_ORDER_DESC;
                    selectedFilterTypeString = StoreController.sortByStringArr[2];
                    break;
                case NAME_ASC:
                    GAEvent.Store_GiftSortAToZ.send();
                    categorySorting = StoreController.SORT_BY_NAME;
                    categoryOrderBy = StoreController.SORT_ORDER_ASC;
                    selectedFilterTypeString = StoreController.sortByStringArr[3];
                    break;
                case NAME_DESC:
                    GAEvent.Store_GiftSortZToA.send();
                    categorySorting = StoreController.SORT_BY_NAME;
                    categoryOrderBy = StoreController.SORT_ORDER_DESC;
                    selectedFilterTypeString = StoreController.sortByStringArr[4];
                    break;
            }
        }

        refreshData();
        sortByLabel.setText(sortingName);

    }

}
