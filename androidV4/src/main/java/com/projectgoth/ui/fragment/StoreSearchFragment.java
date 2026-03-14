package com.projectgoth.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.b.data.StoreItem;
import com.projectgoth.b.data.StoreItems;
import com.projectgoth.b.enums.StoreItemTypeEnum;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.StoreController;
import com.projectgoth.datastore.Session;
import com.projectgoth.i18n.I18n;
import com.projectgoth.listener.SectionUpdateListener;
import com.projectgoth.model.StickerStoreItem;
import com.projectgoth.model.StorePagerItem;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.adapter.StoreItemListAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;

/**
 * Created by houdangui on 12/12/14.
 */
public class StoreSearchFragment extends BaseListFragment implements BaseViewListener<StoreItem> {

    private static final int      LOAD_MORE_INCREMENT       = 15;
    private int                   currentLoadMoreLimit      = LOAD_MORE_INCREMENT;
    private StoreItems            storeItems;
    private float                 searchMinPrice = -1.0f;
    private float                 searchMaxPrice = -1.0f;
    private String                currentSearchString;
    private StorePagerItem.StorePagerType storeType;
    private StoreItemListAdapter storeItemListAdapter;
    private SectionUpdateListener sectionUpdateListener;
    private String initialRecipient;

    public static final String PARAM_INITIAL_RECIPIENT = "PARAM_INITIAL_RECIPIENT";
    public static final String STORE_ITEM_TYPE = "STORE_ITEM_TYPE";

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
        currentLoadMoreLimit = LOAD_MORE_INCREMENT;
        currentSearchString = mFilterText;

        updateListData();

        setPullToRefreshEnabled(false);
    }

    @Override
    protected void updateListData() {

        storeItems = StoreController.getInstance().searchStoreItems(storeType.getValue(),
                currentSearchString, searchMinPrice, searchMaxPrice, StoreController.SORT_BY_DATELISTED,
                StoreController.SORT_ORDER_DESC, StoreController.NOT_FEATURED, currentLoadMoreLimit, 0, null);

        if (storeItems != null) {
            StoreItem[] items = storeItems.getListData();

            if (storeType == StorePagerItem.StorePagerType.STICKERS) {
                StickerStoreItem[] stickerStoreItems = StoreController.getInstance().fetchStickerPacks(items);
                if (stickerStoreItems != null && stickerStoreItems.length > 0 ) {
                    storeItemListAdapter.setStoreItemList(stickerStoreItems);
                }
            } else {
                if (items != null && items.length > 0) {
                    storeItemListAdapter.setStoreItemList(items);
                }
            }

            if (sectionUpdateListener != null && items != null) {
                sectionUpdateListener.setSectionCount(items.length);
            }
        }
    }

    @Override
    protected BaseAdapter createAdapter() {

        if (storeType == StorePagerItem.StorePagerType.STICKERS) {
            storeItemListAdapter = new StoreItemListAdapter<StickerStoreItem>();
            storeItemListAdapter.setStickerItemListener(createStickerItemListener());
        } else {
            storeItemListAdapter = new StoreItemListAdapter<StoreItem>();
            storeItemListAdapter.setStoreItemListener(this);
        }

        storeItemListAdapter.setStoreType(storeType);

        return storeItemListAdapter;
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.MigStore.Item.FETCH_FOR_SEARCH_COMPLETED);
        registerEvent(Events.Emoticon.RECEIVED);
        if (storeType == StorePagerItem.StorePagerType.STICKERS) {
            registerEvent(Events.Sticker.PACK_RECEIVED);
            registerEvent(Events.Sticker.FETCH_PACKS_COMPLETED);
            registerEvent(Events.Sticker.FETCH_PACK_ERROR);
            registerEvent(Events.MigStore.Item.PURCHASED);
            registerEvent(Events.MigStore.Item.PURCHASE_ERROR);
        }
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Bundle data = intent.getExtras();

        if (action.equals(Events.MigStore.Item.FETCH_FOR_SEARCH_COMPLETED)) {
            updateListData();
            hideLoadingMore();
        } else if (action.equals(Events.Emoticon.RECEIVED)) {
            storeItemListAdapter.notifyDataSetChanged();
        } else if (action.equals(Events.Sticker.PACK_RECEIVED) || action.equals(Events.Sticker.FETCH_PACKS_COMPLETED)
                || action.equals(Events.Sticker.FETCH_PACK_ERROR)) {
            //refresh the list for stickers
            updateListData();
        } else if (action.equals(Events.MigStore.Item.PURCHASED)) {
            int packId = data.getInt(Events.MigStore.Extra.ITEM_ID);
            updateStickerStoreItem(packId);
            updateListData();
        } else if (action.equals(Events.MigStore.Item.PURCHASE_ERROR)) {
            Tools.showToastForIntent(context, intent);
        }
    }

    public void setSectionUpdateListener(SectionUpdateListener listener) {
        this.sectionUpdateListener = listener;
    }

    //- TODO: Workaround to update the status of the pack after purchase
    //- Remove after server sends the correct value of owned field.
    private void updateStickerStoreItem(int packId) {
        StickerStoreItem[] stickerStoreItems = (StickerStoreItem[])storeItemListAdapter.getStoreItemList();
        StoreController.getInstance().updateStickerStoreItem(stickerStoreItems, packId);
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
    protected View createFooterView() {
        return createLoadingView();
    }

    @Override
    protected void onListEndReached() {
        super.onListEndReached();
        // if loading limit is already more than post number we have, no need to
        // add even more, just update the current limit again
        if (currentLoadMoreLimit <= storeItemListAdapter.getCount()) {
            showLoadingMore();
            currentLoadMoreLimit += LOAD_MORE_INCREMENT;
        }
        updateListData();
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
