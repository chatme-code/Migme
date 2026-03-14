/**
 * Copyright (c) 2013 Project Goth
 *
 * UnlockedGiftListFragment.java
 * Created Jan 6, 2015, 11:17:26 AM
 */

package com.projectgoth.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.b.data.StoreUnlockedItem;
import com.projectgoth.controller.StoreController;
import com.projectgoth.datastore.Session;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.BaseCustomFragmentActivity;
import com.projectgoth.ui.activity.CustomActionBarConfig;
import com.projectgoth.ui.activity.CustomActionBarConfig.NavigationButtonState;
import com.projectgoth.ui.adapter.UnlockedGiftListAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;

/**
 * @author mapet
 * 
 */
public class UnlockedGiftListFragment extends BaseListFragment implements BaseViewListener<StoreUnlockedItem> {

    private UnlockedGiftListAdapter unlockedGiftListAdapter;
    private StoreUnlockedItem[]     storeUnlockedItemArr;
    private View                    emptyView;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshData(false);
        ((BaseCustomFragmentActivity)this.getActivity()).setFragment(this);
    }

    @Override
    protected BaseAdapter createAdapter() {
        unlockedGiftListAdapter = new UnlockedGiftListAdapter();
        refreshData(false);
        return unlockedGiftListAdapter;
    }

    private void refreshData(boolean shouldForceFetch) {
        storeUnlockedItemArr = StoreController.getInstance().getUnlockedGifts(Session.getInstance().getUsername(),
                shouldForceFetch);
        if (storeUnlockedItemArr != null && storeUnlockedItemArr.length > 0) {
            unlockedGiftListAdapter.setUnlockedItemList(storeUnlockedItemArr);
            unlockedGiftListAdapter.setUnlockedItemListListener(this);
        } else {
            mList.setVisibility(View.GONE);
        }

        showOrHideEmptyViewIfNeeded();
    }

    @Override
    protected View createHeaderView() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View headerView = inflater.inflate(R.layout.header_unlocked_gifts, null);
        TextView headerText = (TextView) headerView.findViewById(R.id.label);
        headerText.setText(I18n.tr("Once you unlock a gift, you can send it to your friend for free."));
        return headerView;
    }

    private View createEmptyView() {
        View emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_view_unlocked_gifts, mList, false);

        TextView unlockedText = (TextView) emptyView.findViewById(R.id.unlocked_text);
        TextView unlockedLabel = (TextView) emptyView.findViewById(R.id.unlocked_label);
        TextView unlockedHint = (TextView) emptyView.findViewById(R.id.empty_list_hint);

        unlockedText.setText(I18n.tr("Once you unlock a gift, you can send it to your friend for free."));
        unlockedLabel.setText(I18n.tr("No unlocked gifts yet."));
        unlockedHint.setText(I18n.tr("Hint: You unlock gifts when you do certain things, such as making your first friend or post, or increasing your level."));

        return emptyView;
    }

    protected void showOrHideEmptyViewIfNeeded() {
        if (unlockedGiftListAdapter.getCount() == 0) {
            // show emptyView
            if (emptyView == null) {
                emptyView = createEmptyView();
            }
            // prevent it from being added multiple times
            if (emptyView.getParent() != null) {
                ((ViewGroup) emptyView.getParent()).removeView(emptyView);
            }

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            ((ViewGroup) mMainContainer).addView(emptyView, params);
            emptyView.setVisibility(View.VISIBLE);

        } else {
            // hide emptyView
            if (emptyView != null && emptyView.getParent() != null) {
                ((ViewGroup) emptyView.getParent()).removeView(emptyView);
            }
            mList.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public CustomActionBarConfig getActionBarConfig() {
        CustomActionBarConfig config = new CustomActionBarConfig();
        config.setNavigationButtonState(NavigationButtonState.BACK);
        return config;
    }

    @Override
    protected String getTitle() {
        return I18n.tr("My unlocked gifts");
    }

    @Override
    protected int getTitleIcon() {
        return R.drawable.ad_gift_white;
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.MigStore.Item.FETCH_UNLOCKED_COMPLETED);
        registerEvent(Events.Emoticon.RECEIVED);
        registerEvent(Events.Emoticon.FETCH_ALL_COMPLETED);
        registerEvent(Events.MigStore.Item.PURCHASE_STORE_ITEM_COMPLETED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (action.equals(Events.MigStore.Item.PURCHASE_STORE_ITEM_COMPLETED)) {
            refreshData(true);
        } else {
            refreshData(false);
        }
    }

    @Override
    public void onItemClick(View v, StoreUnlockedItem data) {
        GAEvent.Store_SendUnlockGift.send();
        ActionHandler.getInstance().displayUnlockedGiftFragment(getActivity(),
               data.getStoreItemData().getId().toString());
    }

    @Override
    public void onItemLongClick(View v, StoreUnlockedItem data) {
    }

}
