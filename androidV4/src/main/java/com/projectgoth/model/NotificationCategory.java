/**
 * Copyright (c) 2013 Project Goth
 *
 * ChatRoomCategory.java
 * Created Jun 7, 2013, 12:03:13 PM
 */

package com.projectgoth.model;

import android.support.annotation.NonNull;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.b.data.Alert;
import com.projectgoth.b.enums.AlertTypeEnum;
import com.projectgoth.b.enums.NotificationTypeEnum;
import com.projectgoth.common.Constants;
import com.projectgoth.common.TextUtils;
import com.projectgoth.datastore.AlertsDatastore;
import com.projectgoth.events.AppEvents;
import com.projectgoth.i18n.I18n;

import java.util.ArrayList;

public class NotificationCategory {

    String                     name             = null;
    short                      id;
    NotificationTypeEnum       notificationTypeEnum;

    // store a list of alert
    ArrayList<Alert>           alertList        = new ArrayList<Alert>();
    String                     categoryFooter   = null;
    boolean                    isChatMessageNotification;
    int                        counterOverwrite = 0;

    public NotificationCategory() {
        // TODO Auto-generated constructor stub
    }

    public NotificationCategory(String name, short id) {
        this.name = name;
        this.id = id;
    }

    public NotificationTypeEnum getNotificationTypeEnum() {
        return notificationTypeEnum;
    }

    public void setNotificationTypeEnum(NotificationTypeEnum notificationTypeEnum) {
        this.notificationTypeEnum = notificationTypeEnum;
    }

    public static NotificationTypeEnum getNotificationTypeEnum(@NonNull Alert alert) {
        if (alert == null) {
            throw new NullPointerException();
        }

        AlertTypeEnum alertType = alert.getType();
        switch (alertType) {
            case MUTUAL_FOLLOWING_ALERT:
            case NEW_FOLLOWER_ALERT:
                return NotificationTypeEnum.NEW_PEOPLE;
            case VIRTUALGIFT_ALERT:
                return NotificationTypeEnum.NEW_GIFTS;
            case REPLY_TO_MIGBO_PARTICIPATED_POST_ALERT:
            case REPLY_TO_MIGBO_POST_ALERT:
            case MENTIONED_IN_MIGBO_POST_ALERT:
            case REPLY_TO_MIGBO_WATCHED_POST_ALERT:
                return NotificationTypeEnum.NEW_MINIBLOG_ACTIVITIES;
            default:
                return NotificationTypeEnum.NEW_OTHERS;
        }

    }

    public void setCategoryFooterLabel(String categoryFooterLabel) {
        this.categoryFooter = categoryFooterLabel;
    }

    public String getName() {
        return this.name;
    }

    public String getCategoryFooter() {
        return this.categoryFooter;
    }

    public short getID() {
        return this.id;
    }

    public boolean showAchieveOption() {
        return !TextUtils.isEmpty(categoryFooter) && notificationTypeEnum != NotificationTypeEnum.ALREADY_READ;
    }


    public ArrayList<Alert> getAlertList() {
        synchronized (alertList) {
            return alertList;
        }
    }

    public void setAlertList(final ArrayList<Alert> alertList) {
        if (alertList != null) {
            synchronized (alertList) {
                this.alertList = alertList;
            }
        } else {
            this.alertList = new ArrayList<Alert>();
        }
    }

    public int getCategoryIcon() {
        NotificationTypeEnum type = getNotificationTypeEnum();

        if (type == NotificationTypeEnum.NEW_PEOPLE) {
            return R.drawable.ad_user_grey;
        } else if (type == NotificationTypeEnum.NEW_GIFTS) {
            return R.drawable.ad_gift_grey;
        } else if (type == NotificationTypeEnum.NEW_MESSAGES) {
            return R.drawable.ad_chat_grey;
        } else if (type == NotificationTypeEnum.NEW_MINIBLOG_ACTIVITIES) {
            return R.drawable.ad_feed_grey;
        } else if (type == NotificationTypeEnum.NEW_OTHERS) {
            return R.drawable.ad_grp_grey;
        } else if (type == NotificationTypeEnum.NEW_MENTIONS) {
            return R.drawable.ad_mention_grey;
        } else {
            return R.drawable.ad_recent_grey;
        }
    }

    public int getUnreadNotificationCount() {
        if (isChatMessageNotification) {
            return ApplicationEx.getInstance().getNotificationHandler().getAllUnreadMessagesCount();
        } else if (getNotificationTypeEnum() == NotificationTypeEnum.NEW_MENTIONS) {
            return counterOverwrite;
        } else {
            if (alertList.size() > Constants.MAX_COUNT_DISPLAY_NOTIFICATIONS) {
                return Constants.MAX_COUNT_DISPLAY_NOTIFICATIONS;
            }
            int unreadAlertCount = 0;
            AlertsDatastore alertsDatastoreInstance = AlertsDatastore.getInstance();
            for (Alert alert : alertList) {
                if (!alertsDatastoreInstance.checkWhetherAlertIsRead(alert)) {
                    ++unreadAlertCount;
                }
            }
            return unreadAlertCount;
        }
    }

    public void setCounterOverwrite(int counterOverwrite) {
        this.counterOverwrite = counterOverwrite;
    }

    public String getTitle() {
        String title = null;

        if (isChatMessageNotification) {
            title = I18n.tr("New messages");
        } else {
            NotificationTypeEnum type = getNotificationTypeEnum();

            if (type == NotificationTypeEnum.NEW_PEOPLE) {
                title = I18n.tr("New people");
            } else if (type == NotificationTypeEnum.NEW_GIFTS) {
                title = I18n.tr("New gifts");
            } else if (type == NotificationTypeEnum.NEW_MINIBLOG_ACTIVITIES) {
                title = I18n.tr("New on feed");
            } else if (type == NotificationTypeEnum.NEW_OTHERS) {
                title = I18n.tr("Others");
            } else if (type == NotificationTypeEnum.ALREADY_READ) {
                title = I18n.tr("Seen");
            } else if (type == NotificationTypeEnum.NEW_MENTIONS) {
                title = I18n.tr("New mentions");
            }
        }

        return title;
    }

    public boolean isChatMessageNotification() {
        return isChatMessageNotification;
    }

    public void setChatMessageNotification(boolean isChatMessageNotification) {
        this.isChatMessageNotification = isChatMessageNotification;
    }

    public int getAlertItemsSize() {
        if (alertList != null) {
            return alertList.size();
        }
        return 0;
    }
}
