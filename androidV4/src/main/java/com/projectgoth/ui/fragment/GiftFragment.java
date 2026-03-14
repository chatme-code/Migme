/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftFragment.java
 * Created Dec 6, 2013, 3:18:19 PM
 */

package com.projectgoth.ui.fragment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.StoreItem;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.EmoticonsController;
import com.projectgoth.controller.StoreController;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.nemesis.model.Friend;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.listener.KeyboardListener;
import com.projectgoth.ui.widget.ButtonEx;
import com.projectgoth.ui.widget.FlowLayout;
import com.projectgoth.ui.widget.RelativeLayoutEx;
import com.projectgoth.ui.widget.UserBasicDetails;

/**
 * @author mapet
 * 
 */
public class GiftFragment extends BaseDialogFragment implements OnClickListener, OnItemClickListener, OnKeyListener,
        TextWatcher, KeyboardListener {

    protected String             mGiftItemId;
    private StoreItem            mGiftItem;

    protected ImageView          mGiftImage;
    protected TextView           mGiftName;
    private TextView             mGiftPrice;
    protected TextView           mDescription;
    protected TextView           mNumberOfRecipients;
    protected TextView           mTotalPrice;
    protected EditText           mMessage;
    protected CheckBox           mPrivateGift;
    protected CheckBox           mPostInMiniblog;
    private ButtonEx             mSendGiftButton;
    protected LinearLayout       premiumGiftContainer;
    private ImageView            premiumGiftImage;
    private TextView             premiumGiftLabel;
    protected TextView           commandTip;
    private View                 separator;

    private RelativeLayoutEx     mMainGiftContainer;
    private RelativeLayout       mMainRecipientContainer;
    protected LinearLayout       mSelectedDetailsContainer;
    private LinearLayout         mGiftDetails;
    private ScrollView           mScrollContainer;
    private FlowLayout           mRecipientContainer;
    private LinearLayout         mFriendListContainer;
    private ListView             mFriendList;
    private EditText             mFriendListFilter;

    private ArrayAdapter<String> mFriendListAdapter;
    protected boolean            isFriendListDisplayed;
    private List<Friend>         mFusionFriendList;
    protected Set<String>        mSelectedList           = new HashSet<String>();
    protected String             mRecipientsStr;

    protected int                RECIPIENT_LIMIT         = 5;
    private String               mInitialRecipient       = null;
    private String               counterId               = null;

    public static final String   PARAM_GIFT_ITEM_ID      = "PARAM_GIFT_ITEM_ID";
    public static final String   PARAM_INITIAL_RECIPIENT = "PARAM_INITIAL_RECIPIENT";
    public static final String   PARAM_COUNTER_ID        = "PARAM_COUNTER_ID";

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        mGiftItemId = args.getString(PARAM_GIFT_ITEM_ID);
        mInitialRecipient = args.getString(PARAM_INITIAL_RECIPIENT);
        counterId = args.getString(PARAM_COUNTER_ID);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_gift;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGiftItem = StoreController.getInstance().getStoreItem(mGiftItemId);

        mMainGiftContainer = (RelativeLayoutEx) view.findViewById(R.id.main_gift_container);
        mGiftDetails = (LinearLayout) view.findViewById(R.id.gift_details_container);
        mSelectedDetailsContainer = (LinearLayout) view.findViewById(R.id.selected_gift_details);

        mGiftImage = (ImageView) view.findViewById(R.id.gift_image);
        mGiftName = (TextView) view.findViewById(R.id.gift_name);
        mGiftPrice = (TextView) view.findViewById(R.id.gift_price);
        
        premiumGiftContainer = (LinearLayout) view.findViewById(R.id.premium_gift_details);
        premiumGiftImage = (ImageView) view.findViewById(R.id.label_premium);
        premiumGiftLabel = (TextView) view.findViewById(R.id.premium_description);

        commandTip = (TextView) view.findViewById(R.id.command_tip);
        separator = view.findViewById(R.id.separator);
        separator.setBackgroundColor(Theme.getColor(ThemeValues.GRAY_SEPARATOR_COLOR));

        TextView recipientLabel = (TextView) view.findViewById(R.id.recipient_label);
        TextView messageLabel = (TextView) view.findViewById(R.id.message_label);

        mDescription = (TextView) view.findViewById(R.id.description);
        mNumberOfRecipients = (TextView) view.findViewById(R.id.number_of_recipients);
        mTotalPrice = (TextView) view.findViewById(R.id.total_price);

        mMessage = (EditText) view.findViewById(R.id.message);

        mPrivateGift = (CheckBox) view.findViewById(R.id.checkbox_private_gift);
        mPostInMiniblog = (CheckBox) view.findViewById(R.id.checkbox_post_to_miniblog);
        mSendGiftButton = (ButtonEx) view.findViewById(R.id.send_gift_button);

        mMainRecipientContainer = (RelativeLayout) view.findViewById(R.id.recipients_container);
        mScrollContainer = (ScrollView) view.findViewById(R.id.scroll_container);
        mRecipientContainer = (FlowLayout) view.findViewById(R.id.selected_container);

        mFriendListContainer = (LinearLayout) view.findViewById(R.id.friend_list_container);
        mFriendList = (ListView) view.findViewById(R.id.friend_list);
        mFriendListFilter = (EditText) view.findViewById(R.id.friend_list_filter);
        mFriendListAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
        mFriendList.setAdapter(mFriendListAdapter);

        setGiftData();

        recipientLabel.setText(I18n.tr("Send to"));
        messageLabel.setText(I18n.tr("Add a message (optional)"));
        mPrivateGift.setText(I18n.tr("Private gift"));
        mPostInMiniblog.setText(I18n.tr("Share on my feed"));
        mSendGiftButton.setText(I18n.tr("SEND GIFT"));
        mNumberOfRecipients.setText(String.format(I18n.tr("Recipient(s): %d"), 0));
        mTotalPrice.setText(String.format(I18n.tr("Total: %s"), 0));

        refreshSelectedGiftLabels();

        mSendGiftButton.setOnClickListener(this);
        mMainRecipientContainer.setOnClickListener(this);
        mScrollContainer.setOnClickListener(this);
        mRecipientContainer.setOnClickListener(this);
        mFriendList.setOnItemClickListener(this);
        mFriendListFilter.setOnKeyListener(this);
        mFriendListFilter.addTextChangedListener(this);
        mMainGiftContainer.setKeyboardListener(this);

        mNumberOfRecipients.requestFocus();

        if (!TextUtils.isEmpty(mInitialRecipient)) {
            addRecipientToContainer(mInitialRecipient);
        }

        // register no connection disable buttons
        addButtonToNoConnectionDisableButtonList(mSendGiftButton);
    }

    public void setInitialRecipient(String initialRecipient) {
        mInitialRecipient = initialRecipient;
    }

    protected void setGiftData() {
        mGiftItem = StoreController.getInstance().getStoreItem(mGiftItemId);

        if (mGiftItem != null) {
            mGiftName.setText(mGiftItem.getName());
            float roundedPrice = mGiftItem.getRoundedPrice();
            mGiftPrice.setText(roundedPrice + Constants.SPACESTR + mGiftItem.getLocalCurrency());

            String hotkey = mGiftItem.getGiftHotkey();
            if (!TextUtils.isEmpty(hotkey)) {
                EmoticonsController.getInstance().loadGiftEmoticonImage(mGiftImage, hotkey, R.drawable.ad_loadstatic_grey);
            }

            commandTip.setText(String.format(
                    I18n.tr("To send this directly from your chat window, type /gift username %s"),
                    mGiftItem.getName()));

            if (mGiftItem.isPremium()) {
                premiumGiftLabel.setText(String.format(
                        I18n.tr("When you send this Premium gift, your friend will also receive %s %s bonus credit!"),
                        mGiftItem.getRoundedReward(), mGiftItem.getLocalCurrency()));
                premiumGiftLabel.setVisibility(View.VISIBLE);
                premiumGiftImage.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void registerReceivers() {
        registerEvent(Events.MigStore.Item.RECEIVED);
        registerEvent(Events.MigStore.Item.PURCHASED);
        registerEvent(Events.Emoticon.RECEIVED);
        registerEvent(Events.MigStore.Item.PURCHASE_ERROR);
        registerEvent(Events.MigStore.Item.UNLOCKED_SENT);
    }

    @Override
    protected void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(Events.MigStore.Item.RECEIVED) || action.equals(Events.Emoticon.RECEIVED)) {
            setGiftData();
        } else if (action.equals(Events.MigStore.Item.PURCHASED)) {
            ActionHandler.getInstance().displayGiftPurchased(getActivity(), mGiftItemId, mRecipientsStr);
        } else if (action.equals(Events.MigStore.Item.UNLOCKED_SENT)) {
            ActionHandler.getInstance().displayGiftPurchased(getActivity(), mGiftItemId, mRecipientsStr);
        } else if (action.equals(Events.MigStore.Item.PURCHASE_ERROR)) {
            Tools.showToastForIntent(context, intent);
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

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

        switch (viewId) {
            case R.id.recipients_container:
            case R.id.scroll_container:
            case R.id.selected_container:
                if (!isFriendListDisplayed) {
                    showFriendList();
                }
                break;
            case R.id.send_gift_button:
                GAEvent.Store_SendGift.send();
                handleSendButton();
                break;
            default:
                break;
        }
    }

    private void handleSendButton() {
        boolean privateGift = mPrivateGift.isChecked();
        boolean postToMiniblog = mPostInMiniblog.isChecked();
        mRecipientsStr = Constants.BLANKSTR;

        if (mSelectedList != null && mSelectedList.size() > 0) {
            StringBuilder buff = new StringBuilder();
            String sep = Constants.BLANKSTR;
            for (String str : mSelectedList) {
                buff.append(sep);
                buff.append(str);
                sep = ",";
            }
            mRecipientsStr = buff.toString();
        }

        // Do not attempt to purchase gift if
        // - there are no recipients for the gift,
        // - the min. miglevel requirement for the gift is met,
        // - the gift is group exclusive and the user is not a part of that
        // group.
        if (TextUtils.isEmpty(mRecipientsStr)) {
            Tools.showToast(null, I18n.tr("Add a recipient."), Toast.LENGTH_LONG);
        } else if (mGiftItem != null && mGiftItem.getMigLevelMin() <= Session.getInstance().getMigLevel()
                && (!mGiftItem.isGroupOnly())) {
            if (StoreController.getInstance().canPurchaseItem(mGiftItem.getPrice())) {
                final String messageToSend = (mMessage.getText().toString() == null) ? Constants.BLANKSTR : mMessage
                        .getText().toString().trim();
                StoreController.getInstance().purchaseGift(mGiftItemId, mRecipientsStr, messageToSend,
                        Boolean.toString(privateGift), Boolean.toString(postToMiniblog), counterId);
                Tools.showToast(getActivity(), I18n.tr("Sending gift"), Toast.LENGTH_SHORT);
            } else {
                ActionHandler.getInstance().displayAccountBalance(getActivity());
            }
        }
    }

    protected void showFriendList() {
        getFriendList();
        mFriendListContainer.setVisibility(View.VISIBLE);
        mFriendListFilter.requestFocus();
        isFriendListDisplayed = true;
    }

    private void hideFriendList() {
        mFriendListFilter.setText(Constants.BLANKSTR);
        mFriendList.clearTextFilter();
        mFriendListContainer.setVisibility(View.GONE);
        mFriendListFilter.clearFocus();
        isFriendListDisplayed = false;
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (isFriendListDisplayed) {
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
                    if (!TextUtils.isEmpty(friendListFilter)) {
                        addRecipientToContainer(friendListFilter);
                    }
                    hideFriendList();
                    break;
            }
            return true;

        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isFriendListDisplayed) {
                hideFriendList();
                return true;
            }
        }

        return false;
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (isFriendListDisplayed) {
            mFriendListAdapter.getFilter().filter(s);
        }
    }

    private void addRecipientToContainer(String userName) {
        if (mSelectedList != null && mSelectedList.size() < RECIPIENT_LIMIT) {
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

    @Override
    public void onSoftKeyboardShown() {
        mGiftDetails.setVisibility(View.GONE);
        mSelectedDetailsContainer.setVisibility(View.GONE);
    }

    @Override
    public void onSoftKeyboardHidden() {
        mGiftDetails.setVisibility(View.VISIBLE);
        mSelectedDetailsContainer.setVisibility(View.VISIBLE);
    }

}
