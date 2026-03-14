/**
 * Copyright (c) 2013 Project Goth
 *
 * CustomAlertDialog.java
 * Created Aug 23, 2013, 2:26:53 PM
 */

package com.projectgoth.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.i18n.I18n;
import com.projectgoth.notification.AlertListener;

/**
 * @author cherryv
 * 
 */
public class CustomAlertDialog extends Dialog implements android.view.View.OnClickListener, OnCancelListener {

    private TextView      mTitleView;
    private View          mTitleSeparator;
    private TextView      mMessageView;
    private ImageView     mCloseButton;
    private ButtonEx      mConfirmButton;

    private String        mTitle;
    private String        mMessage;
    private AlertListener mListener;

    public CustomAlertDialog(Context context, String title, String message) {
        this(context, title, message, null);
    }

    public CustomAlertDialog(Context context, String title, String message, AlertListener listener) {
        super(context, R.style.AlertDialog);
        this.mTitle = title;
        this.mMessage = message;
        this.mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_alert);

        mTitleView = (TextView) findViewById(R.id.alert_title);
        mTitleSeparator = findViewById(R.id.title_separator);
        mMessageView = (TextView) findViewById(R.id.alert_message);

        mCloseButton = (ImageView) findViewById(R.id.close_button);
        mCloseButton.setOnClickListener(this);

        mConfirmButton = (ButtonEx) findViewById(R.id.alert_confirm_button);
        mConfirmButton.setText(I18n.tr("Ok"));
        mConfirmButton.setOnClickListener(this);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.setOnCancelListener(this);
        this.setCanceledOnTouchOutside(true);
        initContent();
    }

    private void initContent() {
        if (!TextUtils.isEmpty(mTitle)) {
            mTitleView.setText(mTitle);
            mTitleView.setVisibility(View.VISIBLE);
            mTitleSeparator.setVisibility(View.VISIBLE);
        } else {
            mTitleView.setVisibility(View.GONE);
            mTitleSeparator.setVisibility(View.GONE);
        }

        mMessageView.setText(mMessage);
    }

    public void setAlertListener(AlertListener listener) {
        this.mListener = listener;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        if (v == mCloseButton) {
            dismiss();
            if (mListener != null) {
                mListener.onDismiss();
            }
        } else if (v == mConfirmButton) {
            dismiss();
            if (mListener != null) {
                mListener.onConfirm();
            }
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (mListener != null) {
            mListener.onDismiss();
        }
    }

}
