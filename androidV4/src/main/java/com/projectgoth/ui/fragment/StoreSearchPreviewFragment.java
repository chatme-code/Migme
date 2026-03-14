package com.projectgoth.ui.fragment;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.b.data.StoreItem;
import com.projectgoth.b.data.StoreItems;
import com.projectgoth.b.enums.StoreItemTypeEnum;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.StoreController;
import com.projectgoth.datastore.Session;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.StickerStoreItem;
import com.projectgoth.model.StorePagerItem;
import com.projectgoth.model.StoreSearchCategory;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.adapter.StoreSearchAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;

/**
 * Created by houdangui on 8/12/14.
 */
public class StoreSearchPreviewFragment extends BaseSearchFragment implements View.OnClickListener, BaseViewListener<StoreItem> , StoreSearchAdapter.FooterClickListener {

    private StoreSearchAdapter searchResultAdapter;
    private ExpandableListView searchResultListView;

    private String currentSearchString;
    private int limit = 3;
    private int offset = 0;
    private float searchMinPrice = -1.0f;
    private float searchMaxPrice = -1.0f;

    private ArrayList<StoreSearchCategory> searchResults = new ArrayList<StoreSearchCategory>();

    private String initialRecipient;
    //type of store pager from which the fragment is started. It will be the first category to display
    private StorePagerItem.StorePagerType storeType;

    public static final String PARAM_INITIAL_RECIPIENT = "PARAM_INITIAL_RECIPIENT";
    public static final String STORE_ITEM_TYPE = "STORE_ITEM_TYPE";

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_store_search;
    }

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        int typeValue = args.getInt(STORE_ITEM_TYPE);
        storeType = StorePagerItem.StorePagerType.fromValue(typeValue);
        initialRecipient = args.getString(PARAM_INITIAL_RECIPIENT);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initSearchCategories();

        searchResultListView = (ExpandableListView)view.findViewById(R.id.search_results);
        searchResultAdapter = createAdapter();
        searchResultListView.setAdapter(searchResultAdapter);
        searchResultListView.setGroupIndicator(null);
        currentSearchString = getFilterText();
        checkAndPerformGlobalSearch(currentSearchString);

    }

    private void initSearchCategories() {
        searchResults = new ArrayList<StoreSearchCategory>();

        searchResults.add(new StoreSearchCategory<StoreItem>(I18n.tr("Gifts"), StorePagerItem.StorePagerType.GIFTS));
        searchResults.add(new StoreSearchCategory<StickerStoreItem>(I18n.tr("Stickers"), StorePagerItem.StorePagerType.STICKERS));

        if (storeType != null) {
            for (StoreSearchCategory category : searchResults) {
                if (category.getType().getValue() == storeType.getValue()) {
                    //move the category to the first item
                    searchResults.remove(category);
                    searchResults.add(0, category);
                    break;
                }
            }
        }
    }

    @Override
    protected void performGlobalSearch(final String searchString) {
        super.performGlobalSearch(searchString);
        currentSearchString = searchString;
        fetchDataForSearchParam(searchString);

        focusSearchBox(false);

        Tools.showToast(getActivity(), I18n.tr("Searching"));
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.MigStore.Item.FETCH_FOR_SEARCH_COMPLETED);
        registerEvent(Events.Emoticon.RECEIVED);
        registerEvent(Events.Sticker.PACK_RECEIVED);
        registerEvent(Events.Sticker.FETCH_PACKS_COMPLETED);
        registerEvent(Events.Sticker.FETCH_PACK_ERROR);
        registerEvent(Events.MigStore.Item.PURCHASED);
        registerEvent(Events.MigStore.Item.PURCHASE_ERROR);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Bundle data = intent.getExtras();

        if (action.equals(Events.MigStore.Item.FETCH_FOR_SEARCH_COMPLETED)) {
            String requestKey = intent.getStringExtra(Events.MigStore.Item.Extra.KEY);
            for(StoreSearchCategory searchCategory : searchResults) {
                String key = searchCategory.getRequestKey();
                if (key != null && key.equals(requestKey)) {
                    //got the result of the particular search request
                    searchStoreItems(searchCategory.getType(), currentSearchString, true);
                    break;
                }
            }
        } else if (action.equals(Events.Emoticon.RECEIVED)) {
            searchResultAdapter.notifyDataSetChanged();
        } else if (action.equals(Events.Sticker.PACK_RECEIVED) || action.equals(Events.Sticker.FETCH_PACKS_COMPLETED)
                || action.equals(Events.Sticker.FETCH_PACK_ERROR)) {
            //refresh the list for stickers
            searchStoreItems(StorePagerItem.StorePagerType.STICKERS, currentSearchString, false);
        } else if (action.equals(Events.MigStore.Item.PURCHASED)) {
            int packId = data.getInt(Events.MigStore.Extra.ITEM_ID);
            updateStickerStoreItem(packId);
            //refresh the list for stickers
            searchStoreItems(StorePagerItem.StorePagerType.STICKERS, currentSearchString, false);
        } else if (action.equals(Events.MigStore.Item.PURCHASE_ERROR)) {
            Tools.showToastForIntent(context, intent);
        }
    }

    //- TODO: Workaround to update the status of the pack after purchase
    //- Remove after server sends the correct value of owned field.
    private void updateStickerStoreItem(int packId) {
        StickerStoreItem[] stickerStoreItems = getStickerStoreItems();
        StoreController.getInstance().updateStickerStoreItem(stickerStoreItems, packId);
    }

    private StickerStoreItem[] getStickerStoreItems() {
        for(StoreSearchCategory searchCategory : searchResults) {
            if (searchCategory.getType() == StorePagerItem.StorePagerType.STICKERS) {

                return (StickerStoreItem[])searchCategory.getStoreItems();
            }
        }
        return null;
    }

    @Override
    public void onClick(View view) {

    }

    private void fetchDataForSearchParam(final String searchString) {
        for(StoreSearchCategory searchCategory : searchResults) {
            searchStoreItems(searchCategory.getType(), searchString, false);
        }
    }

    private void searchStoreItems(StorePagerItem.StorePagerType storeType, String searchString, boolean isFetchingDone){
        float minPrice = searchMinPrice;
        float maxPrice = searchMaxPrice;
        String sortBy = StoreController.SORT_BY_DATELISTED;
        String sortOrder = StoreController.SORT_ORDER_DESC;
        String featured = StoreController.NOT_FEATURED;

        // get store items from data cache
        StoreItems storeItems = StoreController.getInstance().searchStoreItems(storeType.getValue(),
                searchString, minPrice, maxPrice, sortBy, sortOrder, featured, limit, offset, null);


        // set data to search results
        for(StoreSearchCategory searchCategory : searchResults) {
            if (searchCategory.getType().getValue() == storeType.getValue()) {
                //update the request key
                String requestKey = StoreController.getInstance().generateSearchStoreRequestKey(storeType.getValue(),
                        searchString, minPrice, maxPrice, sortBy, sortOrder, featured, limit, offset);
                searchCategory.setRequestKey(requestKey);

                //set data list to the category
                if (searchCategory.getType() == StorePagerItem.StorePagerType.STICKERS) {
                    //sticker has a different data type
                    if (storeItems != null) {
                        StickerStoreItem[] stickerStoreItems = StoreController.getInstance().fetchStickerPacks(storeItems.getListData());
                        searchCategory.setTotalNum(storeItems.getTotalResults());
                        searchCategory.setStoreItems(stickerStoreItems);
                    } else {
                        searchCategory.setTotalNum(0);
                        searchCategory.setStoreItems(new StickerStoreItem[0]);
                    }
                } else {
                    if (storeItems != null) {
                        searchCategory.setTotalNum(storeItems.getTotalResults());
                        searchCategory.setStoreItems(storeItems.getListData());
                    } else {
                        searchCategory.setTotalNum(0);
                        searchCategory.setStoreItems(new StoreItem[0]);
                    }
                }

                // display no result when the search is done, do not display it at the beginning
                if (isFetchingDone && searchCategory.getTotalNum() == 0) {
                    searchCategory.setNoResult(true);
                } else {
                    searchCategory.setNoResult(false);
                }

                break;
            }
        }

       // refresh the UI
       searchResultAdapter.setSearchString(currentSearchString);
       searchResultAdapter.setData(searchResults);

       //expand the groups
       for(int i=0; i < searchResultAdapter.getGroupCount(); i++) {
          searchResultListView.expandGroup(i);
       }

    }

    @Override
    public void onItemClick(View v, StoreItem data) {
        StoreItemTypeEnum typeEnum = StoreItemTypeEnum.fromValue(data.getType());

        switch (typeEnum) {
            case GIFT:
                if (!data.isAvailableForLevel(Session.getInstance().getMigLevel())) {
                    Tools.showToast(getActivity(),
                            String.format(I18n.tr("Unlock this gift when you get to level %s!"), data.getMigLevelMin()));
                } else if (data.isGroupOnly()) {
                    Tools.showToast(getActivity(),
                            String.format(I18n.tr("Unlock this gift by joining the %s group!"), data.getGroupName()));
                } else {
                    ActionHandler.getInstance().displayGiftItem(getActivity(), data.getId().toString(), initialRecipient);
                }
                break;
            case STICKER:
                break;
        }
    }

    @Override
    public void onItemLongClick(View v, StoreItem data) {

    }

    @Override
    public void onGroupFooterClick(View v, StoreSearchCategory data) {
        StorePagerItem.StorePagerType type = data.getType();
        switch (type) {
            case GIFTS:
                ActionHandler.getInstance().displayGlobalSearch(getActivity(), GlobalSearchFragment.SearchType.GIFT, currentSearchString);
                break;
            case STICKERS:
                ActionHandler.getInstance().displayGlobalSearch(getActivity(), GlobalSearchFragment.SearchType.STICKER, currentSearchString);
                break;
            case EMOTICONS:
                break;
            case AVATAR:
                break;
        }
    }

    private StoreSearchAdapter createAdapter() {
        StoreSearchAdapter adapter = new StoreSearchAdapter();
        adapter.setStoreItemListener(this);
        adapter.setGroupFooterListener(this);
        adapter.setStickerItemListener(createStickerItemListener());
        return  adapter;
    }

    private BaseViewListener<StickerStoreItem> createStickerItemListener() {
        BaseViewListener<StickerStoreItem> listener = new BaseViewListener<StickerStoreItem>() {
            @Override
            public void onItemClick(View v, StickerStoreItem data) {
                final int viewId = v.getId();
                switch (viewId) {
                    case R.id.option_button:
                        if (StoreController.getInstance().canPurchaseItem(data.getStoreItem().getPrice())) {
                            ActionHandler.getInstance().showStickerPurchaseConfirmDlg(getActivity(), data.getStoreItem());
                        } else {
                            ActionHandler.getInstance().displayAccountBalance(getActivity());
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

        };

        return listener;
    }
}
