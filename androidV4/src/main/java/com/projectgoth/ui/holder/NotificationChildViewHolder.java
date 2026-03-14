/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatroomItemViewHolder.java
 * Created Jun 6, 2013, 10:35:45 AM
 */

package com.projectgoth.ui.holder;

import android.content.res.Resources;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Action;
import com.projectgoth.b.data.Alert;
import com.projectgoth.b.data.Variable;
import com.projectgoth.b.enums.AlertTypeEnum;
import com.projectgoth.b.enums.ImageSizeEnum;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Logger;
import com.projectgoth.common.SpannableBuilder;
import com.projectgoth.common.TimeAgo;
import com.projectgoth.common.Tools;
import com.projectgoth.datastore.AlertsDatastore;
import com.projectgoth.i18n.I18n;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.ui.widget.ClickableSpanEx;
import com.projectgoth.ui.widget.TextViewEx;
import com.projectgoth.util.LogUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mapet
 * 
 */
public class NotificationChildViewHolder extends BaseViewHolder<Alert> {

    private ImageView displayPic;
    private TextViewEx message;
    private ImageView notificationIcon;
    private TextView timestamp;
    private ImageView actionbtn;
    private LinearLayout container;
    private ViewGroup childLayout;
    private final ConcurrentHashMap<String, SpannableStringBuilder> spannableCache;
    private final ClickableSpanEx.ClickableSpanExListener clickableSpanListener;
    public static final int                                         MAX_ACTIONS_PER_LINE = 2;

    public NotificationChildViewHolder(View rootView, ConcurrentHashMap<String, SpannableStringBuilder> spannableCache,
                                       ClickableSpanEx.ClickableSpanExListener clickableSpanListener) {
        super(rootView);
        this.spannableCache = spannableCache;
        this.clickableSpanListener = clickableSpanListener;

        displayPic = (ImageView) rootView.findViewById(R.id.display_picture);
        message = (TextViewEx) rootView.findViewById(R.id.notification_msg);
        notificationIcon = (ImageView) rootView.findViewById(R.id.notification_icon);
        timestamp = (TextView) rootView.findViewById(R.id.timestamp);
        actionbtn = (ImageView) rootView.findViewById(R.id.action_btn);
        container = (LinearLayout) rootView.findViewById(R.id.container);
        childLayout = (ViewGroup) rootView.findViewById(R.id.child_layout);
    }

    @Override
    public void setData(Alert alert) {
        super.setData(alert);
        Resources resources = ApplicationEx.getContext().getResources();

        if (AlertsDatastore.getInstance().checkWhetherAlertIsRead(alert)) {
            container.setBackgroundColor((resources.getColor(R.color.white)));
            childLayout.setPadding(Math.round((resources.getDimension(R.dimen.medium_padding))),
                    Math.round((resources.getDimension(R.dimen.large_padding))),
                    Math.round((resources.getDimension(R.dimen.medium_padding))),
                    Math.round((resources.getDimension(R.dimen.large_padding))));
        } else {
            container.setBackgroundColor((resources.getColor(R.color.light_turquoise)));
            childLayout.setPadding(Math.round((resources.getDimension(R.dimen.xxlarge_margin))),
                    Math.round((resources.getDimension(R.dimen.large_padding))),
                    Math.round((resources.getDimension(R.dimen.medium_padding))),
                    Math.round((resources.getDimension(R.dimen.large_padding))));
        }

        String url = "";
        if (alert.getImage() != null) {
            url = Tools.getUrlFromImage(alert.getImage(), ImageSizeEnum.SIZE_120X);
            Logger.debug.log(LogUtils.TAG_IMAGE_FETCHER, "AlertViewHolder.setData: URL: ", url);
        }

        ImageHandler.getInstance().loadImageFromUrl(displayPic, url, true, R.drawable.icon_default_avatar);

        setMessage(alert.getId(), message, I18n.tr(alert.getMessage()), spannableCache, clickableSpanListener,
                false, false, true, alert.getVariables());

        timestamp.setText(TimeAgo.format(alert.getTimestamp()));

        notificationIcon.setImageResource(getNotificationIcon());

        actionbtn.setImageResource(getActionBtnIcon());
        setActions(alert);
    }


    private int getNotificationIcon() {
        AlertTypeEnum type = data.getType();

        if (type == AlertTypeEnum.MUTUAL_FOLLOWING_ALERT) {
            return R.drawable.ad_solidfriend;
        }  else if (type == AlertTypeEnum.GROUP_INVITE) {
            return R.drawable.ad_solidgrp;
        }  else if (type == AlertTypeEnum.VIRTUALGIFT_ALERT) {
            return R.drawable.ad_solidgift;
        }  else if (type == AlertTypeEnum.GAME_INVITE) {
            return R.drawable.ad_solidgame;
        }  else if (type == AlertTypeEnum.NEW_BADGE_ALERT) {
            return R.drawable.ad_solidbadge;
        }  else if (type == AlertTypeEnum.FOLLOWING_REQUEST) {
            return R.drawable.ad_soliduser;
        }  else if (type == AlertTypeEnum.MIGLEVEL_INCREASE_ALERT) {
            return R.drawable.ad_solidlvlup;
        } else if (type == AlertTypeEnum.REPLY_TO_MIGBO_POST_ALERT) {
            return R.drawable.ad_solidfeed;
        } else if (type == AlertTypeEnum.MENTIONED_IN_MIGBO_POST_ALERT) {
            return R.drawable.ad_solidmention;
        } else if (type == AlertTypeEnum.INCOMING_CREDIT_TRANSFER_ALERT) {
            return R.drawable.ad_solidcredit;
        } else if (type == AlertTypeEnum.MERCHANT_STATUS_CHANGE_ALERT) {
            return R.drawable.ad_solidmerch;
        }

        return R.drawable.ad_emotibot;
    }

    private int getActionBtnIcon() {
        AlertTypeEnum type = data.getType();

        if (type == AlertTypeEnum.MUTUAL_FOLLOWING_ALERT) {
            return R.drawable.ad_giftback_green;
        }  else if (type == AlertTypeEnum.GROUP_INVITE) {
            return R.drawable.ad_join_green;
        }  else if (type == AlertTypeEnum.VIRTUALGIFT_ALERT) {
            return R.drawable.ad_giftback_green;
        }  else if (type == AlertTypeEnum.GAME_INVITE) {
            return R.drawable.ad_join_green;
        }  else if (type == AlertTypeEnum.NEW_BADGE_ALERT) {
            return R.drawable.ad_visible_green;
        }  else if (type == AlertTypeEnum.FOLLOWING_REQUEST) {
            return R.drawable.ad_approve_green;
        }  else if (type == AlertTypeEnum.MIGLEVEL_INCREASE_ALERT) {
            return R.drawable.ad_info_green;
        } else if (type == AlertTypeEnum.REPLY_TO_MIGBO_POST_ALERT) {
            return R.drawable.ad_visible_green;
        } else if (type == AlertTypeEnum.MENTIONED_IN_MIGBO_POST_ALERT) {
            return R.drawable.ad_visible_green;
        } else if (type == AlertTypeEnum.INCOMING_CREDIT_TRANSFER_ALERT) {
            return R.drawable.ad_visible_green;
        } else if (type == AlertTypeEnum.MERCHANT_STATUS_CHANGE_ALERT) {
            return R.drawable.ad_visible_green;
        }

        return R.drawable.ad_emotibot;
    }

    private void setMessage(String alertId, TextViewEx view, String text,
                            ConcurrentHashMap<String, SpannableStringBuilder> spannableCache, ClickableSpanEx.ClickableSpanExListener clickableSpanListener,
                            boolean isUrlEnabled, boolean isMentionEnabled, boolean isUsernameEnabled, Variable[] vars) {

        SpannableStringBuilder bodySpan = null;
        if (spannableCache != null) {
            bodySpan = spannableCache.get(alertId);
        }

        if (bodySpan == null) {
            if (text == null) {
                view.setText(Constants.BLANKSTR);

            } else {
                SpannableBuilder.SpannableStringBuilderEx span = SpannableBuilder.build(view.getContext(),
                        text, view.getTextSize(), clickableSpanListener, vars, null, true,
                        isUrlEnabled, false, isMentionEnabled,
                        false, isUsernameEnabled, null);

                if (spannableCache != null && span.isComplete()) {
                    spannableCache.put(alertId, span);
                }

                view.setText(span);
            }
        } else {
            view.setText(bodySpan);
        }
    }

    private void setActions(Alert alert) {

        Action[] actions = alert.getActions();
        if (actions != null) {
            List<Action> actionItems = Arrays.asList(actions);
            createActionLayout(alert, actionItems.subList(0, actionItems.size()));
        }
    }

    //Currently only display the last action
    private void createActionLayout(Alert alert, List<Action> actions) {
        if (alert.getType() == AlertTypeEnum.GROUP_INVITE ||
           alert.getType() == AlertTypeEnum.GAME_INVITE  ||
           alert.getType() == AlertTypeEnum.FOLLOWING_REQUEST) {
            container.setTag(R.id.action_id, R.id.action_waiting_action);
            message.setTag(R.id.action_id, R.id.action_waiting_action);
            actionbtn.setTag(R.id.action_id, R.id.action_waiting_action);

            container.setOnClickListener(this);
            message.setOnClickListener(this);
            actionbtn.setOnClickListener(this);
            return;
        }
        int i = 0;
        for (Action action : actions) {
            if (action.getLabel() == null) {
                continue;
            }
            //only assign the first action to the row
            if (i == 0) {
                container.setTag(R.id.action_id, R.id.action_execute_action);
                message.setTag(R.id.action_id, R.id.action_execute_action);
                container.setTag(R.id.action_alert_action, action);
                message.setTag(R.id.action_alert_action, action);

                if (alert.getType() != null) {
                    container.setTag(R.id.action_alert_type, alert.getType());
                    message.setTag(R.id.action_alert_type, alert.getType());
                }
                container.setOnClickListener(this);
                message.setOnClickListener(this);
            }
            i++;
            // assign the last action to the action icon
            actionbtn.setTag(R.id.action_id, R.id.action_execute_action);
            actionbtn.setTag(R.id.action_alert_action, action);
            if (alert.getType() != null) {
                // Currently, the only reason for us to send this data as a tag,
                // is so that the alert type can be logged as a flurry event.
                actionbtn.setTag(R.id.action_alert_type, alert.getType());
            }
            actionbtn.setOnClickListener(this);
        }
    }

}
