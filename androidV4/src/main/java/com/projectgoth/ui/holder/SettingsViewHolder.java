/**
 * Copyright (c) 2013 Project Goth
 *
 * SettingsViewHolder.java
 * Created Sep 4, 2013, 1:46:11 PM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.projectgoth.R;
import com.projectgoth.common.ColorPalette;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.SettingsItem;
import com.projectgoth.model.SettingsItem.SettingsViewType;

/**
 * @author mapet
 * 
 */
public class SettingsViewHolder extends BaseViewHolder<SettingsItem> {

    private final TextView       label;
    private final TextView       info;
    private final ToggleButton   toggleButton;
    private final RelativeLayout container;
    private final TextView       edit;
    private final ImageView      icon;

    public SettingsViewHolder(View view) {
        super(view);

        label = (TextView) view.findViewById(R.id.label);
        info = (TextView) view.findViewById(R.id.info);
        toggleButton = (ToggleButton) view.findViewById(R.id.toggle_button);
        edit = (TextView) view.findViewById(R.id.edit);
        icon = (ImageView) view.findViewById(R.id.icon);
        container = (RelativeLayout) view.findViewById(R.id.settings_container);

        edit.setText(I18n.tr("Edit"));
        edit.setOnClickListener(this);
        toggleButton.setOnClickListener(this);
    }

    @Override
    public void setData(SettingsItem data) {
        super.setData(data);

        SettingsItem settingsItem = data;
        label.setText(settingsItem.getLabel());
        container.setBackgroundColor(ColorPalette.BG_WHITE);
        info.setVisibility(View.GONE);
        toggleButton.setVisibility(View.GONE);
        icon.setVisibility(View.GONE);
        edit.setVisibility(View.GONE);

        if (data.getViewType() == SettingsViewType.TEXT) {
            info.setText((String) data.getData());
            info.setVisibility(View.VISIBLE);
        }

        if (data.getViewType() == SettingsViewType.TOGGLE) {
            toggleButton.setChecked((Boolean) data.getData());
            toggleButton.setVisibility(View.VISIBLE);
        }

        if (data.getViewType() == SettingsViewType.TOGGLEnEDIT) {
            toggleButton.setChecked((Boolean) data.getData());
            toggleButton.setVisibility(View.VISIBLE);
            edit.setVisibility(View.VISIBLE);
        }

        if (data.getViewType() == SettingsViewType.EDIT) {
            edit.setVisibility(View.VISIBLE);
        }

        if (data.getViewType() == SettingsViewType.SUBTITLE) {
            container.setBackgroundColor(Theme.getColor(ThemeValues.LIST_CATEGORY_BACKGROUND_COLOR));
        }

        if (data.getIconResource() != 0) {
            icon.setVisibility(View.VISIBLE);
            icon.setBackgroundResource(data.getIconResource());
        }
    }

}
