/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftCategoryFragment.java
 * Created Dec 6, 2013, 3:17:39 PM
 */

package com.projectgoth.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.b.data.StoreItem;
import com.projectgoth.b.data.StoreItems;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.StoreController;
import com.projectgoth.controller.StoreController.StoreItemFilterType;
import com.projectgoth.datastore.Session;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.StorePagerItem.StorePagerType;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.AlertHandler;
import com.projectgoth.ui.adapter.StoreCategoryAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.widget.ButtonEx;

/**
 * @author mapet
 * 
 */
public class GiftCategoryFragment extends BaseDialogFragment implements OnClickListener, BaseViewListener<StoreItem> {

    private StoreItems           mGiftCategoryItems;

    private Spinner              mSortBySpinner;
    private Spinner              mSortOrderSpinner;
    private ArrayAdapter<String> mSortBySpinnerAdapter;
    private ArrayAdapter<String> mSortOrderSpinnerAdapter;
    private ButtonEx             mSortButton;
    private GridView             mGiftGrid;
    private StoreCategoryAdapter mGiftGridAdapter;

    private StoreItemFilterType  mGiftFilterType;
    private String               mGiftCategoryId;
    private String               mGiftCategoryName;

    private String               mSearchString;
    private float                mSearchMinPrice;
    private float                mSearchMaxPrice;

    public static final String   PARAM_GIFT_FILTER_TYPE   = "PARAM_GIFT_FILTER_TYPE";
    public static final String   PARAM_GIFT_CATEGORY_ID   = "PARAM_GIFT_CATEGORY_ID";
    public static final String   PARAM_GIFT_CATEGORY_NAME = "PARAM_GIFT_CATEGORY_NAME";

    public static final String   PARAM_SEARCH_STRING      = "PARAM_SEARCH_STRING";
    public static final String   PARAM_SEARCH_MIN_PRICE   = "PARAM_SEARCH_MIN_PRICE";
    public static final String   PARAM_SEARCH_MAX_PRICE   = "PARAM_SEARCH_MAX_PRICE";

    public static final String   PARAM_IS_IN_CHAT         = "PARAM_IS_IN_CHAT";
    public static final String   PARAM_CONVERSATION_ID    = "PARAM_CONVERSATION_ID";
    public static final String   PARAM_INITIAL_RECIPIENT  = "PARAM_INITIAL_RECIPIENT";

    private int                  storeType                = StorePagerType.GIFTS.getValue();
    private int                  limit                    = 30;
    private int                  offset                   = 0;

    private boolean              isInChat                 = false;
    private String               mConversationId;

    private String               mInitialRecipient;

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        mGiftFilterType = StoreItemFilterType.fromValue(args.getInt(PARAM_GIFT_FILTER_TYPE));
        mGiftCategoryId = args.getString(PARAM_GIFT_CATEGORY_ID);
        mGiftCategoryName = args.getString(PARAM_GIFT_CATEGORY_NAME);
        mSearchString = args.getString(PARAM_SEARCH_STRING);
        mSearchMinPrice = args.getFloat(PARAM_SEARCH_MIN_PRICE);
        mSearchMaxPrice = args.getFloat(PARAM_SEARCH_MAX_PRICE);
        mInitialRecipient = args.getString(PARAM_INITIAL_RECIPIENT);
        isInChat = args.getBoolean(PARAM_IS_IN_CHAT);
        if (isInChat) {
            mConversationId = args.getString(PARAM_CONVERSATION_ID);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_gift_category;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        refreshStoreItems();

        TextView giftCategory = (TextView) view.findViewById(R.id.gift_category);
        ImageView giftSearch = (ImageView) view.findViewById(R.id.gift_search);
        TextView sortingLabel = (TextView) view.findViewById(R.id.spinner_label);

        mSortBySpinner = (Spinner) view.findViewById(R.id.gift_sort_by_spinner);
        mSortOrderSpinner = (Spinner) view.findViewById(R.id.gift_sort_order_spinner);
        mSortButton = (ButtonEx) view.findViewById(R.id.sort_button);

        mGiftGrid = (GridView) view.findViewById(R.id.gift_grid);

        giftCategory.setText(I18n.tr(mGiftCategoryName));
        
        sortingLabel.setText(I18n.tr("Sort by"));

        List<String> sortByList = new ArrayList<String>();
        sortByList.add(I18n.tr("Name"));
        sortByList.add(I18n.tr("Price"));
        sortByList.add(I18n.tr("Popular"));

        List<String> sortOrderList = new ArrayList<String>();
        sortOrderList.add(I18n.tr("Ascending"));
        sortOrderList.add(I18n.tr("Descending"));

        mSortBySpinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,
                sortByList);
        mSortBySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSortBySpinner.setClickable(true);
        mSortBySpinner.setAdapter(mSortBySpinnerAdapter);

        mSortOrderSpinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,
                sortOrderList);
        mSortOrderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSortOrderSpinner.setClickable(true);
        mSortOrderSpinner.setAdapter(mSortOrderSpinnerAdapter);

        mSortButton.setText(I18n.tr("Sort"));
        mSortButton.setOnClickListener(this);

        giftSearch.setOnClickListener(this);

        mGiftGridAdapter = new StoreCategoryAdapter();
        mGiftGridAdapter.setInChat(isInChat);

        refreshStoreItems();

        if (mGiftCategoryItems != null) {
            mGiftCategoryItems.filterListToLevel(Session.getInstance().getMigLevel());
            mGiftGridAdapter.setGiftList(mGiftCategoryItems.getListData());
        }

        mGiftGrid.setAdapter(mGiftGridAdapter);
        mGiftGridAdapter.setStoreCategoryListener(this);
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.MigStore.Item.FETCH_FOR_POPULAR_COMPLETED);
        registerEvent(Events.MigStore.Item.FETCH_FOR_FEATURED_COMPLETED);
        registerEvent(Events.MigStore.Item.FETCH_FOR_NEW_COMPLETED);
        registerEvent(Events.MigStore.Item.FETCH_FOR_MAINCATEGORY_COMPLETED);
        registerEvent(Events.MigStore.Item.FETCH_FOR_SEARCH_COMPLETED);
        registerEvent(Events.MigStore.Item.FETCH_FOR_SUBCATEGORY_COMPLETED);
        registerEvent(Events.Emoticon.RECEIVED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Events.MigStore.Item.FETCH_FOR_POPULAR_COMPLETED)
                || action.equals(Events.MigStore.Item.FETCH_FOR_FEATURED_COMPLETED)
                || action.equals(Events.MigStore.Item.FETCH_FOR_NEW_COMPLETED)
                || action.equals(Events.MigStore.Item.FETCH_FOR_MAINCATEGORY_COMPLETED)
                || action.equals(Events.MigStore.Item.FETCH_FOR_SUBCATEGORY_COMPLETED)
                || action.equals(Events.MigStore.Item.FETCH_FOR_SEARCH_COMPLETED)
                || action.equals(Events.Emoticon.RECEIVED)) {
            refreshStoreItems();
        }
    }

    private final void refreshStoreItems() {
        if (mGiftGridAdapter != null) {
            switch (mGiftFilterType) {
                case POPULAR:
                case FEATURED:
                case NEW:
                    mGiftCategoryItems = StoreController.getInstance().getMainCategories(storeType, limit, offset,
                            mGiftFilterType);
                    break;
                case CATEGORY:
                    mGiftCategoryItems = StoreController.getInstance().getStoreCategory(mGiftCategoryId,
                            StoreController.SORT_BY_NAME, StoreController.SORT_ORDER_ASC, StoreController.NOT_FEATURED,
                            limit, offset);
                    break;
                case GENERAL:
                    mGiftCategoryItems = StoreController.getInstance().searchStoreItems(storeType, mSearchString,
                            mSearchMinPrice, mSearchMaxPrice, StoreController.SORT_BY_NAME,
                            StoreController.SORT_ORDER_ASC, StoreController.NOT_FEATURED, limit, offset, null);
                    break;
                default:
                    break;
            }

            if (mGiftCategoryItems != null && mGiftCategoryItems.getListData() != null) {
                mGiftCategoryItems.filterListToLevel(Session.getInstance().getMigLevel());
                mGiftGridAdapter.setGiftList(mGiftCategoryItems.getListData());
            }
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        switch (viewId) {
            case R.id.gift_search:
                AlertHandler.getInstance().showSearchStoreDialog(getActivity(), getLocalCurrency(),
                        false, null, mInitialRecipient);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(View v, StoreItem data) {
        int viewId = v.getId();

        switch (viewId) {
            case R.id.gift_image:
            case R.id.gift_name:
            case R.id.gift_price:
                if (!data.isAvailableForLevel(Session.getInstance().getMigLevel())) {
                    Tools.showToast(
                            this.getActivity(),
                            String.format(I18n.tr("Unlock this gift when you get to level %s!"),
                                    data.getMigLevelMin()));
                } else if (!isInChat && data.isGroupOnly()) {
                    Tools.showToast(this.getActivity(),
                            String.format(I18n.tr("Unlock this gift by joining the %s group!"), data.getGroupName()));
                } else {
                    if (isInChat) {
                        closeFragment();
                        ActionHandler.getInstance().displayGiftPreviewFragment(getActivity(), data.getId().toString(), 
                                mConversationId, false);
                    }   else {
                        ActionHandler.getInstance().displayGiftItem(getActivity(), data.getId().toString(), mInitialRecipient);
                    }                   
                }

                break;
            default:
                break;
        }
    }

    @Override
    public void onItemLongClick(View v, StoreItem data) {
    }

    private String getLocalCurrency() {
        if (mGiftCategoryItems != null) {
            StoreItem[] storeItem = mGiftCategoryItems.getListData();

            if (storeItem != null && storeItem.length > 0) {
                return storeItem[0].getLocalCurrency();
            }
        }
        return Constants.BLANKSTR;
    }
}
