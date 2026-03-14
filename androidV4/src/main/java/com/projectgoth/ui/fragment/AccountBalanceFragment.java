/**
 * Copyright (c) 2013 Project Goth
 *
 * AccountBalanceFragment.java
 * Created Aug 7, 2014, 2:59:38 PM
 */

package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.common.WebURL;
import com.projectgoth.datastore.Session;
import com.projectgoth.events.GAEvent;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.activity.ActionHandler;
import com.projectgoth.ui.activity.FragmentHandler;
import com.projectgoth.ui.widget.ButtonEx;

/**
 * @author mapet
 * 
 */
public class AccountBalanceFragment extends BaseDialogFragment implements OnClickListener {

    private ImageView       mCloseBtn;
    private ButtonEx        mRechargeBtn;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_account_balance;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCloseBtn = (ImageView) view.findViewById(R.id.close_button);
        mCloseBtn.setOnClickListener(this);

        mRechargeBtn = (ButtonEx) view.findViewById(R.id.recharge_button);
        mRechargeBtn.setText(I18n.tr("RECHARGE"));
        mRechargeBtn.setOnClickListener(this);

        TextView message = (TextView) view.findViewById(R.id.message);
        message.setText(String.format(I18n.tr("Running low on credit.\nCurrent balance: %s"), 
                Session.getInstance().getAccountBalance()));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.recharge_button:
                GAEvent.Chat_SendGiftUiRecharge.send();
                ActionHandler.getInstance().displayRechargeCreditsFromChat(getActivity(), WebURL.URL_ACCOUNT_SETTINGS,
                        I18n.tr("Buy credit"), R.drawable.ad_credit_white);
                FragmentHandler.getInstance().clearBackStack();
                closeFragment();
                break;
            case R.id.close_button:
                closeFragment();
                break;
            default:
                break;
        }
    }

}
