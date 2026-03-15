/**
 * Copyright (c) 2013 Project Goth
 * MyGiftsOverviewFilterViewHolder.java
 * Created Jan 26, 2015, 2:18:35 PM
 */

package com.projectgoth.ui.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectgoth.R;
import com.projectgoth.i18n.I18n;
import com.projectgoth.model.MyGiftsFilterItem;

/**
 * @author mapet
 */
public class MyGiftsOverviewFilterViewHolder extends BaseViewHolder<MyGiftsFilterItem> {

    private TextView mFilterName;
    private ImageView mFilterCheck;

    public MyGiftsOverviewFilterViewHolder(View view) {
        super(view);
        mFilterName = (TextView) view.findViewById(R.id.name);
        mFilterCheck = (ImageView) view.findViewById(R.id.check);
    }

    @Override
    public void setData(MyGiftsFilterItem data) {
        super.setData(data);

        mFilterName.setText(I18n.tr(data.getName()));
        if (data.isSelected()) {
            mFilterName.setTextColor(mFilterName.getResources().getColor(R.color.default_green));
            mFilterCheck.setVisibility(View.VISIBLE);
        } else {
            mFilterName.setTextColor(mFilterName.getResources().getColor(R.color.light_text_color));
            mFilterCheck.setVisibility(View.GONE);
        }
    }

}
