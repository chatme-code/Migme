/**
 * Copyright (c) 2013 Project Goth
 *
 * UnlockedGiftFragment.java
 * Created Jan 7, 2015, 1:52:47 PM
 */

package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.projectgoth.R;
import com.projectgoth.b.data.StoreUnlockedItem;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.EmoticonsController;
import com.projectgoth.controller.StoreController;
import com.projectgoth.datastore.Session;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;

/**
 * @author mapet
 * 
 */
public class UnlockedGiftFragment extends GiftFragment {

    private StoreUnlockedItem unlockedItem;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        unlockedItem = StoreController.getInstance().getUnlockedGift(Session.getInstance().getUsername(), mGiftItemId);
        
        mDescription = (TextView) view.findViewById(R.id.description);
        mDescription
                .setText(String.format(
                        I18n.tr("This is an unlocked gift, send it now for free! You have unlocked %d of this %s gift, you can send them to %d of your friends for free!"),
                        unlockedItem.getCount(), unlockedItem.getStoreItemData().getName(), unlockedItem.getCount()));
        mDescription.setVisibility(View.VISIBLE);
        mDescription.requestFocus();

        RECIPIENT_LIMIT = unlockedItem.getCount();

        mNumberOfRecipients.setVisibility(View.GONE);
        mTotalPrice.setVisibility(View.GONE);
        premiumGiftContainer.setVisibility(View.GONE);
    }
    
    @Override
    protected void setGiftData() {
        super.setGiftData();

        if (unlockedItem != null) {
            mDescription.requestFocus();
            
            mGiftName.setText(unlockedItem.getStoreItemData().getName() + String.format(" (%d)", unlockedItem.getCount()));
            
            String hotkey = unlockedItem.getStoreItemData().getGiftHotkey();
            if (hotkey != null) {
                EmoticonsController.getInstance().loadGiftEmoticonImage(mGiftImage, hotkey, R.drawable.ad_loadstatic_grey);
            }

            commandTip.setText(String.format(I18n.tr("To send this directly from your chat window, type /gift -u username %s"), 
                    unlockedItem.getStoreItemData().getName()));
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        
        switch (viewId) {
            case R.id.recipients_container:
            case R.id.scroll_container:
            case R.id.selected_container:
                if (!isFriendListDisplayed) {
                    showFriendList();
                }
                break;
            case R.id.send_gift_button:
                GAEvent.Store_SendUnlockGift.send();
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

        if (TextUtils.isEmpty(mRecipientsStr)) {
            Tools.showToast(null, I18n.tr("Add a recipient."), Toast.LENGTH_LONG);
        } else {
            final String messageToSend = (mMessage.getText().toString() == null) ? Constants.BLANKSTR : mMessage
                    .getText().toString().trim();
            StoreController.getInstance().sendUnlockedItem(mGiftItemId, mRecipientsStr, messageToSend,
                    Boolean.toString(privateGift), Boolean.toString(postToMiniblog));
        }
    }

}
