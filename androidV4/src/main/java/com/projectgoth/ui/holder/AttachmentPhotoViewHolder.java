/**
 * Copyright (c) 2013 Project Goth
 *
 * AttachmentPhotoViewHolder.java
 * Created Jul 19, 2013, 4:08:06 PM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.projectgoth.R;
import com.projectgoth.common.Theme;
import com.projectgoth.common.ThemeValues;
import com.projectgoth.model.GridItem;

/**
 * @author mapet
 * 
 */
public class AttachmentPhotoViewHolder extends BaseViewHolder<GridItem> {

    private final ImageView icon;
    private final TextView  label;

    public AttachmentPhotoViewHolder(View view) {
        super(view);
        icon = (ImageView) view.findViewById(R.id.item);
        label = (TextView) view.findViewById(R.id.label);
    }

    @Override
    public void setData(GridItem data) {
        super.setData(data);
        
        GridItem gridItem = data;
        icon.setImageResource(gridItem.getResId());
        label.setText(gridItem.getTitle());
        
        label.setTextColor(Theme.getColor(ThemeValues.LIGHT_TEXT_COLOR));
    }

}
