package com.projectgoth.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;

import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.mime.GiftMimeData;
import com.projectgoth.datastore.GiftsDatastore;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.adapter.MyGiftsAllListAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.util.FragmentUtils;

import java.util.List;

/**
 * Created by lopenny on 1/22/15.
 */
public class MyGiftsAllListFragment extends BaseListFragment implements BaseViewListener<GiftMimeData> {

    private MyGiftsAllListAdapter mGiftsAdapter;
    private List<GiftMimeData> mGiftItems;
    private String mUserId;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.setPullToRefreshEnabled(false);
        mList.setScrollContainer(false); //disable scroll
    }

    @Override
    public void onAttach(Activity activity) {
        setShouldUpdateActionBarOnAttach(false);
        super.onAttach(activity);
    }

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        mUserId = args.getString(FragmentUtils.PARAM_USERID);
    }

    @Override
    protected BaseAdapter createAdapter() {
        mGiftsAdapter = new MyGiftsAllListAdapter(ApplicationEx.getInstance().getCurrentActivity());
        mGiftsAdapter.setItemListListener(this);
        return mGiftsAdapter;
    }

    private void notifyDataChange() {
        if (mGiftsAdapter == null) {
            mGiftsAdapter = new MyGiftsAllListAdapter(ApplicationEx.getInstance().getCurrentActivity());
        }

        if (mGiftsAdapter != null && mGiftItems != null) {
            mGiftsAdapter.setItemList(mGiftItems);
        }
    }

    public void refreshData(List<GiftMimeData> items) {
        mGiftItems = items;
        notifyDataChange();
    }

    @Override
    public void onItemClick(View v, GiftMimeData data) {
        ActionHandler.getInstance().displayMyGiftsCardListFragment(getActivity(), I18n.tr("All gifts"),
                GiftsDatastore.Category.ALL.ordinal(), true, mUserId);
    }

    @Override
    public void onItemLongClick(View v, GiftMimeData data) {

    }
}
