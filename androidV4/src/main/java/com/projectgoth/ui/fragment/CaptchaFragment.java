/**
 * Copyright (c) 2013 Project Goth
 *
 * CaptchaFragment.java
 * Created Apr 29, 2014, 12:02:23 PM
 */

package com.projectgoth.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.blackhole.model.Captcha;
import com.projectgoth.common.Constants;
import com.projectgoth.common.TextUtils;
import com.projectgoth.controller.SystemController;
import com.projectgoth.events.BroadcastHandler;
import com.projectgoth.i18n.I18n;
import com.projectgoth.ui.widget.ButtonEx;

/**
 * @author angelorohit
 * 
 */
public class CaptchaFragment extends BaseFragment {

    private TextView    lblMessage = null;
    private EditText    txtCaptcha      = null;
    private ImageView   imgCaptcha      = null;
    private ButtonEx    btnContinue     = null;
    private ImageButton btnClose        = null;

    private String      captchaMessage    = null;
    private byte[]      captchaImgRaw   = null;
    private String      captchaInput    = Constants.BLANKSTR;

    public CaptchaFragment(final Captcha captcha) {
        this(captcha.getLabel(), captcha.getImage());
    }

    public CaptchaFragment(final String captchaMessage, final byte[] captchaImgRaw) {
        this.captchaMessage = captchaMessage;
        this.captchaImgRaw = captchaImgRaw;
        this.captchaInput = Constants.BLANKSTR;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_captcha;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the keyboard as visible for this fragment's activity.
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        lblMessage = (TextView) view.findViewById(R.id.message_label);
        txtCaptcha = (EditText) view.findViewById(R.id.captcha_textfield);
        imgCaptcha = (ImageView) view.findViewById(R.id.captcha_image);
        btnContinue = (ButtonEx) view.findViewById(R.id.btn_continue);
        btnClose = (ImageButton) view.findViewById(R.id.btn_close);

        if (lblMessage != null) {            
            lblMessage.setText((captchaMessage != null) ? captchaMessage : I18n.tr("Security code"));
        }

        if (imgCaptcha != null && captchaImgRaw != null && captchaImgRaw.length > 0) {
            final Bitmap bitmap = BitmapFactory.decodeByteArray(captchaImgRaw, 0, captchaImgRaw.length);
            if (bitmap != null) {
                imgCaptcha.setImageBitmap(bitmap);
            }
        }

        if (btnContinue != null) {
            btnContinue.setText(I18n.tr("CONTINUE"));
            btnContinue.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    if (txtCaptcha != null) {
                        captchaInput = txtCaptcha.getText().toString();
                        SystemController.getInstance().sendCaptchaResponse(captchaInput);
                        closeFragment();
                    }
                }
            });
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    closeFragment();
                }
            });
        }
    }

    @Override
    protected void onHideFragment() {
        if (TextUtils.isEmpty(captchaInput)) {
            ApplicationEx.getInstance().getNetworkService().logout();    
        }

        SystemController.getInstance().setCaptcha(null);

        super.onHideFragment();
    }

}
