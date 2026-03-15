/**
 * Copyright (c) 2013 Project Goth
 *
 * ActionButton.java
 * Created Jul 8, 2013, 1:31:52 AM
 */

package com.projectgoth.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.projectgoth.R;

/**
 * @author cherryv
 * 
 */
public abstract class MenuOptionView extends RelativeLayout {

    private ImageView           menuIcon;
    private TextView            menuLabel;
    private TextView            menuSubLabel;
    private ImageView           menuIconChecked;
    private ImageView           menuIconSelected;
    private ImageView           menuIconUnselected;
    private Button              menuActionButton;

    public MenuOptionView(Context context) {
        this(context, null, 0);
    }

    public MenuOptionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuOptionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inflateLayout(context);

        menuIcon = (ImageView) findViewById(R.id.icon);
        menuLabel = (TextView) findViewById(R.id.title);

        menuSubLabel = (TextView) findViewById(R.id.subtitle);

        menuIconChecked = (ImageView) findViewById(R.id.action_icon_checked);
        menuIconSelected = (ImageView) findViewById(R.id.action_icon_selected);
        menuIconUnselected = (ImageView) findViewById(R.id.action_icon_unselected);
        menuActionButton = (Button) findViewById(R.id.action_button);
    }

    protected abstract void inflateLayout(Context context);

    public void setIcon(Bitmap bitmap) {
        menuIcon.setImageBitmap(bitmap);
        menuIcon.setVisibility(View.VISIBLE);
    }

    public void setIcon(int resId) {
        menuIcon.setImageResource(resId);
        menuIcon.setVisibility(View.VISIBLE);
    }

    public void showCheckedIcon() {
        menuIconChecked.setVisibility(View.VISIBLE);
    }

    public void hideCheckedIcon() {
        menuIconChecked.setVisibility(View.GONE);
    }

    public void showActionButton(String text) {
        menuActionButton.setText(text);
        menuActionButton.setVisibility(View.VISIBLE);
    }

    public void hideActionButton() {
        menuActionButton.setVisibility(View.GONE);
    }

    public void showSelectedIcon() {
        menuIconUnselected.setVisibility(View.GONE);
        menuIconSelected.setVisibility(View.VISIBLE);
    }

    public void showUnselectedIcon() {
        menuIconSelected.setVisibility(View.GONE);
        menuIconUnselected.setVisibility(View.VISIBLE);
    }

    public void setLabelTextColor(int color) {
        menuLabel.setTextColor(color);
        menuLabel.setVisibility(View.VISIBLE);
    }

    public void setLabel(String text) {
        menuLabel.setText(text);
        menuLabel.setVisibility(View.VISIBLE);
    }

    public void setSubLabel(String text) {
        menuSubLabel.setText(text);
        menuSubLabel.setVisibility(View.VISIBLE);
    }

}
