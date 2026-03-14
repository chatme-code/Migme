/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftCenterPagerAdapter.java
 * Created 8 May, 2014, 3:27:26 pm
 */

package com.projectgoth.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import com.projectgoth.enums.ViewPagerType;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.ViewPagerItem;
import com.projectgoth.ui.fragment.GiftCenterFragment;
import com.projectgoth.ui.fragment.SinglePostGiftFragment;
import com.projectgoth.util.FragmentUtils;

import java.util.ArrayList;

/**
 * @author dan
 * 
 */
public class GiftCenterPagerAdapter extends BasePagerAdapter<ViewPagerItem> {

    private ArrayList<ViewPagerItem> mItems;
    private String                   mConversationId;
    private String                   mInitialRecipient;
    private ArrayList<String>        mSelectedUsers;
    private boolean                  mIsFromSinglePost;
    private String                   mRootPostId;
    private String                   mParentPostId;

    /**
     * @param fm
     * @param context
     */
    public GiftCenterPagerAdapter(FragmentManager fm, Context context, String initialRecipient) {
        super(fm, context);
        mItems = createItemList();
        mIsFromSinglePost = false;
        this.mInitialRecipient = initialRecipient;
        setPagerItemList(mItems);
    }

    public GiftCenterPagerAdapter(FragmentManager fm, Context context, String initialRecipient, String rootPostId, String rootParentId) {
        super(fm, context);
        mIsFromSinglePost = true;
        mItems = createItemList();
        mParentPostId = rootParentId;
        mRootPostId = rootPostId;
        this.mInitialRecipient = initialRecipient;
        setPagerItemList(mItems);
    }

    @Override
    protected ArrayList<ViewPagerItem> createItemList() {
        ArrayList<ViewPagerItem> items = new ArrayList<ViewPagerItem>();
        items.add(new ViewPagerItem(I18n.tr("Popular"), ViewPagerType.GIFT_CENTER_POPULAR_GIFTS));
        items.add(new ViewPagerItem(I18n.tr("New"), ViewPagerType.GIFT_CENTER_NEW_GIFTS));
        if (!mIsFromSinglePost) {
            items.add(new ViewPagerItem(I18n.tr("Categories"), ViewPagerType.GIFT_CENTER_CATEGORY_LIST));
        }

        return items;
    }

    @Override
    public void setPagerItemList(ArrayList<ViewPagerItem> data) {
        super.setPagerItemList(data);
    }

    @Override
    public Fragment getItem(int position) {

        ViewPagerItem item = mItems.get(position);
        Bundle args = new Bundle();
        switch (item.getType()) {
            case GIFT_CENTER_POPULAR_GIFTS:
            case GIFT_CENTER_NEW_GIFTS:
                args.putBoolean(SinglePostGiftFragment.PARAM_IS_FROM_SINGLE_POST, mIsFromSinglePost);
                args.putString(SinglePostGiftFragment.PARAM_POST_PARENT_ID, mParentPostId);
                args.putString(SinglePostGiftFragment.PARAM_POST_ROOT_ID, mRootPostId);
            case GIFT_CENTER_CATEGORY_LIST:
                args.putString(GiftCenterFragment.PARAM_CONVERSATION_ID, mConversationId);
                args.putStringArrayList(GiftCenterFragment.PARAM_SELECTED_USERS, mSelectedUsers);
                args.putString(FragmentUtils.PARAM_INITIAL_RECIPIENT, mInitialRecipient);
                break;
            default:
                break;
        }
        item.setArgs(args);
        return FragmentUtils.getFragmentByType(item);
    }

    /**
     * @param conversationId
     *            the conversationId to set
     */
    public void setConversationId(String conversationId) {
        this.mConversationId = conversationId;
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mItems.get(position).getLabel();
    }

    @Override
    public void onPositionChanged(int newPosition) {
    }

    public void setSelectedUsers(ArrayList<String> selectedUsers) {
        mSelectedUsers = selectedUsers;
    }

}
