package com.projectgoth.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.StoreItem;
import com.projectgoth.b.data.StoreItems;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.common.WebURL;
import com.projectgoth.controller.ChatController;
import com.projectgoth.controller.StoreController;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.events.GAEvent;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.i18n.I18n;
import com.projectgoth.listener.SendGiftCommandListener;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.model.StorePagerItem;
import com.projectgoth.nemesis.model.Friend;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.listeners.GetStoreItemListener;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.FragmentHandler;
import com.projectgoth.ui.adapter.HorizontalGiftListAdapter;
import com.projectgoth.ui.adapter.HorizontalRecipientListAdapter;
import com.projectgoth.ui.holder.BaseViewHolder;
import com.projectgoth.ui.widget.ButtonEx;
import com.projectgoth.ui.widget.HorizontalListViewEx;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by houdangui on 17/6/15.
 */
public class SendGiftFragment extends BaseDialogFragment implements View.OnClickListener, BaseViewHolder.BaseViewListener<StoreItem>,HorizontalRecipientListAdapter.RecipientItemListener, AdapterView.OnItemClickListener {

    private TextView                        mTitle;
    private TextView                        mBalance;
    private HorizontalListViewEx            mGiftList;
    private TextView                        mSendTo;
    private HorizontalListViewEx            mRecipientList;
    private EditText                        mGiftMessage;
    private ButtonEx                        mSendGift;
    private TextView                        mSendGiftHint;
    private TextView                        mTotalCost;
    private HorizontalGiftListAdapter       mGiftListAdapter;
    private HorizontalRecipientListAdapter  mRecipientListAdapter;
    private ListView                        mFriendList;
    private EditText                        mFriendListFilter;
    private ArrayAdapter<String>            mFriendListAdapter;
    private LinearLayout                    mFriendListContainer;
    protected boolean                       mIsFriendListDisplayed;
    private ArrayList<String>               mRecipients                 = new ArrayList<>();
    private StoreItem                       mSelectedGift;
    private TextView                        mRecharge;
    private TextView                        mRechargeHint2;
    private LinearLayout                    mRechargeBody;
    private ArrayList<String>               mSelectedUsers;
    private ActionType                      mActionType;
    private String                          mConversationId;
    private ChatConversation                mConversation;
    private String                          mRootPostId;
    private String                          mPostId;
    private List<Friend>                    mFusionFriendList;
    private ScrollView                      mContainer;
    static final private int                GIFT_NUM                    = 10;
    private String                          mAccountBalance             = "";
    public static final String              PARAM_RECIPIENT             = "PARAM_RECIPIENT";
    public static final String              PARAM_ACTION_TYPE           = "PARAM_ACTION_TYPE";
    public static final String              PARAM_CONVERSATION_ID       = "PARAM_CONVERSATION_ID";
    public static final String              PARAM_POST_ROOT_ID          = "PARAM_POST_ROOT_ID";
    public static final String              PARAM_POST_ID               = "PARAM_POST_ID";

    enum SendCondition {BALANCE, NORMAL, UNAVAILABLE}

    public enum ActionType {
        GIFT_IN_CHAT(0),
        GIFT_TO_POST(1);

        private int type;

        private ActionType(int type) {
            this.type = type;
        }

        public int getType() {
            return this.type;
        }

        public static ActionType fromValue(int type) {
            for (ActionType listType : values()) {
                if (listType.getType() == type) {
                    return listType;
                }
            }
            return GIFT_IN_CHAT;
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_send_gift;
    }

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        mRecipients = args.getStringArrayList(PARAM_RECIPIENT);
        mActionType = ActionType.fromValue(args.getInt(PARAM_ACTION_TYPE));
        mConversationId = args.getString(PARAM_CONVERSATION_ID);
        mConversation = ChatDatastore.getInstance().getChatConversationWithId(mConversationId);
        mRootPostId = args.getString(PARAM_POST_ROOT_ID);
        mPostId = args.getString(PARAM_POST_ID);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getAccountBalance();
        mFriendList = (ListView) view.findViewById(R.id.friend_list);
        mFriendList.setOnItemClickListener(this);
        mBalance = (TextView) view.findViewById(R.id.balance);
        mBalance.setTextColor(getResources().getColor(R.color.gift_balance));
        mFriendListFilter = (EditText) view.findViewById(R.id.friend_list_filter);
        mRecharge = (TextView) view.findViewById(R.id.recharge);
        mRechargeBody = (LinearLayout) view.findViewById(R.id.recharge_body);
        mRechargeHint2 = (TextView) view.findViewById(R.id.send_gift_hint2);
        mTitle = (TextView) view.findViewById(R.id.title);
        mTitle.setText(I18n.tr("Send gift"));
        mTotalCost = (TextView) view.findViewById(R.id.total_cost);
        mContainer = (ScrollView) view.findViewById(R.id.scrollview_container);
        mGiftList = (HorizontalListViewEx) view.findViewById(R.id.gift_list);
        HorizontalScrollView.LayoutParams lp = new HorizontalScrollView.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        mGiftList.setItemLayoutParams(lp);
        mGiftListAdapter = new HorizontalGiftListAdapter();
        mGiftList.setAdapter(mGiftListAdapter);
        mGiftListAdapter.setItemClickListener(this);
        mFriendListContainer = (LinearLayout) view.findViewById(R.id.friend_list_container);
        updateGiftListView();
        mSendGiftHint = (TextView) view.findViewById(R.id.send_gift_hint);
        mRecipientList = (HorizontalListViewEx) view.findViewById(R.id.recipient_list_horizontal);
        mRecipientList.setItemLayoutParams(lp);
        mRecipientListAdapter = new HorizontalRecipientListAdapter();
        mRecipientListAdapter.setListener(this);
        mRecipientList.setAdapter(mRecipientListAdapter);
        updateRecipientListView();
        mSendTo = (TextView) view.findViewById(R.id.send_to);
        mSendTo.setText(I18n.tr("Send to"));
        mGiftMessage = (EditText) view.findViewById(R.id.gift_msg);
        mGiftMessage.setHint(I18n.tr("Add message (optional)"));
        mSendGift = (ButtonEx) view.findViewById(R.id.send_gift);
        mSendGift.setText(I18n.tr("SEND GIFT"));
        mSendGift.setOnClickListener(this);
        sendGiftCondition(SendCondition.NORMAL);

    }

    private void updateGiftListView() {
        StoreItems storeItems = StoreController.getInstance().getMainCategories(
                StorePagerItem.StorePagerType.GIFTS.getValue(),
                GIFT_NUM, 0, StoreController.StoreItemFilterType.FEATURED);

        if (storeItems != null && storeItems.getListData().length > 0) {

            mGiftListAdapter.setGiftItems(storeItems.getListData());
            //select the first one by default
            if (mSelectedGift == null) {
                mSelectedGift = storeItems.getListData()[0];
                mGiftListAdapter.setSelectedItem(mSelectedGift);
            } else {
                //the store items might be updated, but mSelectedGift stills
                //remains as one item of the previous items
                resetSelectedGift(storeItems.getListData());
                mGiftListAdapter.setSelectedItem(mSelectedGift);
            }
            mBalance.setText(String.format("%s %s", mSelectedGift.getLocalCurrency(), mAccountBalance));

            //refresh the views
            mGiftList.notifyDataSetChanged();
            showTotalCost();
        }

    }

    private void resetSelectedGift(StoreItem[] giftItemList) {
        boolean found = false;
        for (StoreItem storeItem : giftItemList) {
            if (storeItem.getId() == mSelectedGift.getId()) {
                mSelectedGift = storeItem;
                found = true;
                break;
            }
        }

        if (!found) {
            mSelectedGift = giftItemList[0];
        }
    }

    private void updateRecipientListView() {
        mSelectedUsers = new ArrayList<>();
        mRecipientListAdapter.setUsernames(mRecipients);
        boolean showSelectAll;
        if (mRecipients.size() > 1) {
            showSelectAll = true;
        } else {
            showSelectAll = false;
        }
        mRecipientListAdapter.setShowSelectAll(showSelectAll);
        //select the only one by default
        if (mRecipients.size() == 1) {
            if (!isRecipientSelected()) {
                mSelectedUsers.add(mRecipients.get(0));
                mRecipientListAdapter.setSelectedUsers(mSelectedUsers);
            }
        } else if (mRecipients.size() > 1) {
            for (String recipient : mRecipients) {
                mSelectedUsers.add(recipient);
            }
            mRecipientListAdapter.setSelectedUsers(mSelectedUsers);
        }

        mRecipientList.notifyDataSetChanged();
        showTotalCost();
    }

    private void addRecipients(String username) {
        mRecipients.add(username);
        mSelectedUsers.add(username);
        updateRecipientListView();
        showTotalCost();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.send_gift:
                processSendGift();
                break;
        }
    }

    private boolean isRecipientSelected() {
        if (mSelectedUsers != null) {
            return mSelectedUsers.size() > 0 ? true : false;
        }
        return false;
    }

    private void processSendGift() {
        if (!isRecipientSelected() || mSendGift == null) {
            return;
        }

        if (mActionType == ActionType.GIFT_IN_CHAT) {
            sendGiftInChat();
        } else if (mActionType == ActionType.GIFT_TO_POST) {
            sendGiftInPost();
        }
    }

    private void sendGiftInChat() {
        Tools.showToast(getActivity(), I18n.tr("Sending gift"), Toast.LENGTH_SHORT);
        SendGiftCommandListener sendGiftListener = new SendGiftCommandListener(mSelectedGift, mConversationId);
        if (mConversation.isChatroom() || mConversation.isMigGroupChat()) {
            if (!isAllSelected()) {
                ChatController.getInstance().sendGift(mConversationId, mSelectedUsers.get(0), mSelectedGift.getName(), mGiftMessage.getText().toString(), sendGiftListener);
            } else {
                ChatController.getInstance().sendGift(mConversationId, mSelectedGift.getName(), mGiftMessage.getText().toString(), sendGiftListener);
            }
        } else {
            ChatController.getInstance().sendGift(mConversationId, mConversation.getDisplayName(),
                    mSelectedGift.getName(), mGiftMessage.getText().toString(), sendGiftListener);
        }
    }

    private void sendGiftInPost() {
        Tools.showToast(getActivity(), I18n.tr("Sending gift"), Toast.LENGTH_SHORT);

        String recipientsStr = Constants.BLANKSTR;

        if (mSelectedUsers != null && isRecipientSelected()) {
            StringBuilder buff = new StringBuilder();
            String sep = Constants.BLANKSTR;
            for (String str : mSelectedUsers) {
                buff.append(sep);
                buff.append(str);
                sep = ",";
            }
            recipientsStr = buff.toString();
        }

        final ApplicationEx appEx = ApplicationEx.getInstance();
        if (appEx != null) {
            final RequestManager requestManager = appEx.getRequestManager();
            if (requestManager != null) {
                requestManager.purchaseSinglePostGiftItem(mGetStoreItemListener, Session.getInstance().getUserId(), String.valueOf(mSelectedGift.getId())
                        , "false", recipientsStr, mGiftMessage.getText().toString(), mRootPostId, mPostId);
            }
        }

    }

    private GetStoreItemListener mGetStoreItemListener = new GetStoreItemListener() {
        @Override
        public void onStoreItemReceived(String key, StoreItem storeItem) {
            BroadcastHandler.MigStore.Item.sendReceived();
        }

        @Override
        public void onStoreItemPurchased(String key, StoreItem storeItem) {
            BroadcastHandler.MigStore.Item.sendPurchased(storeItem.getId());
        }

        @Override
        public void onStoreItemPurchaseError(final MigError error) {
            BroadcastHandler.MigStore.Item.sendPurchaseError(error);
            if (error.getErrorMsg().contains("Insufficient Credit")) {
                sendGiftCondition(SendCondition.BALANCE);
            }
        }

        @Override
        public void onStoreUnlockedItemSent(String key, StoreItem storeItem) {
            BroadcastHandler.MigStore.Item.sendUnlockedItem();
        }
    };

    private boolean isAllSelected() {
        return mRecipients.size() == mSelectedUsers.size();
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.MigStore.Item.FETCH_FOR_FEATURED_COMPLETED);
        registerEvent(Events.MigStore.Item.PURCHASED);
        registerEvent(Events.MigStore.Item.PURCHASE_ERROR);
        registerEvent(Events.Emoticon.FETCH_ALL_COMPLETED);
        registerEvent(Events.User.AVATAR_RECEIVED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Events.MigStore.Item.FETCH_FOR_FEATURED_COMPLETED)) {
            updateGiftListView();
        } else if (action.equals(Events.Emoticon.FETCH_ALL_COMPLETED)) {
            updateGiftListView();
        } else if (action.equals(Events.MigStore.Item.PURCHASED)) {
            FragmentHandler.getInstance().clearBackStack();
            ActionHandler.getInstance().displayGiftSent(getActivity());
        } else if (action.equals(Events.MigStore.Item.PURCHASE_ERROR)) {
            Tools.showToastForIntent(context, intent);
        } else if (action.equals(Events.User.AVATAR_RECEIVED)) {
            updateRecipientListView();
        }
    }

    @Override
    public void onItemClick(View v, StoreItem data) {
        mSelectedGift = data;
        mGiftListAdapter.setSelectedItem(mSelectedGift);
        mGiftList.notifyDataSetChanged();
        showTotalCost();
    }

    @Override
    public void onItemLongClick(View v, StoreItem data) {

    }

    @Override
    public void onSelectAllClicked() {
        if (mSelectedUsers.size() == mRecipients.size()) {
            mSelectedUsers.clear();
            mRecipientListAdapter.setSelectedUsers(mSelectedUsers);
            mRecipientList.notifyDataSetChanged();
        } else {
            mSelectedUsers.clear();
            mSelectedUsers.addAll(mRecipients);
            mRecipientListAdapter.setSelectedUsers(mSelectedUsers);
            mRecipientList.notifyDataSetChanged();
        }
        if (isRecipientSelected()) {
            sendGiftCondition(SendCondition.NORMAL);
        } else {
            sendGiftCondition(SendCondition.UNAVAILABLE);
        }
        showTotalCost();
    }

    @Override
    public void onRecipientClicked(String username) {
        if (mSelectedUsers.contains(username)) {
            if (mActionType == ActionType.GIFT_IN_CHAT) {
                //because of server dependency SE-1103 we cannot let multiple selected happen
                if (mSelectedUsers.size() - 1 > 1) {
                    showTotalCost();
                    return;
                }
            }
            mSelectedUsers.remove(username);
            mRecipientListAdapter.setSelectedUsers(mSelectedUsers);
            mRecipientList.notifyDataSetChanged();
            showTotalCost();
        } else {
            if (mActionType == ActionType.GIFT_IN_CHAT) {
                //because of server dependency SE-1103 we only do single choice here
                mSelectedUsers.clear();
                mSelectedUsers.add(username);
            } else {
                mSelectedUsers.add(username);
            }
            mRecipientListAdapter.setSelectedUsers(mSelectedUsers);
            mRecipientList.notifyDataSetChanged();
            showTotalCost();
        }
        if (isRecipientSelected()) {
            sendGiftCondition(SendCondition.NORMAL);
        } else {
            sendGiftCondition(SendCondition.UNAVAILABLE);
        }
    }

    private String getTotalCost() {
        String totalCost = "0";
        if (mSelectedGift != null && mSelectedUsers != null) {
            int recipientAmount = mSelectedUsers.size();
            float roundedPrice = mSelectedGift.getRoundedPrice();
            if (recipientAmount >= 0 && roundedPrice >= 0) {
                totalCost = String.format(Locale.US, "%,.2f", (float) Math.round(recipientAmount * roundedPrice * 100) / 100);
            }
        }
        return totalCost;
    }

    private void showTotalCost() {
        if (mSelectedGift != null) {
            mTotalCost.setText(I18n.tr("Total : ") + String.format("%s %s", mSelectedGift.getLocalCurrency(), getTotalCost()));
        }
    }

    private void sendGiftCondition(final SendCondition condition) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setHint(condition);
                setSendButton(condition);
                setTotalCost(condition);
            }
        });
    }

    private void setTotalCost(SendCondition condition) {
        switch (condition) {
            case BALANCE:
                mTotalCost.setTextColor(getResources().getColor(R.color.gift_balance_red));
                break;
            case NORMAL:
            case UNAVAILABLE:
                mTotalCost.setTextColor(getResources().getColor(R.color.gift_balance_black));
                break;
        }
    }

    private void setSendButton(SendCondition condition) {
        switch (condition) {
            case BALANCE:
            case UNAVAILABLE:
                mSendGift.setBackgroundColor(getResources().getColor(R.color.send_gift_button_abnormal));
                break;
            case NORMAL:
                mSendGift.setBackgroundColor(getResources().getColor(R.color.send_gift_button_normal));
                break;
        }
    }

    private void setHint(SendCondition condition) {
        switch (condition) {
            case BALANCE:
                mRechargeBody.setVisibility(View.VISIBLE);
                mSendGiftHint.setText(I18n.tr("You don't have enough credits."));
                mSendGiftHint.setVisibility(View.VISIBLE);
                mRechargeHint2.setText(I18n.tr("Please "));
                mRechargeHint2.setVisibility(View.VISIBLE);
                mRecharge.setText(I18n.tr("recharge"));
                mRecharge.setVisibility(View.VISIBLE);
                mRecharge.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GAEvent.Chat_SendGiftUiRecharge.send();
                        ActionHandler.getInstance().displayRechargeCreditsFromChat(getActivity(), WebURL.URL_ACCOUNT_SETTINGS,
                                I18n.tr("Buy credit"), R.drawable.ad_credit_white);
                        FragmentHandler.getInstance().clearBackStack();
                    }
                });
                break;
            case NORMAL:
                mSendGiftHint.setText("");
                mSendGiftHint.setVisibility(View.GONE);
                mRechargeBody.setVisibility(View.VISIBLE);
                mRecharge.setOnClickListener(null);
                mRecharge.setVisibility(View.GONE);
                mRechargeHint2.setVisibility(View.GONE);

                break;
            case UNAVAILABLE:
                mSendGiftHint.setText(I18n.tr("Oops! whom do you want to send to ?"));
                mSendGiftHint.setVisibility(View.VISIBLE);
                mRechargeBody.setVisibility(View.VISIBLE);
                mRecharge.setOnClickListener(null);
                mRecharge.setVisibility(View.GONE);
                mRechargeHint2.setVisibility(View.GONE);
                break;
        }
    }

    protected void showFriendList() {
        getFriendList();
        mFriendListContainer.setVisibility(View.VISIBLE);
        mContainer.setVisibility(View.GONE);
        mFriendListFilter.requestFocus();
        mIsFriendListDisplayed = true;
    }

    private void hideFriendList() {
        mFriendListFilter.setText(Constants.BLANKSTR);
        mFriendList.clearTextFilter();
        mFriendListContainer.setVisibility(View.GONE);
        mContainer.setVisibility(View.VISIBLE);
        mFriendListFilter.clearFocus();
        mIsFriendListDisplayed = false;
    }

    private void getFriendList() {
        mFusionFriendList = UserDatastore.getInstance().getAllFusionFriends(false, false);
        mFriendListAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
        if (mFusionFriendList != null && mFusionFriendList.size() > 0) {
            mFriendListAdapter.clear();
            for (Friend friend : mFusionFriendList) {
                // If this friend has already been selected,
                // then do not add it to the list of friends that can be
                // selected from.
                if (!mSelectedUsers.contains(friend.getUsername())) {
                    mFriendListAdapter.add(friend.getUsername());
                }
            }
            mFriendListAdapter.notifyDataSetChanged();
            mFriendList.setAdapter(mFriendListAdapter);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mIsFriendListDisplayed) {
            String friend = mFriendListAdapter.getItem(position);
            addRecipients(friend);
            hideFriendList();
            Tools.hideVirtualKeyboard(getActivity());
        }
    }

    private String getAccountBalance() {
        String origin = Session.getInstance().getAccountBalance();
        if (origin.isEmpty()) {
            mAccountBalance = "0";
            return mAccountBalance;
        }
        String regEx = "[^0-9,.]";
        String regExNotInt = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(origin);
        String numberString = m.replaceAll("").trim();
        int decimalPointPosition = -1;

        for (int i = numberString.length() - 1; i >= 0; i--) {
            if (!Character.isDigit(numberString.charAt(i))) {
                decimalPointPosition = i;
                break;
            }
        }

        if (decimalPointPosition != -1) {
            String intPart = numberString.substring(0, decimalPointPosition);
            p = Pattern.compile(regExNotInt);
            m = p.matcher(intPart);
            String filteredIntPart = m.replaceAll("").trim(); //Might have . or , in intPart, remove them.
            String result = filteredIntPart + "." + numberString.substring(decimalPointPosition + 1);
            mAccountBalance = result;
        } else {
            mAccountBalance = origin;
        }
        return mAccountBalance;
    }

    @Override
    public void onAddMoreClicked() {
//        showFriendList();
    }

    public void onResume() {
        super.onResume();
        sendGiftCondition(SendCondition.NORMAL);
    }
}