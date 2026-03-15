/**
 * Copyright (c) 2013 Project Goth
 *
 * DialogAlert.java
 * Created Aug 22, 2013, 7:44:41 PM
 */

package com.projectgoth.notification;

import java.util.UUID;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;

import com.projectgoth.ui.widget.CustomAlertDialog;

/**
 * @author cherryv
 * 
 */
public class DialogAlert implements BaseAlert, OnDismissListener {

    private String            id;

    private CustomAlertDialog dialog;

    public DialogAlert(Context context, String message) {
        this(context, null, message, null);
    }

    public DialogAlert(Context context, String message, AlertListener listener) {
        this(context, null, message, listener);
    }

    public DialogAlert(Context context, String title, String message, AlertListener listener) {
        this.id = generateId();

        this.dialog = new CustomAlertDialog(context, title, message, listener);
        this.dialog.setOnDismissListener(this);
    }

    private String generateId() {
        UUID uuid = UUID.randomUUID();
        return "DIALOG-" + uuid.toString();
    }

    @Override
    public String getId() {
        return this.id;
    }

    public void showAlert() {
        dialog.show();
    }

    public void dismissAlert() {
        dialog.dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        // TODO Inform notification handler that dialog is now closed
    }

}
