/**
 * Copyright (c) 2013 Project Goth
 *
 * PositiveAlertFragment.java
 * Created Mar 18, 2014, 11:52:26 AM
 */

package com.projectgoth.ui.fragment;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mig33.diggle.events.Events;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Action;
import com.projectgoth.b.data.Alert;
import com.projectgoth.b.data.Profile;
import com.projectgoth.b.data.Variable;
import com.projectgoth.b.data.VariableLabel;
import com.projectgoth.b.enums.AlertDestinationEnum;
import com.projectgoth.b.enums.AlertTypeEnum;
import com.projectgoth.b.enums.ImageSizeEnum;
import com.projectgoth.common.Config;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.AlertsDatastore;
import com.projectgoth.datastore.Session;
import com.projectgoth.datastore.UserDatastore;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;

/**
 * @author angelorohit
 * 
 */
public class PositiveAlertFragment extends BaseDialogFragment {

    private String              title                         = null;
    private String              message                       = null;
    private String              imageUrl                      = null;
    private List<Action>        actionList                    = null;
    private AlertTypeEnum       alertType                     = null;

    private ImageView           mutualFollowingImgView        = null;
    private ImageView           loggedInUserImgView           = null;

    private static final String MUTUAL_FRIENDS_IMAGE_RESOURCE = "ic_friends";

    /**
     * Constructor
     * 
     * @param message
     *            The main message of the positive alert.
     */
    public PositiveAlertFragment(final String alertId) {
        super();
        final Alert alert = AlertsDatastore.getInstance().getUnreadMigAlertWithIdAndDestination(alertId,
                AlertDestinationEnum.POSITIVE_ALERT);
        extractDataFromAlert(alert);
    }

    private void extractDataFromAlert(final Alert alert) {
        if (alert != null) {
            // Extract data here.
            this.title = getUnreadPositiveAlertTitle(alert.getType());
            this.message = ((alert.getMessage() == null) ? Constants.BLANKSTR : alert.getMessage());
            this.message = processAlertVariablesForMessage(this.message, alert.getVariables());
            this.alertType = alert.getType();

            if (alert.getImage() != null && alertType == AlertTypeEnum.MUTUAL_FOLLOWING_ALERT) {
                this.imageUrl = Tools.getUrlFromImage(alert.getImage(), ImageSizeEnum.SIZE_64X);
            } else {
                Variable[] varArr = alert.getVariables();
                if (varArr != null) {
                    for (int i = varArr.length - 1; i > -1; --i) {
                        final Variable var = varArr[i];
                        if (var != null && var.getLabel() != null && var.getLabel().getImage() != null) {
                           this.imageUrl = Tools.getUrlFromImage(var.getLabel().getImage(), ImageSizeEnum.SIZE_64X);
                        }
                    }
                }
            }

            final Action[] actionArr = alert.getActions();
            if (actionArr != null) {
                this.actionList = Arrays.asList(actionArr);
            }
        }
    }

    private int mNotifyDrawableRes;
    private String getUnreadPositiveAlertTitle(final AlertTypeEnum alertTypeEnum) {
        String title = Constants.BLANKSTR;
        if (alertTypeEnum == AlertTypeEnum.MIGLEVEL_INCREASE_ALERT) {
            mNotifyDrawableRes = R.drawable.ad_collevelup;
            title = I18n.tr("Level Up!");
        } else if (alertTypeEnum == AlertTypeEnum.NEW_BADGE_ALERT) {
            mNotifyDrawableRes = R.drawable.ad_colbadge;
            title = I18n.tr("Badge Unlocked!");
        } else if (alertTypeEnum == AlertTypeEnum.MUTUAL_FOLLOWING_ALERT) {
            title = I18n.tr("1 New Friend!");
            mNotifyDrawableRes = R.drawable.ad_colmyfan;
        }
        return title;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_positive_alert;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView titleTextView = (TextView) view.findViewById(R.id.positivealert_title);
        final TextView descTextView = (TextView) view.findViewById(R.id.positivealert_content_desc);
        final ImageView iconView = (ImageView) view.findViewById(R.id.positivealert_image);
        final RelativeLayout positivealert_container = (RelativeLayout)view.findViewById(R.id.positivealert_container);

        positivealert_container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                return true;
            }
        });

        iconView.setImageResource(mNotifyDrawableRes);

        // Set title
        if (titleTextView != null && !TextUtils.isEmpty(title)) {
            titleTextView.setText(title);
        }

        // Set Content description
        if (descTextView != null && !TextUtils.isEmpty(message)) {
            descTextView.setText(message);
        }

    }

    private void setMyProfileImage(final Profile profile) {
        if (profile != null && mutualFollowingImgView != null && loggedInUserImgView != null
                && alertType == AlertTypeEnum.MUTUAL_FOLLOWING_ALERT) {
            mutualFollowingImgView.setImageResource(Tools.getDrawableResId(ApplicationEx.getContext(),
                    MUTUAL_FRIENDS_IMAGE_RESOURCE));
            ImageHandler.getInstance().loadDisplayPictureOfUser(loggedInUserImgView, profile.getUsername(),
                    profile.getDisplayPictureType(), Config.getInstance().getDisplayPicSizeNormal(), true);

            mutualFollowingImgView.setVisibility(View.VISIBLE);
            loggedInUserImgView.setVisibility(View.VISIBLE);
        }
    }

    private String processAlertVariablesForMessage(String message, final Variable[] vars) {
        for (Variable var : vars) {
            String label = Constants.BLANKSTR;
            VariableLabel varLabel = var.getLabel();
            if (varLabel != null && varLabel.getText() != null) {
                label = varLabel.getText();
            }

            final String match = "%{" + var.getName() + "}";
            message = message.replace(match, label);
        }

        return message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.projectgoth.ui.fragment.BaseFragment#registerReceivers()
     */
    @Override
    protected void registerReceivers() {
        registerEvent(Events.Profile.RECEIVED);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.projectgoth.ui.fragment.BaseFragment#onReceive(android.content.Context
     * , android.content.Intent)
     */
    @Override
    protected void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(Events.Profile.RECEIVED)) {
            Bundle data = intent.getExtras();
            final String username = data.getString(Events.User.Extra.USERNAME);
            final String loggedinUsername = Session.getInstance().getUsername();

            if (loggedinUsername != null && loggedinUsername.equals(username)) {
                setMyProfileImage(UserDatastore.getInstance().getProfileWithUsername(
                        Session.getInstance().getUsername(), false));
            }
        }
    }

}
