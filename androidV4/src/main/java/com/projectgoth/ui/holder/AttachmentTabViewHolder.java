/**
 * Copyright (c) 2013 Project Goth
 *
 * AttachmentTabViewHolder.java
 * Created Jul 11, 2013, 2:40:06 PM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.ImageView;
import com.projectgoth.R;
import com.projectgoth.app.ApplicationEx;
import com.projectgoth.common.Constants;
import com.projectgoth.common.Tools;
import com.projectgoth.imagefetcher.ImageHandler;
import com.projectgoth.nemesis.model.BaseEmoticonPack;

/**
 * @author mapet
 * 
 */
public class AttachmentTabViewHolder extends BaseViewHolder<BaseEmoticonPack> {

    private final View      mView;
    private final ImageView mTabIcon;
    private       boolean   mSelected;

    public AttachmentTabViewHolder(View view) {
        super(view);
        mView = view;
        mTabIcon = (ImageView) view.findViewById(R.id.tab_image);
    }

    @Override
    public void setData(BaseEmoticonPack tabData) {
        String icon = tabData.getIconUrl();

        if (icon.startsWith(Constants.LINK_DRAWABLE)) {
            mTabIcon.setSelected(mSelected);
            int resId = Tools.getDrawableResId(ApplicationEx.getContext(), icon);
            mTabIcon.setImageResource(resId);
            
        } else {
            ImageHandler.getInstance().loadImageFromUrl(mTabIcon, icon, false, R.drawable.ad_loadstatic12_grey);
        }

        mView.setSelected(mSelected);
    }

    public void setIsSelected(boolean selected) {
        mSelected = selected;
    }

    public void setImagePadding(int padding) {
        mTabIcon.setPadding(padding, padding, padding, padding);
    }
    
}
