/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftPurchasedFragment.java
 * Created Dec 11, 2013, 4:11:40 PM
 */

package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.b.data.StoreItem;
import com.projectgoth.controller.EmoticonsController;
import com.projectgoth.controller.StoreController;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.widget.ButtonEx;

/**
 * @author mapet
 * 
 */
public class GiftPurchasedFragment extends BaseDialogFragment implements OnClickListener {

    private String             mGiftItemId;
    private StoreItem          mGiftItem;

    private ImageView          mGiftImage;
    private TextView           mMessage;

    private ButtonEx           mBackToGiftsButton;

    public static final String PARAM_GIFT_ITEM_ID    = "PARAM_GIFT_ITEM_ID";
    public static final String PARAM_GIFT_RECIPIENTS = "PARAM_GIFT_RECIPIENTS";

    @Override
    protected void readBundleArguments(Bundle args) {
        super.readBundleArguments(args);
        mGiftItemId = args.getString(PARAM_GIFT_ITEM_ID);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_gift_purchased;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGiftImage = (ImageView) view.findViewById(R.id.gift_image);

        mGiftItem = StoreController.getInstance().getStoreItem(mGiftItemId);
        //FIXME mGiftItem should not be null, check later.
        String hotkey = "";
        if(mGiftItem != null){
            hotkey = mGiftItem.getGiftHotkey();
        }
        
        if (!TextUtils.isEmpty(hotkey)) {
            EmoticonsController.getInstance().loadGiftEmoticonImage(mGiftImage, hotkey, R.drawable.ad_loadstatic_grey);
        }

        mMessage = (TextView) view.findViewById(R.id.message);
        mMessage.setText(I18n.tr("Gift sent. You rock!"));

        mBackToGiftsButton = (ButtonEx) view.findViewById(R.id.back_to_gifts);
        mBackToGiftsButton.setText(I18n.tr("BACK TO GIFTS"));
        mBackToGiftsButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        switch (viewId) {
            case R.id.back_to_gifts:
                if (getShowsDialog()) {
                    dismiss();
                } else {
                    closeFragment();
                }
                BroadcastHandler.MigStore.Item.sendPurchaseItemCompleted();
                break;
            default:
                break;
        }
    }

}
