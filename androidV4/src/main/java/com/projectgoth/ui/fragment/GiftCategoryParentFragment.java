/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftCategoryParentFramgment.java
 * Created 27 May, 2014, 3:09:19 pm
 */

package com.projectgoth.ui.fragment;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.controller.StoreController.StoreItemFilterType;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.ui.activity.FragmentHandler;
import com.projectgoth.util.ChatUtils;

/**
 * @author Dan
 * 
 *         this Fragment is for adding a header on top of the
 *         GiftCenterGiftCategoryFragment, because we don't want to implement the
 *         header directly in the GiftCenterGiftCategoryFragment,
 *         which is used as a child fragment in GiftCenterFragment
 */
public class GiftCategoryParentFragment extends BaseDialogFragment implements OnClickListener {

    private StoreItemFilterType mGiftFilterType;
    private String              mConversationId;
    private String              mGiftCategoryName;
    private String              mGiftCategoryId;
    private String              mInitialRecipient;
    // for start chat with a gift message, the conversation is not created yet
    private ArrayList<String>   mSelectedUsers;

    private ImageView           mCloseBtn;
    private ChatConversation    mConversation;

    public static final String  PARAM_CONVERSATION_ID    = "PARAM_CONVERSATION_ID";
    public static final String  PARAM_GIFT_FILTER_TYPE   = "PARAM_GIFT_FILTER_TYPE";
    public static final String  PARAM_GIFT_CATEGORY_ID   = "PARAM_GIFT_CATEGORY_ID";
    public static final String  PARAM_GIFT_CATEGORY_NAME = "PARAM_GIFT_CATEGORY_NAME";
    public static final String  PARAM_INITIAL_RECIPIENT  = "PARAM_INITIAL_RECIPIENT";
    public static final String  PARAM_SELECTED_USERS     = "PARAM_SELECTED_USERS";

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        mGiftFilterType = StoreItemFilterType.fromValue(args.getInt(PARAM_GIFT_FILTER_TYPE));
        mConversationId = args.getString(PARAM_CONVERSATION_ID);
        mGiftCategoryId = args.getString(PARAM_GIFT_CATEGORY_ID);
        mGiftCategoryName = args.getString(PARAM_GIFT_CATEGORY_NAME);
        mInitialRecipient = args.getString(PARAM_INITIAL_RECIPIENT);
        mSelectedUsers = args.getStringArrayList(PARAM_SELECTED_USERS);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_gift_category_parent;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mConversation = ChatDatastore.getInstance().getChatConversationWithId(mConversationId);

        ImageView headerIcon = (ImageView) view.findViewById(R.id.icon);
        TextView headerTitle = (TextView) view.findViewById(R.id.title);
        TextView headerStep = (TextView) view.findViewById(R.id.step_count);

        headerIcon.setImageResource(R.drawable.ad_gift_grey);
        headerTitle.setText(I18n.tr("Select gift"));

        if (ChatUtils.canUseSelectedUsersForPrivateChat(mConversation, mSelectedUsers)) {
            headerStep.setText(String.format(I18n.tr("%d of %d"), 1, 2));
        } else if (ChatUtils.canUseSelectedUsersForGroupChat(mConversation, mSelectedUsers)
                || ChatUtils.canUseSelectedUsersForChatroom(mConversation)) {
            headerStep.setText(String.format(I18n.tr("%d of %d"), 1, 3));
        }

        mCloseBtn = (ImageView) view.findViewById(R.id.close_button);
        mCloseBtn.setOnClickListener(this);

        GiftCenterGiftCategoryFragment fragment = FragmentHandler.getInstance().getGiftCenterGiftCategoryFragment(
                mGiftFilterType, mConversationId, mGiftCategoryId, mGiftCategoryName, mInitialRecipient, mSelectedUsers);
        addChildFragment(R.id.gift_category_fragment_holder, fragment);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.close_button:
                FragmentHandler.getInstance().clearBackStack();
            default:
                break;
        }
    }
}
