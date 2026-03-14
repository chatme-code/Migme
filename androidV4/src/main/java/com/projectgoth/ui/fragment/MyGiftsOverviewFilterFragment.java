/**
 * Copyright (c) 2013 Project Goth
 * MyGiftsOverviewFilterFragment.java
 * Created Jan 26, 2015, 1:21:07 PM
 */

package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.common.Config;
import com.projectgoth.datastore.GiftsDatastore.StatisticsPeriod;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.MyGiftsFilterItem;
import com.projectgoth.ui.adapter.MyGiftsOverviewFilterAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mapet
 */
public class MyGiftsOverviewFilterFragment extends BaseDialogFragment implements BaseViewListener<MyGiftsFilterItem> {

    private MyGiftsOverviewFilterType mFilterType;
    private ListView mList;
    private MyGiftsOverviewFilterAdapter mAdapter;
    private List<MyGiftsFilterItem> mCategoryFilterItemsList;
    private List<MyGiftsFilterItem> mPeriodFilterItemsList;
    private MyGiftsOverviewSortingListener mListener;

    private int mSelectedFilter;
    private String[] mCategories;
    private String[] mPeriodLabel;
    private StatisticsPeriod[] mPeriod;

    public static final String PARAM_FILTER_TYPE = "PARAM_FILTER_TYPE";
    public static final String PARAM_SELECTED_FILTER = "PARAM_SELECTED_FILTER";

    public interface MyGiftsOverviewSortingListener {

        public void onCategorySelected(int categoryId, String categoryName);

        public void onPeriodSelected(int periodId, StatisticsPeriod period, String periodName);
    }

    public enum MyGiftsOverviewFilterType {
        CATEGORY(0), PERIOD(1);

        private int type;

        private MyGiftsOverviewFilterType(int type) {
            this.type = type;
        }

        public int getType() {
            return this.type;
        }

        public static MyGiftsOverviewFilterType fromValue(int type) {
            for (MyGiftsOverviewFilterType listType : values()) {
                if (listType.getType() == type) {
                    return listType;
                }
            }
            return CATEGORY;
        }
    }

    @Override
    protected void readBundleArguments(Bundle bundleArgs) {
        super.readBundleArguments(bundleArgs);
        mFilterType = MyGiftsOverviewFilterType.fromValue(bundleArgs.getInt(PARAM_FILTER_TYPE));
        mSelectedFilter = bundleArgs.getInt(PARAM_SELECTED_FILTER);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();

        mList = (ListView) view.findViewById(R.id.list_view);
        mList.addHeaderView(createHeader());
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mList.getLayoutParams();
        lp.width = (int) (Config.getInstance().getScreenWidth());
        mList.setLayoutParams(lp);

        mAdapter = new MyGiftsOverviewFilterAdapter();
        setListData();
    }

    private void initData() {
        mCategories = new String[2];
        mCategories[0] = I18n.tr("Top 5 senders");
        mCategories[1] = I18n.tr("Top 5 gifts");

        mPeriodLabel = new String[2];
        mPeriodLabel[0] = I18n.tr("This week");
        mPeriodLabel[1] = I18n.tr("30 days");

        mPeriod = new StatisticsPeriod[2];
        mPeriod[0] = StatisticsPeriod.WEEKLY;
        mPeriod[1] = StatisticsPeriod.MONTHLY;
    }

    private void setListData() {
        if (mFilterType == MyGiftsOverviewFilterType.CATEGORY) {
            mCategoryFilterItemsList = new ArrayList<MyGiftsFilterItem>();
            MyGiftsFilterItem mgfi = null;
            for (int i = 0; i < mCategories.length; i++) {
                mgfi = new MyGiftsFilterItem();
                mgfi.setId(i);
                mgfi.setName(mCategories[i]);
                mgfi.setSelected(mSelectedFilter == i ? true : false);
                mCategoryFilterItemsList.add(mgfi);
            }
            mAdapter.setList(mCategoryFilterItemsList);

        } else if (mFilterType == MyGiftsOverviewFilterType.PERIOD) {
            mPeriodFilterItemsList = new ArrayList<MyGiftsFilterItem>();
            MyGiftsFilterItem mgfi = null;
            for (int i = 0; i < mPeriodLabel.length; i++) {
                mgfi = new MyGiftsFilterItem();
                mgfi.setId(i);
                mgfi.setName(mPeriodLabel[i]);
                mgfi.setSelected(mSelectedFilter == i ? true : false);
                mgfi.setPeriod(mPeriod[i]);
                mPeriodFilterItemsList.add(mgfi);
            }
            mAdapter.setList(mPeriodFilterItemsList);
        }

        mAdapter.setMyGiftsOverviewListener(this);
        mList.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_my_gifts_overview_filter;
    }

    public void setMyGiftsOverviewSortingListener(MyGiftsOverviewSortingListener listener) {
        this.mListener = listener;
    }

    private View createHeader() {
        View header = LayoutInflater.from(getActivity()).inflate(R.layout.header_store_filter, null);
        TextView headerText = (TextView) header.findViewById(R.id.label);

        if (mFilterType == MyGiftsOverviewFilterType.CATEGORY) {
            headerText.setText(I18n.tr("Category"));
        } else if (mFilterType == MyGiftsOverviewFilterType.PERIOD) {
            headerText.setText(I18n.tr("Period"));
        }

        return header;
    }

    @Override
    public void onItemClick(View v, MyGiftsFilterItem data) {
        if (mListener != null) {
            if (mFilterType == MyGiftsOverviewFilterType.CATEGORY) {
                mListener.onCategorySelected(data.getId(), data.getName());
            } else if (mFilterType == MyGiftsOverviewFilterType.PERIOD) {
                mListener.onPeriodSelected(data.getId(), data.getPeriod(), data.getName());
            }

            closeFragment();
        }
    }

    @Override
    public void onItemLongClick(View v, MyGiftsFilterItem data) {
    }

}
