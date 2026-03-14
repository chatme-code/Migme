/**
 * Copyright (c) 2013 Project Goth
 *
 * ToyboxViewHolder.java
 * Created Jul 1, 2013, 11:31:56 AM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.projectgoth.R;
import com.projectgoth.controller.EmoticonsController;
import com.projectgoth.nemesis.model.Emoticon;

/**
 * @author mapet
 * 
 */
public class AttachmentEmoticonViewHolder extends BaseViewHolder<Object> {

    private final RelativeLayout container;
    private final ImageView mItem;

    public AttachmentEmoticonViewHolder(View rootView) {
        super(rootView);
        container = (RelativeLayout) rootView.findViewById(R.id.emoticon_container);
        mItem = (ImageView) rootView.findViewById(R.id.item);
    }

    @Override
    public void setData(Object baseEmoticon) {
        super.setData(baseEmoticon);

        Emoticon e = (Emoticon) baseEmoticon;
        EmoticonsController.getInstance().loadEmoticonImage(mItem, e.getMainHotkey(),
                R.drawable.ad_loadstatic_grey);
    }
    
    public void setVerticalSpacing(int verticalSpacing) {
        container.setPadding(container.getPaddingLeft(), 
                verticalSpacing / 2,
                container.getPaddingRight(),
                verticalSpacing / 2);
    }

}
