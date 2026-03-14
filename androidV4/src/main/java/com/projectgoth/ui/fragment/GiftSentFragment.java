/**
 * Copyright (c) 2013 Project Goth
 *
 * GiftSentFragment.java
 * Created Aug 6, 2014, 1:48:07 PM
 */

package com.projectgoth.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.common.Tools;
import com.projectgoth.i18n.I18n;

/**
 * @author mapet
 * 
 */
public class GiftSentFragment extends BaseDialogFragment implements OnClickListener {

    private ImageView mCloseBtn;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_gift_sent;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCloseBtn = (ImageView) view.findViewById(R.id.close_button);
        mCloseBtn.setOnClickListener(this);

        TextView message = (TextView) view.findViewById(R.id.message);
        TextView infoMessage = (TextView) view.findViewById(R.id.info_message);

        message.setText(I18n.tr("Gift sent. You rock!"));
        infoMessage.setText(I18n.tr("To find out more about sending gifts from your chat window, type /gift"));

        RelativeLayout container = (RelativeLayout) view.findViewById(R.id.gift_sent_container);
        Tools.makeBackgroundTiled(container);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.close_button:
                closeFragment();
                break;
            default:
                break;
        }
    }

}
