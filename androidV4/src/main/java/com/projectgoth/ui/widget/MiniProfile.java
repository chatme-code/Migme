
package com.projectgoth.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.b.data.Labels;
import com.projectgoth.blackhole.enums.PresenceType;
import com.projectgoth.common.Tools;
import com.projectgoth.controller.FriendsController;
import com.projectgoth.datastore.Session;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.UIUtils;

/**
 * @author dangui
 * 
 */

public class MiniProfile extends RelativeLayout {

    private ImageView        displayPicture;
    private TextView         username;
    private UserBasicDetails userBasicDetails;
    private ImageView        presenceIcon;
    private ImageView        presenceArrow;
    private EditText         statusMsg;
    private ImageView        statusTick;

    public MiniProfile(Context context) {
        this(context, null);
    }

    public MiniProfile(final Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.mini_profile, this, true);

        displayPicture = (ImageView) findViewById(R.id.display_pic);
        username = (TextView) findViewById(R.id.username);
        userBasicDetails = (UserBasicDetails) findViewById(R.id.user_basic_details);

        presenceIcon = (ImageView) findViewById(R.id.presence);
        presenceArrow = (ImageView) findViewById(R.id.presence_arrow);
        statusMsg = (EditText) findViewById(R.id.status_msg);
        statusMsg.setHint(I18n.tr("Share your status"));
        statusMsg.setHintTextColor(getResources().getColor(R.color.default_timestamp));
        statusMsg.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    updateStatus(context);
                }
                return false;
            }
        });
        statusTick = (ImageView) findViewById(R.id.status_tick);
        statusMsg.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    statusTick.setVisibility(View.VISIBLE);
                }
            }
        });

        userBasicDetails.hideUsername();
    }

    private void hideStatusTick() {
        statusTick.setVisibility(View.GONE);
        statusMsg.clearFocus();
    }

    public void resetStatusMsg(Context context) {
        Tools.hideVirtualKeyboard(statusMsg, context);
        statusTick.setVisibility(View.GONE);
        statusMsg.clearFocus();
        String originalStatusMsg = Session.getInstance().getStatusMessage();
        setStatusMessage(originalStatusMsg);
    }

    public void setUsername(String username) {
        this.username.setText(username);
    }

    public void setLabels(Labels labels) {
        username.setTextColor(UIUtils.getUsernameColorFromLabels(labels, false));
        userBasicDetails.setLabels(labels);
    }

    public void setMigLevelNumber(String strLevelNumber) {
        userBasicDetails.setMigLevel(String.format(I18n.tr("Level %s"), strLevelNumber));
    }

    public void setPresenceIcon(PresenceType presence) {
        presenceIcon.setImageResource(Tools.getFusionPresenceResource(presence));
    }

    public void setStatusMessage(String statusMsg) {
        this.statusMsg.setText(statusMsg);
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        username.setOnClickListener(listener);
        displayPicture.setOnClickListener(listener);
        userBasicDetails.setOnClickListener(listener);
        presenceIcon.setOnClickListener(listener);
        presenceArrow.setOnClickListener(listener);
        statusMsg.setOnClickListener(listener);
        statusTick.setOnClickListener(listener);
    }

    public ImageView getDisplayPicture() {
        return displayPicture;
    }

    private void sendUpdateStatusMessage() {
        String newStatusMessage = statusMsg.getText().toString();
        FriendsController.getInstance().requestSetStatusMessage(newStatusMessage);
    }

    public void updateStatus(Context context) {
        Tools.hideVirtualKeyboard(statusMsg, context);
        sendUpdateStatusMessage();
        hideStatusTick();
    }

}
