/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftCenterCategoryListFragment.java
 * Created 8 May, 2014, 3:25:56 pm
 */

package com.projectgoth.ui.fragment;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.b.data.StoreCategory;
import com.projectgoth.controller.StoreController;
import com.projectgoth.controller.StoreController.StoreItemFilterType;
import com.projectgoth.model.StorePagerItem.StorePagerType;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.adapter.GiftCategoryListAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;

/**
 * @author Dan
 * 
 */
public class GiftCenterCategoryListFragment extends BaseDialogFragment implements BaseViewListener<StoreCategory> {

    private ListView                giftCategoryList;
    private GiftCategoryListAdapter giftCategoryListAdapter;
    private int                     storeType               = StorePagerType.GIFTS.getValue();
    private StoreCategory[]         mGiftCategories;
    private String                  mConversationId;
    private String                  mInitialRecipient;
    //for start chat with a gift message, the conversation is not created yet
    private ArrayList<String>       mSelectedUsers;

    public static final String      PARAM_CONVERSATION_ID   = "PARAM_CONVERSATION_ID";
    public static final String      PARAM_INITIAL_RECIPIENT = "PARAM_INITIAL_RECIPIENT";
    public static final String      PARAM_SELECTED_USERS    = "PARAM_SELECTED_USERS";

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        mConversationId = args.getString(PARAM_CONVERSATION_ID);
        mInitialRecipient = args.getString(PARAM_INITIAL_RECIPIENT);
        mSelectedUsers = args.getStringArrayList(PARAM_SELECTED_USERS);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_gift_center_category_list;
    }
    
    @Override
    public void onAttach(Activity activity) {
        setShouldUpdateActionBarOnAttach(false);
        super.onAttach(activity);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        giftCategoryList = (ListView) view.findViewById(R.id.gift_category_list);
        giftCategoryListAdapter = new GiftCategoryListAdapter();
        giftCategoryListAdapter.setGiftCategoryListener(this);
        giftCategoryList.setAdapter(giftCategoryListAdapter);

        refreshCategoryList();
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.MigStore.SubCategory.FETCH_ALL_COMPLETED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(Events.MigStore.SubCategory.FETCH_ALL_COMPLETED)) {
            refreshCategoryList();
        }
    }

    private final void refreshCategoryList() {
        mGiftCategories = StoreController.getInstance().getStoreCategories(Integer.toString(storeType));
        if (mGiftCategories != null) {
            giftCategoryListAdapter.setGiftCategories(mGiftCategories);
        }
    }

    @Override
    public void onItemClick(View v, StoreCategory data) {
        ActionHandler.getInstance().displayGiftCategoryParent(getActivity(), StoreItemFilterType.CATEGORY,
                data.getId().toString(), data.getName(), mConversationId, mInitialRecipient, mSelectedUsers);
    }

    @Override
    public void onItemLongClick(View v, StoreCategory data) {
    }

}
