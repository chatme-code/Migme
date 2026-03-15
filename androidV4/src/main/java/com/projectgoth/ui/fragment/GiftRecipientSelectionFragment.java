/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftRecipientSelectionFragment.java
 * Created Aug 4, 2014, 11:33:52 PM
 */

package com.projectgoth.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.b.data.StoreItem;
import com.projectgoth.blackhole.enums.ChatParticipantType;
import com.projectgoth.common.Constants;
import com.projectgoth.common.TextUtils;
import com.projectgoth.controller.StoreController;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.nemesis.model.ChatParticipant;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.FragmentHandler;
import com.projectgoth.ui.adapter.GiftRecipientAdapter;
import com.projectgoth.ui.holder.BaseViewHolder.BaseViewListener;
import com.projectgoth.ui.widget.ButtonEx;
import com.projectgoth.util.ChatUtils;

/**
 * @author mapet
 * 
 */
public class GiftRecipientSelectionFragment extends BaseDialogFragment implements OnClickListener,
        BaseViewListener<ChatParticipant> {

    private String               mGiftItemId;
    private StoreItem            mGiftItem;
    private String               mConversationId;
    private boolean              mIsFromRecent;

    private ListView             recipientListView;
    private GiftRecipientAdapter recipientAdapter;

    private ImageView            mBackBtn;
    private ImageView            mCloseBtn;
    private TextView             mHeaderStep;
    private TextView             mTotalPrice;
    private ButtonEx             mNextBtn;

    private ChatConversation     mConversation;
    private String               mSelectedRecipient;
    private float                totalPrice;
    //for start chat with a gift message, the conversation is not created yet
    private ArrayList<String>    mSelectedUsers;

    private final String         RECIPIENT_EVERYONE       = I18n.tr("Everyone");

    public static final String   PARAM_GIFT_ITEM_ID       = "PARAM_GIFT_ITEM_ID";
    public static final String   PARAM_CONVERSATION_ID    = "PARAM_CONVERSATION_ID";
    public static final String   PARAM_SELECTED_RECIPIENT = "PARAM_SELECTED_RECIPIENT";
    public static final String   PARAM_IS_FROM_RECENT     = "PARAM_IS_FROM_RECENT";
    public static final String   PARAM_SELECTED_USERS     = "PARAM_SELECTED_USERS";

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        mGiftItemId = args.getString(PARAM_GIFT_ITEM_ID);
        mConversationId = args.getString(PARAM_CONVERSATION_ID);
        mSelectedRecipient = args.getString(PARAM_SELECTED_RECIPIENT);
        mIsFromRecent = args.getBoolean(PARAM_IS_FROM_RECENT);
        mSelectedUsers = args.getStringArrayList(PARAM_SELECTED_USERS);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_gift_recipient_selection;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mConversation = ChatDatastore.getInstance().getChatConversationWithId(mConversationId);

        mGiftItem = StoreController.getInstance().getStoreItem(mGiftItemId);

        recipientListView = (ListView) view.findViewById(R.id.recipient_list);

        recipientAdapter = new GiftRecipientAdapter();
        recipientAdapter.setParticipantClickListener(this);
        recipientAdapter.setSelectedParticipant(mSelectedRecipient);

        recipientListView.setAdapter(recipientAdapter);

        updateParticipantsData();

        ImageView headerIcon = (ImageView) view.findViewById(R.id.icon);
        TextView headerTitle = (TextView) view.findViewById(R.id.title);
        mHeaderStep = (TextView) view.findViewById(R.id.step_count);
        mCloseBtn = (ImageView) view.findViewById(R.id.close_button);
        mBackBtn = (ImageView) view.findViewById(R.id.back_button);

        mTotalPrice = (TextView) view.findViewById(R.id.total_price);
        mNextBtn = (ButtonEx) view.findViewById(R.id.next_button);

        headerIcon.setImageResource(R.drawable.ad_user_grey);
        headerTitle.setText(I18n.tr("Select friends"));

        if (ChatUtils.canUseSelectedUsersForGroupChat(mConversation, mSelectedUsers) ||
                ChatUtils.canUseSelectedUsersForChatroom(mConversation)) {
            if (mIsFromRecent) {
                mHeaderStep.setText(String.format(I18n.tr("%d of %d"), 1, 2));
            } else {
                mHeaderStep.setText(String.format(I18n.tr("%d of %d"), 2, 3));
            }
        }
        
        mBackBtn.setVisibility(View.VISIBLE);

        mTotalPrice.setText(String.format(I18n.tr("Total: %s"), Constants.BLANKSTR));
        mNextBtn.setText(I18n.tr("NEXT"));

        mBackBtn.setOnClickListener(this);
        mCloseBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.MigStore.Item.RECEIVED);
        registerEvent(Events.Emoticon.RECEIVED);
        registerEvent(Events.ChatParticipant.FETCH_ALL_COMPLETED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(Events.ChatParticipant.FETCH_ALL_COMPLETED) || action.equals(Events.MigStore.Item.RECEIVED)
                || action.equals(Events.Emoticon.RECEIVED)) {
            updateParticipantsData();
        }
    }

    private void updateParticipantsData() {
        mGiftItem = StoreController.getInstance().getStoreItem(mGiftItemId);

        if (mGiftItem != null) {

            List<ChatParticipant> participantList = new ArrayList<ChatParticipant>();

            if (mConversation != null && (mConversation.isMigGroupChat() || mConversation.isChatroom())) {

                participantList = ChatDatastore.getInstance().getParticipantsForChatConversationWithId(
                            mConversationId, false);


            } else if (mSelectedUsers != null && mSelectedUsers.size() > 0 ) {
                for(String username : mSelectedUsers) {
                    ChatParticipant user = new ChatParticipant(Constants.BLANKSTR, ChatParticipantType.NORMAL);
                    user.setUsername(username);
                    participantList.add(user);
                }
            }

            ChatParticipant everyone = new ChatParticipant(Constants.BLANKSTR, ChatParticipantType.NORMAL);
            everyone.setUsername(RECIPIENT_EVERYONE);
            participantList.add(0, everyone);

            recipientAdapter.setParticipantsList(participantList);
        }
    }

    @Override
    public void onItemClick(View v, ChatParticipant data) {
        mSelectedRecipient = data.getUsername();
        recipientAdapter.setSelectedParticipant(mSelectedRecipient);

        if (mConversation != null && mSelectedRecipient.equals(RECIPIENT_EVERYONE)) {
            List<ChatParticipant> participantList = ChatDatastore.getInstance()
                .getParticipantsForChatConversationWithId(mConversationId, false);

            if (participantList != null && participantList.size() >= 1) {
                totalPrice = mGiftItem.getRoundedPrice(participantList.size());
            }
        } else if (mSelectedUsers != null && mSelectedRecipient.equals(RECIPIENT_EVERYONE)) {
            totalPrice = mGiftItem.getRoundedPrice(mSelectedUsers.size());
        } else {
            totalPrice = mGiftItem.getRoundedPrice();
        }

        mTotalPrice.setText(String.format(I18n.tr("Total: %s"), Float.toString(totalPrice) + mGiftItem.getLocalCurrency()));

        recipientAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemLongClick(View v, ChatParticipant data) {
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.next_button:
                if (!TextUtils.isEmpty(mSelectedRecipient) && StoreController.getInstance().canPurchaseItem(totalPrice)) {
                    ActionHandler.getInstance().displayGiftPreviewFragment(getActivity(), mGiftItem.getId().toString(),
                            mConversationId, mIsFromRecent, mSelectedRecipient, mSelectedUsers);
                } else {
                    ActionHandler.getInstance().displayAccountBalance(getActivity());
                }
                break;
            case R.id.close_button:
                FragmentHandler.getInstance().clearBackStack();
                break;
            case R.id.back_button:
                closeFragment();
                break;
            default:
                break;
        }
    }

}
