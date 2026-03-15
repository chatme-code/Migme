/**
 * Copyright (c) 2013 Project Goth
 *
 * StoreFilterFragment.java
 * Created Dec 2, 2014, 10:57:24 AM
 */

package com.projectgoth.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.b.data.StoreCategory;
import com.projectgoth.common.Config;
import com.projectgoth.controller.StoreController;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.StoreFilterItem;
import com.projectgoth.model.StoreSortType;
import com.projectgoth.ui.adapter.StoreFilterListAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;

/**
 * @author mapet
 * 
 */
public class StoreFilterFragment extends BaseDialogFragment implements BaseViewListener<StoreFilterItem> {

    private String[]               sortByArr;
    private ListView               mList;
    private StoreFilterListAdapter storeFilterListAdapter;
    private StoreFilterType        filterType;
    private String                 giftFilterType;
    private int                    selectedCategoryId;
    private int                    storeType;
    private StoreSortingListener   sortingListener;
    private StoreCategory[]        storeCategoriesArr;
    private List<StoreFilterItem>  categoryFilterItemsList;
    private List<StoreFilterItem>  sortingFilterItemsList;

    public static final String     PARAM_FILTER_TYPE      = "PARAM_FILTER_TYPE";
    public static final String     PARAM_GIFT_FILTER_TYPE = "PARAM_GIFT_FILTER_TYPE";
    public static final String     PARAM_CATEGORY_ID      = "PARAM_CATEGORY_ID";
    public static final String     PARAM_STORE_TYPE       = "PARAM_STORE_TYPE";

    public interface StoreSortingListener {

        public void onCategorySelected(int categoryId, String categoryName);

        public void onSortingSelected(StoreSortType storeSortType, String sortingName);
    }

    private String[] initSortOptions() {

        return StoreController.sortByStringArr;
    }

    public enum StoreFilterType {
        CATEGORY(0), SORT_BY(1);

        private int type;

        private StoreFilterType(int type) {
            this.type = type;
        }

        public int getType() {
            return this.type;
        }

        public static StoreFilterType fromValue(int type) {
            for (StoreFilterType listType : values()) {
                if (listType.getType() == type) {
                    return listType;
                }
            }
            return CATEGORY;
        }
    }

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        filterType = StoreFilterType.fromValue(args.getInt(PARAM_FILTER_TYPE));
        giftFilterType = args.getString(PARAM_GIFT_FILTER_TYPE);
        selectedCategoryId = args.getInt(PARAM_CATEGORY_ID);
        storeType = args.getInt(PARAM_STORE_TYPE);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mList = (ListView) view.findViewById(R.id.list_view);
        mList.addHeaderView(createHeader());
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mList.getLayoutParams();
        lp.height = (int) (Config.getInstance().getScreenHeight() * 0.6f);
        mList.setLayoutParams(lp);
        storeFilterListAdapter = new StoreFilterListAdapter();

        sortByArr = initSortOptions();
        setListData();
    }

    private void setListData() {
        if (filterType == StoreFilterType.CATEGORY) {
            storeCategoriesArr = StoreController.getInstance().getStoreCategories(Integer.toString(storeType));

            if (storeCategoriesArr != null && storeCategoriesArr.length > 0) {
                categoryFilterItemsList = new ArrayList<StoreFilterItem>();

                StoreFilterItem sfi = new StoreFilterItem();
                sfi.setId(StoreController.newCategoryId);
                sfi.setName(I18n.tr("New"));
                if (selectedCategoryId == StoreController.newCategoryId) { 
                    sfi.setIsSelected(true);
                } else {
                    sfi.setIsSelected(false);
                }
                categoryFilterItemsList.add(sfi);

                sfi = new StoreFilterItem();
                sfi.setId(StoreController.featuredCategoryId);
                sfi.setName(I18n.tr("Featured"));
                if (selectedCategoryId == StoreController.featuredCategoryId) { 
                    sfi.setIsSelected(true);
                } else {
                    sfi.setIsSelected(false);
                }
                categoryFilterItemsList.add(sfi);
                
                sfi = new StoreFilterItem();
                sfi.setId(StoreController.allCategoryId);
                sfi.setName(I18n.tr("All"));
                if (selectedCategoryId == StoreController.allCategoryId) { 
                    sfi.setIsSelected(true);
                } else {
                    sfi.setIsSelected(false);
                }
                categoryFilterItemsList.add(sfi);

                for (int i = 0; i < storeCategoriesArr.length; i++) {
                    StoreFilterItem storeFilterItem = new StoreFilterItem();
                    storeFilterItem.setId(storeCategoriesArr[i].getId());
                    storeFilterItem.setName(storeCategoriesArr[i].getName());

                    if (selectedCategoryId == storeCategoriesArr[i].getId()) {
                        storeFilterItem.setIsSelected(true);
                    } else {
                        storeFilterItem.setIsSelected(false);
                    }

                    categoryFilterItemsList.add(storeFilterItem);
                }

                storeFilterListAdapter.setList(categoryFilterItemsList);
                storeFilterListAdapter.setGiftListListener(this);
            }

        } else if (filterType == StoreFilterType.SORT_BY) {
            if (sortByArr != null && sortByArr.length > 0) {

                sortingFilterItemsList = new ArrayList<StoreFilterItem>();

                for (int i = 0; i < sortByArr.length; i++) {
                    StoreFilterItem storeFilterItem = new StoreFilterItem();
                    storeFilterItem.setName(sortByArr[i]);
                    storeFilterItem.setStoreSortType(StoreSortType.fromValue(i));

                    if (giftFilterType == sortByArr[i]) {
                        storeFilterItem.setIsSelected(true);
                    } else {
                        storeFilterItem.setIsSelected(false);
                    }

                    sortingFilterItemsList.add(storeFilterItem);
                }
                storeFilterListAdapter.setList(sortingFilterItemsList);
                storeFilterListAdapter.setGiftListListener(this);
            }
        }
        mList.setAdapter(storeFilterListAdapter);
        storeFilterListAdapter.notifyDataSetChanged();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_store_filter;
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.MigStore.SubCategory.FETCH_ALL_COMPLETED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(Events.MigStore.SubCategory.FETCH_ALL_COMPLETED)) {
            setListData();
        }
    }

    public void setStoreSortingListener(StoreSortingListener listener) {
        sortingListener = listener;
    }

    private View createHeader() {
        View header = LayoutInflater.from(getActivity()).inflate(R.layout.header_store_filter, null);
        TextView headerText = (TextView) header.findViewById(R.id.label);

        if (filterType == StoreFilterType.CATEGORY) {
            headerText.setText(I18n.tr("Category"));
        } else if (filterType == StoreFilterType.SORT_BY) {
            headerText.setText(I18n.tr("Sort by"));
        }

        return header;
    }

    @Override
    public void onItemClick(View v, StoreFilterItem data) {

        if (sortingListener != null) {
            if (filterType == StoreFilterType.CATEGORY) {
                sortingListener.onCategorySelected(data.getId(), data.getName());
            } else if (filterType == StoreFilterType.SORT_BY) {
                sortingListener.onSortingSelected(data.getStoreSortType(), data.getName());
            }

            closeFragment();
        }

    }

    @Override
    public void onItemLongClick(View v, StoreFilterItem data) {
    }
}
