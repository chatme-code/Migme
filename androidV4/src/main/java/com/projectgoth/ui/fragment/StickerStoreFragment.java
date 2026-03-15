/**
 * Copyright (c) 2013 Project Goth
 *
 * StickerStoreFragment.java
 * Created Dec 8, 2014, 11:24:05 AM
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
import com.projectgoth.b.data.StoreItems;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.StoreController;
import com.projectgoth.controller.StoreController.StoreItemFilterType;
import com.projectgoth.datastore.Session;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.StickerStoreItem;
import com.projectgoth.model.StorePagerItem.StorePagerType;
import com.projectgoth.model.StoreSortType;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.adapter.StickerStoreListAdapter;
import com.projectgoth.ui.fragment.StoreFilterFragment.StoreFilterType;
import com.projectgoth.ui.fragment.StoreFilterFragment.StoreSortingListener;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;

/**
 * @author mapet
 * 
 */
public class StickerStoreFragment extends BaseListFragment implements BaseViewListener<StickerStoreItem>,
        OnClickListener, StoreSortingListener {

    private StickerStoreListAdapter storeItemListAdapter;

    private StickerStoreItem[]      stickerStoreItems;
    private StoreItems              storeItems;
    private int                     storeType                = StorePagerType.STICKERS.getValue();
    private StoreItemFilterType     filterType               = StoreController.StoreItemFilterType.NEW;
    private String                  selectedFilterTypeString = Constants.BLANKSTR;

    private TextView                categoryLabel;
    private TextView                sortByLabel;

    private int                     categoryId               = StoreController.newCategoryId;
    private String                  categorySorting          = StoreController.SORT_BY_NAME;
    private String                  categoryOrderBy          = StoreController.SORT_ORDER_ASC;
    private String                  categoryFeatured         = StoreController.NOT_FEATURED;

    private static final int        LOAD_MORE_INCREMENT      = 15;
    private int                     currentLoadMoreLimit     = LOAD_MORE_INCREMENT;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentLoadMoreLimit = LOAD_MORE_INCREMENT;
        initDataCache();
        refreshData();
    }

    private void initDataCache() {
        // New items are displayed by default
        storeItems = StoreController.getInstance().getMainCategories(storeType, currentLoadMoreLimit, 0,
                StoreItemFilterType.NEW);
    }

    @Override
    protected BaseAdapter createAdapter() {
        storeItemListAdapter = new StickerStoreListAdapter();

        if (stickerStoreItems != null && stickerStoreItems.length > 0) {
            storeItemListAdapter.setStoreItemList(stickerStoreItems);
            storeItemListAdapter.setStoreItemListListener(this);
        }

        return storeItemListAdapter;
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
        registerEvent(Events.Sticker.PACK_RECEIVED);
        registerEvent(Events.Sticker.FETCH_PACKS_COMPLETED);
        registerEvent(Events.Sticker.FETCH_PACK_ERROR);
        registerEvent(Events.MigStore.Item.PURCHASED);
        registerEvent(Events.MigStore.Item.PURCHASE_ERROR);
        registerEvent(Events.MigStore.Item.BEGIN_PURCHASE_STORE_ITEM);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Bundle data = intent.getExtras();

        if (action.equals(Events.MigStore.Item.BEGIN_FETCH_STORE_ITEMS)) {
            if (!isShowingLoadingMore()) {
                showLoadProgressDialog();
            }
        } else if (action.equals(Events.Sticker.PACK_RECEIVED) || action.equals(Events.Sticker.FETCH_PACKS_COMPLETED)
                || action.equals(Events.Sticker.FETCH_PACK_ERROR) || action.equals(Events.Emoticon.RECEIVED)
                || action.equals(Events.Emoticon.FETCH_ALL_COMPLETED)
                || action.equals(Events.MigStore.Item.BEGIN_PURCHASE_STORE_ITEM)) {
            refreshData();
        } else if (action.equals(Events.MigStore.Item.PURCHASED)) {
            refreshData();
            int packId = data.getInt(Events.MigStore.Extra.ITEM_ID);
            updateStickerStoreItem(packId);
        } else if (action.equals(Events.MigStore.Item.PURCHASE_ERROR)) {
            Tools.showToastForIntent(context, intent);
        } else {
            refreshData();
            setRefreshDone();
        }
    }

    // - TODO: Workaround to update the status of the pack after purchase
    // - Remove after server sends the correct value of owned field.
    private void updateStickerStoreItem(int packId) {
        boolean isUpdated = StoreController.getInstance().updateStickerStoreItem(stickerStoreItems, packId);
        if (isUpdated) {
            refreshData();
        }
    }

    private void fetchStickerPacks() {
        if (storeItems != null) {
            stickerStoreItems = StoreController.getInstance().fetchStickerPacks(storeItems.getListData());
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
        if (currentLoadMoreLimit <= storeItemListAdapter.getListCount()) {
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
                storeItems = getStickersByFilter(filterType);
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

        fetchStickerPacks();

        if (stickerStoreItems != null && stickerStoreItems.length > 0) {
            storeItemListAdapter.setStoreItemList(stickerStoreItems);
            storeItemListAdapter.setStoreItemListListener(this);
        }
    }

    protected void setRefreshDone() {
        setPullToRefreshComplete();
        hideLoadingMore();
        hideLoadProgressDialog();
    }

    private StoreItems getStickersByFilter(StoreItemFilterType filterType) {
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
    protected View createHeaderView() {
        View header = LayoutInflater.from(getActivity()).inflate(R.layout.header_sticker_store, null);

        RelativeLayout myStickersContainer = (RelativeLayout) header.findViewById(R.id.my_stickers_container);
        RelativeLayout categoryContainer = (RelativeLayout) header.findViewById(R.id.category_container);
        RelativeLayout sortByContainer = (RelativeLayout) header.findViewById(R.id.sortby_container);

        myStickersContainer.setOnClickListener(this);
        categoryContainer.setOnClickListener(this);
        sortByContainer.setOnClickListener(this);

        TextView myStickersLabel = (TextView) header.findViewById(R.id.label);
        categoryLabel = (TextView) header.findViewById(R.id.category_label);
        sortByLabel = (TextView) header.findViewById(R.id.sortby_label);

        myStickersLabel.setText(I18n.tr("My stickers"));
        categoryLabel.setText(I18n.tr("Category"));
        sortByLabel.setText(I18n.tr("Sort by"));

        header.setOnClickListener(this);
        return header;
    }

    @Override
    protected View createFooterView() {
        return createLoadingView();
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
    public void onSortingSelected(StoreSortType storeSortType, String sortingName) {
        if (categoryId == 0) {
            switch (storeSortType) {
                case POPULARITY:
                    GAEvent.Store_StickerSortPopularity.send();
                    filterType = StoreItemFilterType.POPULAR;
                    selectedFilterTypeString = StoreController.sortByStringArr[0];
                    break;
                case PRICE_ASC:
                    GAEvent.Store_StickerSortLowToHigh.send();
                    filterType = StoreItemFilterType.PRICE_ASC;
                    selectedFilterTypeString = StoreController.sortByStringArr[1];
                    break;
                case PRICE_DESC:
                    GAEvent.Store_StickerSortHighToLow.send();
                    filterType = StoreItemFilterType.PRICE_DESC;
                    selectedFilterTypeString = StoreController.sortByStringArr[2];
                    break;
                case NAME_ASC:
                    GAEvent.Store_StickerSortAToZ.send();
                    filterType = StoreItemFilterType.NAME_ASC;
                    selectedFilterTypeString = StoreController.sortByStringArr[3];
                    break;
                case NAME_DESC:
                    GAEvent.Store_StickerSortZToA.send();
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

            switch (storeSortType) {
                case POPULARITY:
                    GAEvent.Store_StickerSortPopularity.send();
                    categorySorting = StoreController.SORT_BY_NUMSOLD;
                    categoryOrderBy = StoreController.SORT_ORDER_DESC;
                    selectedFilterTypeString = StoreController.sortByStringArr[0];
                    break;
                case PRICE_ASC:
                    GAEvent.Store_StickerSortLowToHigh.send();
                    categorySorting = StoreController.SORT_BY_PRICE;
                    categoryOrderBy = StoreController.SORT_ORDER_ASC;
                    selectedFilterTypeString = StoreController.sortByStringArr[1];
                    break;
                case PRICE_DESC:
                    GAEvent.Store_StickerSortHighToLow.send();
                    categorySorting = StoreController.SORT_BY_PRICE;
                    categoryOrderBy = StoreController.SORT_ORDER_DESC;
                    selectedFilterTypeString = StoreController.sortByStringArr[2];
                    break;
                case NAME_ASC:
                    GAEvent.Store_StickerSortAToZ.send();
                    categorySorting = StoreController.SORT_BY_NAME;
                    categoryOrderBy = StoreController.SORT_ORDER_ASC;
                    selectedFilterTypeString = StoreController.sortByStringArr[3];
                    break;
                case NAME_DESC:
                    GAEvent.Store_StickerSortZToA.send();
                    categorySorting = StoreController.SORT_BY_NAME;
                    categoryOrderBy = StoreController.SORT_ORDER_DESC;
                    selectedFilterTypeString = StoreController.sortByStringArr[4];
                    break;
            }
        }

        refreshData();
        sortByLabel.setText(sortingName);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.my_stickers_container:
                GAEvent.Store_ClickMyStickerPage.send();
                ActionHandler.getInstance().displayMyStickers(getActivity());
                break;
            case R.id.category_container:
                GAEvent.Store_StickerCategoryList.send();
                ActionHandler.getInstance().displayStoreFilterFragment(getActivity(), StoreFilterType.CATEGORY,
                        storeType, this, categoryId, selectedFilterTypeString);
                break;
            case R.id.sortby_container:
                GAEvent.Store_StickerSortList.send();
                ActionHandler.getInstance().displayStoreFilterFragment(getActivity(), StoreFilterType.SORT_BY,
                        storeType, this, categoryId, selectedFilterTypeString);
                break;
        }
    }

    @Override
    public void onItemClick(View v, StickerStoreItem data) {
        final int viewId = v.getId();
        switch (viewId) {
            case R.id.option_button:
                GAEvent.Store_PurchaseSticker.send();
                if (!StoreController.getInstance().isStickerPackPurchaseInProcess(
                        String.valueOf(data.getStoreItem().getId()))) {
                    if (StoreController.getInstance().canPurchaseItem(data.getStoreItem().getPrice())) {
                        ActionHandler.getInstance().showStickerPurchaseConfirmDlg(getActivity(), data.getStoreItem());
                    } else {
                        ActionHandler.getInstance().displayAccountBalance(getActivity());
                    }
                }
                break;
            default:
                ActionHandler.getInstance().displayStickerPackDetails(getActivity(), data.getStoreItem().getId(),
                        data.getStoreItem().getReferenceID());
                break;
        }
    }

    @Override
    public void onItemLongClick(View v, StickerStoreItem data) {
    }

}
