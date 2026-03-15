/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftPreviewFragment.java
 * Created 12 May, 2014, 4:33:09 pm
 */

package com.projectgoth.ui.fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.widget.Toast;
import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Post;
import com.projectgoth.b.data.StoreItem;
import com.projectgoth.blackhole.enums.MessageType;
import com.projectgoth.common.Constants;
import com.projectgoth.common.TextUtils;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.ChatController;
import com.projectgoth.controller.EmoticonsController;
import com.projectgoth.controller.ProgressDialogController;
import com.projectgoth.controller.StoreController;
import com.projectgoth.datastore.ChatDatastore;
import com.projectgoth.datastore.PostsDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.listener.SendGiftCommandListener;
import com.projectgoth.model.ChatConversation;
import com.projectgoth.nemesis.RequestManager;
import com.projectgoth.nemesis.listeners.ChatListener;
import com.projectgoth.nemesis.listeners.GetStoreItemListener;
import com.projectgoth.nemesis.model.Friend;
import com.projectgoth.nemesis.model.MigError;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.FragmentHandler;
import com.projectgoth.ui.widget.ButtonEx;
import com.projectgoth.ui.widget.FlowLayout;
import com.projectgoth.ui.widget.UserBasicDetails;
import com.projectgoth.util.ChatUtils;
import com.projectgoth.util.PostUtils;

/**
 * @author dan
 */

public class GiftPreviewFragment extends BaseDialogFragment implements OnClickListener, View.OnKeyListener, AdapterView.OnItemClickListener {

    private String             mGiftItemId;
    private StoreItem          mGiftItem;
    private String             mConversationId;
    private boolean            mIsFromRecent;

    private ImageView          mGiftImage;
    private TextView           mGiftName;
    private TextView           mGiftPrice;
    private EditText           mMessageEdit;
    private ButtonEx           mSendGift;
    private LinearLayout       mGiftClass;
    private ImageView          mGiftClassIcon;
    private TextView           mGiftClassDescription;
    private TextView           mGiftRecipient;

    private String             mSelectedRecipient       = Constants.BLANKSTR;
    private final String       RECIPIENT_EVERYONE       = I18n.tr("Everyone");

    /**
     * this is listener for sending gift command message for storing the used
     * gift if server returns OK
     */
    private ChatListener       sendGiftListener;

    private ImageView          mBackBtn;
    private ImageView          mCloseBtn;
    private TextView           mHeaderStep;
    private ChatConversation   mConversation;
    //for start chat with a gift message, the conversation is not created yet
    private ArrayList<String>  mSelectedUsers;
    private boolean            mIsFromSinglePost;
    private CheckBox           mRepostCheckBox;
    private String             mRootPostId;
    private String             mParentPostId;

    private FlowLayout         mRecipientContainer;
    private RelativeLayout     mMainRecipientContainer;

    private LinearLayout       mFriendListContainer;
    private ListView           mFriendList;
    private EditText           mFriendListFilter;
    private ArrayAdapter<String> mFriendListAdapter;
    protected boolean          mIsFriendListDisplayed;
    private List<Friend>       mFusionFriendList;
    protected Set<String>      mSelectedList              = new HashSet<String>();
    protected int              mRecipientLimit = 5;

    public static final String PARAM_GIFT_ITEM_ID         = "PARAM_GIFT_ITEM_ID";
    public static final String PARAM_CONVERSATION_ID      = "PARAM_CONVERSATION_ID";
    public static final String PARAM_GROUP_ID             = "PARAM_GROUP_ID";
    public static final String PARAM_IS_FROM_RECENT       = "PARAM_IS_FROM_RECENT";
    public static final String PARAM_SELECTED_RECIPIENT   = "PARAM_SELECTED_RECIPIENT";
    public static final String PARAM_SELECTED_USERS       = "PARAM_SELECTED_USERS";
    public static final String PARAM_IS_FROM_SINGLE_POST  = "PARAM_IS_FROM_SINGLE_POST";
    public static final String PARAM_POST_ROOT_ID         = "PARAM_POST_ROOT_ID";
    public static final String PARAM_POST_PARENT_ID       = "PARAM_POST_PARENT_ID";

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        mGiftItemId = args.getString(PARAM_GIFT_ITEM_ID);
        mConversationId = args.getString(PARAM_CONVERSATION_ID);
        mIsFromRecent = args.getBoolean(PARAM_IS_FROM_RECENT);
        mSelectedRecipient = args.getString(PARAM_SELECTED_RECIPIENT);
        mSelectedUsers = args.getStringArrayList(PARAM_SELECTED_USERS);
        mIsFromSinglePost = args.getBoolean(PARAM_IS_FROM_SINGLE_POST, false);
        mRootPostId = args.getString(PARAM_POST_ROOT_ID);
        mParentPostId = args.getString(PARAM_POST_PARENT_ID);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_gift_preview;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mConversation = ChatDatastore.getInstance().getChatConversationWithId(mConversationId);

        mGiftItem = StoreController.getInstance().getStoreItem(mGiftItemId);

        mGiftClass = (LinearLayout) view.findViewById(R.id.gift_class);
        mGiftClassIcon = (ImageView) view.findViewById(R.id.gift_class_icon);
        mGiftClassDescription = (TextView) view.findViewById(R.id.gift_class_description);

        mGiftRecipient = (TextView) view.findViewById(R.id.gift_recipient);

        if (mGiftItem != null && !mIsFromSinglePost) {
            sendGiftListener = new SendGiftCommandListener(mGiftItem, mConversationId);
        }

        ImageView headerIcon = (ImageView) view.findViewById(R.id.icon);
        TextView headerTitle = (TextView) view.findViewById(R.id.title);
        mHeaderStep = (TextView) view.findViewById(R.id.step_count);

        headerIcon.setImageResource(R.drawable.ad_gift_grey);
        headerTitle.setText(I18n.tr("Preview"));

        mCloseBtn = (ImageView) view.findViewById(R.id.close_button);
        mCloseBtn.setOnClickListener(this);
        
        mBackBtn = (ImageView) view.findViewById(R.id.back_button);
        mBackBtn.setVisibility(View.VISIBLE);
        mBackBtn.setOnClickListener(this);

        if (mIsFromRecent) {
            if (mConversation.isMigPrivateChat()) {
                mHeaderStep.setVisibility(View.INVISIBLE);
            } else {
                mHeaderStep.setText(String.format(I18n.tr("%d of %d"), 2, 2));
            }
        } else {
            if (ChatUtils.canUseSelectedUsersForPrivateChat(mConversation, mSelectedUsers)) {
                mHeaderStep.setText(String.format(I18n.tr("%d of %d"), 2, 2));
            } else if (ChatUtils.canUseSelectedUsersForGroupChat(mConversation, mSelectedUsers) ||
                       ChatUtils.canUseSelectedUsersForChatroom(mConversation)) {
                mHeaderStep.setText(String.format(I18n.tr("%d of %d"), 3, 3));
            }
        }

        if (ChatUtils.canUseSelectedUsersForPrivateChat(mConversation, mSelectedUsers)) {
            String name = Constants.BLANKSTR;
            if (mConversation != null) {
                name = mConversation.getDisplayName();
            } else if (mSelectedUsers != null && mSelectedUsers.size()>=1) {
                name = mSelectedUsers.get(0);
            }
            mGiftRecipient.setText(String.format(I18n.tr("To: %s"), name));
        } else if (ChatUtils.canUseSelectedUsersForGroupChat(mConversation, mSelectedUsers) ||
                ChatUtils.canUseSelectedUsersForChatroom(mConversation)) {
            mGiftRecipient.setText(String.format(I18n.tr("To: %s"), mSelectedRecipient));
        }

        mGiftImage = (ImageView) view.findViewById(R.id.gift_image);

        mGiftName = (TextView) view.findViewById(R.id.gift_name);
        mGiftPrice = (TextView) view.findViewById(R.id.gift_price);

        TextView messageLabel = (TextView) view.findViewById(R.id.gift_msg_label);
        messageLabel.setText(I18n.tr("Add a note, make it special!"));

        mMessageEdit = (EditText) view.findViewById(R.id.gift_msg);
        mMessageEdit.setHint(I18n.tr("(Message optional)"));

        mSendGift = (ButtonEx) view.findViewById(R.id.send_gift);
        mSendGift.setOnClickListener(this);

        setGiftData();


        if (mIsFromSinglePost) {
            mRepostCheckBox = (CheckBox) view.findViewById(R.id.check_also_repost);
            mRepostCheckBox.setVisibility(View.VISIBLE);
            mRepostCheckBox.setText(I18n.tr("Also Repost"));
            mHeaderStep.setText(String.format(I18n.tr("%d of %d"), 2, 2));

            mMainRecipientContainer = (RelativeLayout) view.findViewById(R.id.recipients_container);
            mRecipientContainer = (FlowLayout) view.findViewById(R.id.selected_container);
            mMainRecipientContainer.setVisibility(View.VISIBLE);

            TextView recipientLabel = (TextView) view.findViewById(R.id.recipient_label);
            recipientLabel.setText(I18n.tr("Send to"));

            mFriendListContainer = (LinearLayout) view.findViewById(R.id.friend_list_container);
            mFriendList = (ListView) view.findViewById(R.id.friend_list);
            mFriendListFilter = (EditText) view.findViewById(R.id.friend_list_filter);
            mFriendListAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
            mFriendList.setAdapter(mFriendListAdapter);
            mRecipientContainer.setOnClickListener(this);

            mNumberOfRecipients = (TextView) view.findViewById(R.id.number_of_recipients);
            mTotalPrice = (TextView) view.findViewById(R.id.total_price);
            mNumberOfRecipients.setText(String.format(I18n.tr("Recipient(s): %d"), 0));
            mTotalPrice.setText(String.format(I18n.tr("Total: %s"), 0));

            mMainRecipientContainer.setOnClickListener(this);
            mRecipientContainer.setOnClickListener(this);
            mFriendList.setOnItemClickListener(this);
            mFriendListFilter.setOnKeyListener(this);
            mNumberOfRecipients.requestFocus();

            //add post owner name of its default recipient
            Post post = PostsDatastore.getInstance().getPost(mParentPostId, false);
            addRecipientToContainer(PostUtils.getPostAuthorUsername(post));
        }
    }

    private void resetSendGiftButton() {
        if (mGiftItem != null) {

            if (mGiftItem.isGroupOnly()) {
                String strJoin = I18n.tr("JOIN %s");
                mSendGift.setText(String.format(strJoin, mGiftItem.getGroupName()));
                mSendGift.setVisibility(View.VISIBLE);

            } else {
                ChatConversation conversation = ChatDatastore.getInstance().getChatConversationWithId(mConversationId);
                String btnText = I18n.tr("SEND");

                String priceText = Constants.BLANKSTR;

                if (conversation != null && (conversation.isMigGroupChat() || conversation.isChatroom())) {
                    mSendGift.setText(btnText);
                    mSendGift.setVisibility(View.VISIBLE);

                } else {
                    if (mIsFromSinglePost) {
                        btnText = I18n.tr("SEND GIFT");
                    } else {
                        float price = mGiftItem.getRoundedPrice();
                        priceText = Float.toString(price) + mGiftItem.getLocalCurrency();

                        if (!TextUtils.isEmpty(priceText)) {
                            btnText = btnText + Constants.SPACESTR + priceText;
                        }
                    }
                    mSendGift.setText(btnText);
                    mSendGift.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void setGiftData() {
        mGiftItem = StoreController.getInstance().getStoreItem(mGiftItemId);

        if (mGiftItem != null) {
            // gift info: name, price, image
            mGiftName.setText(mGiftItem.getName());
            mGiftPrice.setText(mGiftItem.getRoundedPrice() + Constants.SPACESTR + mGiftItem.getLocalCurrency());

            String hotkey = mGiftItem.getGiftHotkey();
            if (!TextUtils.isEmpty(hotkey)) {
                EmoticonsController.getInstance().loadGiftEmoticonImage(mGiftImage, hotkey, R.drawable.ad_loadstatic_grey);
            }

            // the send gift button UI
            resetSendGiftButton();

            // gift class UI
            if (mGiftItem.isGroupOnly()) {
                mGiftClassIcon.setImageResource(R.drawable.ad_store_ggroup);
                String description = I18n.tr("Unlock this gift by joining the %s group!");
                mGiftClassDescription.setText(String.format(description, mGiftItem.getGroupName()));
                mGiftClass.setVisibility(View.VISIBLE);

            } else if (mGiftItem.isPremium()) {
                mGiftClassIcon.setImageResource(R.drawable.ad_store_pgift);
                mGiftClassDescription.setText(String.format(
                        I18n.tr("When you send this Premium gift, your friend will also receive %s %s bonus credit!"),
                        mGiftItem.getRoundedReward(), mGiftItem.getLocalCurrency()));
                mGiftClass.setVisibility(View.VISIBLE);

            } else {
                mGiftClass.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.MigStore.Item.RECEIVED);
        registerEvent(Events.Emoticon.RECEIVED);
        registerEvent(Events.MigStore.Item.PURCHASED);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(Events.MigStore.Item.RECEIVED) || action.equals(Events.Emoticon.RECEIVED)) {
            sendGiftListener = new SendGiftCommandListener(mGiftItem, mConversationId);
            setGiftData();
            //auto fetched price for default recipient in post flow
            if (mIsFromSinglePost) {
                refreshSelectedGiftLabels();
            }
        } else if (action.equals(Events.MigStore.Item.PURCHASED)) {
            FragmentHandler.getInstance().clearBackStack();
            ActionHandler.getInstance().displayGiftSent(getActivity());
        } else if (action.equals(Events.ChatParticipant.FETCH_ALL_COMPLETED)) {
            resetSendGiftButton();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == Constants.RESULT_FROM_GIFT_CENTER_SHOW_GROUP) {
            String groupId = intent.getStringExtra(GiftPreviewFragment.PARAM_GROUP_ID);
            ActionHandler.getInstance().displayGroupPageFromChat(getActivity(), groupId);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (view instanceof UserBasicDetails) {
            Object viewTag = view.getTag();

            if (viewTag != null) {
                UserBasicDetails userBasicDetails = (UserBasicDetails) view;

                if (userBasicDetails.isCloseIconVisible()) {
                    mRecipientContainer.removeView(mRecipientContainer.findViewWithTag(viewTag));
                    mSelectedList.remove((String) viewTag);
                    refreshSelectedGiftLabels();
                }
            }
        }


        switch (id) {
            case R.id.recipients_container:
            case R.id.scroll_container:
            case R.id.selected_container:
                if (!mIsFriendListDisplayed) {
                    showFriendList();
                }
                break;
            case R.id.send_gift:
                if (mGiftItem != null) {

                    if (mIsFromSinglePost) {
                        if (mSelectedList.size() == 0) {
                            Tools.showToast(null, I18n.tr("Add a recipient."), Toast.LENGTH_LONG);
                        } else {
                            GAEvent.Chat_SendGift.send(mGiftItem.getId());
                            sendSinglePostGift();
                        }
                        return;
                    }

                    if (mGiftItem.isGroupOnly()) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(PARAM_GROUP_ID, Integer.toString(mGiftItem.getGroupId()));
                        onActivityResult(Constants.REQ_SHOW_GIFT_CENTER_FROM_CHAT,
                                Constants.RESULT_FROM_GIFT_CENTER_SHOW_GROUP, resultIntent);
                        FragmentHandler.getInstance().clearBackStack();

                    } else {
                        // send gift
                        GAEvent.Chat_SendGift.send(mGiftItem.getId());
                        if (mConversation != null) {
                            sendGift();
                        } else {
                            // start chat with a gift message, no conversation yet
                            startChat(new StartChatFragment.StartChatListener() {

                                @Override
                                public void onChatCreated(String conversationId) {
                                    ProgressDialogController.getInstance().hideProgressDialog();
                                    //set the conversation id
                                    ((SendGiftCommandListener) sendGiftListener).setConversationId(conversationId);
                                    mConversationId = conversationId;
                                    mConversation = ChatDatastore.getInstance().getChatConversationWithId(mConversationId);

                                    //send the first gift message
                                    sendGift();

                                    //close the start chat screen after creating a new chat
                                    getActivity().finish();
                                }

                            });
                        }

                    }
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

    private void getFriendList() {
        mFusionFriendList = UserDatastore.getInstance().getAllFusionFriends(false, false);

        mFriendListAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
        if (mFusionFriendList != null && mFusionFriendList.size() > 0) {
            mFriendListAdapter.clear();

            for (Friend friend : mFusionFriendList) {
                // If this friend has already been selected,
                // then do not add it to the list of friends that can be
                // selected from.
                if (!mSelectedList.contains(friend.getUsername())) {
                    mFriendListAdapter.add(friend.getUsername());
                }
            }

            mFriendListAdapter.notifyDataSetChanged();
            mFriendList.setAdapter(mFriendListAdapter);
        }
    }

    protected void showFriendList() {
        getFriendList();
        mFriendListContainer.setVisibility(View.VISIBLE);
        mFriendListFilter.requestFocus();
        mIsFriendListDisplayed = true;
    }

    private void hideFriendList() {
        mFriendListFilter.setText(Constants.BLANKSTR);
        mFriendList.clearTextFilter();
        mFriendListContainer.setVisibility(View.GONE);
        mFriendListFilter.clearFocus();
        mIsFriendListDisplayed = false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mIsFriendListDisplayed) {
            String friend = mFriendListAdapter.getItem(position);
            addRecipientToContainer(friend);
            hideFriendList();
            Tools.hideVirtualKeyboard(getActivity(), mFriendListFilter);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.FLAG_EDITOR_ACTION) {

            switch (v.getId()) {
                case R.id.friend_list_filter:
                    String friendListFilter = mFriendListFilter.getText().toString();
                    if (!android.text.TextUtils.isEmpty(friendListFilter)) {
                        addRecipientToContainer(friendListFilter);
                    }
                    hideFriendList();
                    break;
            }
            return true;

        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mIsFriendListDisplayed) {
                hideFriendList();
                return true;
            }
        }

        return false;
    }

    private void addRecipientToContainer(String userName) {
        if (mSelectedList != null && mSelectedList.size() < mRecipientLimit) {
            mSelectedList.add(userName);

            refreshSelectedGiftLabels();

            int smallPadding = ApplicationEx.getDimension(R.dimen.small_padding);
            int normalMargin = ApplicationEx.getDimension(R.dimen.normal_margin);

            UserBasicDetails recipient = new UserBasicDetails(ApplicationEx.getContext());

            recipient.showCloseIcon();
            recipient.setClickable(true);
            recipient.setUsernameColor(ApplicationEx.getColor(R.color.white));
            recipient.setBackgroundResource(R.drawable.rounded_green_background);

            recipient.setPadding(smallPadding, smallPadding, smallPadding, smallPadding);
            recipient.setUsername(userName);
            recipient.setOnClickListener(this);
            recipient.setTag(userName);

            LinearLayout.LayoutParams userBasicDetailsParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ApplicationEx.getDimension(R.dimen.selected_recipient_height));
            userBasicDetailsParams.setMargins(normalMargin, normalMargin, normalMargin, normalMargin);
            recipient.setLayoutParams(userBasicDetailsParams);

            mRecipientContainer.addView(recipient);
        }
    }

    protected TextView           mNumberOfRecipients;
    protected TextView           mTotalPrice;

    private void refreshSelectedGiftLabels() {
        if (mSelectedList != null && mSelectedList.size() > 0) {
            mNumberOfRecipients.setText(String.format(I18n.tr("Recipient(s): %d"), mSelectedList.size()));

            if (mGiftItem != null) {
                float roundedPrice = mGiftItem.getRoundedPrice(mSelectedList.size());

                mTotalPrice.setText(String.format(I18n.tr("Total: %s"), roundedPrice));
            }

        } else {
            mNumberOfRecipients.setText(String.format(I18n.tr("Recipient(s): %d"), 0));
            mTotalPrice.setText(String.format(I18n.tr("Total: %s"), 0));
        }
    }

    private void startChat(StartChatFragment.StartChatListener listener) {
        if (mSelectedUsers == null || mSelectedUsers.isEmpty())
            return;

        GAEvent.Chat_StartChat.send();

        ArrayList<Friend> friendsList = new ArrayList<Friend>();
        for (String username : mSelectedUsers) {
            Friend friend = UserDatastore.getInstance().findMig33User(username);
            friendsList.add(friend);
        }

        String conversationId;

        if (friendsList.size() == 1) {
            Friend friend = friendsList.get(0);
            if (friend.isFusionContact()) {
                conversationId = ActionHandler.getInstance().displayPrivateChat(getActivity(), friendsList.get(0).getUsername());
                if (listener != null)
                    listener.onChatCreated(conversationId);
            }
        } else if (friendsList.size() >= 2) {
            ActionHandler.getInstance().startGroupChat(friendsList, MessageType.FUSION,
                    listener);
        }
    }

    private void sendGift() {
        Tools.showToast(getActivity(), I18n.tr("Sending gift"), Toast.LENGTH_SHORT);

        if (mConversation.isChatroom() || mConversation.isMigGroupChat()) {
            if (!mSelectedRecipient.equals(RECIPIENT_EVERYONE)) {
                ChatController.getInstance().sendGift(mConversationId, mSelectedRecipient,
                        mGiftItem.getName(), mMessageEdit.getText().toString(), sendGiftListener);
            } else {
                ChatController.getInstance().sendGift(mConversationId, mGiftItem.getName(),
                        mMessageEdit.getText().toString(), sendGiftListener);
            }
        } else {
            ChatController.getInstance().sendGift(mConversationId, mConversation.getDisplayName(),
                    mGiftItem.getName(), mMessageEdit.getText().toString(), sendGiftListener);
        }
    }

    private void sendSinglePostGift() {
        boolean postToMiniblog = mRepostCheckBox.isChecked();
        String recipientsStr = Constants.BLANKSTR;

        if (mSelectedList != null && mSelectedList.size() > 0) {
            StringBuilder buff = new StringBuilder();
            String sep = Constants.BLANKSTR;
            for (String str : mSelectedList) {
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
                requestManager.purchaseSinglePostGiftItem(getStoreItemListener, Session.getInstance().getUserId(), String.valueOf(mGiftItem.getId())
                        , String.valueOf(postToMiniblog), recipientsStr, mMessageEdit.getText().toString(), mRootPostId, mParentPostId);
            }
        }
    }

    private GetStoreItemListener getStoreItemListener = new GetStoreItemListener() {
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
        }

        @Override
        public void onStoreUnlockedItemSent(String key, StoreItem storeItem) {
            BroadcastHandler.MigStore.Item.sendUnlockedItem();
        };

    };

}
