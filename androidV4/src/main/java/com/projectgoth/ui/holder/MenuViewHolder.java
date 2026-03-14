/**
 * Copyright (c) 2013 Project Goth
 *
 * MenuViewHolder.java
 * Created Feb 24, 2014, 11:44:14 AM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Constants;
import com.projectgoth.common.TextUtils;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.model.MenuOption;

/**
 * @author mapet
 * 
 */
public class MenuViewHolder extends BaseViewHolder<MenuOption> {

    private final ImageView icon;
    private final TextView  title;
    private final TextView  label;

    public MenuViewHolder(View view) {
        super(view);
        icon = (ImageView) view.findViewById(R.id.icon);
        title = (TextView) view.findViewById(R.id.title);
        label = (TextView) view.findViewById(R.id.label);
    }

    @Override
    public void setData(MenuOption data) {
        super.setData(data);

        title.setText(Constants.BLANKSTR);
        label.setText(Constants.BLANKSTR);
        
        icon.setImageBitmap(data.getIcon());
        
        title.setText(data.getTitle());
        title.setTextColor(ApplicationEx.getColor(R.color.default_green));
        
        if (!TextUtils.isEmpty(data.getLabel())) {
            label.setText(data.getLabel());
            label.setTextColor(Theme.getColor(ThemeValues.LIGHT_FONT_COLOR));
        }
    }

}
