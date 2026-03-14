/**
 * Copyright (c) 2013 Project Goth
 *
 * MigAlertStatus.java
 * Created Aug 23, 2013, 11:58:12 AM
 */

package com.projectgoth.notification;

import com.projectgoth.b.data.Alert;
import com.projectgoth.b.data.MigAlertsUnread;
import com.projectgoth.b.data.Variable;
import com.projectgoth.b.data.VariableLabel;
import com.projectgoth.b.enums.ViewTypeEnum;
import com.projectgoth.i18n.I18n;

/**
 * @author cherryv
 * 
 */
public class MigAlertStatus extends StatusAlert {

    public static final String ACTION_ALERT_STATUS_ID = "ACTION_ALERT_STATUS_ID";

    private Alert[]    data;

    public static boolean isValidAlertNotification(MigAlertsUnread unreadAlert) {
        if (unreadAlert != null && unreadAlert.getAlerts() != null) {
            return true;
        }
        return false;
    }

    public MigAlertStatus(Alert[] alertArr) {
        super();
        this.data = alertArr;
    }

    @Override
    public String getTickerMessage() {
        Alert lastAlert = data[0];
        if (lastAlert != null) {
            return formatAlertMessage(lastAlert);
        }
        return null;
    }

    @Override
    public String getTitle() {
        int notificationCount = getCount();
        if (notificationCount > 1) {
            return String.format(I18n.tr("%d notifications"), notificationCount);
        } else {
            Alert lastAlert = data[0];
            if (lastAlert != null && lastAlert.getImage() != null) {
                return lastAlert.getImage().getTitle();
            }
        }
        return null;
    }

    @Override
    public String getMessage() {
        Alert lastAlert = data[0];
        if (lastAlert != null) {
            return formatAlertMessage(lastAlert);
        }
        return null;
    }

    @Override
    public String getId() {
        return ACTION_ALERT_STATUS_ID;
    }

    @Override
    public int getCount() {
        Integer count = data.length;
        return (count != null ? count : 1);
    }

    @Override
    public NotificationType getNotificationType() {
        return NotificationType.MIG_ALERT_NOTIFICATION;
    }

    @Override
    public boolean willTriggerNewEvent() {
        return true;
    }

    /**
     * Replaces the variables in the Alert message with the proper values.
     * 
     * @param alert
     *            Alert to format
     * @return The alert message with the variables swapped with correct values
     */
    private String formatAlertMessage(Alert alert) {
        String message = alert.getMessage();
        Variable[] variables = alert.getVariables();

        String label;
        String match;
        for (Variable var : variables) {
            label = null;
            VariableLabel varLabel = var.getLabel();
            if (varLabel != null && varLabel.getText() != null) {
                label = varLabel.getText();
            }

            match = "%{" + var.getName() + "}";
            while (message.indexOf(match) > -1) {
                message = message.replace(match, label);
            }
        }

        return message;
    }
    

    public String getUrlActionForLatestAlert() {
        if (data != null && data.length > 0) {
            final Alert latestAlert = data[0];
            if (latestAlert != null && 
                latestAlert.getActions() != null && 
                latestAlert.getActions().length > 0 && 
                latestAlert.getActions()[0] != null) {
                return latestAlert.getActions()[0].getUrlFromView(ViewTypeEnum.TOUCH);
            }
        }
        
        return null;
    }
}
