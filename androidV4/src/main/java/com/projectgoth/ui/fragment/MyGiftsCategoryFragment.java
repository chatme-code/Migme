package com.projectgoth.ui.fragment;

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
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.GiftCategoryData;
import com.projectgoth.b.data.GiftCategoryItem;
import com.projectgoth.datastore.GiftsDatastore;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.adapter.MyGiftsCategoryListAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewPositionListener;
import com.projectgoth.util.FragmentUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lopenny on 1/27/15.
 */
public class MyGiftsCategoryFragment extends BaseDialogFragment implements BaseViewPositionListener<GiftCategoryItem> {

    public final static String PARAM_IDX = "PARAM_IDX";

    private ListView mList;
    private MyGiftsCategoryListAdapter mListAdapter;
    private List<GiftCategoryItem> mListItems;
    private CategoryListener mListener;

    private int mCurrentCategory = 0;
    private String mUserId;

    public interface CategoryListener {
        public void onCategorySelected(int position, GiftCategoryItem data);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_store_filter;
    }

    @Override
    protected void readBundleArguments(Bundle bundleArgs) {
        super.readBundleArguments(bundleArgs);
        mCurrentCategory = bundleArgs.getInt(PARAM_IDX, 0);
        mUserId = bundleArgs.getString(FragmentUtils.PARAM_USERID);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mList = (ListView) view.findViewById(R.id.list_view);
        mList.addHeaderView(createHeader());
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mList.getLayoutParams();
        lp.height = ApplicationEx.getDimension(R.dimen.gift_category_panel_height);
        mList.setLayoutParams(lp);

        mListAdapter = new MyGiftsCategoryListAdapter(getActivity());

        setListData();
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.Gift.FETCH_GIFT_CATEGORIES_COMPLETED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(Events.Gift.FETCH_GIFT_CATEGORIES_COMPLETED)) {
            setListData();
        }
    }

    private void setListData() {
        GiftCategoryData giftCategoryData = GiftsDatastore.getInstance().getUserGiftCategories(mUserId, false);
        if (giftCategoryData != null && giftCategoryData.getResponse() != null) {
            mListItems = Arrays.asList(giftCategoryData.getResponse());
            mListAdapter.setItemListListener(this);
            mListAdapter.setItemList(mCurrentCategory, mListItems);
            mList.setAdapter(mListAdapter);
        }
    }

    private View createHeader() {
        View header = LayoutInflater.from(getActivity()).inflate(R.layout.header_store_filter, null);
        TextView headerText = (TextView) header.findViewById(R.id.label);
        headerText.setText(I18n.tr("Category"));
        return header;
    }

    public void setCategoryListener(CategoryListener listener) {
        mListener = listener;
    }

    @Override
    public void onItemClick(View v, int position, GiftCategoryItem data) {
        mListener.onCategorySelected(position, data);
        closeFragment();
    }

}
