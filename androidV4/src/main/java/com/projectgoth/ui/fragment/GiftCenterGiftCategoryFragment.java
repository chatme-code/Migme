/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftCenterCategoryFragment.java
 * Created 8 May, 2014, 3:25:12 pm
 */

package com.projectgoth.ui.fragment;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.StoreItem;
import com.projectgoth.b.data.StoreItems;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.StoreController;
import com.projectgoth.controller.StoreController.StoreItemFilterType;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.events.AppEvents;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.model.StorePagerItem.StorePagerType;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.adapter.GiftCenterGiftsAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.widget.ButtonEx;
import com.projectgoth.util.ChatUtils;

/**
 * @author dan
 * 
 */

public class GiftCenterGiftCategoryFragment extends BaseDialogFragment implements BaseViewListener<StoreItem>,
        OnClickListener {

    private StoreItemFilterType    mGiftFilterType;
    private String                 mConversationId;
    private String                 mGiftCategoryId;

    /**
     * to add a footer to a ListView is easy, but unfortunately, to add a footer
     * to a GridView is very hard. a workaround is just using ListView, then
     * every item of the ListView is a LinearLayout to make it look the same as
     * a GridView
     */
    private FrameLayout            container;
    private ListView               giftGridView;
    private View                   emptyView;
    private View                   giftGridFooter;
    private GiftCenterGiftsAdapter giftsAdapter;
    private StoreItems             mGiftItems;

    private int                    storeType                = StorePagerType.GIFTS.getValue();
    private final int              limit                    = 50;
    private int                    offset                   = 0;

    private String                 mInitialRecipient;
    private ChatConversation       mConversation;
    //for start chat with a gift message, the conversation is not created yet
    private ArrayList<String>      mSelectedUsers;
    private boolean                mIsFromSinglePost;
    private String                 mRootPostId;
    private String                 mParentPostId;

    public static final String     PARAM_CONVERSATION_ID    = "PARAM_CONVERSATION_ID";
    public static final String     PARAM_GIFT_FILTER_TYPE   = "PARAM_GIFT_FILTER_TYPE";
    public static final String     PARAM_GIFT_CATEGORY_ID   = "PARAM_GIFT_CATEGORY_ID";
    public static final String     PARAM_GIFT_CATEGORY_NAME = "PARAM_GIFT_CATEGORY_NAME";
    public static final String     PARAM_INITIAL_RECIPIENT  = "PARAM_INITIAL_RECIPIENT";
    public static final String     PARAM_SELECTED_USERS     = "PARAM_SELECTED_USERS";
    public static final String     PARAM_IS_FROM_SINGLE_POST = "PARAM_IS_FROM_SINGLE_POST";
    public static final String     PARAM_POST_ROOT_ID       = "PARAM_POST_ROOT_ID";
    public static final String     PARAM_POST_PARENT_ID     = "PARAM_POST_PARENT_ID";

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        mGiftFilterType = StoreItemFilterType.fromValue(args.getInt(PARAM_GIFT_FILTER_TYPE));
        mConversationId = args.getString(PARAM_CONVERSATION_ID);
        mGiftCategoryId = args.getString(PARAM_GIFT_CATEGORY_ID);
        mInitialRecipient = args.getString(PARAM_INITIAL_RECIPIENT);
        mSelectedUsers = args.getStringArrayList(PARAM_SELECTED_USERS);
        mIsFromSinglePost = args.getBoolean(PARAM_IS_FROM_SINGLE_POST);
        mRootPostId = args.getString(PARAM_POST_ROOT_ID);
        mParentPostId = args.getString(PARAM_POST_PARENT_ID);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_gift_center_gift_category;
    }
    
    @Override
    public void onAttach(Activity activity) {
        setShouldUpdateActionBarOnAttach(false);
        super.onAttach(activity);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mConversation = ChatDatastore.getInstance().getChatConversationWithId(mConversationId);

        container = (FrameLayout) view.findViewById(R.id.container);

        giftGridView = (ListView) view.findViewById(R.id.gift_grid);
        giftGridView.setDivider(null);
        giftGridView.setBackgroundColor(Theme.getColor(ThemeValues.LIGHT_BACKGROUND_COLOR));

        View footerView = createFooterView();
        giftGridView.addFooterView(footerView);

        giftsAdapter = new GiftCenterGiftsAdapter();
        giftsAdapter.setGiftViewListener(this);

        giftGridView.setAdapter(giftsAdapter);

        if (mGiftItems != null) {
            mGiftItems.filterListToLevel(Session.getInstance().getMigLevel());
            giftsAdapter.setGiftList(mGiftItems.getListData());
        }

        hideFooter();

        refreshGiftItems();
    }

    private View createFooterView() {
        LayoutInflater inflater = LayoutInflater.from(ApplicationEx.getContext());

        View footerView = inflater.inflate(R.layout.footer_gift_category_grid, null, false);

        giftGridFooter = footerView.findViewById(R.id.gift_category_footer_container);
        giftGridFooter.setOnClickListener(this);

        TextView moreHint = (TextView) footerView.findViewById(R.id.hint);
        moreHint.setText(I18n.tr("View all gifts"));

        ButtonEx storeButton = (ButtonEx) footerView.findViewById(R.id.store_icon_button);
        storeButton.setText(I18n.tr("GO TO STORE"));

        return footerView;
    }

    @Override
    protected void registerReceivers() {
        switch (mGiftFilterType) {
            case POPULAR:
                registerEvent(Events.MigStore.Item.FETCH_FOR_POPULAR_COMPLETED);
                break;
            case NEW:
                registerEvent(Events.MigStore.Item.FETCH_FOR_NEW_COMPLETED);
                break;
            case CATEGORY:
                registerEvent(Events.MigStore.Item.FETCH_FOR_SUBCATEGORY_COMPLETED);
                break;
            default:
                break;
        }

        registerEvent(Events.Emoticon.RECEIVED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Events.MigStore.Item.FETCH_FOR_POPULAR_COMPLETED)) {
            if(mGiftFilterType == StoreItemFilterType.POPULAR) {
                refreshGiftItems();
            }
        } else if (action.equals(Events.MigStore.Item.FETCH_FOR_NEW_COMPLETED)) {
            if(mGiftFilterType == StoreItemFilterType.NEW) {
                refreshGiftItems();
            }
        } else if (action.equals(Events.MigStore.Item.FETCH_FOR_SUBCATEGORY_COMPLETED)) {
            if(mGiftFilterType == StoreItemFilterType.CATEGORY) {
                refreshGiftItems();
            }
        } else if (action.equals(Events.Emoticon.RECEIVED)) {
            giftsAdapter.notifyDataSetChanged();
        }
    }

    private final void refreshGiftItems() {
        if (giftsAdapter != null) {
            switch (mGiftFilterType) {
                case POPULAR:
                case NEW:
                    mGiftItems = StoreController.getInstance().getMainCategories(storeType, limit, offset,
                            mGiftFilterType);
                    if (mGiftItems != null) {
                        mGiftItems.filterListToLevel(Session.getInstance().getMigLevel());
                        giftsAdapter.setGiftList(mGiftItems.getListData());

                        // set the localCurrency in StoreController, so that
                        // search dialog of parent fragment
                        // which has no StoreItem in it can show it
                        String localCurrency = getLocalCurrency();
                        StoreController.getInstance().setLocalCurrency(localCurrency);
                    }
                    showOrHideFooter();
                    break;
                case CATEGORY:
                    mGiftItems = StoreController.getInstance().getStoreCategory(mGiftCategoryId,
                            StoreController.SORT_BY_NAME, StoreController.SORT_ORDER_ASC, StoreController.NOT_FEATURED,
                            limit, offset);
                    if (mGiftItems != null) {
                        mGiftItems.filterListToLevel(Session.getInstance().getMigLevel());
                        giftsAdapter.setGiftList(mGiftItems.getListData());
                    }
                    break;
                default:
                    break;
            }
        }

        showOrHideEmptyViewIfNeeded();
    }

    protected void showOrHideEmptyViewIfNeeded() {
        if (giftsAdapter == null || giftsAdapter.getCount() == 0) {
            if (emptyView == null) {
                emptyView = getEmptyView();
                container.addView(emptyView);
            }
        } else {
            if (emptyView != null) {
                emptyView.clearAnimation();
                container.removeView(emptyView);
                emptyView = null;
            }
        }
    }

    private View getEmptyView() {
        View emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.empty_view_loading, null);
        ImageView loadingIcon = (ImageView) emptyView.findViewById(R.id.loading_icon);
        loadingIcon.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.loading_icon_rotate));
        return emptyView;

    }

    private void showOrHideFooter() {
        if (mGiftItems == null || mGiftItems.getListData().length <= 0) {
            if (giftGridFooter != null) {
                giftGridFooter.setVisibility(View.GONE);
            }
        } else {
            if (giftGridFooter != null) {
                giftGridFooter.setVisibility(View.VISIBLE);
            }
        }
    }

    private void hideFooter() {
        if (giftGridFooter != null) {
            giftGridFooter.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(View v, StoreItem data) {
        if (!data.isAvailableForLevel(Session.getInstance().getMigLevel())) {
            Tools.showToast(this.getActivity(),
                    String.format(I18n.tr("This gift will be unlocked when you reach Level %s."), data.getMigLevelMin()));
        } else {
            if (StoreController.getInstance().canPurchaseItem(data.getPrice())) {
                if (mIsFromSinglePost) {
                    ActionHandler.getInstance().displaySinglePostGiftPreviewFragment(getActivity(), data.getId().toString(), mRootPostId, mParentPostId);
                } else {
                    if (ChatUtils.canUseSelectedUsersForPrivateChat(mConversation, mSelectedUsers)) {
                        ActionHandler.getInstance().displayGiftPreviewFragment(getActivity(), data.getId().toString(),
                                mConversationId, false, Constants.BLANKSTR, mSelectedUsers);

                    } else if (ChatUtils.canUseSelectedUsersForGroupChat(mConversation, mSelectedUsers) ||
                            ChatUtils.canUseSelectedUsersForChatroom(mConversation)) {
                        ActionHandler.getInstance().displayGiftRecipientSelectionFragment(getActivity(),
                                data.getId().toString(), mConversationId, false, mSelectedUsers);
                    }
                }
            } else {
                ActionHandler.getInstance().displayAccountBalance(getActivity());
            }
        }
    }

    @Override
    public void onItemLongClick(View v, StoreItem data) {
    }

    private String getLocalCurrency() {
        if (mGiftItems != null) {
            StoreItem[] storeItem = mGiftItems.getListData();

            if (storeItem != null && storeItem.length > 0) {
                return storeItem[0].getLocalCurrency();
            }
        }
        return Constants.BLANKSTR;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.gift_category_footer_container:
                Intent resultIntent = new Intent();
                resultIntent.putExtra(PARAM_INITIAL_RECIPIENT, mInitialRecipient);
                onActivityResult(Constants.REQ_SHOW_GIFT_CENTER_FROM_CHAT,
                        Constants.RESULT_FROM_GIFT_CENTER_SHOW_STORE, resultIntent);
                break;
            default:
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == Constants.REQ_SHOW_GIFT_CENTER_FROM_CHAT
                && resultCode == Constants.RESULT_FROM_GIFT_CENTER_SHOW_STORE) {

            String initialRecipient = intent.getStringExtra(GiftCenterFragment.PARAM_INITIAL_RECIPIENT);
            ActionHandler.getInstance().displayStoreFromChat(getActivity(), initialRecipient);
        }
    }

}
