/**
 * Copyright (c) 2013 Project Goth
 *
 * ShareToItemHolder.java
 * Created Feb 27, 2015, 2:33:51 PM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.ShareToItem;


/**
 * @author shiyukun
 *
 */
public class ShareToItemHolder extends BaseViewHolder<ShareToItem>{

    private TextView  shareItemName;
    private ImageView shareItemIcon;
    
    public ShareToItemHolder(View view) {
        super(view);
        shareItemName = (TextView) view.findViewById(R.id.title);
        shareItemIcon = (ImageView) view.findViewById(R.id.icon);
    }
    
    @Override
    public void setData(ShareToItem data) {
        super.setData(data);

        shareItemName.setText(I18n.tr(data.getTitle()));
        shareItemIcon.setImageResource(data.getResId());
    }

}
